package platformer.service.rest.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.core.TokenStorage;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.service.rest.requests.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;

import static platformer.constants.Constants.API_GATEWAY_URL;

/**
 * A low-level client for communicating with the game's RESTful backend API.
 * <p>
 * This class is responsible for constructing and sending HTTP requests, handling authentication tokens, and parsing JSON responses.
 * It abstracts away the raw HTTP logic from the rest of the service layer.
 */
public class GameServiceClient {

    private final Gson gson = new Gson();

    private static final class ApiEndpoints {
        private static final String AUTH_LOGIN = "/auth/login";
        private static final String AUTH_REGISTER = "/auth/register";
        private static final String GAME_ACCOUNT = "/game/account";
        private static final String LEADERBOARD = "/leaderboard";
        private static final String MASTER_ITEMS = "/items/master";
        private static final String SHOP = "/shop";
        private static final String SHOP_BUY = "/shop/buy";
        private static final String SHOP_SELL = "/shop/sell";
    }

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
            String url = API_GATEWAY_URL + ApiEndpoints.AUTH_LOGIN;
            HttpURLConnection conn = HttpRequestHandler.createPostConnection(url, "application/json");
            HttpRequestHandler.sendJsonPayload(conn, gson.toJson(authRequest));

            if (conn.getResponseCode() == 200) {
                AuthenticationResponse authResponse = gson.fromJson(HttpRequestHandler.getResponse(conn), AuthenticationResponse.class);
                TokenStorage.getInstance().setToken(authResponse.getJwt());
                Logger.getInstance().notify("Login successful!", Message.INFORMATION);
                return true;
            }
            else {
                Logger.getInstance().notify("Login failed with code: " + conn.getResponseCode(), Message.WARNING);
                return false;
            }
        } catch (IOException e) {
            Logger.getInstance().notify("Login network error: " + e.getMessage(), Message.WARNING);
            return false;
        }
    }

    public int createAccount(String username, String password) throws IOException {
        String url = API_GATEWAY_URL + ApiEndpoints.AUTH_REGISTER;
        HttpURLConnection conn = HttpRequestHandler.createPostConnection(url, "application/json");
        RegistrationRequest request = new RegistrationRequest(username, password, List.of("USER"));
        HttpRequestHandler.sendJsonPayload(conn, gson.toJson(request));

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            Logger.getInstance().notify("Account creation successful!", Message.INFORMATION);
            return 0;
        }
        else if (responseCode == 400) {
            Logger.getInstance().notify("Account creation failed, name conflict!", Message.ERROR);
            return 2;
        }
        else throw new IOException("Account creation failed with code: " + responseCode);
    }

    public AccountDataDTO fetchAccountData(String username) throws IOException {
        String url = API_GATEWAY_URL + ApiEndpoints.GAME_ACCOUNT + "/" + username;
        return sendGetRequest(url, AccountDataDTO.class);
    }

    public List<BoardItemDTO> loadLeaderboardData() throws IOException {
        String url = API_GATEWAY_URL + ApiEndpoints.LEADERBOARD;
        return sendGetRequest(url, new TypeToken<List<BoardItemDTO>>() {}.getType());
    }

    public void updateAccountData(AccountDataDTO accountDataDTO) throws IOException {
        String url = API_GATEWAY_URL + ApiEndpoints.GAME_ACCOUNT;
        sendRequestWithBody("PUT", url, accountDataDTO, Void.class);
    }

    public List<ItemMasterDTO> getMasterItems() throws IOException {
        String url = API_GATEWAY_URL + ApiEndpoints.MASTER_ITEMS;
        return sendGetRequest(url, new TypeToken<List<ItemMasterDTO>>() {}.getType());
    }

    public List<ShopItemDTO> getShopInventory(String shopId) throws IOException {
        String url = API_GATEWAY_URL + ApiEndpoints.SHOP + "/" + shopId;
        return sendGetRequest(url, new TypeToken<List<ShopItemDTO>>() {}.getType());
    }

    public ShopTransactionResponse buyItem(ShopTransactionRequest request) throws IOException {
        String url = API_GATEWAY_URL + ApiEndpoints.SHOP_BUY;
        return sendRequestWithBody("POST", url, request, ShopTransactionResponse.class);
    }

    public ShopTransactionResponse sellItem(ShopTransactionRequest request) throws IOException {
        String url = API_GATEWAY_URL + ApiEndpoints.SHOP_SELL;
        return sendRequestWithBody("POST", url, request, ShopTransactionResponse.class);
    }

    /**
     * A generic helper for sending GET requests that expect a JSON response.
     *
     * @param urlString The full URL for the request.
     * @param responseType The class or type token of the expected response.
     * @return The deserialized response object.
     * @throws IOException if the request fails or returns a non-200 status code.
     */
    private <T> T sendGetRequest(String urlString, Type responseType) throws IOException {
        HttpURLConnection conn = HttpRequestHandler.createGetConnection(urlString, TokenStorage.getInstance().getToken());
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = HttpRequestHandler.getResponse(conn);
            return gson.fromJson(response, responseType);
        }
        else throw new IOException("GET request failed with code: " + responseCode);
    }

    /**
     * A generic helper for sending POST or PUT requests with a JSON payload.
     *
     * @param method The HTTP method ("POST" or "PUT").
     * @param urlString The full URL for the request.
     * @param payload The object to be serialized into the JSON request body.
     * @param responseType The class of the expected response. Use Void.class if no response body is expected.
     * @return The deserialized response object.
     * @throws IOException if the request fails or returns a non-200 status code.
     */
    private <T> T sendRequestWithBody(String method, String urlString, Object payload, Class<T> responseType) throws IOException {
        HttpURLConnection conn;
        if ("POST".equalsIgnoreCase(method)) {
            conn = HttpRequestHandler.createPostConnection(urlString, "application/json");
        }
        else if ("PUT".equalsIgnoreCase(method)) {
            conn = HttpRequestHandler.createPutConnection(urlString, "application/json");
        }
        else throw new IllegalArgumentException("Unsupported HTTP method: " + method);

        conn.setRequestProperty("Authorization", "Bearer " + TokenStorage.getInstance().getToken());
        HttpRequestHandler.sendJsonPayload(conn, gson.toJson(payload));
        int responseCode = conn.getResponseCode();
        String responseBody = HttpRequestHandler.getResponse(conn);

        if (responseCode == 200) {
            if (responseType == Void.class) return null;
            return gson.fromJson(responseBody, responseType);
        }
        else throw new IOException(method + " request failed with code: " + responseCode + ", body: " + responseBody);
    }
}

