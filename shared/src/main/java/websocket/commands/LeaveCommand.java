package websocket.commands;

public class LeaveCommand extends UserGameCommand {
    public LeaveCommand(CommandType commandType, String username, String authToken, Integer gameID) {
        super(commandType, username, authToken, gameID);
    }
}
