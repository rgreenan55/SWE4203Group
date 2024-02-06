import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * A handler that will serve files in an HTTP response.
 */
abstract class FileHandler implements HttpHandler {
  /**
   * Get the path of the file to return.
   * 
   * @param exchange The HttpExchange object.
   * @return The path object of the file to stream back.
   */
  protected abstract Path getPath(HttpExchange exchange);

  /**
   * The HTTP handler. Returns a 200 if the file exists and a 404 if the file is not found.
   * 
   * @param exchange The HttpExchange object.
   */
  public void handle(HttpExchange exchange) throws IOException {
    final Path filePath = this.getPath(exchange);
    final OutputStream body = exchange.getResponseBody();

    final File file = filePath.toFile();
    if (!file.exists()) {
      byte[] response = ("File not found").getBytes();
      exchange.sendResponseHeaders(404, response.length);
      body.write(response);
      body.close();
      return;
    }

    final FileInputStream stream = new FileInputStream(file);
    final byte[] response = new byte[(int) file.length()];
    stream.read(response);
    stream.close();

    exchange.sendResponseHeaders(200, response.length);
    body.write(response);
    body.close();
  }
}