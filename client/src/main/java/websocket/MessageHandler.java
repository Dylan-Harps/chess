package websocket;

import websocket.messages.ServerMessage;

public interface MessageHandler {
    void notify(ServerMessage message);
}
