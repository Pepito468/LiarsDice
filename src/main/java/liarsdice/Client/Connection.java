package liarsdice.Client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import liarsdice.Actions.Action;
import liarsdice.Actions.ConnectionAction;
import liarsdice.Messages.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Connection implements Runnable {

    private Socket socket;
    private Thread connectionThread;
    private Integer clockSpeed = 20;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void run() {
        connectionThread = Thread.currentThread();
        try {
            socket = new Socket(Client.serverAddress, 46800);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(mapper.writeValueAsString(new ConnectionAction(Client.nickname)));

            // Set time for anti disconnections
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    out.println("KEEPALIVE");
                }
            }, 0, clockSpeed);

            while (!Thread.currentThread().isInterrupted()) {
                String json = in.readLine();
                if (json == null) {
                    disconnect();
                    return;
                }
                Message message = mapper.readValue(json, Message.class);
                Client.renderer.drawState(message);
            }
        } catch (IOException e) {
            System.err.println("Error: "+e.getMessage());
            System.exit(1);
        }
    }

    /** Sends the given Action ot the Server */
    public void sendAction(Action action) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String json = mapper.writeValueAsString(action);
            out.println(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error: "+e.getMessage());
        }
        connectionThread.interrupt();
        System.exit(0);
    }
}
