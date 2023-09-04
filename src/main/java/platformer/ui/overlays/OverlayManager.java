package platformer.ui.overlays;

import platformer.state.GameState;
import platformer.state.PlayingState;
import platformer.ui.dialogue.DialogueOverlay;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class OverlayManager {

    private final GameState gameState;
    private final Map<PlayingState, Overlay> overlays;

    public OverlayManager(GameState gameState) {
        this.gameState = gameState;
        this.overlays = new HashMap<>();

        this.overlays.put(PlayingState.PAUSE, new PauseOverlay(gameState.getGame(), gameState));
        this.overlays.put(PlayingState.GAME_OVER, new GameOverOverlay(gameState.getGame()));
        this.overlays.put(PlayingState.SHOP, new ShopOverlay(gameState));
        this.overlays.put(PlayingState.BLACKSMITH, new BlacksmithOverlay(gameState));
        this.overlays.put(PlayingState.DIALOGUE, new DialogueOverlay());
        this.overlays.put(PlayingState.SAVE, new SaveGameOverlay());
    }

    // Core
    public void update(PlayingState playingState) {
        Overlay overlay = overlays.get(playingState);
        if (overlay != null) {
            overlay.update();
        }
    }

    public void render(Graphics g) {
        PlayingState overlay = gameState.getActiveState();
        if (overlay != null) {
            overlays.get(overlay).render(g);
        }
    }

    public void mousePressed(MouseEvent e) {
        PlayingState overlay = gameState.getActiveState();
        if (overlay != null) {
            overlays.get(overlay).mousePressed(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        PlayingState overlay = gameState.getActiveState();
        if (overlay != null) {
            overlays.get(overlay).mouseReleased(e);
        }
    }

    public void mouseMoved(MouseEvent e) {
        PlayingState overlay = gameState.getActiveState();
        if (overlay != null) {
            overlays.get(overlay).mouseMoved(e);
        }
    }

    public void mouseDragged(MouseEvent e) {
        PlayingState overlay = gameState.getActiveState();
        if (overlay != null) {
            overlays.get(overlay).mouseDragged(e);
        }
    }

    public void reset() {
        overlays.get(PlayingState.SHOP).reset();
        overlays.get(PlayingState.DIALOGUE).reset();
    }

    public Map<PlayingState, Overlay> getOverlays() {
        return overlays;
    }
}
