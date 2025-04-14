import chess.*;
import endpoints.*;
import model.GameData;
import ui.ServerFacade;
import websocket.MessageHandler;
import websocket.WebSocketFacade;
import java.util.*;
import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade serverFacade;
    private final WebSocketFacade webSocketFacade;

    //user info
    private String username = null;
    private String authToken = null;
    private enum PlayerState { LOGGED_OUT, LOGGED_IN, PLAYING, OBSERVING }
    private PlayerState state = PlayerState.LOGGED_OUT;

    //game info (when playing)
    private String team;
    private GameData gameData;

    //store gamesList
    Integer nextClientGameID = 1;
    private final Map<Integer, Integer> gameIdList = new TreeMap<>(); //<client id, database id>
    private Collection<GameData> allGames = null;

    public ChessClient(String port, MessageHandler messageHandler) {
        serverFacade = new ServerFacade("http://localhost:" + port);
        webSocketFacade = new WebSocketFacade("http://localhost:" + port, messageHandler);
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
                case "redraw" -> redrawChessBoard(params);
                case "leave" -> leave();
                case "make" -> makeMove(params);
                case "resign" -> resign();
                case "highlight" -> highlightLegalMoves(params);
                default -> help();
            };
        } catch (ResponseException ex) {
            return SET_TEXT_COLOR_RED + ex.getMessage() + RESET_TEXT_COLOR;
        }
    }

    public String help() {
        return switch (state) {
            case LOGGED_OUT -> """
                    Available options are:
                    register <USERNAME> <PASSWORD> <EMAIL> - create a new account
                    login <USERNAME> <PASSWORD> - sign in to start playing
                    quit - exit program
                    help - list available commands
                    """;
            case LOGGED_IN -> """
                    Available options are:
                    list - list all chess games
                    create <NAME> - create a chess game to play
                    join <ID> <WHITE|BLACK> - join a game as a player with the color of your choice
                    observe <ID> - join a game as a spectator
                    logout - log out
                    help - list available commands
                    """;
            case OBSERVING -> """
                    Available options are:
                    redraw chess board - display the board
                    leave - leave the game
                    highlight legal moves <POSITION> - show selected piece's possible moves
                    help - list available commands
                    """;
            case PLAYING -> """
                    Available options are:
                    redraw chess board - display the board
                    leave - leave the game
                    make move <FROM POSITION> <TO POSITION> - move a piece
                    make move <FROM POSITION> <TO POSITION> <PROMOTION> - move and promote a piece
                    resign - surrender the game to your opponent
                    highlight legal moves <POSITION> - show selected piece's possible moves
                    help - list available commands
                    """;
        };
    }

    //pre-game UI
    public String quit() throws ResponseException {
        assertLoggedOut();
        return "quit";
    }
    public String login(String... params) throws ResponseException {
        assertLoggedOut();
        //sanitize input
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: login <USERNAME> <PASSWORD>");
        }
        if (state == PlayerState.LOGGED_IN && this.username.equals(params[0])) {
            throw new ResponseException(400, "You are already logged in");
        }

        //login the user
        try {
            String username = params[0];
            //set user info
            authToken = serverFacade.loginUser(new LoginRequest(username, params[1])).authToken();
            state = PlayerState.LOGGED_IN;
            this.username = username;

            return "Logged in as " + username;
        } catch (ResponseException e) {
            throw new ResponseException(400, "Wrong username or password");
        }
    }
    public String register(String... params) throws ResponseException {
        assertLoggedOut();
        //sanitize input
        if (params.length != 3) {
            throw new ResponseException(400, "Expected: register <USERNAME> <PASSWORD> <EMAIL>");
        }

        //register the user
        try {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            //set user info
            authToken = serverFacade.registerUser(new RegisterRequest(username, password, email)).authToken();
            state = PlayerState.LOGGED_IN;
            this.username = username;

            return "Logged in as " + username;
        } catch (ResponseException e) {
            throw new ResponseException(400, "That username is already taken");
        }
    }
    public String logout() throws ResponseException {
        assertLoggedIn();
        serverFacade.logoutUser(new LogoutRequest(authToken));

        //reset user info
        authToken = null;
        state = PlayerState.LOGGED_OUT;
        username = null;

        return "Logged out";
    }
    public String createGame(String... params) throws ResponseException {
        //sanitize input
        assertLoggedIn();
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: create <NAME>");
        }

        //create the game
        String gameName = params[0];
        int dbGameID = serverFacade.createGame(new CreateGameRequest(authToken, gameName)).gameID();
        gameIdList.put(nextClientGameID++, dbGameID); //optional, I think

        //after adding the game, refresh games list
        allGames = serverFacade.listGames(new ListGamesRequest(authToken)).games();

        return "Created game with the name " + gameName;
    }
    public String listGames() throws ResponseException {
        assertLoggedIn();
        var gamesList = serverFacade.listGames(new ListGamesRequest(authToken)).games();
        allGames = gamesList; //refresh list of games
        gameIdList.clear(); //If there are errors with createGame, try deleting this line

        var result = new StringBuilder().append("Available games:\n");
        nextClientGameID = 1;
        for (var dbGame : gamesList) {
            gameIdList.put(nextClientGameID, dbGame.gameID());
            result.append(nextClientGameID).append(". ").append(dbGame.gameName());
            result.append(dbGame.game().getGameOver() ? " (Finished)\n" : '\n');
            String white = (dbGame.whiteUsername() != null) ? dbGame.whiteUsername() : "Nobody";
            result.append("    ").append(white).append(" playing as white\n");
            String black = (dbGame.blackUsername() != null) ? dbGame.blackUsername() : "Nobody";
            result.append("    ").append(black).append(" playing as black\n");
            ++nextClientGameID;
        }

        return result.toString();
    }
    public String joinGame(String... params) throws ResponseException {
        assertLoggedIn();
        //sanitize input
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: join <ID> <WHITE|BLACK>");
        }
        int selectedId;
        try {
            selectedId = Integer.parseInt(params[0]);
        } catch (Exception e) {
            throw new ResponseException(400, "<ID> should be an integer");
        }
        String teamColor = params[1].toUpperCase();
        if (!(teamColor.equals("WHITE") || teamColor.equals("BLACK"))) {
            throw new ResponseException(400, "<WHITE|BLACK> should be either WHITE or BLACK");
        }
        if (allGames == null) {
            listGames(); //since selectedId() uses the result from listGames(), if they haven't called it yet, do it for them
        }

        //join the game
        try {
            serverFacade.joinGame(new JoinGameRequest(authToken, teamColor, selectGameID(selectedId)));
            GameData gameData = selectGameData(selectGameID(selectedId));
            state = PlayerState.PLAYING;
            team = teamColor;
            this.gameData = gameData;
            webSocketFacade.connectToGame(authToken, selectGameID(selectedId));
        } catch (Exception e) {
            if (e.getMessage().equals("Invalid game ID")) {
                throw e;
            }
            throw new ResponseException(400, teamColor + " team is already taken");
        }
        return "";
    }
    public String observeGame(String... params) throws ResponseException {
        assertLoggedIn();
        //sanitize input
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: observe <ID>");
        }
        int selectedId;
        try {
            selectedId = Integer.parseInt(params[0]);
        } catch (Exception e) {
            throw new ResponseException(400, "<ID> should be an integer");
        }
        if (allGames == null) {
            listGames(); //since selectedId() uses the result from listGames(), if they haven't called it yet, do it for them
        }

        //join game as observer
        try {
            GameData gameData = selectGameData(selectGameID(selectedId));
            state = PlayerState.OBSERVING;
            team = "observer";
            this.gameData = gameData;
            webSocketFacade.connectToGame(authToken, selectGameID(selectedId));
        } catch (Exception e) {
            if (e.getMessage().equals("Invalid game ID")) {
                throw e;
            }
            throw new ResponseException(500, e.getMessage());
        }
        return "";
    }

    //gameplay UI
    public String redrawChessBoard(String... params) throws ResponseException {
        assertInGame();
        //sanitize input
        if (params.length != 2
                || !(params[0].equals("chess") || params[1].equals("board"))) {
            throw new ResponseException(400, "Expected: redraw chess board");
        }

        //display the game
        return displayGame();
    }
    public String leave() throws ResponseException {
        assertInGame();
        try {
            int gameID = gameData.gameID();
            state = PlayerState.LOGGED_IN;
            team = null;
            gameData = null;
            webSocketFacade.leaveGame(authToken, gameID);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
        return "";
    }
    public String makeMove(String... params) throws ResponseException {
        assertPlayer();
        //sanitize and parse input
        if (params.length < 3 || params.length > 4 || !params[0].equals("move")) {
            throw new ResponseException(400, "Expected: make move <FROM POSITION> <TO POSITION> <PROMOTION>");
        }
        ChessPiece.PieceType promotion = params.length == 4 ? parsePromotion(params[3]) : null;
        ChessMove move = new ChessMove(parsePosition(params[1]), parsePosition(params[2]), promotion);

        //make the move
        try {
            webSocketFacade.makeMove(authToken, gameData.gameID(), move);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
        return "";
    }
    public String resign() throws ResponseException {
        assertPlayer();
        try {
            webSocketFacade.resign(authToken, gameData.gameID());
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
        return "";
    }
    public String highlightLegalMoves(String... params) throws ResponseException {
        assertInGame();
        //sanitize input
        if (params.length != 3
                || !(params[0].equals("legal") || params[1].equals("moves"))) {
            throw new ResponseException(400, "Expected: highlight legal moves <POSITION>");
        }
        ChessPosition target = null;
        try {
            target = parsePosition(params[2]);
        } catch (Exception e) {
            throw new ResponseException(400, "<POSITION> should be a letter and a number: e.g. e5");
        }

        //display the board with highlights
        var highlightMoves = gameData.game().validMoves(target);
        return displayGameHighlight(finalPositionsOf(highlightMoves), target);
    }

    //helper functions
    //assert playerState
    private void assertLoggedIn() throws ResponseException {
        if (state == PlayerState.LOGGED_OUT) {
            throw new ResponseException(400, "You must sign in first");
        } else if (state != PlayerState.LOGGED_IN) {
            throw new ResponseException(400, "You must leave the game first");
        }
    }
    private void assertLoggedOut() throws ResponseException {
        if (state != PlayerState.LOGGED_OUT) {
            throw new ResponseException(400, "You must log out first");
        }
    }
    private void assertInGame() throws ResponseException {
        if (!(state == PlayerState.PLAYING || state == PlayerState.OBSERVING)) {
            throw new ResponseException(400, "You must join a game first");
        }
    }
    private void assertPlayer() throws ResponseException {
        if (state != PlayerState.PLAYING) {
            throw new ResponseException(400, "You must be a player in the game");
        }
    }
    //getting game info
    private int selectGameID(int clientID) throws ResponseException {
        //converts client IDs to database IDs
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
    public void updateGameData(GameData updatedChessGame) {
        gameData = updatedChessGame;
    }
    //chess positions
    private ChessPosition parsePosition(String position) throws ResponseException {
        int col;
        int row;
        try {
            if (position.length() != 2) {
                throw new Exception();
            }
            char colChar = position.charAt(0);
            col = 1 + (colChar - 'a');
            row = Integer.parseInt(String.valueOf(position.charAt(1)));

            if (!(colChar >= 'a' && colChar <= 'h') || !(row >= 1 && row <= 8)) {
                throw new Exception("Error: out of range");
            }
        } catch (Exception e) {
            throw new ResponseException(400, "<POSITION> should be a letter and a number: e.g. e5");
        }
        return new ChessPosition(row, col);
    }
    private ChessPiece.PieceType parsePromotion(String promoteTo) throws ResponseException {
        return switch (promoteTo.toLowerCase()) {
            case "q", "queen" -> ChessPiece.PieceType.QUEEN;
            case "n", "knight" -> ChessPiece.PieceType.KNIGHT;
            case "b", "bishop" -> ChessPiece.PieceType.BISHOP;
            case "r", "rook" -> ChessPiece.PieceType.ROOK;
            default -> throw new ResponseException(400, "<PROMOTION> should be a valid chess piece");
        };
    }
    private Collection<ChessPosition> finalPositionsOf(Collection<ChessMove> moves) {
        Collection<ChessPosition> positions = new ArrayList<>();
        for (ChessMove m : moves) {
            positions.add(m.getEndPosition());
        }
        return positions;
    }

    //displaying the board
    public String displayGame() {
        return displayGameHighlight(null, null);
    }
    private String[][] setUpBoardLabels() {
        String[][] tempBoard = new String[10][10];
        var setBG = SET_BG_COLOR_LIGHT_GREY;

        //corners
        tempBoard[0][0] = setBG + "   " + RESET_BG_COLOR;
        tempBoard[0][9] = setBG + "   " + RESET_BG_COLOR;
        tempBoard[9][0] = setBG + "   " + RESET_BG_COLOR;
        tempBoard[9][9] = setBG + "   " + RESET_BG_COLOR;

        //column labels (letters)
        char col = 'a';
        for (int i = 1; i <= 8; ++i) {
            tempBoard[0][i] = setBG + " " + col + " " + RESET_BG_COLOR;
            tempBoard[9][i] = setBG + " " + col + " " + RESET_BG_COLOR;
            ++col;
        }

        //row labels (numbers)
        for (int i = 1; i <= 8; ++i) {
            tempBoard[9-i][0] = setBG + " " + i + " " + RESET_BG_COLOR;
            tempBoard[9-i][9] = setBG + " " + i + " " + RESET_BG_COLOR;
        }

        return tempBoard;
    }
    private String printPiece(ChessPiece p) {
        if (p == null) {
            return "   ";
        }
        String color = (p.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
        return " " + color + p.toString().toUpperCase() + " ";
    }
    private void flipBoardIfNeeded(StringBuilder result, String[][] board) {
        int start = (team.equals("WHITE") || team.equals("observer") ? 0 : 9);
        int direction = (team.equals("WHITE") || team.equals("observer") ? 1 : -1);

        for (int r = start; withinDisplayBounds(r); r += direction) {
            for (int c = start; withinDisplayBounds(c); c += direction) {
                result.append(board[r][c]);
            }
            result.append("\n");
        }
    }
    private boolean withinDisplayBounds(int i) {
        return i >= 0 && i <= 9;
    }
    //displaying the board with highlights
    public String displayGameHighlight(Collection<ChessPosition> highlight, ChessPosition target) {
        //create and set up the board with labels
        String[][] tempBoard = setUpBoardLabels();

        //put pieces onto the board
        putPiecesOnBoardHighlight(tempBoard, gameData, highlight, target);

        //turn tempBoard into a String, reversing it if viewing from black's perspective
        StringBuilder result = new StringBuilder();
        result.append(gameData.gameName()).append(":\n");
        flipBoardIfNeeded(result, tempBoard);

        return result.toString();
    }
    private void putPiecesOnBoardHighlight(String[][] board, GameData chessGame, Collection<ChessPosition> highlight, ChessPosition target) {
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                ChessPiece p = chessGame.game().getBoard().getPiece(new ChessPosition(r, c));
                board[9-r][c] = squareColorHighlight(r, c, highlight, target) + printPiece(p) + RESET_BG_COLOR + RESET_TEXT_COLOR;
            }
        }

    }
    private String squareColorHighlight(int row, int col, Collection<ChessPosition> highlight, ChessPosition target) {
        if (target != null && target.getRow() == row && target.getColumn() == col) {
            return SET_BG_COLOR_YELLOW;
        }
        if (highlight != null && highlight.contains(new ChessPosition(row, col))) {
            return (row + col) % 2 == 0 ? SET_BG_COLOR_DARK_GREEN : SET_BG_COLOR_GREEN;
        }
        return (row + col) % 2 == 0 ? SET_BG_COLOR_BLACK : SET_BG_COLOR_WHITE;
    }
}
