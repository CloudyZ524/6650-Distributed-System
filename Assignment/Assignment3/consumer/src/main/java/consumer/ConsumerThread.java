package consumer;

import Dao.SkiRecordDao;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class ConsumerThread implements Runnable{
  private BlockingQueue<Channel> channelPool;
  private String queueName;
  private Gson gson;
  private static final SkiRecordDao skiRecordDao = SkiRecordDao.getInstance();

  public ConsumerThread(BlockingQueue<Channel> channelPool, String queueName, Gson gson) {
    this.channelPool = channelPool;
    this.queueName = queueName;
    this.gson = gson;
  }

  @Override
  public void run() {
    Channel channel = null;
    try {
      channel = channelPool.poll();

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [x] Received '" + message + "'");

        // Add SkiRecord to DynamoDB
        Map<String, AttributeValue> item = convertJsonToAttributeValueMap(message);
        skiRecordDao.addSkiRecord(item);
      };
      channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (channel != null) {
        channelPool.offer(channel); // Return the channel to the pool
      }
    }
  }

  private Map<String, AttributeValue> convertJsonToAttributeValueMap(String message) {
    JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

    Map<String, AttributeValue> item = new HashMap<>();
    item.put("SkierId", AttributeValue.builder().n(jsonObject.get("skierID").getAsString()).build());
    item.put("SeasonDayId", AttributeValue.builder().s(jsonObject.get("seasonID").getAsString() + "#" + jsonObject.get("dayID").getAsString()).build());
    item.put("ResortId", AttributeValue.builder().n(jsonObject.get("resortID").getAsString()).build());

    JsonObject body = jsonObject.getAsJsonObject("body");
    Map<String, AttributeValue> liftRideAttributes = new HashMap<>();
    liftRideAttributes.put("LiftID", AttributeValue.builder().n(body.get("liftID").getAsString()).build());
    liftRideAttributes.put("Time", AttributeValue.builder().n(body.get("time").getAsString()).build());
    item.put("LiftRide", AttributeValue.builder().m(liftRideAttributes).build());

    return item;
  }
}
