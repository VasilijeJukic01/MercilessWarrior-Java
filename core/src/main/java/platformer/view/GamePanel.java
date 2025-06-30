package platformer.view;

import platformer.controller.GameKeyListener;
import platformer.controller.GameMouseListener;
import platformer.core.Framework;
import platformer.core.Game;

import javax.swing.*;
import java.awt.*;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Responsible for rendering the game.
 */
@SuppressWarnings("FieldCanBeLocal")
public class GamePanel extends JPanel {

    private final Game game;
    private Dimension currentSize;

    public GamePanel(Game game) {
        this.game = game;
        initListeners();
        initFocus();
        initPanelSize();
        setFocusTraversalKeysEnabled(false);
    }

    private void initListeners() {
        GameMouseListener mouseListener = new GameMouseListener(this);
        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseListener);
        this.addKeyListener(new GameKeyListener(this));
    }

    private void initFocus() {
        this.setFocusable(true);
    }

    private void initPanelSize() {
        this.currentSize = new Dimension(GAME_WIDTH, GAME_HEIGHT);
        this.setMinimumSize(currentSize);
        this.setPreferredSize(currentSize);
        this.setMaximumSize(currentSize);
    }

    /**
     * Updates the panel size. Used when switching between windowed and fullscreen modes.
     * @param width The new width
     * @param height The new height
     */
    public void updateSize(int width, int height) {
        this.currentSize = new Dimension(width, height);
        this.setMinimumSize(currentSize);
        this.setPreferredSize(currentSize);
        this.setMaximumSize(currentSize);
        this.setSize(currentSize);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.requestFocus(true);
        super.paintComponent(g);
        renderGame(g);
        renderInfo(g);
    }

    private void renderGame(Graphics g) {
        game.render(g);
    }

    private void renderInfo(Graphics g) {
        g.setColor(INFO_TXT_COLOR);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(Framework.getInstance().getAccount().getName(), PLAYER_NAME_X, PLAYER_NAME_Y);
        g.drawString("FPS: "+game.getCurrentFps(), FPS_X, FPS_Y);
        g.drawString("UPS: "+game.getCurrentUpdates(), UPS_X, UPS_Y);
    }

    public Game getGame() {
        return game;
    }

    public Dimension getCurrentSize() {
        return currentSize;
    }
}
