package Client;

import Client.EventGenerator;
import io.swagger.client.model.LiftRide;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadLiftRideClient {
  private static int TOTAL_REQUESTS = 200000;
  private static int INITIAL_THREADS = 15;
  private static int REQUESTS_PER_THREAD = 1000;
  private static BlockingQueue<LiftRide> GeneratorQueue = new LinkedBlockingQueue<>(TOTAL_REQUESTS);
  private static AtomicInteger successfulRequests = new AtomicInteger(0);
  private static AtomicInteger failedRequests = new AtomicInteger(0);
  private static List<Long> latenciesList = Collections.synchronizedList(new ArrayList<>());
  private static PrintWriter fileWriter;

  public static void main(String[] args) throws InterruptedException, IOException {
    // Start LiftRide generation
    Thread eventGeneratorThread = new Thread(new EventGenerator(GeneratorQueue, TOTAL_REQUESTS));
    eventGeneratorThread.start();

    long startTime = System.currentTimeMillis();
    // Create ThreadPoolExecutor
    ThreadPoolExecutor executor = new ThreadPoolExecutor(
        INITIAL_THREADS,
        INITIAL_THREADS,
        5000L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(REQUESTS_PER_THREAD * INITIAL_THREADS),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    CountDownLatch completed = new CountDownLatch(TOTAL_REQUESTS);

    // Create a new CSV file
    fileWriter = new PrintWriter(new FileWriter("latencies.csv"));
    fileWriter.println("StartTime,RequestType,Latency,ResponseCode");

    for (int i = 0; i < TOTAL_REQUESTS / REQUESTS_PER_THREAD; i++) {
      executor.execute(new PostingThread(GeneratorQueue, REQUESTS_PER_THREAD, successfulRequests, failedRequests, completed, latenciesList, fileWriter));
    }

    try{
      completed.await();
    } catch (InterruptedException e) {
      System.out.println("Main thread was interrupted");
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    } finally {
      // Close the PrintWriter after all threads have completed
      if (fileWriter != null) {
        fileWriter.flush();
        fileWriter.close();
      }
    }

    // Print results
    long totalTime = System.currentTimeMillis() - startTime;
    System.out.println("Successful requests: " + successfulRequests.get());
    System.out.println("Failed requests: " + failedRequests.get());
    System.out.println("Total time: " + totalTime + " ms");
    System.out.println("Throughput: " + (TOTAL_REQUESTS / (totalTime / 1000.0)) + " requests/second\n");
    calculateMetrics();
    executor.shutdown();
  }

  private static void calculateMetrics() {
    Collections.sort(latenciesList);
    int size = latenciesList.size();

    long totalLatencies = latenciesList.stream().mapToLong(Long::longValue).sum();
    long mean = totalLatencies / latenciesList.size();
    long median = latenciesList.get(size / 2);
    long p99 = latenciesList.get((int) (size * 0.99));
    long min = latenciesList.get(0);
    long max = latenciesList.get(size - 1);

    System.out.println("Mean response time: " + mean + " ms");
    System.out.println("Median response time: " + median + " ms");
    System.out.println("p99 response time: " + p99 + " ms");
    System.out.println("Min response time: " + min + " ms");
    System.out.println("Max response time: " + max + " ms");
  }
}
