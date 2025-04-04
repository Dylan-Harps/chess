package service;

import org.junit.jupiter.api.*;
import endpoints.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {
    public static ChessService chessService = new ChessService();

    @BeforeEach
    public void reset() {
        chessService.clear(new ClearRequest());
    }

    //register
    @Test
    @Order(1)
    @DisplayName("Register Normal")
    public void registerNormal() {
        RegisterRequest request = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result = chessService.register(request);

        Assertions.assertEquals(request.username(), result.username(), "register: username mutated");
        Assertions.assertNotNull(result.authToken(), "register: did not produce authToken");
    }

    @Test
    @Order(2)
    @DisplayName("Register Empty Username")
    public void registerEmptyUsername() {
        RegisterRequest request = new RegisterRequest("", "myPassword", "myEmail@email.com");

        Assertions.assertThrows(Exception.class, () -> chessService.register(request), "register: does not throw bad request");
    }

    @Test
    @Order(3)
    @DisplayName("Register Null Username")
    public void registerNullUsername() {
        RegisterRequest request = new RegisterRequest(null, "myPassword", "myEmail@email.com");

        Assertions.assertThrows(Exception.class, () -> chessService.register(request), "register: does not throw bad request");
    }

    @Test
    @Order(4)
    @DisplayName("Register Username Already Taken")
    public void registerUsernameAlreadyTaken() {
        RegisterRequest request1 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result1 = chessService.register(request1);

        RegisterRequest request2 = new RegisterRequest("myUsername", "myPassword2", "myEmail2@email.com");

        Assertions.assertThrows(Exception.class, () -> chessService.register(request2), "register: does not throw already taken");
    }

    //logout
    @Test
    @Order(5)
    @DisplayName("Logout Normal")
    public void logoutNormal() {
        //register
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);

        //logout
        LogoutRequest request1 = new LogoutRequest(result0.authToken());
        LogoutResult result1 = chessService.logout(request1);

        Assertions.assertThrows(Exception.class, ()->chessService.database.getAuth(result0.authToken()), "logout: doesn't delete authData");
    }

    @Test
    @Order(6)
    @DisplayName("Logout Twice")
    public void logoutTwice() {
        //register
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);

        //logout
        LogoutRequest request1 = new LogoutRequest(result0.authToken());
        LogoutResult result1 = chessService.logout(request1);

        Assertions.assertThrows(Exception.class, () -> chessService.logout(request1), "logout: logout doesn't throw already logged out");
    }

    //login
    @Test
    @Order(7)
    @DisplayName("Login Normal")
    public void loginNormal() {
        //register
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);

        //logout
        LogoutRequest request1 = new LogoutRequest(result0.authToken());
        LogoutResult result1 = chessService.logout(request1);

        //log back in
        LoginRequest request2 = new LoginRequest("myUsername", "myPassword");
        LoginResult result2 = chessService.login(request2);

        Assertions.assertNotEquals(result0.authToken(), result2.authToken(), "login: login uses same authData each time");
        Assertions.assertDoesNotThrow(() -> chessService.database.getAuth(result2.authToken()), "login: login does not add authToken");
    }

    @Test
    @Order(8)
    @DisplayName("Login Wrong Password")
    public void loginWrongPassword() {
        //register
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);

        //logout
        LogoutRequest request1 = new LogoutRequest(result0.authToken());
        LogoutResult result1 = chessService.logout(request1);

        //log back in
        LoginRequest request2 = new LoginRequest("myUsername", "wrongPassword");

        Assertions.assertThrows(Exception.class, () -> chessService.login(request2), "login: login does not throw wrong password");
    }

    //create games
    @Test
    @Order(9)
    @DisplayName("Create Game")
    public void createGame() {
        //register user
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);
        String authToken = result0.authToken();

        //create games
        CreateGameRequest request1 = new CreateGameRequest(authToken, "gameName");
        chessService.createGame(request1);

        Assertions.assertDoesNotThrow(() -> chessService.database.getGame(1), "createGame: did not create game");
    }

    @Test
    @Order(9)
    @DisplayName("Create Game Logged Out")
    public void createGameLoggedOut() {
        //register user
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);
        String authToken = result0.authToken();

        //logout
        LogoutRequest request1 = new LogoutRequest(result0.authToken());
        LogoutResult result1 = chessService.logout(request1);

        //create games
        CreateGameRequest request2 = new CreateGameRequest(authToken, "gameName");

        Assertions.assertThrows(Exception.class, () -> chessService.createGame(request2), "createGame: created game while logged out");
    }

    //list games
    @Test
    @Order(10)
    @DisplayName("Add and List Games")
    public void listGames() {
        //register user
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);
        String authToken = result0.authToken();

        //create games
        for (int i = 1; i <= 3; ++i) {
            CreateGameRequest request1 = new CreateGameRequest(authToken, "game #" + i);
            chessService.createGame(request1);
        }

        //list the games
        ListGamesRequest request2 = new ListGamesRequest(authToken);
        ListGamesResult result2 = chessService.listGames(request2);

        Assertions.assertNotNull(result2, "listGames: did not list games");
    }

    @Test
    @Order(11)
    @DisplayName("List Games After Clear")
    public void listGamesAfterClear() {
        //register user
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);
        String authToken = result0.authToken();

        //create games
        for (int i = 1; i <= 3; ++i) {
            CreateGameRequest request1 = new CreateGameRequest(authToken, "game #" + i);
            chessService.createGame(request1);
        }

        //clear
        ClearRequest request2 = new ClearRequest();
        ClearResult result2 = chessService.clear(request2);

        //register user again
        RegisterRequest request3 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result3 = chessService.register(request0);
        String authToken3 = result3.authToken();

        //list the games
        ListGamesRequest request4 = new ListGamesRequest(authToken3);
        ListGamesResult result4 = chessService.listGames(request4);

        Assertions.assertNotNull(result4, "listGames: did not list 0 games");
    }

    //join games

    //clear
    @Test
    @Order(12)
    @DisplayName("Clear Normal")
    public void clearNormal() {
        //register user
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);
        String authToken = result0.authToken();

        //create games
        for (int i = 1; i <= 3; ++i) {
            CreateGameRequest request1 = new CreateGameRequest(authToken, "game #" + i);
            chessService.createGame(request1);
        }

        //clear
        ClearRequest request2 = new ClearRequest();
        ClearResult result2 = chessService.clear(request2);

        Assertions.assertThrows(Exception.class, () -> chessService.database.getGame(1), "clear: games still exist after clear");
    }

    @Test
    @Order(13)
    @DisplayName("Multiple Clears")
    public void multipleClears() {
        //register user
        RegisterRequest request0 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result0 = chessService.register(request0);
        String authToken = result0.authToken();

        //create games
        for (int i = 1; i <= 3; ++i) {
            CreateGameRequest request1 = new CreateGameRequest(authToken, "game #" + i);
            chessService.createGame(request1);
        }

        //clear
        ClearRequest request2 = new ClearRequest();
        ClearResult result2 = chessService.clear(request2);
        result2 = chessService.clear(request2);
        result2 = chessService.clear(request2);
        result2 = chessService.clear(request2);

        Assertions.assertThrows(Exception.class, () -> chessService.database.getGame(1), "clear: games still exist after clear");
    }
}
