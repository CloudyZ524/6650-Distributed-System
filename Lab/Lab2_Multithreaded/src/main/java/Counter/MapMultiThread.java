package Counter;

import java.util.HashMap;
import java.util.Map;

public class MapMultiThread {

  public static void main(String[] args) throws InterruptedException {
    int numElements = 100000;
    int numThreads = 100;

    // Create a synchronized HashMap
    Map<Integer, Integer> synchronizedHashMap = new HashMap<>();

    long startTime = System.currentTimeMillis();

    Thread[] threads = new Thread[numThreads];

    for (int t = 0; t < numThreads; t++) {
      int threadId = t;
      int numPerThread = numElements / numThreads;
      threads[t] = new Thread(() -> {
        for (int i = 0; i < numPerThread; i++) {
          int num = threadId * 100 + i;
          synchronizedHashMap.put(num, num);
        }
      });
      threads[t].start();
    }

    // Wait for all threads to complete
    for (Thread thread : threads) {
      thread.join();
    }

    long endTime = System.currentTimeMillis();
    System.out.println("Time taken to add to Synchronized HashMap (Multithreaded): " + (endTime - startTime) + " ms");
  }
}
