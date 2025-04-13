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
import websocket.messages.*;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final SQLDataAccess database = new SQLDataAccess();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws ResponseException, IOException {
        System.out.println("WebSocketHandler.onMessage(): receiving command");
        UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
        switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(session, new Gson().fromJson(message, ConnectCommand.class));
            case MAKE_MOVE -> makeMove(new Gson().fromJson(message, MakeMoveCommand.class));
            case LEAVE -> leave(new Gson().fromJson(message, LeaveCommand.class));
            case RESIGN -> resign(new Gson().fromJson(message, ResignCommand.class));
        }
    }

    private void connect(Session session, ConnectCommand command) throws ResponseException, IOException {
        System.out.println("WebSocketHandler.connect(): connecting");
        int gameID = command.getGameID();

        String participant = null;
        try {
            participant = verifyUser(command, null);

            //connect
            connections.add(gameID, participant, session);

            //notify loadGame
            verifyGameID(gameID, participant);
            ChessGame game = database.getGame(gameID).game();
            connections.send(gameID, participant, new LoadGameMessage(game));

            //notify others of joining the game
            String teamColor = getTeamColor(participant, gameID);
            if (teamColor == null) {
                teamColor = "an observer";
            }
            var message = String.format("%s joined the game as %s", participant, teamColor);
            var notification = new NotificationMessage(message);
            connections.broadcast(gameID, participant, notification);
        } catch (Exception e) {
            System.out.println("WebSocketHandler.connect(): " + e.getMessage());
            connections.send(gameID, participant, new ErrorMessage(e.getMessage()));
        }
    }

    private void makeMove(MakeMoveCommand command) throws ResponseException {
        int gameID = command.getGameID();

        try {
            //verify user
            ChessGame game = database.getGame(gameID).game();
            String team = game.getTeamTurn() == ChessGame.TeamColor.WHITE ? "WHITE" : "BLACK";
            String participant = verifyUser(command, team);

            //make the move
            game.makeMove(command.getMove());
            database.updateGame(gameID, game);

            //notify participants:
            // load game
            connections.broadcast(gameID, null, new LoadGameMessage(game));

            // notify of move
            var message = String.format("%s made move %s", participant, command.getMove().toString());
            var notification = new NotificationMessage(message);
            connections.broadcast(gameID, participant, notification);

            // notify of check, checkmate, or stalemate
            ChessGame.TeamColor opponent = team.equals("WHITE") ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            NotificationMessage checkNotification = null;
            if (game.isInCheck(opponent)) {
                checkNotification = new NotificationMessage("Check!");
            }
            if (game.isInCheckmate(opponent)) {
                checkNotification = new NotificationMessage("Checkmate!");
            }
            if (game.isInStalemate(opponent)) {
                checkNotification = new NotificationMessage("Stalemate!");
            }
            if (checkNotification != null) {
                connections.broadcast(gameID, null, notification);
            }
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private void leave(LeaveCommand command) throws ResponseException {
        int gameID = command.getGameID();

        try {
            String participant = verifyUser(command, null);

            //remove player from the game
            GameData gameData = database.getGame(gameID);
            String newWhite = participant.equals(gameData.whiteUsername()) ? null : gameData.whiteUsername();
            String newBlack = participant.equals(gameData.blackUsername()) ? null : gameData.blackUsername();
            GameData newGameData = new GameData(gameID, newWhite, newBlack, gameData.gameName(), gameData.game());
            database.updateGame(gameID, newGameData);

            //end connection
            connections.remove(gameID, participant);

            //notify participants
            var message = String.format("%s left the game", participant);
            var notification = new NotificationMessage(message);
            connections.broadcast(gameID, participant, notification);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private void resign(ResignCommand command) throws ResponseException {
        int gameID = command.getGameID();

        try {
            String participant = verifyUser(command, "PLAYER");

            //mark game as ended
            ChessGame game = database.getGame(gameID).game();
            game.setGameOver();
            database.updateGame(gameID, game);

            //notify participants
            var message = String.format("%s resigned", participant);
            var notification = new NotificationMessage(message);
            connections.broadcast(gameID, null, notification);
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    private String verifyUser(UserGameCommand command, String assertTeam) throws ResponseException, IOException {
        System.out.println("WebSocketHandler.verifyUser(): verifying user");
        String username = null;
        try {
            //verify authToken
            AuthData authData = database.getAuth(command.getAuthToken());
            username = authData.username();
            System.out.println("WebSocketHandler.verifyUser(): username = " + username);

            //verify that the participant is the correct player
            if (assertTeam != null) {
                System.out.println("WebSocketHandler.verifyUser(): getting gameData with gameID " + command.getGameID());
                GameData gameData = database.getGame(command.getGameID());
                boolean isWhite = username.equals(gameData.whiteUsername());
                boolean isBlack = username.equals(gameData.blackUsername());
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
        return username;
    }

    public void verifyGameID(int gameID, String username) throws ResponseException, IOException {
        System.out.println("WebSocketHandler.verifyGameID(): verifying gameID");
        try {
            database.getGame(gameID);
        } catch (Exception e) {
            throw new ResponseException(400, "Error: unauthorized");
        }
    }

    private String getTeamColor(String username, int gameID) {
        try {
            GameData game = database.getGame(gameID);
            if (username.equals(game.whiteUsername())) {
                return "WHITE";
            }
            if (username.equals(game.blackUsername())) {
                return "BLACK";
            }
            return null;
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }
}
