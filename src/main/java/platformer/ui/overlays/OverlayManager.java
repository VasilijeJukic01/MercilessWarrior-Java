package platformer.ui.overlays;

import platformer.state.GameState;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class OverlayManager {

    private final GameState gameState;
    private final Map<OverlayType, Overlay> overlays;

    public OverlayManager(GameState gameState) {
        this.gameState = gameState;
        this.overlays = new HashMap<>();

        this.overlays.put(OverlayType.PAUSE, new PauseOverlay(gameState.getGame(), gameState));
        this.overlays.put(OverlayType.GAME_OVER, new GameOverOverlay(gameState.getGame()));
        this.overlays.put(OverlayType.SHOP, new ShopOverlay(gameState));
        this.overlays.put(OverlayType.BLACKSMITH, new BlacksmithOverlay(gameState));
    }

    // Core
    public void update(OverlayType overlayType) {
        Overlay overlay = overlays.get(overlayType);
        if (overlay != null) {
            overlay.update();
        }
    }

    public void render(Graphics g) {
        OverlayType overlay = gameState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).render(g);
        }
    }

    public void mousePressed(MouseEvent e) {
        OverlayType overlay = gameState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).mousePressed(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        OverlayType overlay = gameState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).mouseReleased(e);
        }
    }

    public void mouseMoved(MouseEvent e) {
        OverlayType overlay = gameState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).mouseMoved(e);
        }
    }

    public void mouseDragged(MouseEvent e) {
        OverlayType overlay = gameState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).mouseDragged(e);
        }
    }

    public void reset() {
        overlays.get(OverlayType.SHOP).reset();
    }
}
