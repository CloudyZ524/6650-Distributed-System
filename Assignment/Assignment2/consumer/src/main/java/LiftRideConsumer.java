import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LiftRideConsumer {
  private final static String QUEUE_NAME = "SkierQueue";
  private static Map<Integer, List<LiftRide>> skierLiftRides = new ConcurrentHashMap<>();
  private static Gson gson = new Gson();
  private static int NUM_OF_THREADS = 10;
  private static ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_THREADS);

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.215.62.210");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [x] Received '" + message + "'");

      // Submit tasks to thread pool
      executorService.submit(() -> {
        try {
          LiftRide liftRide = getLiftRideFromMessage(message);
          int skierId = getSkierIdFromMessage(message);

          // Update to Hashmap
          skierLiftRides.compute(skierId, (key, liftRidesList) -> {
            if (liftRidesList == null) {
              liftRidesList = Collections.synchronizedList(new ArrayList<>());
            }
            liftRidesList.add(liftRide);
            return liftRidesList;
          });
        } catch (Exception e) {
          System.err.println("Failed to process message: " + message);
          e.printStackTrace();
        }
      });
    };
    channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
  }

  private static int getSkierIdFromMessage(String message) {
    JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
    return jsonObject.get("skierID").getAsInt();
  }

  private static LiftRide getLiftRideFromMessage(String message) {
    JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
    return gson.fromJson(jsonObject.get("body"), LiftRide.class);
  }
}
