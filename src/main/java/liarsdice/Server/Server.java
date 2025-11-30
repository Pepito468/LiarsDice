package liarsdice.Server;

import com.fasterxml.jackson.databind.ObjectMapper;
import liarsdice.Actions.ConnectionAction;
import liarsdice.Messages.Message;
import liarsdice.Server.Controller.Client;
import liarsdice.Server.Controller.GameController;
import liarsdice.Server.Controller.PlayerDisconnectedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {

    // Constants
    private final Integer maxClientPerLobby = 3;
    private final Integer maxDiceValue = 6;
    private final Integer dicesPerPlayer = 5;

    /** Lobby open to new Players */
    private GameController openLobby;
    /** Lobby with running games */
    private final List<GameController> runningLobbies = new ArrayList<>();
    /** Lock for creating only one Client at a time */
    private final Object lock = new Object();

    /** Main of Server */
    public static void main(String[] args) {
        // Start Server
        Server server = new Server();
        server.run();
    }

    /** Server wait for new connections and connects them to a lobby */
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(46800);

            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                System.out.println("[SERVER] New connection from " + socket.getRemoteSocketAddress());
                new Thread(() -> connectClient(socket)).start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Connects the new Socket to a lobby
     * @param socket the socket to connect
     * */
    private void connectClient(Socket socket) {
        try {
            // Read name
            ObjectMapper mapper = new ObjectMapper();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String firstCommand = in.readLine();
            ConnectionAction action = mapper.readValue(firstCommand, ConnectionAction.class);

            // Create the Client
            synchronized (lock) {
                try {
                    if (isNameInUse(action.getName())) {
                        // Name is duplicate -> refuse connection
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(mapper.writeValueAsString(new Message("NAME ALREADY IN USE", null)));
                        socket.close();
                        return;
                    }
                } catch (PlayerDisconnectedException e) {
                    // Client was disconnected -> reconnect it
                    Client newClient = createClient(action.getName(), e.lobby, socket);
                    e.lobby.swapClients(action.getName(), newClient);
                    System.out.println("[SERVER] Reconnected Client to a game");
                    return;
                }
                // Name was not in use -> connect normally
                // Create open lobby if it doesn't exist yet
                if (openLobby == null) {
                    openLobby = new GameController(maxClientPerLobby, dicesPerPlayer, maxDiceValue);
                    new Thread(openLobby).start();
                }

                // Create Client and add it to lobby
                Client newClient = createClient(action.getName(), openLobby, socket);

                // If lobby is full, start the game and prepare for new connection
                if (openLobby.addPlayer(newClient)) {
                    System.out.println("[SERVER] Lobby is full, starting game.");
                    openLobby.startGame();
                    runningLobbies.add(openLobby);
                    openLobby = null;
                }
                System.out.println("[SERVER] Connected Client to a game");

            }
        } catch (IOException e) {
            System.err.println("[SERVER] Something went wrong when connecting a Client: " + e.getMessage());
        }
    }

    private Client createClient(String nickname, GameController lobby, Socket socket) {
        return new Client(nickname, lobby, socket);
    }

    /** Checks if the given name is in a lobby
     * @param nickname the name to check
     * @return true if the given name is connected to a lobby; false otherwise
     * @throws PlayerDisconnectedException if the Player is of a running lobby, but is disconnected
     * */
    private boolean isNameInUse(String nickname) throws PlayerDisconnectedException {
        // Check lobbies
        for (GameController lobby : runningLobbies) {
            if (lobby.isNameUsed(nickname))
                return true;
        }
        // Check open lobby
        if (openLobby != null && openLobby.isNameUsed(nickname))
            return true;

        // Not found anywhere
        return false;
    }

}
