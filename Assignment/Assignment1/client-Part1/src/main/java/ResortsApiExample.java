//import com.sun.tools.javac.util.DefinedBy.Api;
import io.swagger.client.*;
//import io.swagger.client.auth.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;

//import java.io.File;
//import java.util.*;

public class ResortsApiExample {

  public static void main(String[] args) {

    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("http://localhost:8081/servlet_war");

    SkiersApi skiersApi = new SkiersApi();
    skiersApi.setApiClient(apiClient);
    Integer resortID = 12;
    Integer skierID = 123;
    try {
      ApiResponse<Integer> apiResponse = skiersApi.getSkierDayVerticalWithHttpInfo(resortID, "2019", "1", skierID);
      System.out.println(apiResponse);
    } catch (ApiException e) {
      throw new RuntimeException(e);
    }
  }
}
