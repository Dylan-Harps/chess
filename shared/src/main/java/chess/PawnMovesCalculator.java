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
        ChessPosition frontSpace = new ChessPosition(startPosition.getRow() + forward(piece.getTeamColor()), startPosition.getColumn());
        if (isEmpty(frontSpace)) {
            if (isLastRow(frontSpace.getRow(), piece.getTeamColor())) addPromotions(validMoves, frontSpace);
            else validMoves.add(new ChessMove(startPosition, frontSpace, null));
        }

        //special case: the pawn can move 2 spaces if it hasn't moved yet
        if (isUnmovedPawn(startPosition.getRow(), piece.getTeamColor())) {
            ChessPosition twoSpaces = new ChessPosition(startPosition.getRow() + (2 * forward(piece.getTeamColor())), startPosition.getColumn());
            if (isEmpty(frontSpace) && isEmpty(twoSpaces)) validMoves.add(new ChessMove(startPosition, twoSpaces, null));
        }

        //look at the space diagonal and to the left
        ChessPosition leftCapture = new ChessPosition(frontSpace.getRow(), frontSpace.getColumn() - 1);
        if (isInBounds(leftCapture) && !isEmpty(leftCapture) && isEnemy(board.getPiece(leftCapture))) {
            if (isLastRow(leftCapture.getRow(), piece.getTeamColor())) addPromotions(validMoves, leftCapture);
            else validMoves.add(new ChessMove(startPosition, leftCapture, null));
        }

        //look at the space diagonal and to the right
        ChessPosition rightCapture = new ChessPosition(frontSpace.getRow(), frontSpace.getColumn() + 1);
        if (isInBounds(rightCapture) && !isEmpty(rightCapture) && isEnemy(board.getPiece(rightCapture))) {
            if (isLastRow(rightCapture.getRow(), piece.getTeamColor())) addPromotions(validMoves, rightCapture);
            else validMoves.add(new ChessMove(startPosition, rightCapture, null));
        }

        return validMoves;
    }

    private int forward(ChessGame.TeamColor teamColor) {
        if (teamColor == ChessGame.TeamColor.WHITE) return 1;
        else return -1;
    }

    private boolean isLastRow(int row, ChessGame.TeamColor teamColor) {
        if (teamColor == ChessGame.TeamColor.WHITE) return row == 8;
        else return row == 1;
    }

    private boolean isUnmovedPawn(int row, ChessGame.TeamColor teamColor) {
        if (teamColor == ChessGame.TeamColor.WHITE) return row == 2;
        else return row == 7;
    }

    private void addPromotions(Collection<ChessMove> validMoves, ChessPosition endPos) {
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.ROOK));
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.KNIGHT));
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.BISHOP));
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.QUEEN));
    }
}
