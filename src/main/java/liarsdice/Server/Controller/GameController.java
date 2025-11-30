package liarsdice.Server.Controller;

import liarsdice.Actions.Action;
import liarsdice.Messages.Message;
import liarsdice.Server.Model.Bet;
import liarsdice.Server.Model.GameManager;
import liarsdice.Server.Model.ModelException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameController implements Runnable, Listener {

    private final Integer maxClients;
    private final Integer maxDiceValue;
    private final Integer dicesPerPlayer;

    /** Lobby Thread */
    private Thread lobbyThread;
    /** Clock speed */
    private final Integer clockSpeed = 20;
    /** Timer between games */
    private final Integer timeBetweenGames = 30000;
    /** The Clients */
    private final List<Client> playerClients = new ArrayList<>();
    /** The Game Manager */
    GameManager gameManager;
    /** Action Queue */
    private final Queue<Action> actionQueue = new LinkedList<>();

    public GameController(int maxClients, int dicesPerPlayer, int maxDiceValue) {
        this.maxClients = maxClients;
        this.maxDiceValue = maxDiceValue;
        this.dicesPerPlayer = dicesPerPlayer;
        gameManager = null;
    }

    @Override
    public void run() {
        lobbyThread = Thread.currentThread();

        while (!Thread.currentThread().isInterrupted()) {

            if (gameManager != null && gameManager.isGameOver())
                resetGame();

            Action newAction = null;
            synchronized (actionQueue) {
                newAction = actionQueue.poll();
            }

            if (newAction != null)
                newAction.execute(this);

            try {
                Thread.sleep(clockSpeed);
            } catch (InterruptedException e) {
                // Interrupted
            }
        }
    }

    /** Terminates this lobby and its Clients */
    public void terminateLobby() {
        playerClients.forEach(Client::disconnect);
        playerClients.clear();
        lobbyThread.interrupt();
    }

    /** Adds the Client to the game and starts it
     * @return true if the lobby is full after adding the Client; false otherwise
     * */
    public boolean addPlayer(Client client) {
        playerClients.add(client);
        new Thread(client).start();
        notifySecret(client.getNickname()+" has joined the game");
        return playerClients.size() == maxClients;
    }

    /** Replaces a disconnected Client with a new one. Starts the new Client
     * @param nickname the name of the Client to substitute
     * @param newClient the new Client
     * */
    public void swapClients(String nickname, Client newClient) {
        // Find Client
        Client clientToRemove = playerClients.stream()
                .filter(client -> client.getNickname().equals(nickname))
                .findFirst()
                .orElse(null);
        // Swap
        playerClients.remove(clientToRemove);
        playerClients.add(newClient);
        new Thread(newClient).start();
        notifySecret(newClient.getNickname()+" has reconnected to the game");
    }

    public void handleDisconnection(String nickname) {
        if (playerClients.stream().noneMatch(Client::isConnected))
            terminateLobby();
        notifySecret(nickname+" has left the game");
    }

    /** Checks if the given name is connected to this lobby
     * @return true if the given name is connected; false otherwise
     * */
    public boolean isNameUsed(String playerName) throws PlayerDisconnectedException {
        Client sameName = playerClients.stream().filter(client -> client.getNickname().equals(playerName))
                .findFirst()
                .orElse(null);
        if (sameName != null)
            // Found
            if (!sameName.isConnected())
                // If it is not connected, throw Exception
                throw new PlayerDisconnectedException(this);
            else
                // Player is connected
                return true;
        // Player is not connected
        return false;
    }

    /** Starts the Game */
    public void startGame() {
        gameManager = new GameManager(this, playerClients.stream().map(Client::getNickname).toList(), dicesPerPlayer, maxDiceValue);
        gameManager.startGame();
    }

    /** Adds an Action to this Controller's queue */
    public void addActionQoQueue(Action action) {
        synchronized (actionQueue) {
            actionQueue.add(action);
        }
    }

    /** Notifies every Client with the given message */
    @Override
    public void notify(String message) {
        if (gameManager == null)
            playerClients.forEach(client -> client.notifyClient(new Message(message, null)));
        else
            playerClients.forEach(client -> client.notifyClient(new Message(message, gameManager.compileState())));
    }

    /** Notifies every Client only with the required information for each one of them */
    @Override
    public void notifySecret(String message) {
        if (gameManager == null)
            playerClients.forEach(client -> client.notifyClient(new Message(message, null)));
        else
            playerClients.forEach(client -> client.notifyClient(new Message(message, gameManager.compileState(client.getNickname()))));
    }

    /** Tries to change the Model with the given inputs */
    public void raiseBet(String name, Bet newBet) {
        if (gameManager == null)
            return;
        try {
            gameManager.raiseBet(name, newBet);
        } catch (ModelException e) {
            notifySecret(e.getMessage());
        }
    }

    /** Tries to change the Model with the given inputs */
    public void callLiar(String name) {
        if (gameManager == null)
            return;
        try {
            gameManager.callLiar(name);
        } catch (ModelException e) {
            notifySecret(e.getMessage());
        }
    }

    /** Resets the Game */
    private void resetGame() {
        notify("Game restarting...");
        try {
            Thread.sleep(timeBetweenGames);
        } catch (InterruptedException e) {
            return;
        }
        startGame();
    }

}
