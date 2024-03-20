package Dao;

import Model.SkiRecord;
import consumer.LiftRide;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

public class SkiRecordDao {
    private final DynamoDbClient dynamoDbClient;
    private final static String TABLE_NAME = "SkiResortData";

    public SkiRecordDao() {
        this.dynamoDbClient = DynamoDbClient.builder().region(Region.US_WEST_2).build();
    }

    // Make SkiRecord Singleton
    private static class SingletonHelper {
        private static final SkiRecordDao INSTANCE = new SkiRecordDao();
    }

    public static SkiRecordDao getInstance() {
        return SingletonHelper.INSTANCE;
    }

    // add new ski records into DynamoDB
    public void addSkiRecord(SkiRecord skiRecord) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("SkierId", AttributeValue.builder().n(Integer.toString(skiRecord.getSkierId())).build());
        item.put("SeasonDayId", AttributeValue.builder().s(skiRecord.getSeasonDayId()).build());
        item.put("SeasonId", AttributeValue.builder().n(Integer.toString(skiRecord.getSeasonId())).build());
        item.put("ResortId", AttributeValue.builder().n(Integer.toString(skiRecord.getResortId())).build());
        item.put("DayId", AttributeValue.builder().n(Integer.toString(skiRecord.getDayId())).build());

        LiftRide liftRide = skiRecord.getLiftRide();
        Map<String, AttributeValue> liftRideAttributes = new HashMap<>();
        liftRideAttributes.put("LiftID", AttributeValue.builder().n(String.valueOf(liftRide.getLiftID())).build());
        liftRideAttributes.put("Time", AttributeValue.builder().s(String.valueOf(liftRide.getTime())).build());
        item.put("LiftRide", AttributeValue.builder().m(liftRideAttributes).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
        try {
            dynamoDbClient.putItem(putItemRequest);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }
    }
}
