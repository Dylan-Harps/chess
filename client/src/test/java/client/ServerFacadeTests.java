package client;

import org.junit.jupiter.api.*;
import server.Server;
import service.*;
import ui.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static String username = "username";
    private static String password = "password";
    private static String email = "email";
    private static String gameName = "gameName";

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:8080");
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
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerTest() {
        RegisterResult result = facade.registerUser(new RegisterRequest(username, password, email));
        Assertions.assertFalse(result.username().isEmpty());
        Assertions.assertFalse(result.authToken().isEmpty());
    }

    @Test
    public void loginTest() {
        facade.registerUser(new RegisterRequest(username, password, email));
        LoginResult result = facade.loginUser(new LoginRequest(username, password));
        Assertions.assertNotNull(result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void logoutTest() {
        String authToken = facade.registerUser(new RegisterRequest(username, password, email)).authToken();
        LogoutResult result = facade.logoutUser(new LogoutRequest(authToken));
        Assertions.assertThrows(Exception.class, () -> facade.logoutUser(new LogoutRequest(authToken)));
    }

    @Test
    public void listGamesTest() {
        String authToken = facade.registerUser(new RegisterRequest(username, password, email)).authToken();
        facade.createGame(new CreateGameRequest(authToken, gameName));
        ListGamesResult result = facade.listGames(new ListGamesRequest(authToken));
        Assertions.assertNotNull(result.games());
    }

    @Test
    public void createGameTest() {
        String authToken = facade.registerUser(new RegisterRequest(username, password, email)).authToken();
        CreateGameResult result = facade.createGame(new CreateGameRequest(authToken, gameName));
        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    public void joinGameTest() {
        String authToken = facade.registerUser(new RegisterRequest(username, password, email)).authToken();
        int gameID = facade.createGame(new CreateGameRequest(authToken, gameName)).gameID();
        Assertions.assertDoesNotThrow(()-> facade.joinGame(new JoinGameRequest(authToken, "WHITE", gameID)));
    }

    @Test
    public void clearTest() {
        Assertions.assertDoesNotThrow(()-> facade.clear(new ClearRequest()));
    }
}
