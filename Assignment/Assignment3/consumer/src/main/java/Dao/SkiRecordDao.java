package Dao;

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
    public void addSkiRecord(Map<String, AttributeValue> item) {
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
        try {
            dynamoDbClient.putItem(putItemRequest);
        } catch (DynamoDbException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}
