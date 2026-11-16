package platformer.ui.overlays;

import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.core.Game;
import platformer.state.types.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Class that represents the overlay that is displayed when the player dies.
 */
public class GameOverOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final Game game;
    private SmallButton retryBtn, menuBtn;

    public GameOverOverlay(Game game) {
        this.game = game;
        loadButtons();
    }

    private void loadButtons() {
        this.retryBtn = new SmallButton(RETRY_X, RETRY_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.RETRY);
        this.menuBtn = new SmallButton(MENU_X, MENU_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    // Core
    @Override
    public void update() {
        retryBtn.update();
        menuBtn.update();
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        OverlayLayer.getInstance().renderOverlay(g);
        renderTexts(g);
        renderButtons(g);
    }

    private void renderTexts(Graphics g) {
        UIButtonRenderer.drawRunicHeader(g, "YOU DIED", OVERLAY_X + OVERLAY_WID / 2, DEAD_TEXT_Y);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(UIButtonRenderer.getFont(14f * SCALE));
        g2d.setColor(new Color(218, 226, 224));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString("RESPAWN", RETRY_X + (CRE_BTN_SIZE - fm.stringWidth("RESPAWN")) / 2, RESPAWN_TEXT_Y + (int) (18 * SCALE));
        g2d.drawString("MENU", MENU_X + (CRE_BTN_SIZE - fm.stringWidth("MENU")) / 2, MENU_TEXT_Y + (int) (18 * SCALE));
        g2d.dispose();
    }

    private void renderButtons(Graphics g) {
        retryBtn.render(g);
        menuBtn.render(g);
    }

    @Override
    public void reset() {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, retryBtn)) retryBtn.setMousePressed(true);
        if (isMouseInButton(e, menuBtn)) menuBtn.setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, retryBtn) && retryBtn.isMousePressed()) {
            // Valid cast (to enter this state, the current state must be a GameState)
            ((GameState) game.getCurrentState()).setRespawning(true);
            game.reset();
            Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);
        }
        if(isMouseInButton(e, menuBtn) && menuBtn.isMousePressed()) {
            game.reset();
            game.startMenuState();
        }
        resetButtons();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        menuBtn.setMouseOver(false);
        retryBtn.setMouseOver(false);
        if (isMouseInButton(e, retryBtn)) retryBtn.setMouseOver(true);
        if (isMouseInButton(e, menuBtn)) menuBtn.setMouseOver(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton abstractButton) {
        return abstractButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    private void resetButtons() {
        menuBtn.resetMouseSet();
        retryBtn.resetMouseSet();
    }

}
