import websocket.MessageHandler;
import websocket.messages.ServerMessage;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl implements MessageHandler {
    private final ChessClient client;

    public Repl(String port) {
        client = new ChessClient(port, this);
    }

    public void run() {
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(SET_TEXT_COLOR_RED + msg + RESET_TEXT_COLOR);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_BLINKING);
    }

    @Override
    public void notify(ServerMessage message) {
        //TODO
    }
}
