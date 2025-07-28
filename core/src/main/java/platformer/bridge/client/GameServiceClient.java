package platformer.bridge.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.bridge.requests.*;
import platformer.core.TokenStorage;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

import static platformer.constants.Constants.API_GATEWAY_URL;

public class GameServiceClient {

    private final Gson gson = new Gson();

    /**
     * Attempts to authenticate and store the JWT. Does not throw an exception on failure.
     *
     * @return true if login was successful, false otherwise.
     */
    public boolean loginAndStoreToken(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            Logger.getInstance().notify("No credentials provided, proceeding in offline mode.", Message.INFORMATION);
            return false;
        }
        try {
            AuthenticationRequest authRequest = new AuthenticationRequest(username, password);
            String authUrl = API_GATEWAY_URL + "/auth/login";
            HttpURLConnection authConn = HttpRequestHandler.createPostConnection(authUrl, "application/json");
            String jsonAuthInputString = gson.toJson(authRequest);
            HttpRequestHandler.sendJsonPayload(authConn, jsonAuthInputString);

            int authResponseCode = authConn.getResponseCode();
            if (authResponseCode == 200) {
                AuthenticationResponse authResponse = gson.fromJson(HttpRequestHandler.getResponse(authConn), AuthenticationResponse.class);
                TokenStorage.getInstance().setToken(authResponse.getJwt());
                Logger.getInstance().notify("Login successful!", Message.INFORMATION);
                return true;
            } else {
                Logger.getInstance().notify("Login failed with code: " + authResponseCode, Message.WARNING);
                return false;
            }
        } catch (IOException e) {
            Logger.getInstance().notify("Login network error. Assuming offline mode.", Message.WARNING);
            return false;
        }
    }

    public int createAccount(String username, String password) throws IOException {
        String url = API_GATEWAY_URL + "/auth/register";
        HttpURLConnection conn = HttpRequestHandler.createPostConnection(url, "application/json");

        String jsonInputString = gson.toJson(new RegistrationRequest(username, password, List.of("USER")));
        HttpRequestHandler.sendJsonPayload(conn, jsonInputString);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            Logger.getInstance().notify("Account creation successful!", Message.INFORMATION);
            return 0;
        }
        else if (responseCode == 400) {
            Logger.getInstance().notify("Account creation failed, name conflict!", Message.ERROR);
            return 2;
        }
        else {
            Logger.getInstance().notify("Account creation failed, unknown error!", Message.ERROR);
            return 1;
        }
    }

    public AccountDataDTO fetchAccountData(String username) throws IOException {
        String gameUrl = API_GATEWAY_URL + "/game/account/" + username;
        HttpURLConnection gameConn = HttpRequestHandler.createGetConnection(gameUrl, TokenStorage.getInstance().getToken());

        int gameResponseCode = gameConn.getResponseCode();
        if (gameResponseCode == 200) {
            Logger.getInstance().notify("Account data loaded successfully!", Message.INFORMATION);
            return gson.fromJson(HttpRequestHandler.getResponse(gameConn), AccountDataDTO.class);
        }
        else {
            Logger.getInstance().notify("Failed to load account data!", Message.ERROR);
            throw new IOException("Failed to load account data: " + gameResponseCode);
        }
    }

    public List<BoardItemDTO> loadLeaderboardData() throws IOException {
        String url = API_GATEWAY_URL + "/leaderboard";
        HttpURLConnection conn = HttpRequestHandler.createGetConnection(url, TokenStorage.getInstance().getToken());

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            Logger.getInstance().notify("Leaderboard fetched successfully!", Message.INFORMATION);
            String response = HttpRequestHandler.getResponse(conn);
            return gson.fromJson(response, new TypeToken<List<BoardItemDTO>>(){}.getType());
        }
        else {
            Logger.getInstance().notify("Leaderboard fetch failed!", Message.ERROR);
            throw new IOException("Failed to load leaderboard data: " + responseCode);
        }
    }

    public void updateAccountData(AccountDataDTO accountDataDTO) throws IOException {
        String url = API_GATEWAY_URL + "/game/account";
        HttpURLConnection conn = HttpRequestHandler.createPutConnection(url, "application/json");

        conn.setRequestProperty("Authorization", "Bearer " + TokenStorage.getInstance().getToken());

        String jsonInputString = gson.toJson(accountDataDTO);
        HttpRequestHandler.sendJsonPayload(conn, jsonInputString);

        int responseCode = conn.getResponseCode();

        if (responseCode != 200) Logger.getInstance().notify("Account data update failed!", Message.ERROR);
        else Logger.getInstance().notify("Account data updated successfully!", Message.INFORMATION);
    }

    public List<ItemMasterDTO> getMasterItems() throws IOException {
        String url = API_GATEWAY_URL + "/items/master";
        HttpURLConnection conn = HttpRequestHandler.createGetConnection(url, TokenStorage.getInstance().getToken());

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = HttpRequestHandler.getResponse(conn);
            return gson.fromJson(response, new TypeToken<List<ItemMasterDTO>>(){}.getType());
        } else {
            throw new IOException("Failed to load master item list: " + responseCode);
        }
    }

    public List<ShopItemDTO> getShopInventory(String shopId) throws IOException {
        String url = API_GATEWAY_URL + "/shop/" + shopId;
        HttpURLConnection conn = HttpRequestHandler.createGetConnection(url, TokenStorage.getInstance().getToken());

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = HttpRequestHandler.getResponse(conn);
            return gson.fromJson(response, new TypeToken<List<ShopItemDTO>>(){}.getType());
        } else {
            throw new IOException("Failed to load shop inventory for '" + shopId + "': " + responseCode);
        }
    }

    public boolean buyItem(ShopTransactionRequest request) throws IOException {
        String url = API_GATEWAY_URL + "/shop/buy";
        HttpURLConnection conn = HttpRequestHandler.createPostConnection(url, "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + TokenStorage.getInstance().getToken());

        String jsonPayload = gson.toJson(request);
        HttpRequestHandler.sendJsonPayload(conn, jsonPayload);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            Logger.getInstance().notify("Purchase successful!", Message.INFORMATION);
            return true;
        }
        else {
            String error = HttpRequestHandler.getResponse(conn);
            Logger.getInstance().notify("Purchase failed: " + error, Message.ERROR);
            return false;
        }
    }

    public boolean sellItem(ShopTransactionRequest request) throws IOException {
        String url = API_GATEWAY_URL + "/shop/sell";
        HttpURLConnection conn = HttpRequestHandler.createPostConnection(url, "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + TokenStorage.getInstance().getToken());

        String jsonPayload = gson.toJson(request);
        HttpRequestHandler.sendJsonPayload(conn, jsonPayload);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            Logger.getInstance().notify("Sale successful!", Message.INFORMATION);
            return true;
        }
        else {
            String error = HttpRequestHandler.getResponse(conn);
            Logger.getInstance().notify("Sale failed: " + error, Message.ERROR);
            return false;
        }
    }
}

