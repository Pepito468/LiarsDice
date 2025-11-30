package liarsdice.Server.Controller;

/** Listener listening to the Model */
public interface Listener {

    /** Notify all Listeners */
    void notify(String message);
    /** Notify every Listener with information tailored to their needs */
    void notifySecret(String message);

}
