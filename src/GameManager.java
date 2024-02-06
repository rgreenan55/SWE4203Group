import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

class GameManager implements Disposer {
  private final static Logger log = Logger.getLogger(Main.class.getName());
  List<Game> games = new ArrayList<>();
  public void startGame(HttpExchange exchange) {
    Game game = new Game();
    this.games.add(game);

    Map<String, Object> response = new HashMap<>();
    response.put("accessCode", game.accessCode);
    response.put("gameCode", game.gameCode);
    Utils.sendSuccess(exchange, response);
  }

  public void searchForGame(HttpExchange exchange) {
    final Map<String, String> params = Utils.exchangeToParamMap(exchange);

    if (!params.containsKey("accessCode")) {
      throw new HttpError400("NO_ACCESS_CODE");
    }

    String accessCode = params.get("accessCode");
    for (Game game : this.games) {
      if (game.accessCode.equals(accessCode)) {
        Map<String, Object> response = new HashMap<>();
        response.put("gameCode", game.gameCode);
        Utils.sendSuccess(exchange, response);
        return;
      }
    }

    throw new HttpError400("ACCESS_CODE_INVALID");
  }

  public void joinAsHost(HttpExchange exchange) {
    String gameCode = this.getGameCodeOrThrow(exchange);
    Game game = this.getGameOrThrow(gameCode);
    if (game.hasHost()) {
      throw new HttpError400("PLAYER_ALREADY_PRESENT");
    }

    GameManager.log.info(String.format("Player joined as host (%s)!", gameCode));
    Utils.sendSseStream(exchange);
    final OutputStream body = exchange.getResponseBody();
    game.setHost(body);
  }

  public void joinAsOpponent(HttpExchange exchange) {
    String gameCode = this.getGameCodeOrThrow(exchange);
    Game game = this.getGameOrThrow(gameCode);
    if (game.hasOpponent()) {
      throw new HttpError400("PLAYER_ALREADY_PRESENT");
    }

    GameManager.log.info(String.format("Player joined as opponent (%s)!", gameCode));
    Utils.sendSseStream(exchange);
    final OutputStream body = exchange.getResponseBody();
    game.setOpponent(body);
  }

  public void move(HttpExchange exchange) {
    System.out.println("MOVE");
    String gameCode = this.getGameCodeOrThrow(exchange);
    Game game = this.getGameOrThrow(gameCode);

    final Map<String, String> params = Utils.exchangeToParamMap(exchange);
    if (!params.containsKey("x")) {
      throw new HttpError400("NO_X");
    } else if (!params.containsKey("y")) {
      throw new HttpError400("NO_Y");
    } else if (!params.containsKey("player")) {
      throw new HttpError400("NO_PLAYER");
    }

    int x = this.parseOrThrow(params.get("x"), "INVALID_X");
    int y = this.parseOrThrow(params.get("y"), "INVALID_Y");
    
    Player player;
    try {
      player = Player.valueOf(params.get("player"));
    } catch (IllegalArgumentException e) {
      throw new HttpError400("INVALID_PLAYER");
    }

    PlayResult result = game.play(player, x, y);

    // GAME_FINISHED and GAME_NOT_FINISHED are not error states
    // the rest are and should return 400 errors
    if (result != PlayResult.GAME_FINISHED && result != PlayResult.GAME_NOT_FINISHED) {
      throw new HttpError400(result.toString());
    }

    Map<String, Object> response = new HashMap<>();
    if (result == PlayResult.GAME_FINISHED) {
      this.games.remove(game);
      response.put("gameOver", true);
    } else {
      response.put("gameOver", false);
    }

    Utils.sendSuccess(exchange, response);
  }

  private int parseOrThrow(String s, String errorCode) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      throw new HttpError400(errorCode);
    }
  }

  private String getGameCodeOrThrow(HttpExchange exchange) {
    final Map<String, String> params = Utils.exchangeToParamMap(exchange);

    if (!params.containsKey("gameCode")) {
      throw new HttpError400("NO_GAME_CODE");
    }

    return params.get("gameCode");
  }

  private Game getGameOrThrow(String gameCode) {
    for (Game game : this.games) {
      if (game.gameCode.equals(gameCode)) {
        return game;
      }
    }

    throw new HttpError400("NO_GAME_FOUND");
  }

  public void dispose() {
    this.games.forEach((game) -> {
      game.dispose();
    });
  }
}
