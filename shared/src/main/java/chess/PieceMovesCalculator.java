package chess;

import java.util.Collection;
import java.util.Collections;

public class PieceMovesCalculator {
    ChessPiece piece;
    ChessBoard board;
    ChessPosition startPosition;

    public PieceMovesCalculator(ChessBoard board, ChessPosition startPosition, ChessPiece piece) {
        this.board = board;
        this.startPosition = startPosition;
        this.piece = piece;
    }

    public Collection<ChessMove> calculateMoves() {
        Collection<ChessMove> validMoves = Collections.emptyList();
        switch (piece.getPieceType()) {
            case ChessPiece.PieceType.PAWN -> { calculatePawnMoves(validMoves); }
            case ChessPiece.PieceType.ROOK -> { calculateRookMoves(validMoves); }
            case ChessPiece.PieceType.KNIGHT -> { calculateKnightMoves(validMoves); }
            case ChessPiece.PieceType.BISHOP -> { calculateBishopMoves(validMoves); }
            case ChessPiece.PieceType.KING -> { calculateKingMoves(validMoves); }
            case ChessPiece.PieceType.QUEEN -> { calculateQueenMoves(validMoves); }
        }
        return validMoves;
    }

    private void calculateQueenMoves(Collection<ChessMove> validMoves) {
    }

    private void calculateKingMoves(Collection<ChessMove> validMoves) {
    }

    private void calculateBishopMoves(Collection<ChessMove> validMoves) {
    }

    private void calculateKnightMoves(Collection<ChessMove> validMoves) {
    }

    private void calculateRookMoves(Collection<ChessMove> validMoves) {
    }

    private void calculatePawnMoves(Collection<ChessMove> validMoves) {
        //look at the space in front of the pawn
        ChessPosition frontSpace = new ChessPosition(startPosition.getRow() + forward(piece.getTeamColor()), startPosition.getColumn());
        if (isEmpty(frontSpace)) {
            if (isLastRow(frontSpace.getRow(), piece.getTeamColor())) addPromotions(validMoves, frontSpace);
            else validMoves.add(new ChessMove(startPosition, frontSpace, null));
        }

        //special case: the pawn can move 2 spaces if it hasn't moved yet
        if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && startPosition.getRow() == 2) || (piece.getTeamColor() == ChessGame.TeamColor.BLACK && startPosition.getRow() == 7)) {
            ChessPosition twoSpaces = new ChessPosition(startPosition.getRow() + (2 * forward(piece.getTeamColor())), startPosition.getColumn());
            if (isEmpty(frontSpace) && isEmpty(twoSpaces)) validMoves.add(new ChessMove(startPosition, twoSpaces, null));
        }

        //look at the space diagonal and to the left
        ChessPosition leftCapture = new ChessPosition(frontSpace.getRow(), frontSpace.getColumn() - 1);
        if (inBounds(leftCapture) && !isEmpty(leftCapture) && isEnemy(piece, board.getPiece(leftCapture))) {
            if (isLastRow(leftCapture.getRow(), piece.getTeamColor())) addPromotions(validMoves, leftCapture);
            else validMoves.add(new ChessMove(startPosition, leftCapture, null));
        }

        //look at the space diagonal and to the right
        ChessPosition rightCapture = new ChessPosition(frontSpace.getRow(), frontSpace.getColumn() + 1);
        if (inBounds(rightCapture) && !isEmpty(rightCapture) && isEnemy(piece, board.getPiece(rightCapture))) {
            if (isLastRow(rightCapture.getRow(), piece.getTeamColor())) addPromotions(validMoves, rightCapture);
            else validMoves.add(new ChessMove(startPosition, rightCapture, null));
        }
    }

    private boolean inBounds(ChessPosition pos) {
        return pos.getRow() <= 8 && pos.getRow() >= 1 && pos.getColumn() <= 8 && pos.getColumn() >= 1;
    }

    private boolean isEmpty(ChessPosition position) {
        return board.getPiece(position) == null;
    }

    private boolean isEnemy(ChessPiece piece1, ChessPiece piece2) {
        return piece1.getTeamColor() != piece2.getTeamColor();
    }

    private int forward(ChessGame.TeamColor teamColor) {
        if (teamColor == ChessGame.TeamColor.WHITE) return 1;
        else return -1;
    }

    private boolean isLastRow(int row, ChessGame.TeamColor teamColor) {
        if (teamColor == ChessGame.TeamColor.WHITE) return row == 8;
        else return row == 1;
    }

    private Collection<ChessMove> addPromotions(Collection<ChessMove> validMoves, ChessPosition endPos) {
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.ROOK));
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.KNIGHT));
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.BISHOP));
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.QUEEN));
        return validMoves;
    }
}
