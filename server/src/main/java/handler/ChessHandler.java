package handler;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.*;

@WebSocket
public class ChessHandler {
    private final UserService userService = new UserService();
    //private final GameService gameService = new GameService();

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        return userService.register(request);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        return userService.login(request);
    }

    public LogoutResult logout(LogoutRequest request) throws ResponseException {
        return userService.logout(request);
    }
}
