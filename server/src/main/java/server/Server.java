package server;

import com.google.gson.Gson;
import handler.ChessHandler;
import handler.ResponseException;
import service.*;
import spark.*;

//endpoints:
// 1. register. username, email, password. return username, authToken
// 2. login. username, password. return username, authToken
// 3. logout. authToken. return null

// 4. listGames. authToken. return list of games
// 5. createGame. authToken, gameName. return gameID
// 6. joinGame. authToken, gameName. return gameID

// 7. clear. null. return null

public class Server {
    private final ChessHandler handler = new ChessHandler();
    //private final UserService userService = new UserService();
    //private final GameService gameService = new GameService();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.webSocket("/ws", handler);

        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        //Spark.delete("/session", this::logout);
        //Spark.get("/game", this::listGames);
        //Spark.post("/game", this::createGame);
        //Spark.put("/game", this::joinGame);
        //Spark.delete("/db", this::clear);
        Spark.exception(ResponseException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.Status());
        res.body(ex.toJson());
        //FIXME: exceptions are not being returned
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

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
