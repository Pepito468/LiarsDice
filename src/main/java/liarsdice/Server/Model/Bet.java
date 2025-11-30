package liarsdice.Server.Model;

/** Class to contain the Bets */
public class Bet {

    public final Integer numberOfDices;
    public final Integer valueOfTheDices;

    public Bet() {
        this.numberOfDices = 0;
        this.valueOfTheDices = 0;
    }

    public Bet(Integer numberOfDices, Integer valueOfTheDices) {
        this.numberOfDices = numberOfDices;
        this.valueOfTheDices = valueOfTheDices;
    }

    public String toString() {
        return "DICES: " + numberOfDices + " VALUE: " + valueOfTheDices;
    }

}
