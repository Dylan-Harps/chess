package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.SQLDataAccess;
import endpoints.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.ChessService;
import websocket.commands.*;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private SQLDataAccess database;

    public WebSocketHandler(SQLDataAccess database) {
        this.database = database;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
        switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(session, new Gson().fromJson(message, ConnectCommand.class));
            case MAKE_MOVE -> makeMove(new Gson().fromJson(message, MakeMoveCommand.class));
            case LEAVE -> leave(new Gson().fromJson(message, LeaveCommand.class));
            case RESIGN -> resign(new Gson().fromJson(message, ResignCommand.class));
        }
    }

    private void connect(Session session, ConnectCommand command) throws IOException {
        int gameID = command.getGameID();
        String participant = command.getUsername();
        String teamColor = command.getTeamColor() == null ? "an observer" : command.getTeamColor();

        connections.add(gameID, participant, session);
        connections.send(gameID, participant, new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        var message = String.format("%s joined the game as %s", participant, teamColor);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameID, participant, notification);
    }

    private void makeMove(MakeMoveCommand command) throws ResponseException {
        int gameID = command.getGameID();
        try {
            ChessGame game = database.getGame(gameID).game();
            game.makeMove(command.getMove());
            database.updateGame(gameID, game);
            connections.broadcast(gameID, null, new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private void leave(LeaveCommand command) throws IOException {
        int gameID = command.getGameID();
        String participant = command.getUsername();

        connections.remove(gameID, participant);
        var message = String.format("%s left the game", participant);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameID, participant, notification);
    }

    private void resign(ResignCommand command) throws IOException {
        //TODO
    }

    //pet shop examples
    /*
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
