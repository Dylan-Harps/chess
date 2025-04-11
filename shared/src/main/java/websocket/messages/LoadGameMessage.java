package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {
    ChessGame game;
    String message;

    public LoadGameMessage(ServerMessageType type, ChessGame game) {
        super(type);
        this.game = game;
        this.message = "";
    }

    public LoadGameMessage(ServerMessageType type, ChessGame game, String message) {
        super(type);
        this.game = game;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public ChessGame getGame() {
        return game;
    }
}
