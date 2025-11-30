package liarsdice.Messages;

import liarsdice.Server.Model.GameState.GameState;

public class Message {

    public final String message;
    public final GameState state;

    public Message() {
        message = "";
        state = null;
    }

    public Message(String message, GameState state) {
        this.message = message;
        this.state = state;
    }
}
