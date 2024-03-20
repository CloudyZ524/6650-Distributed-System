package DynamoDB;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class DynamoDBConnection {
    private DynamoDbClient dynamoDbClient;

    public DynamoDBConnection() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
                .build();
    }

    public static void main(String[] args) {
        DynamoDBConnection dbConnection = new DynamoDBConnection();
        dbConnection.CreateSkiTable();
    }

    public void CreateSkiTable() {
        String tableName = "SkiResortData";
        boolean tableExists = dynamoDbClient.listTables().tableNames().contains(tableName);
        if (!tableExists) {
            CreateTableRequest request = CreateTableRequest.builder()
                    .attributeDefinitions(AttributeDefinition.builder()
                                    .attributeName("SkierId")
                                    .attributeType(ScalarAttributeType.N)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("SeasonDayId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build())
                            .keySchema(KeySchemaElement.builder()
                                    .attributeName("SkierId")
                                    .keyType(KeyType.HASH)  // primary key
                                    .build(),
                            KeySchemaElement.builder()
                                    .attributeName("SeasonDayId")
                                    .keyType(KeyType.RANGE)  // sort key
                                    .build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(5L)
                            .writeCapacityUnits(5L)
                            .build())
                    .tableName(tableName)
                    .build();
            try {
                dynamoDbClient.createTable(request);
                System.out.println("Table created: " + tableName);
            } catch (DynamoDbException e) {
                System.err.println("Failed to create table: " + e.getMessage());
            }
        } else {
            System.out.println("Table already exists: " + tableName);
        }
    }
}
