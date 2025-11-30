package liarsdice.Actions;

import liarsdice.Server.Controller.GameController;

public class CallLiarAction extends Action {

    public CallLiarAction() {

    }

    @Override
    public void execute(GameController controller) {
        controller.callLiar(nickname);
    }
}
