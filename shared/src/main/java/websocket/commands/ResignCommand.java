package websocket.commands;

public class ResignCommand extends UserGameCommand {
    public ResignCommand(String username, String authToken, Integer gameID) {
        super(CommandType.RESIGN, username, authToken, gameID);
    }
}
