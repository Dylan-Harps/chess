package service;

import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {

    @Test
    @Order(1)
    @DisplayName("Register Normal")
    public void registerNormal() {
        ChessService chessService = new ChessService();
        RegisterRequest request = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result = chessService.register(request);

        Assertions.assertEquals(request.username(), result.username(), "register: username mutated");
        Assertions.assertNotNull(result.authToken(), "register: did not produce authToken");
    }

    @Test
    @Order(2)
    @DisplayName("Register Empty Username")
    public void registerEmptyUsername() {
        ChessService chessService = new ChessService();
        RegisterRequest request = new RegisterRequest("", "myPassword", "myEmail@email.com");

        Assertions.assertThrows(RuntimeException.class, () -> chessService.register(request), "register: does not throw bad request");
    }

    @Test
    @Order(3)
    @DisplayName("Register Null Username")
    public void registerNullUsername() {
        ChessService chessService = new ChessService();
        RegisterRequest request = new RegisterRequest(null, "myPassword", "myEmail@email.com");

        Assertions.assertThrows(RuntimeException.class, () -> chessService.register(request), "register: does not throw bad request");
    }

    @Test
    @Order(4)
    @DisplayName("Register Username Already Taken")
    public void registerUsernameAlreadyTaken() {
        ChessService chessService = new ChessService();
        RegisterRequest request1 = new RegisterRequest("myUsername", "myPassword", "myEmail@email.com");
        RegisterResult result1 = chessService.register(request1);

        RegisterRequest request2 = new RegisterRequest("myUsername", "myPassword2", "myEmail2@email.com");

        Assertions.assertThrows(RuntimeException.class, () -> chessService.register(request2), "register: does not throw already taken");
    }
}
