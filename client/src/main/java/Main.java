import chess.*;
import ui.ServerFacade;

public class Main {
    ServerFacade serverFacade = new ServerFacade();

    public Main() throws Exception {
    }

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
    }

    public void preLoginHelp() {
        //TODO
    }

    public void quit() {
        //TODO
    }

    public void login() {
        //TODO
    }

    public void register() {
        //TODO
    }

    public void postLoginHelp() {
        //TODO
    }

    public void logout() {
        //TODO
    }

    public void createGame() {
        //TODO
    }

    public void listGames() {
        //TODO
    }

    public void playGame() {
        //TODO
    }

    public void observeGame() {
        //TODO
    }

    private void displayGame(ChessGame.TeamColor perspective) {
        //TODO
    }
}