package endpoints;

public record CreateGameRequest(String authToken, String gameName) {
}
