package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMovesCalculator extends PieceMovesCalculator {
    public KingMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        super(board, startPosition, piece);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        List<ChessMove> validMoves = new ArrayList<ChessMove>();
        for (int r = -1; r <= 1; ++r) {
            for (int c = -1; c <= 1; ++c) {
                if (r == 0 && c == 0) continue;
                ChessPosition currSpace = new ChessPosition(startPosition.getRow() + r, startPosition.getColumn() + c);
                if (isValidSpace(currSpace)) {
                    validMoves.add(new ChessMove(startPosition, currSpace, null));
                }
            }
        }
        return validMoves;
    }
}
