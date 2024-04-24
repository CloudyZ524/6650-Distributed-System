
    import java.util.Map;
    import java.util.concurrent.BlockingQueue;
    import java.util.concurrent.LinkedBlockingQueue;
    import java.util.concurrent.TimeoutException;
    import java.util.stream.Collectors;
    import javax.servlet.*;
    import javax.servlet.annotation.WebServlet;
    import javax.servlet.http.*;
    import java.io.IOException;
    import com.rabbitmq.client.Channel;
    import com.rabbitmq.client.Connection;
    import com.rabbitmq.client.ConnectionFactory;
    import software.amazon.awssdk.regions.Region;
    import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
    import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
    import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
    import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
    import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;


    /**
 * The servlet includes two get method:
 * GET/resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
 * GET/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
 * GET/skiers/{skierID}/vertical
 */
@WebServlet(name = "SkierServlet", urlPatterns = {"/skiers/*" , "/resorts/*"})
public class SkierServlet extends HttpServlet {

    private final static String QUEUE_NAME = "SkierQueue";
    private final static String TABLE_NAME = "SkiersData";
    private static ConnectionFactory factory = new ConnectionFactory();
    private static final int CHANNEL_POOL_SIZE = 120;
    private static final Integer SEASON_ID = 2024;
    static final int RESORT_ID = 1;
    private static final Integer LOWER_BOUND = 1;
    private static final Integer MAX_SKIER_ID = 100000;

    private static final int DAY_MIN = 1;
    private static final int DAY_MAX = 3;

    private static final BlockingQueue<Channel> channelPool = new LinkedBlockingQueue<>(CHANNEL_POOL_SIZE);

    static { initializeChannelPool();}

    protected static DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
        .region(Region.US_WEST_2)
        .build();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        System.out.println("doGet is called ....");
        String urlPath = request.getPathInfo();
        if (urlPath == null || urlPath.isEmpty()) {
            sendResponseMsg(response, HttpServletResponse.SC_NOT_FOUND, "Data not found");
            return;
        }

        String[] urlParts = urlPath.split("/");

