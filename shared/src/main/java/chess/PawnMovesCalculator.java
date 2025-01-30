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
        List<ChessMove> validMoves = new ArrayList<ChessMove>();

        //look at the space in front of the pawn
        ChessPosition frontSpace = new ChessPosition(startPosition.getRow() + forward(), startPosition.getColumn());
        if (isEmpty(frontSpace)) {
            if (shouldPromote(frontSpace)) addPromotions(validMoves, frontSpace);
            else validMoves.add(new ChessMove(startPosition, frontSpace, null));
        }

        //special case: the pawn can move 2 spaces if it hasn't moved yet
        if (isUnmovedPawn()) {
            ChessPosition twoSpaces = new ChessPosition(startPosition.getRow() + (2 * forward()), startPosition.getColumn());
            if (isEmpty(frontSpace) && isEmpty(twoSpaces)) validMoves.add(new ChessMove(startPosition, twoSpaces, null));
        }

        //look at the space diagonal and to the left
        ChessPosition leftCapture = new ChessPosition(frontSpace.getRow(), frontSpace.getColumn() - 1);
        if (isInBounds(leftCapture) && !isEmpty(leftCapture) && isEnemy(leftCapture)) {
            if (shouldPromote(leftCapture)) addPromotions(validMoves, leftCapture);
            else validMoves.add(new ChessMove(startPosition, leftCapture, null));
        }

        //look at the space diagonal and to the right
        ChessPosition rightCapture = new ChessPosition(frontSpace.getRow(), frontSpace.getColumn() + 1);
        if (isInBounds(rightCapture) && !isEmpty(rightCapture) && isEnemy(rightCapture)) {
            if (shouldPromote(rightCapture)) addPromotions(validMoves, rightCapture);
            else validMoves.add(new ChessMove(startPosition, rightCapture, null));
        }

        return validMoves;
    }

    private int forward() {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) return 1;
        else return -1;
    }

    private boolean shouldPromote(ChessPosition position) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) return position.getRow() == 8;
        else return position.getRow() == 1;
    }

    private boolean isUnmovedPawn() {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) return startPosition.getRow() == 2;
        else return startPosition.getRow() == 7;
    }

    private void addPromotions(Collection<ChessMove> validMoves, ChessPosition position) {
        validMoves.add(new ChessMove(startPosition, position, ChessPiece.PieceType.ROOK));
        validMoves.add(new ChessMove(startPosition, position, ChessPiece.PieceType.KNIGHT));
        validMoves.add(new ChessMove(startPosition, position, ChessPiece.PieceType.BISHOP));
        validMoves.add(new ChessMove(startPosition, position, ChessPiece.PieceType.QUEEN));
    }
}
