package handler;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;

@WebSocket
public class ChessHandler {
    public RegisterResult register(RegisterRequest request) throws ResponseException {
        UserService userService = new UserService();
        RegisterResult result;
        try {
            result = userService.register(request);
        }
        catch (RuntimeException e) {
            if (e.getMessage().equals("bad request")) {
                throw new ResponseException(400, e.getMessage());
            } else if (e.getMessage().equals("already taken")) {
                throw new ResponseException(403, e.getMessage());
            } else {
                throw new ResponseException(500, e.getMessage());
            }
        }
        return result;
    }
}
