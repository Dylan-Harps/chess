import chess.ChessGame;
import endpoints.*;
import model.GameData;
import ui.ServerFacade;

import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade serverFacade;
    //user info
    private String username = null;
    private String authToken = null;
    private boolean isLoggedIn = false;
    //store gamesList
    Integer nextClientSideGameID = 1;
    private Map<Integer, Integer> myGames = new TreeMap<>(); //first int is the client id, second int is the db id
    private Map<Integer, Integer> otherGames = new TreeMap<>();

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
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR;
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
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: login <USERNAME> <PASSWORD>");
        }
        try {
            String username = params[0];
            String password = params[1];
            if (isLoggedIn && this.username.equals(username)) {
                throw new ResponseException(400, "You are already logged in");
            }
            authToken = serverFacade.loginUser(new LoginRequest(username, password)).authToken();
            isLoggedIn = true;
            this.username = username;
            return "Logged in as " + username;
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length != 3) {
            throw new ResponseException(400, "Expected: register <USERNAME> <PASSWORD> <EMAIL>");
        }
        try {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            authToken = serverFacade.registerUser(new RegisterRequest(username, password, email)).authToken();
            isLoggedIn = true;
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
            isLoggedIn = false;
            username = null;
            return "Logged out";
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String createGame(String... params) throws ResponseException {
        assertLoggedIn();
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: create <NAME>");
        }
        try {
            String gameName = params[0];
            int externalGameID = serverFacade.createGame(new CreateGameRequest(authToken, gameName)).gameID();
            myGames.put(nextClientSideGameID++, externalGameID);
            return "Created game with the name " + gameName;
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String listGames() throws ResponseException {
        assertLoggedIn();
        try {
            var games = serverFacade.listGames(new ListGamesRequest(authToken)).games();
            var result = new StringBuilder();

            //list the client-made games first
            if (!myGames.isEmpty()) {
                result.append("Recent Games:\n");
            }
            for (var entry : myGames.entrySet()) {
                int myID = entry.getKey();
                int dbID = entry.getValue();
                GameData temp = null;
                List<GameData> gamesToRemove = new ArrayList<>();

                for (var dbGame : games) {
                    if (dbGame.gameID() == dbID) {
                        temp = dbGame;
                        gamesToRemove.add(dbGame);
                    }
                }
                games.removeAll(gamesToRemove); //exclude recent games from the rest of the list
                result.append(myID).append(". ").append(temp.gameName()).append('\n');
            }

            //list the rest of the games
            if (!myGames.isEmpty()) {
                result.append("Other Games:\n");
            }
            otherGames.clear();
            Integer clientGameID = nextClientSideGameID;
            for (var game : games) {
                otherGames.put(clientGameID, game.gameID());
                result.append(clientGameID).append(". ").append(game.gameName()).append('\n');
                ++clientGameID;
            }

            return result.toString();
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String joinGame(String... params) throws ResponseException {
        assertLoggedIn();
        //TODO
        // remember that all input (including color) will be lowercase
        return null;
    }

    public String observeGame(String... params) throws ResponseException {
        assertLoggedIn();
        ChessGame.TeamColor perspective;
        //TODO
        // remember that all input (including color) will be lowercase
        return null;
    }

    private void assertLoggedIn() throws ResponseException {
        if (!isLoggedIn) {
            throw new ResponseException(400, "You must sign in first");
        }
    }

    private GameData selectGame(int clientID) {
        //TODO
        return null;
    }

    private String displayGame(GameData gameData) {
        //TODO
        return null;
    }
}
