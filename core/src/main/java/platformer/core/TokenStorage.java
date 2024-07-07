package platformer.core;

public class TokenStorage {

    private static String token;

    public static void setToken(String token) {
        TokenStorage.token = token;
    }

    public static String getToken() {
        return token;
    }
}
