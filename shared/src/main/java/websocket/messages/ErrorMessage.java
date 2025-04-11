package websocket.messages;

public class ErrorMessage extends ServerMessage {
    String message;

    public ErrorMessage(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
