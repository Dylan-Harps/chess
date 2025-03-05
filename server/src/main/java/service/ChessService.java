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

import java.util.UUID;

public class ChessService {
    MemoryAuthDAO authDataBase = new MemoryAuthDAO();
    MemoryUserDAO userDataBase = new MemoryUserDAO();
    MemoryGameDAO gameDatabase = new MemoryGameDAO();
    private static int id = 1;

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private static int generateGameID() {
        return id++;
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        //if registerRequest is missing data, throw an exception
        sanitizeData(request.username(), request.password(), request.email());

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
        sanitizeData(request.username(), request.password());

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
        sanitizeData(request.authToken());

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
        sanitizeData(request.authToken());

        //check if user is logged in
        checkForAuthData(request.authToken());

        return new ListGamesResult(gameDatabase.listGames());
    }

    public CreateGameResult createGame(CreateGameRequest request) throws ResponseException {
        //if the request is missing data, throw an exception
        sanitizeData(request.authToken(), request.gameName());

        //check if logged in
        checkForAuthData(request.authToken());

        //create the game
        GameData gameData = new GameData(generateGameID(), null, null, request.gameName(), new ChessGame());
        gameDatabase.createGame(gameData);

        return new CreateGameResult(gameData.gameID());
    }

    public JoinGameResult joinGame(JoinGameRequest request) throws ResponseException {
        //if the request is missing data, throw an exception
        sanitizeData(request.authToken(), request.playerColor());

        //check if logged in
        AuthData authData = checkForAuthData(request.authToken());

        //check if game exists
        GameData gameData = checkForGameData(request.gameID());

        //join the game
        GameData updatedGameData = updateGame(request.playerColor(), gameData, authData);
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

    private AuthData checkForAuthData(String authToken) throws ResponseException {
        AuthData authData;
        try {
            authData = authDataBase.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new ResponseException(401, "Error: unauthorized");
        }
        return authData;
    }

    private GameData checkForGameData(int gameID) throws ResponseException {
        GameData gameData;
        try {
            gameData = gameDatabase.getGame(gameID);
        } catch (DataAccessException e) {
            throw new ResponseException(400, "Error: bad request");
        }
        return gameData;
    }

    private void sanitizeData(String... recordElements) throws ResponseException {
        for (String e: recordElements) {
            if (e == null || e.isEmpty()) {
                throw new ResponseException(400, "Error: bad request");
            }
        }
    }

    private GameData updateGame(String playerColor, GameData gameData, AuthData authData) throws ResponseException {
        //figure out which color is wanted
        //(I promise this isn't racist)
        if (!(playerColor.equals("WHITE") || playerColor.equals("BLACK"))) {
            throw new ResponseException(400, "Error: bad request");
        }
        boolean isWhite = playerColor.equals("WHITE");

        //check if the color is already taken
        if (isWhite && gameData.whiteUsername() != null && !gameData.whiteUsername().equals(authData.username())) {
            throw new ResponseException(403, "Error: already taken");
        } else if (!isWhite && gameData.blackUsername() != null && !gameData.blackUsername().equals(authData.username())) {
            throw new ResponseException(403, "Error: already taken");
        }

        //update the gameData
        GameData updatedGameData;
        if (isWhite) {
            updatedGameData = new GameData(gameData.gameID(), authData.username(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else {
            updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), authData.username(), gameData.gameName(), gameData.game());
        }
        return updatedGameData;
    }
}
