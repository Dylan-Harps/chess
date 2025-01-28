package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PieceMovesCalculator {
    ChessPiece piece;
    ChessBoard board;
    ChessPosition startPosition;

    public PieceMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        this.board = board;
        this.startPosition = startPosition;
        this.piece = piece;
    }

    public Collection<ChessMove> calculateMoves() {
        return null;
    }

    protected boolean isInBounds(ChessPosition pos) {
        return pos.getRow() <= 8 && pos.getRow() >= 1 && pos.getColumn() <= 8 && pos.getColumn() >= 1;
    }

    protected boolean isEmpty(ChessPosition position) {
        return board.getPiece(position) == null;
    }

    protected boolean isEnemy(ChessPiece piece2) {
        if (piece2 == null) return false;
        return piece.getTeamColor() != piece2.getTeamColor();
    }

    protected boolean isValidSpace(ChessPosition position) {
        return isInBounds(position) && (isEmpty(position) || isEnemy(board.getPiece(position)));
    }

    protected void goInDirection(Collection<ChessMove> validMoves, int upward, int rightward) {
        //upward and rightward should be -1, 0, or 1. Go in the indicated direction and add all valid moves
        for (int i = 1; i <= 8; ++i) {
            ChessPosition currSpace = new ChessPosition(startPosition.getRow() + (i * upward), startPosition.getColumn() + (i * rightward));
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
            else break;
            if (isEnemy(board.getPiece(currSpace))) break;
        }
    }
}
