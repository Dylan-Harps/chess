package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.SQLDataAccess;
import endpoints.ResponseException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final SQLDataAccess database;

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

    private void connect(Session session, ConnectCommand command) throws ResponseException {
        int gameID = command.getGameID();
        String participant = command.getUsername();
        String teamColor = command.getTeamColor() == null ? "an observer" : command.getTeamColor();

        try {
            verifyUser(command, null);

            //connect
            connections.add(gameID, participant, session);

            //notify participants
            ChessGame game = database.getGame(gameID).game();
            connections.send(gameID, participant, new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game));
            var message = String.format("%s joined the game as %s", participant, teamColor);
            var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameID, participant, notification);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private void makeMove(MakeMoveCommand command) throws ResponseException {
        int gameID = command.getGameID();

        try {
            //verify user
            ChessGame game = database.getGame(gameID).game();
            String team = game.getTeamTurn() == ChessGame.TeamColor.WHITE ? "WHITE" : "BLACK";
            verifyUser(command, team);

            //make the move
            game.makeMove(command.getMove());
            database.updateGame(gameID, game);

            //notify participants
            connections.broadcast(gameID, null, new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game));
            var message = String.format("%s made move %s", command.getUsername(), command.getMove().toString());
            var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameID, command.getUsername(), notification);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private void leave(LeaveCommand command) throws ResponseException {
        int gameID = command.getGameID();
        String participant = command.getUsername();

        try {
            verifyUser(command, null);

            //remove player from the game
            GameData gameData = database.getGame(gameID);
            String newWhite = command.getUsername().equals(gameData.whiteUsername()) ? null : gameData.whiteUsername();
            String newBlack = command.getUsername().equals(gameData.blackUsername()) ? null : gameData.blackUsername();
            GameData newGameData = new GameData(gameID, newWhite, newBlack, gameData.gameName(), gameData.game());
            database.updateGame(gameID, newGameData);

            //end connection
            connections.remove(gameID, participant);

            //notify participants
            var message = String.format("%s left the game", participant);
            var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameID, participant, notification);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private void resign(ResignCommand command) throws ResponseException {
        int gameID = command.getGameID();

        try {
            verifyUser(command, "PLAYER");

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

    private void verifyUser(UserGameCommand command, String assertTeam) throws ResponseException {
        try {
            AuthData authData = database.getAuth(command.getAuthToken()); //verify authToken
            //verify that the authToken and username match
            if (!authData.username().equals(command.getUsername())) {
                throw new ResponseException(401, "Error: unauthorized");
            }
            //verify that the participant is the correct player
            if (assertTeam != null) {
                GameData gameData = database.getGame(command.getGameID());
                boolean isWhite = command.getUsername().equals(gameData.whiteUsername());
                boolean isBlack = command.getUsername().equals(gameData.blackUsername());
                if (assertTeam.equals("WHITE") && !isWhite) {
                    throw new ResponseException(401, "Error: unauthorized");
                }
                if (assertTeam.equals("BLACK") && !isBlack) {
                    throw new ResponseException(401, "Error: unauthorized");
                }
                if (assertTeam.equals("PLAYER") && !(isWhite || isBlack)) {
                    throw new ResponseException(401, "Error: unauthorized");
                }
            }
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }
}
