package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {

    }

    public ChessBoard(ChessBoard b) {
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                board[r-1][c-1] = b.getPiece(new ChessPosition(r, c));
            }
        }
    }

    public ChessBoard hypothetical(ChessMove move) {
        ChessBoard hypo = new ChessBoard(this);
        if (hypo.getPiece(move.initialPos) != null) {
            ChessPiece oldPiece = hypo.getPiece(move.initialPos);
            ChessPiece.PieceType promotion = move.getPromotionPiece();
            ChessPiece newPiece = new ChessPiece(oldPiece.getTeamColor(), promotion != null ? promotion : oldPiece.getPieceType());
            hypo.addPiece(move.finalPos, newPiece);
            hypo.addPiece(move.initialPos, null);
        }
        return hypo;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        if (position.getRow() > 8 || position.getRow() < 1 || position.getColumn() > 8 || position.getColumn() < 1) return;
        board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (position.getRow() > 8 || position.getRow() < 1 || position.getColumn() > 8 || position.getColumn() < 1) return null;
        return board[position.getRow()-1][position.getColumn()-1];
    }

    private void clearRow(int r) {
        for(int i = 0; i < 8; ++i) {
            board[r][i] = null;
        }
    }

    private void pawnRow(int r, ChessGame.TeamColor t) {
        for(int i = 0; i < 8; ++i) {
            board[r][i] = new ChessPiece(t, ChessPiece.PieceType.PAWN);
        }
    }

    private void homeRow(int r, ChessGame.TeamColor t) {
        board[r][0] = new ChessPiece(t, ChessPiece.PieceType.ROOK);
        board[r][1] = new ChessPiece(t, ChessPiece.PieceType.KNIGHT);
        board[r][2] = new ChessPiece(t, ChessPiece.PieceType.BISHOP);
        board[r][3] = new ChessPiece(t, ChessPiece.PieceType.QUEEN);
        board[r][4] = new ChessPiece(t, ChessPiece.PieceType.KING);
        board[r][5] = new ChessPiece(t, ChessPiece.PieceType.BISHOP);
        board[r][6] = new ChessPiece(t, ChessPiece.PieceType.KNIGHT);
        board[r][7] = new ChessPiece(t, ChessPiece.PieceType.ROOK);
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int row = 0; row < 8; ++row) {
            switch (row) {
                case 0 -> { homeRow(row, ChessGame.TeamColor.WHITE); }
                case 1 -> { pawnRow(row, ChessGame.TeamColor.WHITE); }
                case 6 -> { pawnRow(row, ChessGame.TeamColor.BLACK); }
                case 7 -> { homeRow(row, ChessGame.TeamColor.BLACK); }
                default -> { clearRow(row); }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                ChessPiece p = board[r-1][c-1];
                out.append("|" + ((p == null) ? " " : p.toString()));
            }
            out.append("|\n");
        }
        return out.toString();
    }
}
