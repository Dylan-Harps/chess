package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import handler.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.lang.reflect.RecordComponent;
import java.util.UUID;

public class ChessService {
    MemoryAuthDAO authDataBase = new MemoryAuthDAO();
    MemoryUserDAO userDataBase = new MemoryUserDAO();
    MemoryGameDAO gameDatabase = new MemoryGameDAO();

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private static int id = 1;
    private static int generateGameID() {
        return id++;
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        //if registerRequest is missing data, throw error 400 "bad request"
        if (request.username() == null
                || request.username().isEmpty()
                || request.password() == null
                || request.password().isEmpty()
                || request.email() == null
                || request.email().isEmpty()) {
            throw new ResponseException(400, "Error: bad request");
        }
        try {
            userDataBase.getUser(request.username());
        } catch (DataAccessException e) {
            //If getUser() throws an error, then the username is not in the system and can be added
            UserData userData = new UserData(request.username(), request.password(), request.email());
            userDataBase.createUser(userData);

            String authToken = generateToken();
            AuthData authData = new AuthData(authToken, request.username());
            authDataBase.createAuth(authData);

            return new RegisterResult(request.username(), authToken);
        }
        //if getUser() returns without an error, then the username is already taken
        throw new ResponseException(403, "Error: already taken");
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        //if the request is missing data, throw an exception
        if (request.username() == null
                || request.username().isEmpty()
                || request.password() == null
                || request.password().isEmpty()) {
            throw new ResponseException(500, "Error: invalid request");
        }

        //check if username and password are correct
        UserData userData;
        try {
            userData = userDataBase.getUser(request.username());
        } catch (DataAccessException e) {
            throw new ResponseException(401, "Error: unauthorized");
        }
        if (!userData.password().equals(request.password())) {
            throw new ResponseException(401, "Error: unauthorized");
        }

        String authToken = generateToken();
        authDataBase.createAuth(new AuthData(authToken, request.username()));
        return new LoginResult(request.username(), authToken);
    }

    public LogoutResult logout(LogoutRequest request) {
        //if the request is missing data, throw an exception
        if (request.authToken() == null
                || request.authToken().isEmpty()) {
            throw new ResponseException(500, "Error: invalid request");
        }

        //log out the user
        try {
            authDataBase.deleteAuth(request.authToken());
        } catch (DataAccessException e) {
            throw new ResponseException(401, "Error: unauthorized");
        }

        return new LogoutResult();
    }

    public ListGamesResult listGames(ListGamesRequest request) throws ResponseException {
        //if the request is missing data, throw an exception
        if (request.authToken() == null
                || request.authToken().isEmpty()) {
            throw new ResponseException(500, "Error: invalid request");
        }

        //check if user is logged in
        try {
            authDataBase.getAuth(request.authToken());
        } catch (DataAccessException e) {
            throw new ResponseException(401, "Error: unauthorized");
        }

        return new ListGamesResult(gameDatabase.listGames());
    }

    public CreateGameResult createGame(CreateGameRequest request) throws ResponseException {
        //if the request is missing data, throw an exception
        if (request.authToken() == null
                || request.authToken().isEmpty()
                || request.gameName() == null
                || request.gameName().isEmpty()) {
            throw new ResponseException(400, "Error: bad request");
        }

        //check if logged in
        try {
            authDataBase.getAuth(request.authToken());
        } catch (DataAccessException e) {
            throw new ResponseException(401, "Error: unauthorized");
        }

        //create the game
        GameData gameData = new GameData(generateGameID(), null, null, request.gameName(), new ChessGame());
        gameDatabase.createGame(gameData);

        return new CreateGameResult(gameData.gameID());
    }

    public JoinGameResult joinGame(JoinGameRequest request) throws ResponseException {
        //if the request is missing data, throw an exception
        if (request.authToken() == null
                || request.authToken().isEmpty()
                || request.playerColor() == null
                || request.playerColor().isEmpty()) {
            throw new ResponseException(400, "Error: bad request");
        }

        //check if logged in
        AuthData authData;
        try {
            authData = authDataBase.getAuth(request.authToken());
        } catch (DataAccessException e) {
            throw new ResponseException(401, "Error: unauthorized");
        }

        //check if game exists
        GameData gameData;
        try {
            gameData = gameDatabase.getGame(request.gameID());
        } catch (DataAccessException e) {
            throw new ResponseException(400, "Error: bad request");
        }

        //figure out which color is wanted
        if (!(request.playerColor().equals("WHITE") || request.playerColor().equals("BLACK"))) {
            throw new ResponseException(400, "Error: bad request");
        }
        boolean isWhite = request.playerColor().equals("WHITE");

        //check if already taken
        if (isWhite && gameData.whiteUsername() != null && !gameData.whiteUsername().equals(authData.username())) {
            throw new ResponseException(403, "Error: already taken");
        } else if (!isWhite && gameData.blackUsername() != null && !gameData.blackUsername().equals(authData.username())) {
            throw new ResponseException(403, "Error: already taken");
        }

        //join the game
        GameData updatedGameData;
        if (isWhite) {
            updatedGameData = new GameData(gameData.gameID(), authData.username(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else {
            updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), authData.username(), gameData.gameName(), gameData.game());
        }
        try {
            gameDatabase.deleteGame(gameData.gameID());
            gameDatabase.createGame(updatedGameData);
        } catch (DataAccessException e) {
            throw new ResponseException(401, "Error: unauthorized");
        }

        return new JoinGameResult();
    }

    public ClearResult clear(ClearRequest request) {
        userDataBase.clearAllUserData();
        authDataBase.clearAllAuthData();
        gameDatabase.clearAllGameData();
        return new ClearResult();
    }
}
