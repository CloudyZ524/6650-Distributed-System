package consumer;

import Dao.SkiRecordDao;
import Model.SkiRecord;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

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
        SkiRecord newRecord = LiftRideConsumer.convertMessageToSkiRecord(message);
        skiRecordDao.addSkiRecord(newRecord);
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
}
