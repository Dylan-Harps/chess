import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import endpoints.ResponseException;
import model.GameData;
import ui.ServerFacade;
import websocket.WebSocketFacade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static ui.EscapeSequences.*;

//FIXME: I wanted to have ChessClient and GameplayUI be separate, but I think they'll need to be combined

public class GameplayUI {
    private final ServerFacade serverFacade;
    private final WebSocketFacade webSocketFacade;

    //user and game info
    private String team;
    private GameData gameData;

    public GameplayUI(ServerFacade serverFacade, WebSocketFacade webSocketFacade, GameData gameData, String team) {
        this.serverFacade = serverFacade;
        this.webSocketFacade = webSocketFacade;
        this.gameData = gameData;
        this.team = team;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
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

    public String redrawChessBoard(String... params) throws ResponseException {
        //sanitize input
        if (params.length != 2
                || !(params[0].equals("chess") || params[1].equals("board"))) {
            throw new ResponseException(400, "Expected: redraw chess board");
        }

        //display the game
        return displayGame(team, null, null);
    }

    public String leave() throws ResponseException {
        return null;
    }

    public String makeMove(String... params) throws ResponseException {
        return null;
    }

    public String resign() throws ResponseException {
        return null;
    }

    public String highlightLegalMoves(String... params) throws ResponseException {
        //sanitize input
        if (params.length != 3
                || !(params[0].equals("legal") || params[1].equals("moves"))) {
            throw new ResponseException(400, "Expected: highlight legal moves");
        }
        ChessPosition target = null;
        try {
            target = parsePosition(params[2]);
        } catch (Exception e) {
            throw new ResponseException(400, "<POSITION> should be a letter and a number: e.g. e5");
        }

        //display the board with highlights
        var highlightMoves = gameData.game().validMoves(target);
        return displayGame(team, finalPositionsOf(highlightMoves), target);
    }

    public String help() {
        String helpMenu;
        if (!team.equals("observer")) {
            helpMenu = """
                    Available options are:
                    redraw chess board - display the board
                    leave - leave the game
                    make move - move a piece
                    resign - concede the game to your opponent
                    highlight legal moves <POSITION> - show selected piece's possible moves
                    help - list available commands
                    """;
        } else {
            helpMenu = """
                    Available options are:
                    redraw chess board - display the board
                    leave - leave the game
                    highlight legal moves <POSITION> - show selected piece's possible moves
                    help - list available commands
                    """;
        }
        return helpMenu;
    }

    private ChessPosition parsePosition(String position) throws ResponseException {
        int col;
        int row;
        try {
            if (position.length() != 2) {
                throw new ResponseException(400, "<POSITION> should be a letter and a number: e.g. e5");
            }
            char colChar = position.charAt(0);
            col = 1 + (colChar - 'a');
            row = Integer.parseInt(String.valueOf(position.charAt(1)));
        } catch (Exception e) {
            throw new ResponseException(400, e.getMessage());
        }
        return new ChessPosition(row, col);
    }

    private Collection<ChessPosition> finalPositionsOf(Collection<ChessMove> moves) {
        Collection<ChessPosition> positions = new ArrayList<>();
        for (ChessMove m : moves) {
            positions.add(m.getEndPosition());
        }
        return positions;
    }

    public String displayGame(String teamColor, Collection<ChessPosition> highlight, ChessPosition target) {
        //create and set up the board with labels
        String[][] tempBoard = setUpGameDisplay();

        //put pieces onto the board
        fillGameDisplay(tempBoard, gameData, highlight, target);

        //turn tempBoard into a String, reversing it if viewing from black's perspective
        StringBuilder result = new StringBuilder();
        result.append(gameData.gameName()).append(":\n");
        createDisplayString(result, teamColor, tempBoard);

        return result.toString();
    }

    private String[][] setUpGameDisplay() {
        String[][] tempBoard = new String[10][10];
        var SET_BG = SET_BG_COLOR_LIGHT_GREY;

        //corners
        tempBoard[0][0] = SET_BG + "   " + RESET_BG_COLOR;
        tempBoard[0][9] = SET_BG + "   " + RESET_BG_COLOR;
        tempBoard[9][0] = SET_BG + "   " + RESET_BG_COLOR;
        tempBoard[9][9] = SET_BG + "   " + RESET_BG_COLOR;

        //column labels (letters)
        char col = 'a';
        for (int i = 1; i <= 8; ++i) {
            tempBoard[0][i] = SET_BG + " " + col + " " + RESET_BG_COLOR;
            tempBoard[9][i] = SET_BG + " " + col + " " + RESET_BG_COLOR;
            ++col;
        }

        //row labels (numbers)
        for (int i = 1; i <= 8; ++i) {
            tempBoard[9-i][0] = SET_BG + " " + i + " " + RESET_BG_COLOR;
            tempBoard[9-i][9] = SET_BG + " " + i + " " + RESET_BG_COLOR;
        }

        return tempBoard;
    }

    private void fillGameDisplay(String[][] board, GameData chessGame, Collection<ChessPosition> highlight, ChessPosition target) {
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                ChessPiece p = chessGame.game().getBoard().getPiece(new ChessPosition(r, c));
                board[9-r][c] = squareColor(r, c, highlight, target) + printPiece(p) + RESET_BG_COLOR + RESET_TEXT_COLOR;
            }
        }

    }

    private void createDisplayString(StringBuilder result, String teamColor, String[][] board) {
        int start = (teamColor.equals("WHITE") ? 0 : 9);
        int direction = (teamColor.equals("WHITE") ? 1 : -1);

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

    private String squareColor(int row, int col, Collection<ChessPosition> highlight, ChessPosition target) {
        if (target != null && target.getRow() == row && target.getColumn() == col) {
            return SET_BG_COLOR_YELLOW;
        }
        if (highlight != null && highlight.contains(new ChessPosition(row, col))) {
            return (row + col) % 2 == 0 ? SET_BG_COLOR_DARK_GREEN : SET_BG_COLOR_GREEN;
        }
        return (row + col) % 2 == 0 ? SET_BG_COLOR_BLACK : SET_BG_COLOR_WHITE;
    }

    private String printPiece(ChessPiece p) {
        if (p == null) {
            return "   ";
        }
        String color = (p.getTeamColor() == ChessGame.TeamColor.WHITE ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
        return " " + color + p.toString().toUpperCase() + " ";
    }
}
