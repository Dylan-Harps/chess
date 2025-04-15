package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    ChessGame.TeamColor team;
    PieceType type;
    boolean hasMoved = false;
    boolean didDoubleMoveLastTurn = false;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        team = pieceColor;
        this.type = type;
    }

    public ChessPiece(ChessPiece that) {
        this.team = that.getTeamColor();
        this.type = that.getPieceType();
        this.hasMoved = that.getHasMoved();
        this.didDoubleMoveLastTurn = that.getDidDoubleMoveLastTurn();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return team == that.team && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(team, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return team;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    public void setPieceType(PieceType type) {
        this.type = type;
    }

    public boolean getHasMoved() {
        return hasMoved;
    }

    public void setHasMoved() {
        hasMoved = true;
    }

    public boolean getDidDoubleMoveLastTurn() {
        return didDoubleMoveLastTurn;
    }

    public void setDidDoubleMoveLastTurn(boolean didDoubleMove) {
        didDoubleMoveLastTurn = didDoubleMove;
    }

    @Override
    public String toString() {
        PieceType t = this.type;
        boolean isBlack = this.team == ChessGame.TeamColor.BLACK;
        switch (t) {
            case PAWN -> { return (isBlack) ? "p": "P"; }
            case BISHOP -> { return (isBlack) ? "b": "B"; }
            case KNIGHT -> { return (isBlack) ? "n": "N"; }
            case ROOK -> { return (isBlack) ? "r": "R"; }
            case QUEEN -> { return (isBlack) ? "q": "Q"; }
            case KING -> { return (isBlack) ? "k": "K"; }
        }
        return null;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceMovesCalculator calc = switch (type) {
            case ChessPiece.PieceType.PAWN -> new PawnMovesCalculator(board, myPosition, this);
            case ChessPiece.PieceType.ROOK -> new RookMovesCalculator(board, myPosition, this);
            case ChessPiece.PieceType.KNIGHT -> new KnightMovesCalculator(board, myPosition, this);
            case ChessPiece.PieceType.BISHOP -> new BishopMovesCalculator(board, myPosition, this);
            case ChessPiece.PieceType.KING -> new KingMovesCalculator(board, myPosition, this);
            case ChessPiece.PieceType.QUEEN -> new QueenMovesCalculator(board, myPosition, this);
        };
        return calc.calculateMoves();
    }
}
