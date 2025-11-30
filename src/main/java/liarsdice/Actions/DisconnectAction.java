package liarsdice.Actions;

import liarsdice.Server.Controller.GameController;

public class DisconnectAction extends Action{

    public DisconnectAction(String name) {
        nickname = name;
    }

    @Override
    public void execute(GameController controller) {
        controller.handleDisconnection(nickname);
    }
}
