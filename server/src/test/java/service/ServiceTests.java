package service;

import org.junit.jupiter.api.*;
import passoff.model.*;
import passoff.server.TestServerFacade;
import server.Server;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {

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

        Assertions.assertThrows(RuntimeException.class, () -> userService.register(request), "register: does not throw bad request");
    }

    @Test
    @Order(3)
    @DisplayName("Register Null Username")
    public void registerNullUsername() {
        UserService userService = new UserService();
        RegisterRequest request = new RegisterRequest(null, "myPassword", "myEmail@email.com");

        Assertions.assertThrows(RuntimeException.class, () -> userService.register(request), "register: does not throw bad request");
    }

    @Test
    @Order(4)
    @DisplayName("Register Username Already Taken")
    public void registerUsernameAlreadyTaken() {
        UserService userService = new UserService();
        RegisterRequest request1 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result1 = userService.register(request1);

        RegisterRequest request2 = new RegisterRequest("myUsername", "myPassword2", "myEmail2@email.com");

        Assertions.assertThrows(RuntimeException.class, () -> userService.register(request2), "register: does not throw already taken");
    }
}
