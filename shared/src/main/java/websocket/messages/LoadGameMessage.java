package websocket.messages;

public class LoadGameMessage extends ServerMessage {
    public LoadGameMessage(ServerMessageType type) {
        super(type, "");
    }

    public LoadGameMessage(ServerMessageType type, String message) {
        super(type, message);
    }
}
