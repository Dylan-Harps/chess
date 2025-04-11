package websocket.commands;

public class ResignCommand extends UserGameCommand {
    public ResignCommand(CommandType commandType, String username, String authToken, Integer gameID) {
        super(commandType, username, authToken, gameID);
    }
}
