package server;

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

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
