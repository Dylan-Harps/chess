package handler;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.*;

@WebSocket
public class ChessHandler {
    private final ChessService chessService = new ChessService();

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        return chessService.register(request);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        return chessService.login(request);
    }

    public LogoutResult logout(LogoutRequest request) throws ResponseException {
        return chessService.logout(request);
    }

    public ListGamesResult listGames(ListGamesRequest request) throws ResponseException {
        return chessService.listGames(request);
    }

    public ClearResult clear(ClearRequest request) {
        return chessService.clear(request);
    }
}
