package platformer.service.rest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for handling HTTP requests.
 */
public class HttpRequestHandler {

    /**
     * Creates a POST connection to the specified URL with the specified content type.
     *
     * @param url         the URL to connect to
     * @param contentType the content type of the request
     * @return the created connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection createPostConnection(String url, String contentType) throws IOException {
        URL targetUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);
        conn.setDoOutput(true);

        return conn;
    }

    /**
     * Creates a PUT connection to the URL with the content type.
     *
     * @param url         the URL to connect to
     * @param contentType the content type of the request
     * @return the created connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection createPutConnection(String url, String contentType) throws IOException {
        URL targetUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", contentType);
        conn.setDoOutput(true);

        return conn;
    }

    /**
     * Creates a GET connection to the URL with the token.
     *
     * @param url   the URL to connect to
     * @param token the token to use for authentication
     * @return the created connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection createGetConnection(String url, String token) throws IOException {
        URL targetUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection();
        conn.setRequestMethod("GET");
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        return conn;
    }

    /**
     * Reads the response from the specified connection.
     *
     * @param conn the connection to read the response from
     * @return the response as a string
     * @throws IOException if an I/O error occurs
     */
    public static String getResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return response.toString();
        }
    }

    /**
     * Sends the JSON payload to the specified connection.
     *
     * @param conn            the connection to send the payload to
     * @param jsonInputString the JSON payload to send
     * @throws IOException if an I/O error occurs
     */
    public static void sendJsonPayload(HttpURLConnection conn, String jsonInputString) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

}
