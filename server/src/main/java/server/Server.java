package server;

import com.google.gson.Gson;
import handler.ChessHandler;
import endpoints.ResponseException;
import spark.*;
import endpoints.*;

public class Server {
    private final ChessHandler handler = new ChessHandler();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.webSocket("/ws", handler);

        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clear);
        Spark.exception(ResponseException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.status());
        res.body(ex.toJson());
    }

    private Object register(Request request, Response response) throws ResponseException {
        RegisterRequest registerRequest = new Gson().fromJson(request.body(), RegisterRequest.class);
        RegisterResult registerResult = handler.register(registerRequest);
        return new Gson().toJson(registerResult);
    }

    private Object login(Request request, Response response) throws ResponseException {
        LoginRequest loginRequest = new Gson().fromJson(request.body(), LoginRequest.class);
        LoginResult loginResult = handler.login(loginRequest);
        return new Gson().toJson(loginResult);
    }

    private Object logout(Request request, Response response) throws ResponseException {
        String authToken = request.headers("authorization");
        LogoutRequest logoutRequest = new LogoutRequest(authToken);
        LogoutResult logoutResult = handler.logout(logoutRequest);
        return new Gson().toJson(logoutResult);
    }

    private Object listGames(Request request, Response response) throws ResponseException {
        String authToken = request.headers("authorization");
        ListGamesRequest listGamesRequest = new ListGamesRequest(authToken);
        ListGamesResult listGamesResult = handler.listGames(listGamesRequest);
        return new Gson().toJson(listGamesResult);
    }

    private Object createGame(Request request, Response response) throws ResponseException {
        String authToken = request.headers("authorization");
        CreateGameRequest temp = new Gson().fromJson(request.body(), CreateGameRequest.class);
        CreateGameRequest createGameRequest = new CreateGameRequest(authToken, temp.gameName());
        CreateGameResult createGameResult = handler.createGame(createGameRequest);
        return new Gson().toJson(createGameResult);
    }

    private Object joinGame(Request request, Response response) throws ResponseException {
        String authToken = request.headers("authorization");
        JoinGameRequest temp = new Gson().fromJson(request.body(), JoinGameRequest.class);
        JoinGameRequest joinGameRequest = new JoinGameRequest(authToken, temp.playerColor(), temp.gameID());
        JoinGameResult joinGameResult = handler.joinGame(joinGameRequest);
        return new Gson().toJson(joinGameResult);
    }

    private Object clear(Request request, Response response) throws ResponseException {
        ClearResult clearResult = handler.clear(new ClearRequest());
        return new Gson().toJson(clearResult);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
