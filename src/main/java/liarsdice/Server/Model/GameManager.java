package liarsdice.Server.Model;

import liarsdice.Server.Controller.Listener;
import liarsdice.Server.Model.GameState.GameState;

import java.util.*;

public class GameManager {

    /** The listener of this game */
    private final Listener listener;
    /** The Players in this game */
    private final List<Player> players;
    /** The current Player: the one who can raise the Bet */
    private Integer currentPlayerIndex;
    /** The current Bet */
    private Bet currentBet;
    /** Boolean to tell if the game is over */
    private Boolean isGameOver;

    public GameManager(Listener listener, List<String> playerNames, int dicesPerPlayer, int maxDiceValue) {
        this.listener = listener;
        this.players = new ArrayList<>(playerNames.size());
        playerNames.forEach(playerName -> {
                    // Create the Lists of Dices and give them to every Player
                    List<Dice> dices = new ArrayList<>(dicesPerPlayer);
                    for (int j = 0; j < dicesPerPlayer; j++) {
                        dices.add(new Dice(maxDiceValue));
                    }
                    // Create new Player with name and Dices
                    players.add(new Player(playerName, dices));
        });
        // First Bet is null
        this.currentBet = null;
        this.currentPlayerIndex = null;
        this.isGameOver = false;
    }

    /** Crates a state of the game */
    public GameState compileState() {
        return new GameState(players.stream().map(Player::compileState).toList(),
                currentPlayerIndex,
                currentBet);
    }

    public GameState compileState(String playerName) {
        return new GameState(players.stream().map(player -> player.compileState(playerName)).toList(),
                currentPlayerIndex,
                currentBet);
    }

    public Boolean isGameOver() {
        return isGameOver;
    }

    /** Starts the Game */
    public void startGame() {
        currentPlayerIndex = 0;
        // Roll all dices
        players.forEach(player ->  {
            player.rollPlayerDices();

            // Notify every Player with their dices
            listener.notifySecret(player.getName() + " has received their dices");
        });
        // Shuffle Player order
        Collections.shuffle(players);
        listener.notifySecret("Starting game");
    }

    /** Raises the Bet */
    public void raiseBet(String raiser, Bet newBet) throws ModelException {
        // Integrity checks on newBet
        if (newBet == null || newBet.numberOfDices == null || newBet.valueOfTheDices == null)
            throw new ModelException(raiser+": Invalid bet");

        // Only the current Player can raise the bet
        if (!raiser.equals(players.get(currentPlayerIndex).getName()))
            throw new ModelException(raiser+": Not the current Player");

        // Set the bet
        if (currentBet == null) {
            // At the start of the game the current Bet is null, so the first Player can bet whatever he wants to
            currentBet = newBet;
        } else {
            if (newBet.numberOfDices > currentBet.numberOfDices) {
                // The number of dices of the new bet is higher -> set whatever face he wants
                currentBet = newBet;
            } else if (newBet.numberOfDices.intValue() == currentBet.numberOfDices) {
                // The number of dices os the same -> the face value must increase
                if (newBet.valueOfTheDices > currentBet.valueOfTheDices)
                    currentBet = newBet;
                else
                    throw new ModelException(raiser+": Invalid bet");
            } else
                throw new ModelException(raiser+": Invalid bet");
        }
        // Set the next Player as the current Player
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        listener.notifySecret("The bet has been risen!");

    }

    /** Calls a Liar the latest Player to have bet */
    public void callLiar(String callerName) throws ModelException {
        if (currentBet == null)
            throw new ModelException(callerName+": No one has played yet");

        // Get all Game's Dices
        List<Dice> dices = players.stream()
                .flatMap(player -> player.getDices().stream())
                .toList();

        // Get the latest Player to have raised the bet
        Player potentialLiar = players.get((currentPlayerIndex - 1 + players.size()) % players.size());
        // Get the caller
        Player caller = players.stream()
                .filter(player -> player.getName().equals(callerName))
                .findFirst()
                .orElse(null);
        if (caller == null)
            throw new RuntimeException("Caller is null in callLiar");

        if (dices.stream()
                .filter(dice -> dice.getValue().intValue() == currentBet.valueOfTheDices)
                .count() < currentBet.numberOfDices)
            potentialLiar.setIsALiar(true);
        else
            caller.setIsALiar(true);


        // Generate the state of the game
        Map<String, List<Dice>> dicesPerPlayer = new HashMap<>();
        for (Player player : players) {
            dicesPerPlayer.computeIfAbsent(player.getName(), k -> new ArrayList<>());
            for (Dice dice : player.getDices()) {
                dicesPerPlayer.get(player.getName()).add(dice);
            }
        }

        // Notify with the entire Game State
        if (potentialLiar.isALiar())
            listener.notify(potentialLiar.getName()+" is a Liar!");
        else
            listener.notify(caller.getName()+" made a bad call!");
        this.isGameOver = true;
    }
}
