package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    //Map of gameIDs to a set of connections associated with each participant
    public final ConcurrentHashMap<Integer, HashSet<Connection>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, String participant, Session session) {
        var connection = new Connection(participant, session);
        //if the game is already connected, add the participant to the game
        if (connections.containsKey(gameID)) {
            connections.get(gameID).add(connection);
        }
        //otherwise, make a new connection to the game
        else {
            connections.put(gameID, new HashSet<>());
            connections.get(gameID).add(connection);
        }
    }

    public void remove(int gameID, String participant) {
        var gameConnections = connections.get(gameID);
        gameConnections.removeIf(c -> c.participant.equals(participant));
        if (gameConnections.isEmpty()) {
            connections.remove(gameID);
        }
    }

    public void broadcast(int gameID, String excludeParticipant, ServerMessage notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        var gameConnections = connections.get(gameID);
        for (var c : gameConnections) {
            if (c.session.isOpen()) {
                if (!c.participant.equals(excludeParticipant)) {
                    c.send(new Gson().toJson(notification));
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            remove(gameID, c.participant);
        }
    }

    public void send(int gameID, String recipient, ServerMessage notification) throws IOException {
        //System.out.println("ConnectionManager.send(): entered send()");
        var removeList = new ArrayList<Connection>();
        var gameConnections = connections.get(gameID);
        for (var c : gameConnections) {
            if (c.session.isOpen()) {
                if (c.participant.equals(recipient)) {
                    c.send(new Gson().toJson(notification));
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            remove(gameID, c.participant);
        }
    }

    public void send(Session session, ServerMessage notification) throws IOException {
        //System.out.println("ConnectionManager.send()2: " + new Gson().toJson(notification));
        session.getRemote().sendString(new Gson().toJson(notification));
    }
}
