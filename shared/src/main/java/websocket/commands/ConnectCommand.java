package websocket.commands;

public class ConnectCommand extends UserGameCommand {
    String teamColor;

    public ConnectCommand(CommandType commandType, String username, String authToken, Integer gameID, String teamColor) {
        super(commandType, username, authToken, gameID);
        this.teamColor = teamColor;
    }

    public String getTeamColor() {
        return teamColor;
    }
}
