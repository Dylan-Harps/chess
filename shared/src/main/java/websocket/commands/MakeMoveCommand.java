package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {
    ChessMove move;

    public MakeMoveCommand(String username, String authToken, Integer gameID, ChessMove move) {
    super(CommandType.MAKE_MOVE, username, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}