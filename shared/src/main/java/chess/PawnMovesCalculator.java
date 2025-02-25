package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMovesCalculator extends PieceMovesCalculator {
    public PawnMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        super(board, startPosition, piece);
    }

    @Override
    public Collection<ChessMove> calculateMoves() {
        List<ChessMove> validMoves = new ArrayList<>();

        forwardMove(validMoves); //look at the space in front of the pawn
        doubleMove(validMoves); //special case: the pawn can move 2 spaces if it hasn't moved yet
        captureMove(validMoves, shiftOver(forward(1), -1)); //look at the space diagonal and to the left
        captureMove(validMoves, shiftOver(forward(1), 1)); //look at the space diagonal and to the right
        enPassantMove(validMoves, -1); //en passant left
        enPassantMove(validMoves, 1); //en passant right

        return validMoves;
    }

    void forwardMove(Collection<ChessMove> validMoves) {
        ChessPosition frontSpace = shiftOver(forward(1), 0);
        if (isEmpty(frontSpace) && !promotionMove(validMoves, frontSpace)) {
            validMoves.add(new ChessMove(startPosition, frontSpace, null));
        }
    }

    void doubleMove(Collection<ChessMove> validMoves) {
        boolean isInStartingRow = startPosition.row == (piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 2 : 7);
        if (isInStartingRow) {
            ChessPosition twoSpaces = shiftOver(forward(2), 0);
            if (isEmpty(shiftOver(forward(1), 0)) && isEmpty(twoSpaces)) {
                validMoves.add(new ChessMove(startPosition, twoSpaces, null));
            }
        }
    }

    void captureMove(Collection<ChessMove> validMoves, ChessPosition captureSpace) {
        if (isInBounds(captureSpace) && isEnemy(captureSpace) && !promotionMove(validMoves, captureSpace)) {
            validMoves.add(new ChessMove(startPosition, captureSpace, null));
        }
    }

    void enPassantMove(Collection<ChessMove> validMoves, int rightwards) {
        ChessPosition sideSpace = shiftOver(0, rightwards);
        ChessPosition diagonal = shiftOver(forward(1), rightwards);
        boolean isInEnPassantRow = startPosition.row == (piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 5 : 4);
        if (isInEnPassantRow
                && isEnemy(sideSpace)
                && isEmpty(diagonal)
                && board.getPiece(sideSpace).getDidDoubleMoveLastTurn()) {
            validMoves.add(new ChessMove(startPosition, diagonal, null));
        }
    }

    boolean promotionMove(Collection<ChessMove> validMoves, ChessPosition position) {
        if (position.getRow() == (piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1)) {
            validMoves.add(new ChessMove(startPosition, position, ChessPiece.PieceType.ROOK));
            validMoves.add(new ChessMove(startPosition, position, ChessPiece.PieceType.KNIGHT));
            validMoves.add(new ChessMove(startPosition, position, ChessPiece.PieceType.BISHOP));
            validMoves.add(new ChessMove(startPosition, position, ChessPiece.PieceType.QUEEN));
            return true;
        }
        return false;
    }

    private int forward(int amount) {
        if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            amount = -amount;
        }
        return amount;
    }
}
