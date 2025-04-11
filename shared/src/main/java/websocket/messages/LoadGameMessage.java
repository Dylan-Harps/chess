package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage {
    ChessGame game;
    String message;

    public LoadGameMessage(ChessGame game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
        this.message = "";
    }

    public LoadGameMessage(ChessGame game, String message) {
        super(ServerMessageType.LOAD_GAME);
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
