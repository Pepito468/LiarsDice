package liarsdice.Server.Model;

/** Exception thrown by the Model when an input is incorrect */
public class ModelException extends Exception {
    public ModelException(String message) {
        super(message);
    }
}
