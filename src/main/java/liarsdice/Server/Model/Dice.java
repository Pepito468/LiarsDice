package liarsdice.Server.Model;

import liarsdice.Server.Model.GameState.DiceState;

public class Dice {

    /** The maximum value this dice can have*/
    private final Integer maxValue;
    /** The value of this Dice */
    private Integer value;

    public Dice(int maxValue) {
        this.maxValue = maxValue;
        // Initialize to a random value
        roll();
    }

    /** Rolls the dice to a value between 1 and {@link #maxValue} */
    public void roll() {
        value = (int) (Math.random() * 6) + 1;
    }

    /** Returns the {@link #value} of this Dice */
    public Integer getValue() {
        return value;
    }

    public DiceState compileState() {
        return new DiceState(value);
    }
}
