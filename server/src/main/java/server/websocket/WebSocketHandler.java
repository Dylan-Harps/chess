package server.websocket;

import com.google.gson.Gson;
import endpoints.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
        switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(session, new Gson().fromJson(message, ConnectCommand.class));
            case MAKE_MOVE -> makeMove(session, new Gson().fromJson(message, MakeMoveCommand.class));
            case LEAVE -> leave(session, new Gson().fromJson(message, LeaveCommand.class));
            case RESIGN -> resign(session, new Gson().fromJson(message, ResignCommand.class));
        }
    }

    private void connect(Session session, ConnectCommand command) throws IOException {
        int gameID = command.getGameID();
        String participant = command.getUsername();
        String teamColor = command.getTeamColor() == null ? "an observer" : command.getTeamColor();

        connections.add(gameID, participant, session);
        var message = String.format("%s joined the game as %s", participant, teamColor);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, message);
        connections.broadcast(gameID, participant, notification);
    }

    private void makeMove(Session session, MakeMoveCommand command) throws IOException {
        //TODO
    }

    private void leave(Session session, LeaveCommand command) throws IOException {
        //TODO
    }

    private void resign(Session session, ResignCommand command) throws IOException {
        //TODO
    }

    //pet shop examples
    /*
    private void enter(String visitorName, Session session) throws IOException {
        connections.add(visitorName, session);
        var message = String.format("%s is in the shop", visitorName);
        var notification = new ServerMessage(ServerMessage.Type.ARRIVAL, message);
        connections.broadcast(visitorName, notification);
    }

    private void exit(String visitorName) throws IOException {
        connections.remove(visitorName);
        var message = String.format("%s left the shop", visitorName);
        var notification = new ServerMessage(ServerMessage.Type.DEPARTURE, message);
        connections.broadcast(visitorName, notification);
    }

    public void makeNoise(String petName, String sound) throws ResponseException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new ServerMessage(ServerMessage.Type.NOISE, message);
            connections.broadcast("", notification);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
    */
}
