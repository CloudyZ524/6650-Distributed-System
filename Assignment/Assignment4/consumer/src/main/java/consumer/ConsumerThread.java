package consumer;

import static DynamoDB.DynamoDBConnection.TABLE_NAME;
import static DynamoDB.DynamoDBConnection.dynamoDbClient;

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
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

public class ConsumerThread implements Runnable {

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
        processMessage(message);

      };
      channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (channel != null) {
        channelPool.offer(channel); // Return the channel to the pool
      }
    }
  }

  private void processMessage(String message) {
    String[] parts = message.split(",");
    if (parts.length == 6) {
      int skierId = Integer.parseInt(parts[0]); // skierId
      int resortId = Integer.parseInt(parts[1]); // resortId
      int seasonId = Integer.parseInt(parts[2]); // seasonId
      int dayId = Integer.parseInt(parts[3]); // dayId
      int time = Integer.parseInt(parts[4]); // time
      int liftId = Integer.parseInt(parts[5]); // liftId

      int vertical = liftId * 10; // vertical

      // Composite keys
      String resortSeasonDayLiftId = resortId + "#" + seasonId + "#" + dayId + "#" + liftId;
      Map<String, AttributeValue> item = new HashMap<>();
      item.put("ResortSeasonDayLiftId", AttributeValue.builder().s(resortSeasonDayLiftId).build());
      item.put("SkierId", AttributeValue.builder().n(String.valueOf(skierId)).build());
      item.put("Vertical", AttributeValue.builder().n(String.valueOf(vertical)).build());
      item.put("Time", AttributeValue.builder().n(String.valueOf(time)).build());

      // update uniqueSkiers
      update(resortId + "#" + seasonId + "#" + dayId, -1, "ADD UniqueSkiers :val", 1);

      /* update total vertical
       1#2024 skierID totalVertical ->  get the total vertical for the skier for specified seasons at the specified resort
      */
      if (!update(resortId + "#" + seasonId, skierId, "ADD TotalVertical :val", vertical)) {
        writeTotalVertical(resortId + "#" + seasonId, skierId, vertical, "TotalVertical");
      }

      // update day Vertical
      // 1#2024#1 skierId Vertical -> get the day vertical for the skier for the specified ski day
      if (!update(resortId + "#" + seasonId + "#" + dayId, skierId, "ADD DayVertical :val", vertical)) {
        writeTotalVertical(resortId + "#" + seasonId + "#" + dayId, skierId, vertical, "DayVertical");
      }

      // update vertical
      // for on skier, a day can have many liftId(vertical)
      // 1#2024#1 skierId Vertical -> get the total vertical for the skier for the specified ski day
      if (!update(resortSeasonDayLiftId, skierId, "ADD Vertical :val", vertical)) {
        skiRecordDao.addSkiRecord(item);
      }
    }
  }

  private void writeTotalVertical(String partitionKey, int sortKey, int vertical, String attributeName) {
    Map<String, AttributeValue> item = new HashMap<>();
    item.put("ResortSeasonDayLiftId", AttributeValue.builder().s(partitionKey).build());
    item.put("SkierId", AttributeValue.builder().n(String.valueOf(sortKey)).build());
    item.put(attributeName, AttributeValue.builder().n(String.valueOf(vertical)).build());

    PutItemRequest putItemRequest = PutItemRequest.builder()
        .tableName(TABLE_NAME) // The name of the table
        .item(item)
        .build();
    dynamoDbClient.putItem(putItemRequest);

  }

    private boolean update(String partitionKey, int sortKey, String expression, int val) {
      Map<String, AttributeValue> key = new HashMap<>();
      key.put("ResortSeasonDayLiftId", AttributeValue.builder().s(partitionKey).build());
      key.put("SkierId", AttributeValue.builder().n(String.valueOf(sortKey)).build());


      Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
      expressionAttributeValues.put(":val", AttributeValue.builder().n(String.valueOf(val)).build());

      UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
          .tableName(TABLE_NAME) // Replace with your table name
          .key(key)
          .updateExpression(expression)
          .expressionAttributeValues(expressionAttributeValues)
          .conditionExpression("attribute_exists(ResortSeasonDayLiftId) and attribute_exists(SkierId)")
          .build();

      // Execute the update
      try {
        dynamoDbClient.updateItem(updateItemRequest);
        System.out.println("updated successfully.");
        return true;
      } catch (DynamoDbException e) {
        return false;
      }
    }
  }
