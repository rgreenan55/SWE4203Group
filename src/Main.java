import java.io.IOException;
import java.util.logging.Logger;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

class Main {
  // Setup Logging Object
  private final static Logger log = Logger.getLogger(Main.class.getName());

  public static void main(String args[]) throws IOException {
    // If a port is provided on startup use. If not use 3000.
    int port = 3000;
    if (args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        Main.log.severe("Unable to parse port number from: " + args[0]);
        System.exit(1);
      }
    }

    // Set up HTTP Server at LocalHost:3000
    Main.log.info("Running at -> http://localhost:" + port);
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", new TemplateHandler("static/index.html"));
    server.createContext("/static/", new StaticHandler("/static/"));

    // Setup Game Manager
    GameManager manager = new GameManager();
    server.createContext("/api/start-server", HttpError.withErrorHandler(Utils.handleGet(manager::startGame)));
    server.createContext("/api/search-for-game", HttpError.withErrorHandler(Utils.handleGet(manager::searchForGame)));
    server.createContext("/api/join-as-host", HttpError.withErrorHandler(Utils.handleGet(manager::joinAsHost)));
    server.createContext("/api/join-as-opponent", HttpError.withErrorHandler(Utils.handleGet(manager::joinAsOpponent)));
    server.createContext("/api/move", HttpError.withErrorHandler(Utils.handleGet(manager::move)));
    server.createContext("/api/resetGame", HttpError.withErrorHandler(Utils.handleGet(manager::resetGame)));

    // Add shutdown hook to stop the server
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      Main.log.info("Shutting down server...");
      server.stop(0);
      manager.dispose();
    }));

    // Finally, start the server!
    server.start();
  }
}
