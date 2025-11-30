package liarsdice.Actions;

import liarsdice.Server.Controller.GameController;

public class ConnectionAction extends Action {

    public ConnectionAction() {
        super();
    }

    public ConnectionAction(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public void execute(GameController controller) {
        // Won't be executed
    }

    public String getName() {
        return this.nickname;
    }

}
