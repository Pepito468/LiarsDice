package liarsdice.Server.Model;

import liarsdice.Server.Model.GameState.DiceState;
import liarsdice.Server.Model.GameState.PlayerState;

import java.util.List;
import java.util.stream.Collectors;

public class Player {

    /** The name of this Player */
    private String name;
    /** The Dices of this Player */
    private final List<Dice> dices;
    /** Boolean representing if this Player is a Liar */
    private boolean isALiar;

    /** Constructor
     * @param dices a List containing the dices that will be assigned to this Player
     * */
    public Player(String name, List<Dice> dices) {
        this.name = name;
        this.isALiar = false;
        this.dices = dices;
    }

    /** Rolls all of this Player's Dices */
    public void rollPlayerDices() {
        dices.forEach(Dice::roll);
    }

    /** Get this Player's Dices */
    public List<Dice> getDices() {
        return dices;
    }

    public void setIsALiar(boolean isALiar) {
        this.isALiar = isALiar;
    }

    /** Returns the name of this Player */
    public String getName() {
        return name;
    }

    public PlayerState compileState() {
        return new PlayerState(name, dices.stream().map(Dice::compileState).collect(Collectors.toList()), isALiar);
    }

    public boolean isALiar() {
        return isALiar;
    }

    /** Compiles a PlayerState: if the given name is not this Player's name, the dices are set to 0 */
    public PlayerState compileState(String playerName) {
        if (name.equals(playerName))
            return compileState();
        else
            return new PlayerState(name, dices.stream().map(_ -> new DiceState()).collect(Collectors.toList()), isALiar);
    }
}