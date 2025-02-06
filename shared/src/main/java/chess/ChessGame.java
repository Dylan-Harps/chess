package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor activeTeam = TeamColor.WHITE;
    ChessBoard board;

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return activeTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        activeTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if (board.getPiece(startPosition) == null) return null;
        Collection<ChessMove> validMoves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        for (ChessMove m : validMoves) {
            ChessBoard hypo = board.hypothetical(m);
            if (isInCheck(hypo.getPiece(m.finalPos).getTeamColor())) validMoves.remove(m);
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (!validMoves(move.initialPos).contains(move)) throw new InvalidMoveException();
        board = board.hypothetical(move);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        //go through every space on the board
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                //see if the space contains an enemy piece
                ChessPiece p = board.getPiece(new ChessPosition(r, c));
                if (p == null || p.getTeamColor() == teamColor) continue;
                //see if the enemy is putting the king in check
                var enemyMoves = validMoves(new ChessPosition(r, c));
                for (ChessMove m : enemyMoves) {
                    var target = board.getPiece(m.finalPos);
                    if (target != null && target.getTeamColor() == teamColor && target.getPieceType() == ChessPiece.PieceType.KING) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasValidMoves(TeamColor teamColor) {
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                ChessPiece p = board.getPiece(new ChessPosition(r, c));
                if (p == null || p.getTeamColor() != teamColor) continue;
                if (!validMoves(new ChessPosition(r, c)).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasValidMoves(teamColor) && getTeamTurn() == teamColor;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
