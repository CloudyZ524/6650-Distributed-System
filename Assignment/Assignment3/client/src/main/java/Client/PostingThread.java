package Client;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

public class PostingThread implements Runnable {
  private BlockingQueue<LiftRide> queue;
  private int numRequests;
  private AtomicInteger successfulRequests;
  private AtomicInteger failedRequests;
  private CountDownLatch count;
  private Random random = new Random();
  final private int MAX_RETRIES = 5;
  final private int RESORT_ID_RANGE = 10;
  final private int SKIER_ID_RANGE = 100000;
  final private int DAY_ID_RANGE = 366;
  final private String SEASON_ID = "2024";
  // Initialize the circuit breaker
  private static final EventCountCircuitBreaker circuitBreaker =
      new EventCountCircuitBreaker(10, 1, TimeUnit.MINUTES, 5, 1, TimeUnit.MINUTES);

  public PostingThread(BlockingQueue<LiftRide> queue, int numRequests,
      AtomicInteger successfulRequests, AtomicInteger failedRequests, CountDownLatch count) {
    this.queue = queue;
    this.numRequests = numRequests;
    this.successfulRequests = successfulRequests;
    this.failedRequests = failedRequests;
    this.count = count;
  }

  @Override
  public void run() {
    SkiersApi skiersApi = createApiClient();
    for (int i = 0; i < numRequests; i++) {
      LiftRide liftRide = queue.poll();
      if (liftRide == null) {
        break;
      }
      if (!circuitBreaker.checkState()) {
        failedRequests.incrementAndGet();
        continue;
      }
      try {
        ApiResponse<Void> apiResponse = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, random.nextInt(RESORT_ID_RANGE) + 1, SEASON_ID, String.valueOf(random.nextInt(DAY_ID_RANGE) + 1), random.nextInt(SKIER_ID_RANGE) + 1);

        if (apiResponse.getStatusCode() == 201) {
          successfulRequests.incrementAndGet();
          circuitBreaker.close(); // Explicitly close the circuit on a successful call
        } else {
          failedRequests.incrementAndGet();
          circuitBreaker.incrementAndCheckState(); // Increment the counter and check the state
        }
      } catch (Exception e) {
        failedRequests.incrementAndGet();
        circuitBreaker.incrementAndCheckState(); // Increment the counter and check the state
      } finally {
        this.count.countDown();
      }
    }
  }

  private SkiersApi createApiClient() {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("http://localhost:8081/servlet_war/");
    return new SkiersApi(apiClient);
  }
}
