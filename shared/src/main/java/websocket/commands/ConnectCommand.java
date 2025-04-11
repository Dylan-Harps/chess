package websocket.commands;

public class ConnectCommand extends UserGameCommand {
    String teamColor;

    public ConnectCommand(String username, String authToken, Integer gameID, String teamColor) {
        super(CommandType.CONNECT, username, authToken, gameID);
        this.teamColor = teamColor;
    }

    public String getTeamColor() {
        return teamColor;
    }
}
