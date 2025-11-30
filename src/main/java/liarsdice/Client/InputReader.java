package liarsdice.Client;

import liarsdice.Actions.CallLiarAction;
import liarsdice.Actions.RaiseBetAction;
import liarsdice.Server.Model.Bet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputReader implements Runnable {

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            while (!Thread.currentThread().isInterrupted()) {
                Client.renderer.prepareInput();
                Client.renderer.moveCursorToInput();

                String command = in.readLine();

                if (command == null) {
                    continue;
                } else if (command.equals("LIAR")) {
                    Client.connection.sendAction(new CallLiarAction());
                } else if (command.matches("^\\d+\\s+\\d$")) {
                    Client.connection.sendAction(new RaiseBetAction(
                            new Bet(Integer.parseInt(command.split("\\s+")[0]),
                                    Integer.parseInt(command.split("\\s+")[1])
                            )));
                } else if (command.equals("EXIT")) {
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
