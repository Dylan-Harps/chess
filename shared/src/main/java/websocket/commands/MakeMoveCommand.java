package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
    ChessMove move;
    public MakeMoveCommand(CommandType commandType, String username, String authToken, Integer gameID, ChessMove move) {
        super(commandType, username, authToken, gameID);
        this.move = move;
    }
}