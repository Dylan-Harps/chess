package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        List<ChessMove> validMoves = new ArrayList<ChessMove>();
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
        calculateBishopMoves(validMoves);
        calculateRookMoves(validMoves);
    }

    private void calculateKingMoves(Collection<ChessMove> validMoves) {
        for (int r = -1; r <= 1; ++r) {
            for (int c = -1; c <= 1; ++c) {
                if (r == 0 && c == 0) continue;
                ChessPosition currSpace = new ChessPosition(startPosition.getRow() + r, startPosition.getColumn() + c);
                if (isValidSpace(currSpace)) {
                    validMoves.add(new ChessMove(startPosition, currSpace, null));
                }
            }
        }
    }

    private void calculateBishopMoves(Collection<ChessMove> validMoves) {
        //check top left
        for (int i = 1; i <= 8; ++i) {
            ChessPosition currSpace = new ChessPosition(startPosition.getRow() + i, startPosition.getColumn() - i);
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
            else break;
            if (isEnemy(board.getPiece(currSpace))) break;
            //the breaks make it stop searching for valid moves once it finds a piece
        }
        //check top right
        for (int i = 1; i <= 8; ++i) {
            ChessPosition currSpace = new ChessPosition(startPosition.getRow() + i, startPosition.getColumn() + i);
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
            else break;
            if (isEnemy(board.getPiece(currSpace))) break;
        }
        //check bottom right
        for (int i = 1; i <= 8; ++i) {
            ChessPosition currSpace = new ChessPosition(startPosition.getRow() - i, startPosition.getColumn() + i);
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
            else break;
            if (isEnemy(board.getPiece(currSpace))) break;
            //the breaks make it stop searching for valid moves once it finds a piece
        }
        //check bottom left
        for (int i = 1; i <= 8; ++i) {
            ChessPosition currSpace = new ChessPosition(startPosition.getRow() - i, startPosition.getColumn() - i);
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
            else break;
            if (isEnemy(board.getPiece(currSpace))) break;
        }
    }

    private void calculateKnightMoves(Collection<ChessMove> validMoves) {
        ChessPosition[] spacesToCheck = {
                new ChessPosition(startPosition.getRow() + 1, startPosition.getColumn() + 2),
                new ChessPosition(startPosition.getRow() - 1, startPosition.getColumn() + 2),
                new ChessPosition(startPosition.getRow() + 1, startPosition.getColumn() - 2),
                new ChessPosition(startPosition.getRow() - 1, startPosition.getColumn() - 2),
                new ChessPosition(startPosition.getRow() + 2, startPosition.getColumn() + 1),
                new ChessPosition(startPosition.getRow() - 2, startPosition.getColumn() + 1),
                new ChessPosition(startPosition.getRow() + 2, startPosition.getColumn() - 1),
                new ChessPosition(startPosition.getRow() - 2, startPosition.getColumn() - 1),
        };

        for (ChessPosition currSpace : spacesToCheck) {
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
        }
    }

    private void calculateRookMoves(Collection<ChessMove> validMoves) {
        //check upwards spaces
        for (int r = startPosition.getRow() + 1; r <= 8; ++r) {
            ChessPosition currSpace = new ChessPosition(r, startPosition.getColumn());
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
            else break;
            if (isEnemy(board.getPiece(currSpace))) break;
        }
        //check right spaces
        for (int c = startPosition.getColumn() + 1; c <= 8; ++c) {
            ChessPosition currSpace = new ChessPosition(startPosition.getRow(), c);
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
            else break;
            if (isEnemy(board.getPiece(currSpace))) break;
        }
        //check downwards spaces
        for (int r = startPosition.getRow() - 1; r >=1; --r) {
            ChessPosition currSpace = new ChessPosition(r, startPosition.getColumn());
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
            else break;
            if (isEnemy(board.getPiece(currSpace))) break;
        }
        //check left spaces
        for (int c = startPosition.getColumn() - 1; c >= 1; --c) {
            ChessPosition currSpace = new ChessPosition(startPosition.getRow(), c);
            if (isValidSpace(currSpace)) validMoves.add(new ChessMove(startPosition, currSpace, null));
            else break;
            if (isEnemy(board.getPiece(currSpace))) break;
        }
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
    }

    private boolean isInBounds(ChessPosition pos) {
        return pos.getRow() <= 8 && pos.getRow() >= 1 && pos.getColumn() <= 8 && pos.getColumn() >= 1;
    }

    private boolean isEmpty(ChessPosition position) {
        return board.getPiece(position) == null;
    }

    private boolean isEnemy(ChessPiece piece2) {
        return piece.getTeamColor() != piece2.getTeamColor();
    }

    private boolean isValidSpace(ChessPosition position) {
        return isInBounds(position) && (isEmpty(position) || isEnemy(board.getPiece(position)));
    }

    private int forward(ChessGame.TeamColor teamColor) {
        if (teamColor == ChessGame.TeamColor.WHITE) return 1;
        else return -1;
    }

    private boolean isLastRow(int row, ChessGame.TeamColor teamColor) {
        if (teamColor == ChessGame.TeamColor.WHITE) return row == 8;
        else return row == 1;
    }

    private void addPromotions(Collection<ChessMove> validMoves, ChessPosition endPos) {
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.ROOK));
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.KNIGHT));
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.BISHOP));
        validMoves.add(new ChessMove(startPosition, endPos, ChessPiece.PieceType.QUEEN));
    }
}
