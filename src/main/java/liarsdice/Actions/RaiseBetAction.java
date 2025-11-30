package liarsdice.Actions;

import liarsdice.Server.Controller.GameController;
import liarsdice.Server.Model.Bet;

public class RaiseBetAction extends Action {

    public final Bet bet;

    public RaiseBetAction() {
        this.bet = null;
    }

    public RaiseBetAction(Bet bet) {
        this.bet = bet;
    }

    @Override
    public void execute(GameController controller) {
        controller.raiseBet(nickname, bet);
    }
}
