package chess;

import java.util.Collection;

abstract public class PieceMovesCalculator {
    ChessPiece piece;
    ChessBoard board;
    ChessPosition startPosition;

    public PieceMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        this.board = board;
        this.startPosition = startPosition;
        this.piece = piece;
    }

    abstract public Collection<ChessMove> calculateMoves();

    protected boolean isInBounds(ChessPosition position) {
        return  position.getRow() >= 1
                && position.getRow() <= 8
                && position.getColumn() >= 1
                && position.getColumn() <= 8;
    }

    protected boolean isEmpty(ChessPosition position) {
        return board.getPiece(position) == null;
    }

    protected boolean isEnemy(ChessPosition position) {
        if (isEmpty(position)) {
            return false;
        }
        return piece.getTeamColor() != board.getPiece(position).getTeamColor();
    }

    protected boolean isValidSpace(ChessPosition position) {
        return isInBounds(position) && (isEmpty(position) || isEnemy(position));
    }

    protected ChessPosition shiftOver(int upwards, int rightwards) {
        return new ChessPosition(startPosition.getRow() + upwards, startPosition.getColumn() + rightwards);
    }

    protected void goInDirection(Collection<ChessMove> validMoves, int upward, int rightward) {
        //upward and rightward should be -1, 0, or 1. Go in the indicated direction and add all valid moves
        for (int i = 1; i <= 8; ++i) {
            ChessPosition currSpace = shiftOver(i * upward, i * rightward);
            if (isValidSpace(currSpace)) {
                validMoves.add(new ChessMove(startPosition, currSpace, null));
            }
            else {
                break;
            }
            if (isEnemy(currSpace)) {
                break;
            }
        }
    }
}
