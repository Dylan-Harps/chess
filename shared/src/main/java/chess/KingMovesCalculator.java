package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KingMovesCalculator extends PieceMovesCalculator {
    private final int[][] spacesToCheck = { {1, -1}, {1, 0}, {1, 1}, {0, -1}, {0, 1}, {-1, -1}, {-1, 0}, {-1, 1} };

    public KingMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        super(board, startPosition, piece);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        List<ChessMove> validMoves = new ArrayList<>();

        //normal movement
        for (int[] s : spacesToCheck) {
            ChessPosition currSpace = shiftOver(s[0], s[1]);
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
        }

        //castling
        //TODO: not only do the in-between spaces have to be empty, but they also can't put the king in check
        if (!piece.getHasMoved()) {
            ChessPiece rook;
            //queenSide
            rook = board.getPiece(new ChessPosition(startPosition.getRow(), 1));
            if (rook != null && !rook.getHasMoved()
                    && isEmpty(shiftOver(0, -1))
                    && isEmpty(shiftOver(0, -2))
                    && isEmpty(shiftOver(0, -3))) {
                validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), 3), null));
            }
            //kingSide
            rook = board.getPiece(new ChessPosition(startPosition.getRow(), 8));
            if (rook != null && !rook.getHasMoved()
                    && isEmpty(shiftOver(0, 1))
                    && isEmpty(shiftOver(0, 2))) {
                validMoves.add(new ChessMove(startPosition, new ChessPosition(startPosition.getRow(), 7), null));
            }
        }
        return validMoves;
    }
}
