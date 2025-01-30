package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator extends PieceMovesCalculator {
    private final int[][] spacesToCheck = { {1, 2}, {-1, 2}, {1, -2}, {-1, -2}, {2, 1}, {-2, 1}, {2, -1}, {-2, -1} };

    public KnightMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        super(board, startPosition, piece);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        List<ChessMove> validMoves = new ArrayList<>();
        for (int[] s : spacesToCheck) {
            ChessPosition currSpace = shiftOver(s[0], s[1]);
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
        }
        return validMoves;
    }
}
