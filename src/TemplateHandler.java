import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;

/**
 * Return specific file.
 */
class TemplateHandler extends FileHandler {
  final Path path;

  TemplateHandler(String path) {
    this.path = Paths.get(path);
  }

  @Override
  protected Path getPath(HttpExchange exchange) {
    return path;
  }
}
