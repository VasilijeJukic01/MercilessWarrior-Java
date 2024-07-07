package platformer.bridge.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.bridge.requests.AccountDataDTO;
import platformer.bridge.requests.BoardItemDTO;
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
public class GameServiceClient {

    private static final String GAME_SERVICE_URL = "http://localhost:8082";
    private final Gson gson = new Gson();

    public List<BoardItemDTO> loadLeaderboardData() throws IOException {
        URL url = new URL(GAME_SERVICE_URL + "/leaderboard");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + TokenStorage.getToken());

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return gson.fromJson(response.toString(), new TypeToken<List<BoardItemDTO>>(){}.getType());
            }
        } else {
            throw new IOException("Failed to load leaderboard data: " + responseCode);
        }
    }

    public void updateAccountData(AccountDataDTO accountDataDTO) throws IOException {
        System.out.println("Updating account data: " + accountDataDTO);
        URL url = new URL(GAME_SERVICE_URL + "/game/account");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = gson.toJson(accountDataDTO);
        System.out.println("Sending: " + jsonInputString);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode);
        if (responseCode != 200) {
            throw new IOException("Failed to update account data: " + responseCode);
        }
    }
}

