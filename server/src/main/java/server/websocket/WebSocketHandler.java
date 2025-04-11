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

        //connect
        connections.add(gameID, participant, session);

        //notify participants
        connections.send(gameID, participant, new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME));
        var message = String.format("%s joined the game as %s", participant, teamColor);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameID, participant, notification);
    }

    private void makeMove(MakeMoveCommand command) throws ResponseException {
        int gameID = command.getGameID();
        try {
            //make the move
            ChessGame game = database.getGame(gameID).game();
            game.makeMove(command.getMove());
            database.updateGame(gameID, game);

            //notify participants
            connections.broadcast(gameID, null, new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME));
            var message = String.format("%s made move %s", command.getUsername(), command.getMove().toString());
            var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameID, command.getUsername(), notification);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private void leave(LeaveCommand command) throws IOException {
        int gameID = command.getGameID();
        String participant = command.getUsername();

        //end connection
        connections.remove(gameID, participant);

        //notify participants
        var message = String.format("%s left the game", participant);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameID, participant, notification);
    }

    private void resign(ResignCommand command) throws ResponseException {
        int gameID = command.getGameID();
        try {
            //mark game as ended
            ChessGame game = database.getGame(gameID).game();
            game.setGameOver();
            database.updateGame(gameID, game);

            //notify participants
            var message = String.format("%s resigned", command.getUsername());
            var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameID, null, notification);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }
}
