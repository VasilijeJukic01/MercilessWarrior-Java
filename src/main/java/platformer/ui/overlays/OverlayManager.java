package platformer.ui.overlays;

import platformer.state.PlayingState;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class OverlayManager {

    private final PlayingState playingState;
    private final Map<OverlayType, Overlay> overlays;

    public OverlayManager(PlayingState playingState) {
        this.playingState = playingState;
        this.overlays = new HashMap<>();

        this.overlays.put(OverlayType.PAUSE, new PauseOverlay(playingState.getGame()));
        this.overlays.put(OverlayType.GAME_OVER, new GameOverOverlay(playingState.getGame()));
        this.overlays.put(OverlayType.SHOP, new ShopOverlay(playingState));
        this.overlays.put(OverlayType.BLACKSMITH, new BlacksmithOverlay(playingState));
    }

    // Core
    public void update(OverlayType overlayType) {
        Overlay overlay = overlays.get(overlayType);
        if (overlay != null) {
            overlay.update();
        }
    }

    public void render(Graphics g) {
        OverlayType overlay = playingState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).render(g);
        }
    }

    public void mousePressed(MouseEvent e) {
        OverlayType overlay = playingState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).mousePressed(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        OverlayType overlay = playingState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).mouseReleased(e);
        }
    }

    public void mouseMoved(MouseEvent e) {
        OverlayType overlay = playingState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).mouseMoved(e);
        }
    }

    public void mouseDragged(MouseEvent e) {
        OverlayType overlay = playingState.getActiveOverlay();
        if (overlay != null) {
            overlays.get(overlay).mouseDragged(e);
        }
    }

    public void reset() {
        overlays.get(OverlayType.SHOP).reset();
    }
}
