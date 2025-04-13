package server.websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
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
        UserGameCommand userGameCommand = new Gson().fromJson(message, UserGameCommand.class);
        switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(session, new Gson().fromJson(message, ConnectCommand.class));
            case MAKE_MOVE -> makeMove(session, new Gson().fromJson(message, MakeMoveCommand.class));
            case LEAVE -> leave(session, new Gson().fromJson(message, LeaveCommand.class));
            case RESIGN -> resign(session, new Gson().fromJson(message, ResignCommand.class));
        }
    }

    private void connect(Session session, ConnectCommand command) throws ResponseException, IOException {
        int gameID = command.getGameID();
        String participant = null;

        try {
            participant = verifyUser(command);

            //connect
            connections.add(gameID, participant, session);

            //notify loadGame
            verifyGameID(gameID);
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
            connections.send(session, new ErrorMessage(e.getMessage()));
        }
    }

    private void makeMove(Session session, MakeMoveCommand command) throws ResponseException, IOException {
        int gameID = command.getGameID();

        String participant = null;
        try {
            //verify user
            verifyGameID(command.getGameID());
            ChessGame game = database.getGame(gameID).game();
            String team = game.getTeamTurn() == ChessGame.TeamColor.WHITE ? "WHITE" : "BLACK";
            participant = verifyUser(command);
            verifyTeam(gameID, participant, team);

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
            connections.send(session, new ErrorMessage(e.getMessage()));
        }
    }

    private void leave(Session session, LeaveCommand command) throws ResponseException, IOException {
        int gameID = command.getGameID();

        try {
            String participant = verifyUser(command);

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
            connections.send(session, new ErrorMessage(e.getMessage()));
        }
    }

    private void resign(Session session, ResignCommand command) throws ResponseException, IOException {
        int gameID = command.getGameID();

        try {
            String participant = verifyUser(command);
            verifyTeam(gameID, participant, "PLAYER");

            //mark game as ended
            ChessGame game = database.getGame(gameID).game();
            if (!game.getGameOver()) {
                game.setGameOver();
                database.updateGame(gameID, game);
            } else {
                throw new InvalidMoveException("Error: Invalid Move");
            }

            //notify participants
            var message = String.format("%s resigned", participant);
            var notification = new NotificationMessage(message);
            connections.broadcast(gameID, null, notification);
        } catch (Exception e) {
            connections.send(session, new ErrorMessage(e.getMessage()));
        }
    }

    private String verifyUser(UserGameCommand command) throws ResponseException {
        String username;
        try {
            //verify authToken
            AuthData authData = database.getAuth(command.getAuthToken());
            username = authData.username();
            System.out.println("WebSocketHandler.verifyUser(): username = " + username);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
        return username;
    }

    public void verifyTeam(int gameID, String username, String assertTeam) throws InvalidMoveException, ResponseException {
        try {
            //verify that the participant is the correct player
            if (assertTeam != null) {
                System.out.println("WebSocketHandler.verifyUser(): getting gameData with gameID " + gameID);
                GameData gameData = database.getGame(gameID);
                boolean isWhite = username.equals(gameData.whiteUsername());
                boolean isBlack = username.equals(gameData.blackUsername());

                if (assertTeam.equals("WHITE") && !isWhite) {
                    System.out.println("WebSocketHandler.verifyUser(): whiteUser = " + gameData.whiteUsername());
                    throw new InvalidMoveException("Error: Invalid Move");
                }
                if (assertTeam.equals("BLACK") && !isBlack) {
                    System.out.println("WebSocketHandler.verifyUser(): blackUser = " + gameData.blackUsername());
                    throw new InvalidMoveException("Error: Invalid Move");
                }
                if (assertTeam.equals("PLAYER") && !(isWhite || isBlack)) {
                    throw new InvalidMoveException("Error: Invalid Move");
                }
            }
        } catch (InvalidMoveException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException(500, e.getMessage());
        }
    }

    public void verifyGameID(int gameID) throws ResponseException {
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
