package platformer.database;

/**
 * Interface for settings classes.
 * Settings classes are used to store and retrieve settings for the game.
 */
public interface Settings {

    Object getParameter(String parameter);

    void addParameter(String parameter, Object value);

}
