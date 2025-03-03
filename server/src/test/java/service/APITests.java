package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;
import passoff.model.*;
import passoff.server.TestServerFacade;
import server.Server;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import service.UserService;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class APITests {

    private static TestUser existingUser;

    private static TestUser newUser;

    private static TestCreateRequest createRequest;

    private static TestServerFacade serverFacade;
    private static Server server;

    private String existingAuth;

    @Test
    @Order(1)
    @DisplayName("Register Normal")
    public void registerNormal() {
        UserService userService = new UserService();
        RegisterRequest request = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result = userService.register(request);

        Assertions.assertEquals(request.username(), result.username(), "register: username mutated");
        Assertions.assertNotNull(result.authToken(), "register: did not produce authToken");
    }

    @Test
    @Order(2)
    @DisplayName("Register Empty Username")
    public void registerEmptyUsername() {
        UserService userService = new UserService();
        RegisterRequest request = new RegisterRequest("", "myPassword", "myEmail@email.com");

        Assertions.assertThrows(RuntimeException.class, () -> userService.register(request), "register: does not throw error");
    }

    @Test
    @Order(3)
    @DisplayName("Register Null Username")
    public void registerNullUsername() {
        UserService userService = new UserService();
        RegisterRequest request = new RegisterRequest(null, "myPassword", "myEmail@email.com");

        Assertions.assertThrows(RuntimeException.class, () -> userService.register(request), "register: does not throw error");
    }
}
