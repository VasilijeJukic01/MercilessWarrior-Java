package platformer.model.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.constants.UI;
import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.event.EventBus;
import platformer.event.events.multiplayer.ChatMessageReceivedEvent;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.service.multiplayer.MultiplayerManager;
import platformer.service.multiplayer.requests.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import platformer.state.types.GameState;
import platformer.ui.overlays.hud.MultiplayerInfoDisplay;

import static platformer.constants.Constants.*;

/**
 * Manages all high-level client-side multiplayer logic.
 * <p>
 * This class acts as the bridge between the game's state and the low-level networking layer.
 * This handler is updated once per game tick from the main {@link GameState} loop.
 *
 * @see MultiplayerManager
 * @see GameState
 */
public class MultiplayerHandler {

    private final MultiplayerInfoDisplay infoDisplay;

    private final GameContext context;
    private final MultiplayerManager multiplayerManager;
    private final Map<String, PlayerStateDTO> players = new ConcurrentHashMap<>();
    private final Map<String, Color> playerColors = new ConcurrentHashMap<>();
    private int nextColorIndex = 0;
    private final Gson gson = new Gson();
    private int networkTick = 0;
    private int pingTick = 0;

    public MultiplayerHandler(GameContext context) {
        this.context = context;
        this.multiplayerManager = context.getMultiplayerManager();
        this.infoDisplay = new MultiplayerInfoDisplay();
        assignColor(multiplayerManager.getClientId());
    }

    private Color assignColor(String clientId) {
        return playerColors.computeIfAbsent(clientId, id -> {
            Color color = UI.CHAT_COLORS.get(nextColorIndex);
            nextColorIndex = (nextColorIndex + 1) % UI.CHAT_COLORS.size();
            return color;
        });
    }

    public void update() {
        sendLocalPlayerState();
        sendPing();
        processIncomingMessages();
    }

    /**
     * Gathers the local player's current state, serializes it and sends it to the server.
     * This is throttled by {@code networkTick} to avoid sending updates on every single frame.
     */
    private void sendLocalPlayerState() {
        networkTick++;
        if (networkTick < 3) return;
        networkTick = 0;

        Player localPlayer = context.getPlayer();
        String clientId = multiplayerManager.getClientId();
        String username = Framework.getInstance().getAccount().getName();
        int currentLevelI = context.getLevelManager().getLevelIndexI();
        int currentLevelJ = context.getLevelManager().getLevelIndexJ();

        PlayerStateDTO localPlayerState = new PlayerStateDTO(
                clientId,
                username,
                localPlayer.getHitBox().x,
                localPlayer.getHitBox().y,
                currentLevelI,
                currentLevelJ,
                localPlayer.getEntityState().ordinal(),
                localPlayer.getAnimIndex(),
                localPlayer.getFlipSign(),
                localPlayer.getFlipCoefficient(),
                localPlayer.checkAction(PlayerAction.TRANSFORM)
        );
        multiplayerManager.sendMessage(gson.toJson(localPlayerState));
    }

    /**
     * Polls the message queue from the {@link MultiplayerManager}, deserializes each message,
     * and updates the state of the corresponding remote player in the {@code remotePlayers} map.
     */
    private void processIncomingMessages() {
        String message;
        while ((message = multiplayerManager.pollMessage()) != null) {
            try {
                JsonElement element = JsonParser.parseString(message);
                String type = element.getAsJsonObject().get("type").getAsString();

                switch (type) {
                    case "SESSION_CREATED":
                        SessionCreatedDTO sessionMsg = gson.fromJson(message, SessionCreatedDTO.class);
                        infoDisplay.setSessionId(sessionMsg.sessionId);
                        break;
                    case "SESSION_JOINED":
                        SessionJoinedDTO sessionJoinedMsg = gson.fromJson(message, SessionJoinedDTO.class);
                        infoDisplay.setSessionId(sessionJoinedMsg.sessionId);
                        break;
                    case "PLAYER_LEFT":
                        PlayerLeftDTO playerLeftMsg = gson.fromJson(message, PlayerLeftDTO.class);
                        players.remove(playerLeftMsg.clientId);
                        break;
                    case "PLAYER_STATE":
                        PlayerStateDTO remotePlayerState = gson.fromJson(message, PlayerStateDTO.class);
                        if (!remotePlayerState.clientId.equals(multiplayerManager.getClientId())) {
                            players.put(remotePlayerState.clientId, remotePlayerState);
                            assignColor(remotePlayerState.clientId);
                        }
                        break;
                    case "PONG":
                        PongDTO pongMsg = gson.fromJson(message, PongDTO.class);
                        long latency = System.currentTimeMillis() - pongMsg.clientTime;
                        infoDisplay.setPing(latency);
                        break;
                    case "CHAT_MESSAGE":
                        ChatMessageDTO chatMsg = gson.fromJson(message, ChatMessageDTO.class);
                        Color userColor = assignColor(chatMsg.clientId);
                        EventBus.getInstance().publish(new ChatMessageReceivedEvent(chatMsg.username, chatMsg.content, userColor));
                        break;
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Sends a chat message to the multiplayer session.
     *
     * @param content The text content of the message.
     */
    public void sendChatMessage(String content) {
        String username = Framework.getInstance().getAccount().getName();
        ChatMessageDTO chatMessage = new ChatMessageDTO(username, content, System.currentTimeMillis());
        multiplayerManager.sendMessage(gson.toJson(chatMessage));
    }

    private void sendPing() {
        pingTick++;
        if (pingTick >= 200) {
            pingTick = 0;
            PingDTO pingMsg = new PingDTO(System.currentTimeMillis());
            multiplayerManager.sendMessage(gson.toJson(pingMsg));
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        int localLevelI = context.getLevelManager().getLevelIndexI();
        int localLevelJ = context.getLevelManager().getLevelIndexJ();
        for (PlayerStateDTO remotePlayer : players.values()) {
            if (remotePlayer.levelI == localLevelI && remotePlayer.levelJ == localLevelJ) {
                renderRemotePlayer(g, remotePlayer, xLevelOffset, yLevelOffset);
            }
        }
        infoDisplay.render(g);
    }

    private void renderRemotePlayer(Graphics g, PlayerStateDTO remotePlayer, int xLevelOffset, int yLevelOffset) {
        BufferedImage[][] animations = SpriteManager.getInstance().getPlayerAnimations(remotePlayer.isTransformed);
        Anim animState = Anim.values()[remotePlayer.animState];

        if (animState.ordinal() < animations.length && animations[animState.ordinal()] != null) {
            BufferedImage[] animFrames = animations[animState.ordinal()];
            if (remotePlayer.animIndex < animFrames.length) {
                BufferedImage frame = animFrames[remotePlayer.animIndex];
                int x = (int) (remotePlayer.x - PLAYER_HB_OFFSET_X - xLevelOffset + remotePlayer.flipCoefficient);
                int y = (int) (remotePlayer.y - PLAYER_HB_OFFSET_Y - yLevelOffset);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.drawImage(frame, x, y, PLAYER_WIDTH * remotePlayer.flipSign, PLAYER_HEIGHT, null);
                g2d.dispose();
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(remotePlayer.username);
        int textX = (int) (remotePlayer.x + (PLAYER_HB_WID / 2.0) - (textWidth / 2.0) - xLevelOffset);
        int textY = (int) (remotePlayer.y - yLevelOffset - 5);
        g.drawString(remotePlayer.username, textX, textY);
    }
}
