import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Logger;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;

class Utils {
  private final static String LETTERS_NUMBERS = 
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
    "0123456789";

  private final static Logger log = Logger.getLogger(Utils.class.getName());

  /**
   * Return a pseudorandom string of alphanumeric characters. Alpha characters only contain uppercase letters.
   * 
   * @param n The length.
   * @return The string of random characters.
   */
  public static String getAlphaNumericString(int n) { 
    // create StringBuffer size of AlphaNumericString 
    StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      // generate a random number between 
      // 0 to AlphaNumericString variable length 
      int index = (int)(Utils.LETTERS_NUMBERS.length() * Math.random()); 

      // add Character one by one in end of sb 
      sb.append(Utils.LETTERS_NUMBERS.charAt(index)); 
    } 

    return sb.toString(); 
  } 
  
  /**
   * Extract the URL parameters from the HttpExchange objects. A query string might look like 
   * `foo=bar&tic=tac`.
   * 
   * @param exchange The exchange.
   * @return The param map.
   */
  public static Map<String, String> exchangeToParamMap(final HttpExchange exchange) {
    String query = exchange.getRequestURI().getQuery();
    Map<String, String> result = new HashMap<>();
    for (String param : query.split("&")) {
      String[] entry = param.split("=");
      if (entry.length > 1) {
        result.put(entry[0], entry[1]);
      } else {
        result.put(entry[0], "");
      }
    }

    return result;
  }

  /**
   * A simple function that will convert a string -> object map into a JSON object. If the value is 
   * a String, quotes will automatically be placed around the value.
   * 
   * @param map The map.
   * @return The JSON object string.
   */
  public static String mapToJSONString(Map<String, Object> map) {
    StringBuilder b = new StringBuilder();
    b.append("{ ");
    boolean first = true;
    for (String key : map.keySet()) {
      if (!first) {
        b.append(", ");
      }

      Object value = map.get(key);
      if (value instanceof String) {
        b.append(String.format("\"%s\": \"%s\"", key, value));
      } else {
        b.append(String.format("\"%s\": %s", key, value.toString()));
      }


      first = false;
    }

    b.append(" }");
    return b.toString();
  }

  /**
   * Try to close an object and ignore any exceptions.
   * 
   * @param o The closeable object.
   */
  public static void tryToClose(Closeable o) {
    try {
      o.close();
    } catch (IOException e) {
      // ignore
    }
  }

  /**
   * Restrict an HttpHandler to only handle GET methods and to throw 405 errors otherwise.
   * 
   * @param handler The handler.
   * @return The new handler.
   */
  public static HttpHandler handleGet(HttpHandler handler) {
    return (HttpExchange exchange) -> {
      if (!exchange.getRequestMethod().equals("GET")) {
        throw new HttpError405();
      }

      handler.handle(exchange);
    };
  }

  /**
   * Try to get an optional object otherwise throw a 400 error.
   * 
   * @param <T>
   * @param optional The optional object.
   * @param errorCode The error message to throw if not present.
   * @return The object.
   */
  public static <T> T getOrThrow(Optional<T> optional, String errorCode) {
    try {
      return optional.get();
    } catch (NoSuchElementException e) {
      throw new HttpError400(errorCode);
    }
  }

  /**
   * Return a 200 response along with a body.
   * 
   * @param exchange The exchange object.
   * @param response The data to convert into a JSON object and send as the HTTP response body.
   */
  public static void sendSuccess(HttpExchange exchange, Map<String, Object> response) {
    String data = Utils.mapToJSONString(response);
    StringBuilder body = new StringBuilder();
    body.append("{ ");
    body.append("\"result\": \"success\", ");
    body.append(String.format("\"data\": %s", data));
    body.append(" }");

    Utils.sendResponse(exchange, 200, body.toString());
  }

  /**
   * Set the correct headers for a Server-Sent Event stream. Also send a response 200.
   * 
   * @param exchange The exchange object.
   */
  public static void sendSseStream(HttpExchange exchange) {
    Headers headers = exchange.getResponseHeaders();
    headers.set("Content-Type", "text/event-stream");
    headers.set("Cache-Control", "no-cache");
    headers.set("Connection", "keep-alive");
    try {
      exchange.sendResponseHeaders(200, 0);
    } catch (IOException e) {
      Utils.log.severe("Unable to send response to " + exchange.getRemoteAddress());
      Utils.log.severe(e.toString());
    }
  }

  /**
   * Send an HTTP response.
   * 
   * @param exchange The exchange object.
   * @param code The status code. For example, 200 or 400.
   * @param response The response body.
   */
  public static void sendResponse(HttpExchange exchange, int code, String response) {
    final OutputStream body = exchange.getResponseBody();
    byte[] bytes = response.getBytes();
    try {
      exchange.sendResponseHeaders(code, bytes.length);
      body.write(bytes);
      body.close();
    } catch (IOException e) {
      Utils.log.severe("Unable to send response to " + exchange.getRemoteAddress());
      Utils.log.severe(e.toString());
    }
  }
}