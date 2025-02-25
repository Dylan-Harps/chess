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
    ChessBoard board = new ChessBoard();

    public ChessGame() {
        board.resetBoard();
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
        if (board.getPiece(startPosition) == null) {
            return null;
        }
        Collection<ChessMove> validMoves = board.getPiece(startPosition).pieceMoves(board, startPosition);
        Collection<ChessMove> invalidMoves = new ArrayList<>();

        for (ChessMove m : validMoves) {
            TeamColor team = board.getPiece(m.initialPos).getTeamColor();
            ChessBoard hypo = board.hypothetical(m);

            //check if move would put king in check
            if (hypo.isInCheck(team)) {
                invalidMoves.add(m);
            }

            //check for invalid castling
            if (board.getPiece(m.initialPos).getPieceType() == ChessPiece.PieceType.KING
                    && m.getLength() == 2) {
                //look at the in-between space
                ChessPosition inBetween;
                if (m.finalPos.getColumn() == 3) {
                    inBetween = new ChessPosition(m.initialPos.getRow(), 4);
                }
                else {
                    inBetween = new ChessPosition(m.initialPos.getRow(), 6);
                }

                //if the in-between space is in check, castling is invalid
                ChessMove inBetweenMove = new ChessMove(m.initialPos, inBetween, null);
                ChessBoard hypo2 = board.hypothetical(inBetweenMove);
                if (hypo2.isInCheck(team)) {
                    invalidMoves.add(m);
                }
            }
        }
        validMoves.removeAll(invalidMoves);
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (board.getPiece(move.initialPos) == null
                || validMoves(move.initialPos) == null
                || getTeamTurn() != board.getPiece(move.initialPos).getTeamColor()
                || !validMoves(move.initialPos).contains(move)) {
            throw new InvalidMoveException();
        }

        //reset en passant for allied pieces (the opportunity to get captured has passed)
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                ChessPiece piece = board.getPiece(new ChessPosition(r, c));
                if (piece != null
                        && piece.getPieceType() == ChessPiece.PieceType.PAWN
                        && piece.getTeamColor() == getTeamTurn()) {
                    piece.setDidDoubleMoveLastTurn(false);
                }
            }
        }
        board = board.hypothetical(move); //make the move
        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE); //pass the turn
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return board.isInCheck(teamColor);
    }

    private boolean hasValidMoves(TeamColor teamColor) {
        //go through every space on the board
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                //check if the piece at that space exists and if it has any legal moves
                ChessPiece p = board.getPiece(new ChessPosition(r, c));
                if (p == null || p.getTeamColor() != teamColor) {
                    continue;
                }
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
