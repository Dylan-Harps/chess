package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMovesCalculator extends PieceMovesCalculator {
    public RookMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        super(board, startPosition, piece);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        List<ChessMove> validMoves = new ArrayList<>();

        goInDirection(validMoves, 1, 0); //go up
        goInDirection(validMoves, 0, 1); //go right
        goInDirection(validMoves, -1, 0); //go down
        goInDirection(validMoves, 0, -1); //go left

        return validMoves;
    }
}
