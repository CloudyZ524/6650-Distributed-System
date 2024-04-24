package DynamoDB;

import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class DynamoDBConnection {
    public static DynamoDbClient dynamoDbClient;
    public static final String TABLE_NAME = "SkiersData";

    public DynamoDBConnection() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
                .build();
    }

    public static void main(String[] args) {
        DynamoDBConnection dbConnection = new DynamoDBConnection();
        dbConnection.CreateSkiTable();
        // Wait for the table to become active
        waitForTableToBecomeActive(TABLE_NAME);

        addUniqueSkiers("1#2024#1");
        addUniqueSkiers("1#2024#2");
        addUniqueSkiers("1#2024#3");
        System.out.println("successfully add the uniqueSkiers");
    }

    public void CreateSkiTable() {
        String tableName = TABLE_NAME;
        boolean tableExists = dynamoDbClient.listTables().tableNames().contains(tableName);
        if (!tableExists) {
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(tableName)
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("ResortSeasonDayLiftId")
                        .keyType(KeyType.HASH) // Partition key
                        .build(),
                    KeySchemaElement.builder()
                        .attributeName("SkierId")
                        .keyType(KeyType.RANGE) // Sort key
                        .build())
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("ResortSeasonDayLiftId")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("SkierId")
                        .attributeType(ScalarAttributeType.N)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

            try {
                dynamoDbClient.createTable(request);
                System.out.println("DBConnection created successfully: " + TABLE_NAME);

            } catch (DynamoDbException e) {
                System.err.println("DBConnection creation failed: " + e.getMessage());
            }
        }
    }

    private static void waitForTableToBecomeActive(String tableName) {
        DescribeTableRequest describeTableRequest = DescribeTableRequest.builder()
            .tableName(tableName)
            .build();

        boolean isTableActive = false;
        try {
            while (!isTableActive) {
                DescribeTableResponse describeTableResponse = dynamoDbClient.describeTable(describeTableRequest);
                if (describeTableResponse.table().tableStatus() == TableStatus.ACTIVE) {
                    isTableActive = true;
                } else {
                    // Wait for a while before trying again
                    Thread.sleep(5000);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Table creation wait interrupted", e);
        } catch (DynamoDbException e) {
            throw new RuntimeException("Unable to check table status", e);
        }
    }

    private static void addUniqueSkiers(String attributeName) {
        // Create a new item with the necessary attributes
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ResortSeasonDayLiftId", AttributeValue.builder().s(attributeName).build());
        item.put("SkierId", AttributeValue.builder().n(String.valueOf(-1)).build());
        item.put("UniqueSkiers", AttributeValue.builder().n(String.valueOf(0)).build()); // Assuming 'totalVertical' is a number
        PutItemRequest putItemRequest = PutItemRequest.builder()
            .tableName(TABLE_NAME) // The name of the table
            .item(item)
            .build();
        dynamoDbClient.putItem(putItemRequest);
    }
}



