import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * A runtime exception class to easily return HTTP errors by throwing an exception.
 */
abstract class HttpError extends RuntimeException {
  private static final long serialVersionUID = 3903228837194891673L;

  private final int status;
  private final String errorCode;

  protected HttpError(int status, String errorCode) {
    this.status = status;
    this.errorCode = errorCode;
  }

  /**
   * Wrap an HttpHandler to add a try/catch block. If an HttpError is thrown within the given 
   * handler, it is caught and the appropriate HTTP response is returned. This makes it very easy
   * to propagate errors from call stacks.
   * 
   * @param handler The handler to wrap.
   * @return The new handler.
   */
  static HttpHandler withErrorHandler(HttpHandler handler) {
    return (HttpExchange exchange) -> {
      try {
        handler.handle(exchange);
      } catch (HttpError e) {
        Map<String, Object> body = new HashMap<>();
        body.put("result", "error");
        body.put("error", e.errorCode);

        Utils.sendResponse(exchange, e.status, Utils.mapToJSONString(body));
      }
    };
  }
}

/**
 * 400 Bad Request.
 */
class HttpError400 extends HttpError {
  private static final long serialVersionUID = 3903228837194891673L;

  HttpError400(String errorCode) {
    super(400, errorCode);
  }
}

/**
 * 405 Method Not Allowed
 * 
 * This should be returned when an invalid method request is sent to an endpoint.
 */
class HttpError405 extends HttpError {
  private static final long serialVersionUID = 3903228837194891673L;

  HttpError405() {
    super(405, "Method Not Allowed");
  }
}
