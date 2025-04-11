package websocket.commands;

public class LeaveCommand extends UserGameCommand {
    public LeaveCommand(String username, String authToken, Integer gameID) {
        super(CommandType.LEAVE, username, authToken, gameID);
    }
}
