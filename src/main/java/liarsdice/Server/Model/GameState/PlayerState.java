package liarsdice.Server.Model.GameState;

import java.util.List;

public class PlayerState {

    public final String nickname;
    public final List<DiceState> dices;
    public final Boolean isALiar;

    public PlayerState() {
        this.nickname = null;
        this.dices = null;
        this.isALiar = null;
    }

    public PlayerState(String nickname, List<DiceState> dices, Boolean isALiar) {
        this.nickname = nickname;
        this.dices = dices;
        this.isALiar = isALiar;
    }

}
