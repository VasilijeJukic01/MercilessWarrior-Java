package platformer.core;

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