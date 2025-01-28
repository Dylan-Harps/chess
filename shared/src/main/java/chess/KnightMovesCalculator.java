package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KnightMovesCalculator extends PieceMovesCalculator {
    public KnightMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        super(board, startPosition, piece);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        List<ChessMove> validMoves = new ArrayList<ChessMove>();

        int[][] spacesToCheck = { {1, 2}, {-1, 2}, {1, -2}, {-1, -2}, {2, 1}, {-2, 1}, {2, -1}, {-2, -1} };
        for (int[] s : spacesToCheck) {
            ChessPosition currSpace = new ChessPosition(startPosition.getRow() + s[0], startPosition.getColumn() + s[1]);
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
        }

        return validMoves;
    }
}
