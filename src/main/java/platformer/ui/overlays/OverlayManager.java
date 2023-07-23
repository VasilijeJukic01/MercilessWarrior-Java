package platformer.ui.overlays;

import platformer.state.PlayingState;

import java.awt.*;
import java.awt.event.MouseEvent;

public class OverlayManager {

    private final PauseOverlay pauseOverlay;
    private final GameOverOverlay gameOverOverlay;
    private final ShopOverlay shopOverlay;
    private final BlacksmithOverlay blacksmithOverlay;

    public OverlayManager(PlayingState playingState) {
        this.pauseOverlay = new PauseOverlay(playingState.getGame());
        this.gameOverOverlay = new GameOverOverlay(playingState.getGame());
        this.shopOverlay = new ShopOverlay(playingState);
        this.blacksmithOverlay = new BlacksmithOverlay(playingState);
    }

    // Core
    public void update(String overlay) {
        switch (overlay) {
            case "PAUSE": pauseOverlay.update(); break;
            case "GAME_OVER": gameOverOverlay.update(); break;
            case "SHOP": shopOverlay.update(); break;
            case "BLACKSMITH": blacksmithOverlay.update(); break;
            default: break;
        }
    }

    public void render(Graphics g, boolean pause, boolean gameOver, boolean shop, boolean blacksmith) {
        if (pause) pauseOverlay.render(g);
        if (gameOver) gameOverOverlay.render(g);
        if (shop) shopOverlay.render(g);
        else if (blacksmith) blacksmithOverlay.render(g);
    }

    public void mousePressed(MouseEvent e, boolean pause, boolean gameOver, boolean shop, boolean blacksmith) {
        if (pause) pauseOverlay.mousePressed(e);
        else if (gameOver) gameOverOverlay.mousePressed(e);
        else if (shop) shopOverlay.mousePressed(e);
        else if (blacksmith) blacksmithOverlay.mousePressed(e);
    }

    public void mouseReleased(MouseEvent e, boolean pause, boolean gameOver, boolean shop, boolean blacksmith) {
        if (pause) pauseOverlay.mouseReleased(e);
        else if (gameOver) gameOverOverlay.mouseReleased(e);
        else if (shop) shopOverlay.mouseReleased(e);
        else if (blacksmith) blacksmithOverlay.mouseReleased(e);
    }

    public void mouseMoved(MouseEvent e, boolean pause, boolean gameOver, boolean shop, boolean blacksmith) {
        if (pause) pauseOverlay.mouseMoved(e);
        else if (gameOver) gameOverOverlay.mouseMoved(e);
        else if (shop) shopOverlay.mouseMoved(e);
        else if (blacksmith) blacksmithOverlay.mouseMoved(e);
    }

    public void mouseDragged(MouseEvent e, boolean pause) {
        if (pause) pauseOverlay.mouseDragged(e);
    }

    public void reset() {
        shopOverlay.reset();
    }
}
