package service;

public record JoinGameRequest(String authToken, String color, int gameID) {
}
