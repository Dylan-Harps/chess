import chess.*;
import server.Server;

//TODO: add DataAccessException to DAO classes


public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess server.Server: " + piece);

        Server cheese = new Server();
        cheese.run(8080);
    }
}