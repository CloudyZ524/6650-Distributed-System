import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;


public class LiftRideConsumer {
  private final static String QUEUE_NAME = "SkierQueue";
  private static Map<Integer, List<LiftRide>> skierLiftRides = new ConcurrentHashMap<>();
  private static Gson gson = new Gson();

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [x] Received '" + message + "'");
      try {
        LiftRide liftRide = getLiftRideFromMessage(message);
        int skierId = getSkierIdFromMessage(message);

        // Update the skierLiftRides map
        skierLiftRides.computeIfAbsent(skierId, k -> new ArrayList<>()).add(liftRide);
        System.out.println(skierLiftRides);
      } catch (Exception e) {
        System.err.println("Failed to process message: " + message);
        e.printStackTrace();
      }
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
