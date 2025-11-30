package liarsdice.Client;

import liarsdice.Messages.Message;

public class Client implements Runnable {

    public static String nickname;
    public static String serverAddress;
    public static Connection connection;
    public static Renderer renderer;
    public static InputReader inputReader;
    /** Graphics lock */
    public static final Object graphicsLock = new Object();

    /** Main of Client */
    public static void main(String[] args) {
        if (args.length != 2)
            return;

        nickname = args[0];
        serverAddress = args[1];
        Client client = new Client();
        client.run();
    }

    @Override
    public void run() {
        connection = new Connection();
        renderer = new Renderer();
        inputReader = new InputReader();

        new Thread(connection).start();
        new Thread(inputReader).start();
    }
}
