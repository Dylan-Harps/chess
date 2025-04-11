package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import endpoints.CreateGameRequest;
import endpoints.LogoutRequest;
import endpoints.RegisterRequest;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DaoTests {
    public static SQLDataAccess database = new SQLDataAccess();
    public static ChessService existingService = new ChessService(database);
    public static ChessGame existingGame;
    public static String gameJson;
    public static String existingUsername = "username";
    public static String existingPassword = "password";
    public static String existingEmail = "email";
    public static String existingGameName = "gameName";
    public static String existingAuthToken;
    public static int existingGameID;

    @BeforeEach
    public void setUp() {
        database.clear();
        existingAuthToken = existingService.register(new RegisterRequest(existingUsername, existingPassword, existingEmail)).authToken();
        existingGame = new ChessGame();
        existingGameID = existingService.createGame(new CreateGameRequest(existingAuthToken, existingGameName)).gameID();
        gameJson = new Gson().toJson(existingGame);
    }

    @Test
    @DisplayName("Serialize ChessGame")
    public void serializeChessGame() {
        ChessGame game = new ChessGame();
        var json = new Gson().toJson(game);
        Assertions.assertEquals(json, gameJson);
    }

    @Test
    @DisplayName("Deserialize ChessGame")
    public void deserializeChessGame() {
        var builder = new GsonBuilder();
        var chessGame = builder.create().fromJson(gameJson, ChessGame.class);
        Assertions.assertEquals(chessGame.getBoard().toString(), existingGame.getBoard().toString());
    }

    @Test
    @DisplayName("Create Database")
    public void createDataBase() {
        Assertions.assertDoesNotThrow(SQLDataAccess::new);
    }

    //get auth
    @Test
    @DisplayName("getAuth")
    public void getAuth() {
        Assertions.assertDoesNotThrow(() -> database.getAuth(existingAuthToken));
    }

    @Test
    @DisplayName("getAuth After Logout")
    public void wrongPassword() {
        Assertions.assertDoesNotThrow(() -> database.deleteAuth(existingAuthToken));
        Assertions.assertThrows(Exception.class, () -> database.getAuth(existingAuthToken));
    }

    //create auth
    @Test
    @DisplayName("createAuth")
    public void createAuth() {
        AuthData authData = new AuthData("testingAuthToken", "testingUsername");
        Assertions.assertDoesNotThrow(() -> database.createAuth(authData));
    }

    @Test
    @DisplayName("Duplicate createAuth")
    public void badCreateAuth() {
        AuthData authData = new AuthData("testingAuthToken", "testingUsername");
        database.createAuth(authData);
        Assertions.assertThrows(Exception.class, () -> database.createAuth(authData));
    }

    //delete auth
    @Test
    @DisplayName("deleteAuth")
    public void deleteAuth() {
        Assertions.assertDoesNotThrow(() -> database.deleteAuth(existingAuthToken));
    }

    @Test
    @DisplayName("deleteAuth after clear")
    public void badDeleteAuth() {
        database.clear();
        Assertions.assertThrows(Exception.class, () -> database.deleteAuth(existingAuthToken));
    }

    //get game
    @Test
    @DisplayName("getGame")
    public void getGame() {
        Assertions.assertDoesNotThrow(() -> database.getGame(existingGameID));
    }

    @Test
    @DisplayName("Get Nonexistent Game")
    public void badGetGame() {
        Assertions.assertThrows(Exception.class, () -> database.getGame(2));
    }

    //list games
    @Test
    @DisplayName("listGames")
    public void listGames() {
        Assertions.assertDoesNotThrow(() -> database.listGames());
        Assertions.assertDoesNotThrow(() -> database.getGame(existingGameID));
    }

    @Test
    @DisplayName("List Games Even When Empty")
    public void badListGames() {
        database.clear();
        Assertions.assertThrows(Exception.class, () -> database.getGame(existingGameID));
        Assertions.assertDoesNotThrow(() -> database.listGames());
    }

    //create game
    @Test
    @DisplayName("Create Game")
    public void createGame() {
        Assertions.assertDoesNotThrow(() -> existingService.createGame(new CreateGameRequest(existingAuthToken, "newgame")));
    }

    @Test
    @DisplayName("Create Game While Logged Out")
    public void badCreateGame() {
        existingService.logout(new LogoutRequest(existingAuthToken));
        Assertions.assertThrows(Exception.class, () -> existingService.createGame(new CreateGameRequest(existingAuthToken, existingGameName)));
    }

    //delete game
    @Test
    @DisplayName("Delete Game")
    public void deleteGame() {
        Assertions.assertDoesNotThrow(() -> database.deleteGame(existingGameID));
    }

    @Test
    @DisplayName("Delete Nonexistent Game")
    public void badDeleteGame() {
        Assertions.assertThrows(Exception.class, () -> database.deleteGame(2));
    }

    //update game
    @Test
    @DisplayName("Update Game")
    public void updateGame() {
        ChessGame updatedGame = new ChessGame();
        ChessMove move = new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1), null);
        Assertions.assertDoesNotThrow(() -> updatedGame.makeMove(move));
        Assertions.assertDoesNotThrow(() -> database.updateGame(existingGameID, updatedGame));
    }

    @Test
    @DisplayName("Update Nonexistent Game")
    public void badUpdateGame() {
        ChessGame updatedGame = new ChessGame();
        ChessMove move = new ChessMove(new ChessPosition(2, 1), new ChessPosition(3, 1), null);
        Assertions.assertDoesNotThrow(() -> updatedGame.makeMove(move));
        Assertions.assertThrows(Exception.class, () -> database.updateGame(existingGameID + 1, updatedGame));
    }

    //get user
    @Test
    @DisplayName("Get User")
    public void getUser() {
        Assertions.assertDoesNotThrow(() -> database.getUser(existingUsername));
    }

    @Test
    @DisplayName("Get Nonexistent User")
    public void badGetUser() {
        Assertions.assertThrows(Exception.class, () -> database.getUser("wrongUsername"));
    }

    //create user
    @Test
    @DisplayName("Create User")
    public void createUser() {
        UserData userData = new UserData("newUsername", "newPassword", "newEmail");
        Assertions.assertDoesNotThrow(() -> database.createUser(userData));
    }

    @Test
    @DisplayName("Create User Without Username")
    public void badCreateUser() {
        UserData userData = new UserData(null, "newPassword", "newEmail");
        Assertions.assertThrows(Exception.class, () -> database.createUser(userData));
    }

    //clear
    @Test
    @DisplayName("Clear Database")
    public void clear() {
        Assertions.assertDoesNotThrow(() -> database.clear());
        Assertions.assertThrows(Exception.class, () -> database.getUser(existingUsername));
        Assertions.assertThrows(Exception.class, () -> database.getAuth(existingAuthToken));
        Assertions.assertThrows(Exception.class, () -> database.getGame(existingGameID));
    }

}