import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
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
    private Map<Integer, Integer> gameIdList = new TreeMap<>(); //<client id, database id>
    private Collection<GameData> allGames = null;

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
                case "quit" -> quit();
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
                    login <USERNAME> <PASSWORD> - sign in to start playing
                    quit - exit program
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

    public String quit() {
        if (isLoggedIn) {
            logout();
        }
        return "quit";
    }

    public String login(String... params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: login <USERNAME> <PASSWORD>");
        }
        if (isLoggedIn && this.username.equals(params[0])) {
            throw new ResponseException(400, "You are already logged in");
        }
        try {
            String username = params[0];
            String password = params[1];
            authToken = serverFacade.loginUser(new LoginRequest(username, password)).authToken();
            isLoggedIn = true;
            this.username = username;
            return "Logged in as " + username;
        } catch (ResponseException e) {
            throw new ResponseException(400, "Wrong username or password");
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
            throw new ResponseException(400, "That username is already taken");
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
            gameIdList.put(nextClientSideGameID++, externalGameID);
            allGames = serverFacade.listGames(new ListGamesRequest(authToken)).games();
            return "Created game with the name " + gameName;
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String listGames() throws ResponseException {
        assertLoggedIn();
        try {
            var listGames = serverFacade.listGames(new ListGamesRequest(authToken)).games();
            var result = new StringBuilder().append("Available games:\n");
            allGames = listGames;

            nextClientSideGameID = 1;
            for (var dbGame : listGames) {
                gameIdList.put(nextClientSideGameID, dbGame.gameID());
                result.append(nextClientSideGameID).append(". ").append(dbGame.gameName()).append('\n');
                ++nextClientSideGameID;
            }

            return result.toString();
        } catch (ResponseException e) {
            throw e;
        }
    }

    public String joinGame(String... params) throws ResponseException {
        assertLoggedIn();
        //sanitize input
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: join <ID> <WHITE|BLACK>");
        }
        int id;
        try {
            id = Integer.parseInt(params[0]);
        } catch (Exception e) {
            throw new ResponseException(400, "<ID> should be an integer");
        }
        String teamColor = params[1].toUpperCase();
        if (!(teamColor.equals("WHITE") || teamColor.equals("BLACK"))) {
            throw new ResponseException(400, "<WHITE|BLACK> should be either WHITE or BLACK");
        }
        if (allGames == null) {
            listGames();
        }

        try {
            serverFacade.joinGame(new JoinGameRequest(authToken, teamColor, selectGameID(id)));
        } catch (Exception e) {
            if (e.getMessage().equals("Invalid game ID")) {
                throw e;
            }
            throw new ResponseException(400, teamColor + " is already taken"); //FIXME: this isn't working???
        }

        return displayGame(selectGameID(id), teamColor);
    }

    public String observeGame(String... params) throws ResponseException {
        assertLoggedIn();
        //sanitize input
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: observe <ID>");
        }
        int id;
        try {
            id = Integer.parseInt(params[0]);
        } catch (Exception e) {
            throw new ResponseException(400, "<ID> should be an integer");
        }
        if (allGames == null) {
            listGames();
        }

        return displayGame(selectGameID(id), "WHITE");
    }

    private void assertLoggedIn() throws ResponseException {
        if (!isLoggedIn) {
            throw new ResponseException(400, "You must sign in first");
        }
    }

    private int selectGameID(int clientID) throws ResponseException {
        try {
            return gameIdList.get(clientID);
        } catch (Exception e){
            throw new ResponseException(400, "Invalid game ID");
        }
    }

    private GameData selectGameData(int dbID) {
        GameData chosenGame = null;
        for (var g : allGames) {
            if (g.gameID() == dbID) {
                chosenGame = g;
            }
        }
        return chosenGame;
    }

    private String displayGame(int dbID, String teamColor) {
        GameData chosenGame = selectGameData(dbID);

        //create and set up the board with labels
        String[][] tempBoard = new String[10][10];
        //corners
        tempBoard[0][0] = SET_BG_COLOR_LIGHT_GREY + "   " + RESET_BG_COLOR;
        tempBoard[0][9] = SET_BG_COLOR_LIGHT_GREY + "   " + RESET_BG_COLOR;
        tempBoard[9][0] = SET_BG_COLOR_LIGHT_GREY + "   " + RESET_BG_COLOR;
        tempBoard[9][9] = SET_BG_COLOR_LIGHT_GREY + "   " + RESET_BG_COLOR;
        //column labels (letters)
        char col = 'a';
        for (int i = 1; i <= 8; ++i) {
            tempBoard[0][i] = SET_BG_COLOR_LIGHT_GREY + " " + String.valueOf(col) + " " + RESET_BG_COLOR;
            tempBoard[9][i] = SET_BG_COLOR_LIGHT_GREY + " " + String.valueOf(col) + " " + RESET_BG_COLOR;
            ++col;
        }
        //row labels (numbers)
        for (int i = 1; i <= 8; ++i) {
            tempBoard[i][0] = SET_BG_COLOR_LIGHT_GREY + " " + String.valueOf(i) + " " + RESET_BG_COLOR;
            tempBoard[i][9] = SET_BG_COLOR_LIGHT_GREY + " " + String.valueOf(i) + " " + RESET_BG_COLOR;
        }

        //put pieces onto the board
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                ChessPiece p = chosenGame.game().getBoard().getPiece(new ChessPosition(r, c));
                tempBoard[9-r][9-c] = squareColor(r, c) + pieceColor(p) + RESET_BG_COLOR + RESET_TEXT_COLOR;
            }
        }

        //finally turn tempBoard into a result, reversing it if viewing from black's perspective
        StringBuilder result = new StringBuilder();
        result.append(chosenGame.gameName()).append(":\n");
        int start = (teamColor.equals("WHITE") ? 0 : 9);
        int direction = (teamColor.equals("WHITE") ? 1 : -1);
        for (int r = start; withinBounds(r); r += direction) {
            for (int c = start; withinBounds(c); c += direction) {
                result.append(tempBoard[r][c]);
            }
            result.append("\n");
        }
        return result.toString();
    }

    private boolean withinBounds(int i) {
        return i >= 0 && i <= 9;
    }

    private String squareColor(int row, int col) {
        return (row + col) % 2 == 0 ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK;
        //return (row + col) % 2 == 0 ? SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK : SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE;
    }

    private String pieceColor(ChessPiece p) {
        if (p == null) {
            return "   ";
        }
        String color = (p.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
        return " " + color + p.toString().toUpperCase() + " ";
    }
}
