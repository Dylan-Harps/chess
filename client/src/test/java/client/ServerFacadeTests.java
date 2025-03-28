package client;

import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;
import endpoints.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static final String existingUsername = "username";
    private static final String existingPassword = "password";
    private static final String existingEmail = "email";
    private static final String existingGameName = "gameName";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    void clearDatabase() {
        facade.clear(new ClearRequest());
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerTest() {
        RegisterResult result = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail));
        Assertions.assertFalse(result.username().isEmpty());
        Assertions.assertFalse(result.authToken().isEmpty());
    }

    @Test
    public void badRegisterTest() {
        //register the same person twice
        RegisterResult result = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail));
        Assertions.assertThrows(Exception.class, () -> facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail)));
    }

    @Test
    public void loginTest() {
        facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail));
        LoginResult result = facade.loginUser(new LoginRequest(existingUsername, existingPassword));
        Assertions.assertNotNull(result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void badLoginTest() {
        facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail));
        Assertions.assertThrows(Exception.class, () -> facade.loginUser(new LoginRequest("WrongUsername", existingPassword)));
    }

    @Test
    public void logoutTest() {
        String authToken = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail)).authToken();
        LogoutResult result = facade.logoutUser(new LogoutRequest(authToken));
        Assertions.assertThrows(Exception.class, () -> facade.logoutUser(new LogoutRequest(authToken)));
    }

    @Test
    public void badLogoutTest() {
        String authToken = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail)).authToken();
        Assertions.assertThrows(Exception.class, () -> facade.logoutUser(new LogoutRequest("WrongAuthToken")));
    }

    @Test
    public void listGamesTest() {
        String authToken = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail)).authToken();
        facade.createGame(new CreateGameRequest(authToken, existingGameName));
        ListGamesResult result = facade.listGames(new ListGamesRequest(authToken));
        Assertions.assertNotNull(result.games());
    }

    @Test
    public void badListGamesTest() {
        String authToken = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail)).authToken();
        facade.createGame(new CreateGameRequest(authToken, existingGameName));
        Assertions.assertThrows(Exception.class, () -> facade.listGames(new ListGamesRequest("WrongAuthToken")));
    }

    @Test
    public void createGameTest() {
        String authToken = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail)).authToken();
        CreateGameResult result = facade.createGame(new CreateGameRequest(authToken, existingGameName));
        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    public void badCreateGameTest() {
        String authToken = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail)).authToken();
        Assertions.assertThrows(Exception.class, () -> facade.createGame(new CreateGameRequest("WrongAuthToken", existingGameName)));
    }

    @Test
    public void joinGameTest() {
        String authToken = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail)).authToken();
        int gameID = facade.createGame(new CreateGameRequest(authToken, existingGameName)).gameID();
        Assertions.assertDoesNotThrow(()-> facade.joinGame(new JoinGameRequest(authToken, "WHITE", gameID)));
    }

    @Test
    public void badJoinGameTest() {
        String authToken = facade.registerUser(new RegisterRequest(existingUsername, existingPassword, existingEmail)).authToken();
        int gameID = facade.createGame(new CreateGameRequest(authToken, existingGameName)).gameID();
        Assertions.assertThrows(Exception.class, ()-> facade.joinGame(new JoinGameRequest(authToken, "WHITE", gameID + 10)));
    }

    @Test
    public void clearTest() {
        Assertions.assertDoesNotThrow(()-> facade.clear(new ClearRequest()));
    }

    @Test
    public void doubleClearTest() {
        Assertions.assertDoesNotThrow(()-> facade.clear(new ClearRequest()));
        Assertions.assertDoesNotThrow(()-> facade.clear(new ClearRequest()));
    }
}
