package liarsdice.Server.Model.GameState;

import liarsdice.Server.Model.Bet;

import java.util.ArrayList;
import java.util.List;

/** Represents the state of the Game */
public class GameState {

    public final List<PlayerState> players;
    public final Integer currentPlayerIndex;
    public final Bet currentBet;

    public GameState() {
        this.players = null;
        this.currentPlayerIndex = 0;
        this.currentBet = null;
    }

    public GameState(List<PlayerState> players, Integer currentPlayerIndex, Bet currentBet) {
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.currentBet = currentBet;
    }

}
