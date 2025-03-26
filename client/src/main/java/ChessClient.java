import chess.ChessGame;
import endpoints.ResponseException;
import ui.ServerFacade;

import java.util.Arrays;
import java.util.Scanner;

public class ChessClient {
    private ServerFacade serverFacade;
    private String port;
    boolean isLoggedIn = false;

    public ChessClient(String port) {
        this.port = port;
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

    public String login(String... params) {
        //TODO
        return null;
    }

    public String register(String... params) {
        //TODO
        return null;
    }

    public String logout() {
        //TODO
        return null;
    }

    public String createGame(String... params) {
        //TODO
        return null;
    }

    public String listGames() {
        //TODO
        return null;
    }

    public String playGame(String... params) {
        //TODO
        return null;
    }

    public String observeGame(String... params) {
        ChessGame.TeamColor perspective;
        //TODO
        return null;
    }
}
