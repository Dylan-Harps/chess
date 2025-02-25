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

    //makes a deep copy
    public ChessBoard(ChessBoard b) {
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                board[r-1][c-1] = b.getPiece(new ChessPosition(r, c));
            }
        }
    }

    public ChessBoard hypothetical(ChessMove move) {
        ChessBoard hypo = new ChessBoard(this);
        if (hypo.getPiece(move.initialPos) == null) {
            return hypo;
        }

        //check if the PieceType changes due to promotion
        ChessPiece oldPiece = hypo.getPiece(move.initialPos);
        ChessPiece newPiece = new ChessPiece(oldPiece);
        if (move.getPromotionPiece() != null) {
            newPiece.setPieceType(move.getPromotionPiece());
        }

        //if a pawn did a double-move, mark it as having done so
        if (oldPiece.getPieceType() == ChessPiece.PieceType.PAWN
                && move.getLength() == 2) {
            newPiece.setDidDoubleMoveLastTurn(true);
        }

        //check if a pawn captured en passant
        if (oldPiece.getPieceType() == ChessPiece.PieceType.PAWN
                && hypo.getPiece(move.finalPos) == null
                && move.finalPos.getColumn() != move.initialPos.getColumn()) {
            ChessPosition enemy = new ChessPosition(move.initialPos.getRow(), move.finalPos.getColumn());
            hypo.addPiece(enemy, null);
        }

        //check for castling
        if (oldPiece.getPieceType() == ChessPiece.PieceType.KING
                && move.getLength() == 2
                && !isInCheck(oldPiece.getTeamColor())) {
            boolean isQueenSide = move.finalPos.getColumn() == 3;
            ChessPosition rookStart = new ChessPosition(move.initialPos.getRow(), (isQueenSide ? 1 : 8));
            ChessPosition rookEnd = new ChessPosition(move.finalPos.getRow(), (isQueenSide ? 4 : 6));
            if (hypo.getPiece(rookStart) != null) {
                ChessPiece rook = new ChessPiece(hypo.getPiece(rookStart));
                ChessMove rookMove = new ChessMove(rookStart, rookEnd, null);
                doMove(hypo, rook, rookMove);
            }
        }

        doMove(hypo, newPiece, move);
        return hypo;
    }

    private void doMove(ChessBoard board, ChessPiece piece, ChessMove move) {
        if (piece == null) {
            return;
        }
        piece.setHasMoved();
        board.addPiece(move.finalPos, piece); //add piece to new position
        board.addPiece(move.initialPos, null); //remove piece from old position
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        if (position.getRow() > 8
                || position.getRow() < 1
                || position.getColumn() > 8
                || position.getColumn() < 1) {
            return;
        }
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
        if (position.getRow() > 8
                || position.getRow() < 1
                || position.getColumn() > 8
                || position.getColumn() < 1) {
            return null;
        }
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
                case 0 -> homeRow(row, ChessGame.TeamColor.WHITE);
                case 1 -> pawnRow(row, ChessGame.TeamColor.WHITE);
                case 6 -> pawnRow(row, ChessGame.TeamColor.BLACK);
                case 7 -> homeRow(row, ChessGame.TeamColor.BLACK);
                default -> clearRow(row);
            }
        }
    }

    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        //go through every space on the board
        for (int r = 1; r <= 8; ++r) {
            for (int c = 1; c <= 8; ++c) {
                //see if the space contains an enemy piece
                ChessPiece p = this.getPiece(new ChessPosition(r, c));
                if (p == null || p.getTeamColor() == teamColor) {
                    continue;
                }
                //see if the enemy is putting the king in check
                var enemyMoves = p.pieceMoves(this, new ChessPosition(r, c));
                for (ChessMove m : enemyMoves) {
                    var target = this.getPiece(m.finalPos);
                    if (target != null
                            && target.getTeamColor() == teamColor
                            && target.getPieceType() == ChessPiece.PieceType.KING) {
                        return true;
                    }
                }
            }
        }
        return false;
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
