import chess.*;
import endpoints.ResponseException;
import ui.ServerFacade;

import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        String port = "8080"; //3306
        if (args.length == 1) {
            port = args[0];
        }
        new Repl(port).run();
    }


}