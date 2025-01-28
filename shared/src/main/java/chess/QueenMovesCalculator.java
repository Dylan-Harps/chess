package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueenMovesCalculator extends PieceMovesCalculator {
    public QueenMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        super(board, startPosition, piece);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        List<ChessMove> validMoves = new ArrayList<ChessMove>();

        goInDirection(validMoves, 1, 0); //go up
        goInDirection(validMoves, 0, 1); //go right
        goInDirection(validMoves, -1, 0); //go down
        goInDirection(validMoves, 0, -1); //go left
        goInDirection(validMoves, 1, 1); //go diagonal up and right
        goInDirection(validMoves, 1, -1); //go diagonal up and left
        goInDirection(validMoves, -1, 1); //go diagonal down and right
        goInDirection(validMoves, -1, -1); //go diagonal down and left

        return validMoves;
    }
}
