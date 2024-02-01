import java.util.stream.Collectors;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;


public class SkierServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts, false)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      res.getWriter().write("It works!");
    }
  }

  private boolean isUrlValid(String[] urlPath, Boolean get) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    if (get) {
      return true;
    }
    else {
      String urlPath0 = urlPath[0];
      String urlPath1 = urlPath[1];
      String urlPath2 = urlPath[2];
      String urlPath3 = urlPath[3];
      String urlPath4 = urlPath[4];
      String urlPath5 = urlPath[5];
      String urlPath6 = urlPath[6];
      String urlPath7 = urlPath[7];
      try {
        Integer.parseInt(urlPath1);
        Integer.parseInt(urlPath3);
        Integer.parseInt(urlPath5);
        return true;
      } catch(NumberFormatException e){
        return false;
      }
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts, false)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params

      String body = String.valueOf(req.getReader().lines().collect(Collectors.joining()));

      if (body.isEmpty()) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
      else {
        res.getWriter().write("POST ok!");
      }
    }
  }

}
