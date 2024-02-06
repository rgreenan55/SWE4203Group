import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;

/**
 * Static asset handler. 
 */
class StaticHandler extends FileHandler {
  /**
   * The rootURI. This should match the context path.
   */
  final private String rootURI;

  StaticHandler(String rootURI) {
    this.rootURI = rootURI;
  }

  @Override
  protected Path getPath(HttpExchange exchange) {
    final URI requestURI = exchange.getRequestURI();
    final String filePath = requestURI.getPath();
    // replaceFirst removes the rootURI from the beginning of the string since we don't care about it
    // We pass in "static" since we are looking for files in the "static" folder
    return Paths.get("static", filePath.replaceFirst("^" + this.rootURI, ""));
  }
}
