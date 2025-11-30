package liarsdice.Server.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import liarsdice.Actions.Action;
import liarsdice.Actions.DisconnectAction;
import liarsdice.Messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Client implements Runnable {

    private final String nickname;
    private Thread clientThread;
    private final GameController gameController;
    private final Socket socket;
    private final Integer timeoutDuration = 10000;
    private boolean isConnected;

    public Client(String nickname, GameController lobby, Socket socket) {
        this.nickname = nickname;
        this.gameController = lobby;
        this.socket = socket;
        this.isConnected = true;
    }

    /** Client waits for input and sends it to its Controller */
    @Override
    public void run() {
        clientThread = Thread.currentThread();

        ObjectMapper mapper = new ObjectMapper();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!Thread.currentThread().isInterrupted()) {

                // Set timeout
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        disconnect();
                    }
                }, timeoutDuration);

                String JSON = in.readLine();
                // Read something -> cancel timeout
                timer.cancel();

                // If null is read, assume disconnected
                if (JSON == null) {
                    disconnect();
                    break;
                }

                // If action is KEEPALIVE, skip
                if (JSON.equals("KEEPALIVE"))
                    continue;

                // Generate the action
                Action action = mapper.readValue(JSON, Action.class);
                action.setName(this.nickname);
                gameController.addActionQoQueue(action);
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: "+e.getMessage());
            disconnect();
        }
    }

    /** Notifies this Listener */
    public void notifyClient(Message message) {
        if (!isConnected)
            return;
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ObjectMapper mapper = new ObjectMapper();
            out.println(mapper.writeValueAsString(message));
        } catch (IOException e) {
            disconnect();
        }
    }

    /** Disconnects the Client */
    public void disconnect() {
        if (!isConnected)
            return;
        clientThread.interrupt();
        try{
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        isConnected = false;
        gameController.addActionQoQueue(new DisconnectAction(nickname));
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isConnected() {
        return isConnected;
    }

}
