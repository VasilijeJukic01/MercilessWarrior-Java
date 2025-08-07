package platformer.core;

/**
 * A class responsible for storing the user's JWT in memory for the duration of the current game session.
 * <p>
 * This class provides a secure way to manage the authentication token without ever persisting it to disk.
 * The token is lost when the application closes, requiring the user to log in again on the next launch.
 */
public class TokenStorage {

    private static TokenStorage instance;
    private String token;

    private TokenStorage() { }

    public static synchronized TokenStorage getInstance() {
        if (instance == null) {
            instance = new TokenStorage();
        }
        return instance;
    }

    public synchronized void setToken(String token) {
        this.token = token;
    }

    public synchronized String getToken() {
        return token;
    }
}