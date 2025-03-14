package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.AuthData;
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

    //get auth
    @Test
    @Order(4)
    @DisplayName("getAuth")
    public void getAuth() {
        Assertions.assertDoesNotThrow(() -> database.getAuth(existingAuthToken));
    }

    @Test
    @Order(5)
    @DisplayName("getAuth After Logout")
    public void wrongPassword() {
        Assertions.assertDoesNotThrow(() -> database.deleteAuth(existingAuthToken));
        Assertions.assertThrows(Exception.class, () -> database.getAuth(existingAuthToken));
    }

    //create auth
    @Test
    @Order(6)
    @DisplayName("createAuth")
    public void createAuth() {
        AuthData authData = new AuthData("testingAuthToken", "testingUsername");
        Assertions.assertDoesNotThrow(() -> database.createAuth(authData));
    }

    @Test
    @Order(7)
    @DisplayName("Duplicate createAuth")
    public void badCreateAuth() {
        AuthData authData = new AuthData("testingAuthToken", "testingUsername");
        database.createAuth(authData);
        Assertions.assertThrows(Exception.class, () -> database.createAuth(authData));
    }

    //delete auth
    @Test
    @Order(8)
    @DisplayName("deleteAuth")
    public void deleteAuth() {
        Assertions.assertDoesNotThrow(() -> database.deleteAuth(existingAuthToken));
    }

    @Test
    @Order(9)
    @DisplayName("deleteAuth after clear")
    public void badDeleteAuth() {
        database.clear();
        Assertions.assertThrows(Exception.class, () -> database.deleteAuth(existingAuthToken));
    }

    //get game
    @Test
    @Order(10)
    @DisplayName("getGame")
    public void getGame() {
        Assertions.assertDoesNotThrow(() -> database.getGame(existingGameID));
    }

    @Test
    @Order(11)
    @DisplayName("Get Nonexistent Game")
    public void badGetGame() {
        Assertions.assertThrows(Exception.class, () -> database.getGame(2));
    }

    //list games
    @Test
    @Order(12)
    @DisplayName("listGames")
    public void listGames() {
        Assertions.assertDoesNotThrow(() -> database.listGames());
        Assertions.assertDoesNotThrow(() -> database.getGame(existingGameID));
    }

    @Test
    @Order(13)
    @DisplayName("List Games Even When Empty")
    public void badListGames() {
        database.clear();
        Assertions.assertThrows(Exception.class, () -> database.getGame(existingGameID));
        Assertions.assertDoesNotThrow(() -> database.listGames());
    }

    //create game
    @Test
    @Order(14)
    @DisplayName("Create Game")
    public void createGame() {
        Assertions.assertDoesNotThrow(() -> existingService.createGame(new CreateGameRequest(existingAuthToken, "newgame")));
    }

    @Test
    @Order(15)
    @DisplayName("Create Game While Logged Out")
    public void badCreateGame() {
        existingService.logout(new LogoutRequest(existingAuthToken));
        Assertions.assertThrows(Exception.class, () -> existingService.createGame(new CreateGameRequest(existingAuthToken, existingGameName)));
    }

    //delete game
    @Test
    @Order(16)
    @DisplayName("Delete Game")
    public void deleteGame() {
        Assertions.assertDoesNotThrow(() -> database.deleteGame(existingGameID));
    }

    @Test
    @Order(17)
    @DisplayName("Delete Nonexistent Game")
    public void badDeleteGame() {
        Assertions.assertThrows(Exception.class, () -> database.deleteGame(2));
    }

    //get user
    @Test
    @Order(18)
    @DisplayName("Get User")
    public void getUser() {
        Assertions.assertDoesNotThrow(() -> database.getUser(existingUsername));
    }

    @Test
    @Order(19)
    @DisplayName("Get Nonexistent User")
    public void badGetUser() {
        Assertions.assertThrows(Exception.class, () -> database.getUser("wrongUsername"));
    }

    //create user
    @Test
    @Order(20)
    @DisplayName("Create User")
    public void createUser() {
        UserData userData = new UserData("newUsername", "newPassword", "newEmail");
        Assertions.assertDoesNotThrow(() -> database.createUser(userData));
    }

    @Test
    @Order(21)
    @DisplayName("Create User Without Username")
    public void badCreateUser() {
        UserData userData = new UserData(null, "newPassword", "newEmail");
        Assertions.assertThrows(Exception.class, () -> database.createUser(userData));
    }

    //clear
    @Test
    @Order(22)
    @DisplayName("Clear Database")
    public void clear() {
        Assertions.assertDoesNotThrow(() -> database.clear());
        Assertions.assertThrows(Exception.class, () -> database.getUser(existingUsername));
        Assertions.assertThrows(Exception.class, () -> database.getAuth(existingAuthToken));
        Assertions.assertThrows(Exception.class, () -> database.getGame(existingGameID));
    }

}