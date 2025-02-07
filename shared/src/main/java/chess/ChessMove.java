package chess;

import java.util.Objects;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    ChessPosition initialPos;
    ChessPosition finalPos;
    ChessPiece.PieceType promoteTo;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        initialPos = startPosition;
        finalPos = endPosition;
        promoteTo = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return initialPos;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return finalPos;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promoteTo;
    }

    public int getLength() {
        int rowDiff = (finalPos.row - initialPos.row) + (finalPos.col - initialPos.col);
        int colDiff = (finalPos.row - initialPos.row) + (finalPos.col - initialPos.col);
        return max(abs(rowDiff), abs(colDiff));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(initialPos, chessMove.initialPos) && Objects.equals(finalPos, chessMove.finalPos) && promoteTo == chessMove.promoteTo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialPos, finalPos, promoteTo);
    }
}
