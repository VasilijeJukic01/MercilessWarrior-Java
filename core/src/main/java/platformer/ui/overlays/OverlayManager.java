package platformer.ui.overlays;

import platformer.state.GameState;
import platformer.state.PlayingState;
import platformer.ui.dialogue.DialogueOverlay;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the overlays that are displayed on top of the game screen.
 * <p>
 * Overlays are used to display additional information or options to the player.
 * The overlay that is displayed is determined by the current playing state.
 */
public class OverlayManager {

    private final GameState gameState;
    private final Map<PlayingState, Overlay<MouseEvent, KeyEvent, Graphics>> overlays;

    public OverlayManager(GameState gameState) {
        this.gameState = gameState;
        this.overlays = new HashMap<>();

        this.overlays.put(PlayingState.PAUSE, new PauseOverlay(gameState.getGame(), gameState));
        this.overlays.put(PlayingState.GAME_OVER, new GameOverOverlay(gameState.getGame()));
        this.overlays.put(PlayingState.SHOP, new ShopOverlay(gameState));
        this.overlays.put(PlayingState.BLACKSMITH, new BlacksmithOverlay(gameState));
        this.overlays.put(PlayingState.DIALOGUE, new DialogueOverlay());
        this.overlays.put(PlayingState.SAVE, new SaveGameOverlay(gameState));
        this.overlays.put(PlayingState.INVENTORY, new InventoryOverlay(gameState));
        this.overlays.put(PlayingState.CRAFTING, new CraftingOverlay(gameState));
        this.overlays.put(PlayingState.LOOTING, new LootingOverlay(gameState));
        this.overlays.put(PlayingState.QUEST, new QuestOverlay(gameState));
        this.overlays.put(PlayingState.MINIMAP, new MinimapOverlay(gameState));
    }

    // Core
    public void update(PlayingState playingState) {
        Overlay<MouseEvent, KeyEvent, Graphics> overlay = overlays.get(playingState);
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

    public void keyPressed(KeyEvent e) {
        PlayingState overlay = gameState.getActiveState();
        if (overlay != null) overlays.get(overlay).keyPressed(e);
    }

    public void reset() {
        overlays.get(PlayingState.SHOP).reset();
        overlays.get(PlayingState.CRAFTING).reset();
        overlays.get(PlayingState.DIALOGUE).reset();
    }

    public Map<PlayingState, Overlay<MouseEvent, KeyEvent, Graphics>> getOverlays() {
        return overlays;
    }
}
