package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMovesCalculator extends PieceMovesCalculator {
    public BishopMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        super(board, startPosition, piece);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        List<ChessMove> validMoves = new ArrayList<>();

        goInDirection(validMoves, 1, 1); //go up and right
        goInDirection(validMoves, 1, -1); //go up and left
        goInDirection(validMoves, -1, 1); //go down and right
        goInDirection(validMoves, -1, -1); //go down and left

        return validMoves;
    }
}
