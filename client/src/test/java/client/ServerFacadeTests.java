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

}
