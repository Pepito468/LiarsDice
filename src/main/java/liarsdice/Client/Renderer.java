package liarsdice.Client;

import liarsdice.Messages.Message;
import liarsdice.Server.Model.GameState.DiceState;
import liarsdice.Server.Model.GameState.GameState;
import org.fusesource.jansi.AnsiConsole;

import java.util.ArrayList;
import java.util.List;

public class Renderer {

    private final Integer X_MAX = 150;
    private final Integer Y_MAX = 40;
    private final Integer MESSAGE_LOG_SIZE = 30;

    private List<String> messageHistory = new ArrayList<>();

    public Renderer() {
        // Setup jansi
        AnsiConsole.systemInstall();
    }

    private void moveCursor(int x, int y) {
        AnsiConsole.out().print("\033["+y+";"+x+"H");
    }

    private void saveCursorPosition() {
        AnsiConsole.out().print("\033[s");
    }

    private void restoreCursorPosition() {
        AnsiConsole.out().print("\033[u");
    }

    private void printString(int x, int y, String string) {
        moveCursor(x, y);
        AnsiConsole.out().print(string);
    }

    private void clean() {
        for (int i = 0; i < Y_MAX; i++)
            printString(0, i, " ".repeat(X_MAX));
    }

    private void drawMessageLog() {
        printString(MESSAGE_LOG_SIZE / 2 - "Message Log".length() / 2, 0, "Message Log");
        // Split logs so that they stay in bounds
        List<String> logs = messageHistory.reversed().stream()
                .flatMap(line -> {
                    List<String> subLine = new ArrayList<>();
                    for (int i = 0; i < line.length(); i+=MESSAGE_LOG_SIZE) {
                        subLine.add(line.substring(i, Math.min(i+MESSAGE_LOG_SIZE, line.length())));
                    }
                    return subLine.stream();})
                .toList();
        // Print
        for (int i = 0; i < logs.size() && i < Y_MAX; i++) {
            printString(0, i + 2, logs.get(i));
        }

    }

    private void drawGame(GameState state) {
        if (state == null)
            return;

        // Bet
        if (state.currentBet != null)
            printString(X_MAX / 2, 1, "Bet: "+state.currentBet);
        else
            printString(X_MAX / 2, 1, "Bet: NO BET");


        for (int i = 0; i < state.players.size(); i++) {

            String dices = "";
            for (DiceState dice: state.players.get(i).dices) {
                dices += dice.value != 0 ? dice.value : "X";
                dices += " ";
            }

            printString(X_MAX / 2, 5 + i, state.players.get(i).nickname +": "+ dices);
            if (i == state.currentPlayerIndex)
                AnsiConsole.out().print(" <--- CURRENT");

        }

        state.players.stream()
                .filter(player -> player.isALiar)
                .findFirst()
                .ifPresent(liar -> printString(X_MAX / 2, 3, liar.nickname + " LOST THE GAME"));
    }

    public void prepareInput() {
        printString(0, Y_MAX, " ".repeat(X_MAX));
    }

    public void moveCursorToInput() {
        moveCursor(X_MAX / 2, Y_MAX);
    }

    public void drawState(Message message) {
        synchronized (Client.graphicsLock) {
            saveCursorPosition();
            if (message.message != null)
                messageHistory.add(message.message);

            clean();
            drawMessageLog();
            drawGame(message.state);

            restoreCursorPosition();
        }
    }
}
