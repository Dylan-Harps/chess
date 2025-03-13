package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import server.Server;

import static java.lang.System.out;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DaoTests {
    public static String gameJson = "{\"activeTeam\":\"WHITE\",\"board\":{\"board\":[[{\"team\":\"WHITE\",\"type\":\"ROOK\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"KNIGHT\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"BISHOP\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"QUEEN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"KING\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"BISHOP\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"KNIGHT\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"ROOK\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false}],[{\"team\":\"WHITE\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"WHITE\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false}],[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],[null,null,null,null,null,null,null,null],[{\"team\":\"BLACK\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"PAWN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false}],[{\"team\":\"BLACK\",\"type\":\"ROOK\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"KNIGHT\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"BISHOP\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"QUEEN\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"KING\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"BISHOP\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"KNIGHT\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false},{\"team\":\"BLACK\",\"type\":\"ROOK\",\"hasMoved\":false,\"didDoubleMoveLastTurn\":false}]]}}";
    public static ChessGame existingGame = new ChessGame();

    //register
    @Test
    @Order(1)
    @DisplayName("Serialize ChessGame")
    public void serializeChessGame() {
        ChessGame game = new ChessGame();
        var json = new Gson().toJson(game);
        Assertions.assertEquals(json, gameJson);
    }

    @Test
    @Order(2)
    @DisplayName("Deserialize ChessGame")
    public void deserializeChessGame() {
        var builder = new GsonBuilder();
        var chessGame = builder.create().fromJson(gameJson, ChessGame.class);
        Assertions.assertEquals(chessGame.getBoard().toString(), existingGame.getBoard().toString());
    }

    @Test
    @Order(3)
    @DisplayName("Create Database")
    public void createDataBase() {
        Assertions.assertDoesNotThrow(SQLDataAccess::new);
    }
}