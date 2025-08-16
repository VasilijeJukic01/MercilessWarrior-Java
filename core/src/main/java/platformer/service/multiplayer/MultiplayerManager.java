package platformer.service.multiplayer;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import platformer.model.world.MultiplayerHandler;

/**
 * Manages the low-level WebSocket connection to the multiplayer service.
 * <p>
 * This class is responsible for establishing, maintaining, and tearing down the WebSocket connection.
 * It implements {@link WebSocket.Listener} to asynchronously handle incoming message fragments, reassemble them,
 * and place complete messages into a thread-safe queue for processing by the {@link MultiplayerHandler}.
 * It also provides a simple interface for sending messages to the server.
 *
 * @see MultiplayerHandler
 */
public class MultiplayerManager implements WebSocket.Listener {

    private WebSocket webSocket;
    private final ConcurrentLinkedQueue<String> receivedMessages = new ConcurrentLinkedQueue<>();
    private final StringBuilder messageBuffer = new StringBuilder();

    private final String clientId = UUID.randomUUID().toString();

    /**
     * Establishes a WebSocket connection to the server to host a new game session.
     *
     * @param username The username of the hosting player.
     */
    public void hostSession(String username) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            client.newWebSocketBuilder()
                  .buildAsync(URI.create("ws://localhost:9000/multiplayer/ws/host"), this)
                  .thenAccept(ws -> {
                      this.webSocket = ws;
                      Logger.getInstance().notify("WebSocket HOST connection established!", Message.INFORMATION);
                  });
        } catch (Exception e) {
            Logger.getInstance().notify("WebSocket connection failed: " + e.getMessage(), Message.ERROR);
        }
    }

    /**
     * Establishes a WebSocket connection to the server to join an existing game session.
     *
     * @param sessionId The ID of the session to join.
     * @param username The username of the joining player.
     */
    public void joinSession(String sessionId, String username) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            client.newWebSocketBuilder()
                  .buildAsync(URI.create("ws://localhost:9000/multiplayer/ws/join/" + sessionId), this)
                  .thenAccept(ws -> {
                      this.webSocket = ws;
                      Logger.getInstance().notify("WebSocket JOIN connection established!", Message.INFORMATION);
                  });
        } catch (Exception e) {
            Logger.getInstance().notify("WebSocket connection failed: " + e.getMessage(), Message.ERROR);
        }
    }

    /**
     * Closes the WebSocket connection gracefully.
     * This method should be called when the client is leaving the game state or shutting down.
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client leaving game state").join();
            webSocket = null;
        }
    }

    /**
     * Sends a text message to the server over the active WebSocket connection.
     *
     * @param message The string message to send.
     */
    public void sendMessage(String message) {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendText(message, true);
        }
    }

    /**
     * Retrieves and removes the oldest message from the incoming message queue.
     *
     * @return The oldest message as a String, or null if the queue is empty.
     */
    public String pollMessage() {
        return receivedMessages.poll();
    }

    @Override
    public void onOpen(WebSocket ws) {
        Logger.getInstance().notify("WebSocket opened!", Message.INFORMATION);
        ws.request(1);
    }

    /**
     * Handles incoming text message fragments.
     * It buffers the fragments and adds the complete message to a queue once the final fragment is received.
     *
     * @param ws The WebSocket instance.
     * @param data The CharSequence containing the message fragment.
     * @param last A boolean indicating if this is the final fragment of a message.
     */
    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        messageBuffer.append(data);
        if (last) {
            receivedMessages.add(messageBuffer.toString());
            messageBuffer.setLength(0);
        }
        ws.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
        Logger.getInstance().notify("WebSocket closed with status " + statusCode + ", reason: " + reason, Message.WARNING);
        this.webSocket = null;
        return null;
    }

    @Override
    public void onError(WebSocket ws, Throwable error) {
        Logger.getInstance().notify("WebSocket error: " + error.getMessage(), Message.ERROR);
        this.webSocket = null;
    }

    public String getClientId() {
        return clientId;
    }
}