        try {
            if (urlParts.length == 7) {
                processResortSeasonDaySkiers(request, response, urlParts);
            } else if (urlParts.length == 8) {
                processSkierDayActivities(request, response, urlParts);
            } else if (urlPath.matches("/\\d+/vertical")) {
                processSkierVertical(request, response, urlParts);
            } else {
                sendResponseMsg(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid inputs supplied");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponseMsg(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "{\"error\": \"Error processing request\"}");
        }
    }

    // get number of unique skiers at resort/season/day
    private void processResortSeasonDaySkiers(HttpServletRequest request,
        HttpServletResponse response, String[] urlParts) throws IOException {
        // URL pattern: /resorts/{resortID}/seasons/{seasonID}/days/{dayID}/skiers

        int resortId = Integer.parseInt(urlParts[1]);
        int seasonId = Integer.parseInt(urlParts[3]);
        int dayId = Integer.parseInt(urlParts[5]);

        if (!urlParts[2].equals("seasons") || !urlParts[4].equals("day") || !urlParts[6].equals(
            "skiers")) {
            sendResponseMsg(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
        } else if (resortId != RESORT_ID || seasonId != SEASON_ID || (dayId != 1 && dayId != 2
            && dayId != 3)) {
            sendResponseMsg(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Resort ID supplied");
        } else {
            try {
                String partitionKey = resortId + "#" + seasonId + "#" + dayId;
                int uniqueSkierCount = getSpecificAttribute(partitionKey,-1, "UniqueSkiers");
                sendResponseMsg(response, HttpServletResponse.SC_OK, "{\"uniqueSkierCount\": " + uniqueSkierCount + "}");
            } catch (DynamoDbException e) {
                e.printStackTrace();
                sendResponseMsg(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "{\"error\": \"Error querying database\"}");
            }
        }
    }



    // get the total vertical for the skier for the specified ski day
    private void processSkierDayActivities(HttpServletRequest request, HttpServletResponse response,
        String[] urlParts) throws IOException {
        // Assuming URL pattern: /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}

        int resortId = Integer.parseInt(urlParts[1]);
        int seasonId = Integer.parseInt(urlParts[3]);
        int dayId = Integer.parseInt(urlParts[5]);
        int skierId = Integer.parseInt(urlParts[7]);

        if (!urlParts[2].equals("seasons") || !urlParts[4].equals("days") || !urlParts[6].equals(
            "skiers")) {
            sendResponseMsg(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
        } else if (resortId != RESORT_ID || seasonId != SEASON_ID || (dayId != 1 && dayId != 2
            && dayId != 3) ||
            skierId < LOWER_BOUND || skierId > MAX_SKIER_ID) {
            sendResponseMsg(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
        } else {
            String partitionKey = resortId + "#" + seasonId + "#" + dayId;
            int dayVertical = getSpecificAttribute(partitionKey, skierId, "DayVertical");
            sendResponseMsg(response, HttpServletResponse.SC_OK, "{\"totalVertical\": " + dayVertical + "}");
        }

    }


    // get the total vertical for the skier the specified resort. If no season is specified, return all seasons
    private void processSkierVertical(HttpServletRequest request, HttpServletResponse response,
        String[] urlParts) throws IOException {
        // Assuming URL pattern: /skiers/{skierID}/vertical
        int skierId = Integer.parseInt(urlParts[1]);
        // Get resort and season from the query parameters
        String resortParam = request.getParameter("resort");

        if (!urlParts[2].equals("vertical")) {
            sendResponseMsg(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
        } else if (skierId < LOWER_BOUND || skierId > MAX_SKIER_ID || resortParam == null
            || Integer.parseInt(resortParam) != RESORT_ID) {
            sendResponseMsg(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Path");
        } else {
            int totalVertical = getSpecificAttribute(RESORT_ID + "#" + SEASON_ID, skierId, "TotalVertical");
            sendResponseMsg(response, HttpServletResponse.SC_OK, "{\"totalVertical\": " + totalVertical + "}");
        }
    }


    private int getSpecificAttribute(String partitionKey, int skierId, String attributeName) {
        GetItemRequest request = GetItemRequest.builder()
            .tableName(TABLE_NAME)
            .key(Map.of(
                "ResortSeasonDayLiftId", AttributeValue.builder().s(partitionKey).build(),
                "SkierId", AttributeValue.builder().n(String.valueOf(skierId)).build() // Include the sort key as well
            ))
            .projectionExpression(attributeName) // Fetching only the specified attribute
            .build();

        try {
            GetItemResponse response = dynamoDbClient.getItem(request);
            int res = 0;
            if (response.hasItem()) {
                Map<String, AttributeValue> item = response.item();
                if (item.containsKey(attributeName)) {
                    AttributeValue attributeValue = item.get(attributeName);
                    // Convert the retrieved AttributeValue to an integer
                    if (attributeValue.n() != null) {
                        res = Integer.parseInt(attributeValue.n());
                        System.out.println(attributeName + " as integer: " + res);
                    } else {
                        System.out.println("Attribute value is null or not a number.");
                    }
                } else {
                    System.out.println("Attribute '" + attributeName + "' not found in the item.");
                }
            } else {
                System.out.println("No item found with the key: " + partitionKey + " and skier ID: " + skierId);
            }
            return res;
        } catch (DynamoDbException e) {
            System.err.println("Error fetching attribute from DynamoDB: " + e.getMessage());
            return 0;
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts, false)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String body = String.valueOf(req.getReader().lines().collect(Collectors.joining()));
            if (body.isEmpty()) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                String resortID = urlParts[1];
                String seasonID = urlParts[3];
                String dayID = urlParts[5];
                String skierID = urlParts[urlParts.length - 1];
                String message = packageMessage(body, resortID, seasonID, dayID, skierID);

                // Send data to RabbitMQ message queue
                sendToMessageQueue(message);
                res.setStatus(HttpServletResponse.SC_CREATED);
                res.getWriter().write("POST ok!");
            }
        }
    }

    private static void initializeChannelPool() {
        factory.setHost("44.235.243.252");
        try {
            Connection connection = factory.newConnection();
            for (int i = 0; i < CHANNEL_POOL_SIZE; i++) {
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                channelPool.add(channel);
            }
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }


        /**
         * This method is exclusive to find the pattern start with /skiers/*
         * */
        private boolean isUrlValid(String[] urlPath, Boolean get) {
            if (get) {
                return true;
            }
            else {
                if(urlPath.length == 8){
                    return isNumeric(urlPath[1]) && urlPath[2].equals("seasons") &&
                        isNumeric(urlPath[3]) && urlPath[3].length() == 4 && urlPath[4].equals("days") &&
                        isNumeric(urlPath[5]) &&
                        Integer.parseInt(urlPath[5]) >= DAY_MIN &&
                        Integer.parseInt(urlPath[5]) <= DAY_MAX &&
                        urlPath[6].equals("skiers") && isNumeric(urlPath[7]);
                }else{
                    return isNumeric(urlPath[1]) && urlPath[2].equals("vertical") ;
                }
            }

        }

        private boolean isNumeric(String s){
            if(s == null || s.equals("")) return false;
            try {
                Integer.parseInt(s);
                return true;
            }catch (NumberFormatException e){
                return false;
            }
        }

        private void sendResponseMsg(HttpServletResponse response, int statusCode, String message)
            throws IOException {
            response.setStatus(statusCode);
            response.getWriter().write(message);
        }


        // Package SkiRecord in JSON
    private String packageMessage(String body, String resortID, String seasonID, String dayID, String skierID) {
        return "{\"body\":" + body +
            ", \"resortID\":\"" + resortID + "\"" +
            ", \"seasonID\":\"" + seasonID + "\"" +
            ", \"dayID\":\"" + dayID + "\"" +
            ", \"skierID\":\"" + skierID + "\"}";
    }

    private void sendToMessageQueue(String message) {
        Channel channel = channelPool.poll();
        try{
            // Send message to queue
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                channelPool.offer(channel);
            }
        }
    }
}
