package liarsdice.Server.Controller;

public class PlayerDisconnectedException extends Exception {

    public final GameController lobby;

    public PlayerDisconnectedException(GameController lobby) {
      this.lobby = lobby;
    }
}
