package platformer.bridge.client;

import com.google.gson.Gson;
import platformer.bridge.requests.AccountDataDTO;
import platformer.bridge.requests.AuthenticationRequest;
import platformer.bridge.requests.AuthenticationResponse;
import platformer.bridge.requests.RegistrationRequest;
import platformer.core.TokenStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

// TODO: Refactor
public class AuthServiceClient {

    private static final String AUTH_SERVICE_URL = "http://localhost:8081";
    private static final String GAME_SERVICE_URL = "http://localhost:8082";
    private final Gson gson = new Gson();

    public int createAccount(String username, String password) throws IOException {
        URL url = new URL(AUTH_SERVICE_URL + "/auth/register");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = gson.toJson(new RegistrationRequest(username, password, List.of("USER")));
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                int userId = Integer.parseInt(response.toString());
                createSettingsForUser(userId);
                return 0;
            }
        }
        else if (responseCode == 400) return 2;
        else return 1;
    }

    public void createSettingsForUser(int userId) throws IOException {
        URL url = new URL(GAME_SERVICE_URL + "/settings/empty/" + userId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Failed to create settings for user: " + responseCode);
        }
    }

    public AccountDataDTO loadAccountData(String username, String password) throws IOException {
        AuthenticationRequest authRequest = new AuthenticationRequest(username, password);

        URL authUrl = new URL("http://localhost:8081/auth/login");
        HttpURLConnection authConn = (HttpURLConnection) authUrl.openConnection();
        authConn.setRequestMethod("POST");
        authConn.setRequestProperty("Content-Type", "application/json");
        authConn.setDoOutput(true);

        String jsonAuthInputString = gson.toJson(authRequest);
        try (OutputStream os = authConn.getOutputStream()) {
            byte[] input = jsonAuthInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        String token;
        int authResponseCode = authConn.getResponseCode();
        if (authResponseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(authConn.getInputStream(), StandardCharsets.UTF_8))) {
                AuthenticationResponse authResponse = gson.fromJson(br, AuthenticationResponse.class);
                token = authResponse.getJwt();
                TokenStorage.setToken(token);
            }
        } else {
            throw new IOException("Failed to authenticate: " + authResponseCode);
        }

        URL gameUrl = new URL(GAME_SERVICE_URL + "/game/account/" + username);
        HttpURLConnection gameConn = (HttpURLConnection) gameUrl.openConnection();
        gameConn.setRequestMethod("GET");
        gameConn.setRequestProperty("Authorization", "Bearer " + token);

        int gameResponseCode = gameConn.getResponseCode();

        System.out.println("Game response code: " + gameResponseCode);
        if (gameResponseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(gameConn.getInputStream(), StandardCharsets.UTF_8))) {
                return gson.fromJson(br, AccountDataDTO.class);
            }
        } else {
            throw new IOException("Failed to load account data: " + gameResponseCode);
        }
    }
}

