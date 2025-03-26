import chess.ChessGame;
import com.google.gson.Gson;
import endpoints.*;
import endpoints.ResponseException;
import ui.ServerFacade;

import java.util.Arrays;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade serverFacade;
    private final boolean isLoggedIn = false;
    private String authToken = null;

    public ChessClient(String port) {
        serverFacade = new ServerFacade("http://localhost:" + port);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> playGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        String helpMenu;
        if (!isLoggedIn) {
            helpMenu = """
                    Available options are:
                    register <USERNAME> <PASSWORD> <EMAIL> - create a new account
                    login <USERNAME> <PASSWORD> - start playing
                    quit - quit playing
                    help - list available commands
                    """;
        } else {
            helpMenu = """
                    Available options are:
                    list - list all chess games
                    create <NAME> - create a chess game to play
                    join <ID> <WHITE|BLACK> - join a game as a player with the color of your choice
                    observe <ID> - join a game as a spectator
                    logout - log out
                    help - list available commands
                    """;
        }
        return helpMenu;
    }

    public String login(String... params) throws ResponseException {
        if (authToken != null) {
            return "You are already logged in";
        }
        try {
            if (params.length != 2) {
                throw new ResponseException(400, "Expected: login <USERNAME> <PASSWORD>");
            }
            String username = params[0];
            String password = params[1];
            authToken = serverFacade.loginUser(new LoginRequest(username, password)).authToken();
            return "Logged in as " + username;
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String register(String... params) throws ResponseException {
        try {
            if (params.length != 3) {
                throw new ResponseException(400, "Expected: register <USERNAME> <PASSWORD> <EMAIL>");
            }
            String username = params[0];
            String password = params[1];
            String email = params[2];
            authToken = serverFacade.registerUser(new RegisterRequest(username, password, email)).authToken();
            return "Logged in as " + username;
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String logout() throws ResponseException {
        assertLoggedIn();
        try {
            serverFacade.logoutUser(new LogoutRequest(authToken));
            authToken = null;
            return "Logged out";
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String createGame(String... params) throws ResponseException {
        assertLoggedIn();
        try {
            if (params.length != 1) {
                throw new ResponseException(400, "Expected: create <NAME>");
            }
            String gameName = params[0];
            serverFacade.createGame(new CreateGameRequest(authToken, gameName));
            return "Created game with the name " + gameName;
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String listGames() {
        assertLoggedIn();
        try {
            var games = serverFacade.listGames(new ListGamesRequest(authToken)).games();
            var result = new StringBuilder();
            var gson = new Gson();
            for (var game : games) {
                result.append(gson.toJson(game)).append('\n'); //TODO: don't print out the games' json
            }
            return result.toString();
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String playGame(String... params) {
        assertLoggedIn();
        //TODO
        return null;
    }

    public String observeGame(String... params) {
        assertLoggedIn();
        ChessGame.TeamColor perspective;
        //TODO
        return null;
    }

    private void assertLoggedIn() {
        if (!isLoggedIn) {
            throw new ResponseException(400, "You must first sign in");
        }
    }
}
