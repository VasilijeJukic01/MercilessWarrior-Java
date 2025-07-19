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
 * The primary drawing surface for the game, extending {@link JPanel}.
 * <p>
 * This panel is responsible for:
 * <ul>
 *     <li>Setting up and maintaining the drawing area with a fixed or dynamic size.</li>
 *     <li>Attaching key and mouse listeners ({@link GameKeyListener}, {@link GameMouseListener}) to capture user input
 *         and forward it to the main {@link Game} logic.</li>
 *     <li>Overriding {@code paintComponent} to trigger the game's main rendering loop.</li>
 *     <li>Displaying debug information such as FPS (Frames Per Second) and UPS (Updates Per Second).</li>
 * </ul>
 * The panel's size is managed to support both windowed and fullscreen modes, with a scaling
 * mechanism handled within the {@link Game#render(Graphics)} method.
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

    /**
     * Attaches the necessary input listeners to this panel.
     */
    private void initListeners() {
        GameMouseListener mouseListener = new GameMouseListener(this);
        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseListener);
        this.addKeyListener(new GameKeyListener(this));
    }

    /**
     * Configures the panel to be focusable so it can receive keyboard input.
     */
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
     * Updates the panel's size dimensions. This is crucial for correctly handling the
     * transition between windowed and fullscreen modes.
     *
     * @param width  The new width of the panel.
     * @param height The new height of the panel.
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

    /**
     * The main rendering method, called by Swing's repaint manager.
     * This method delegates the actual game rendering to the {@link Game#render(Graphics)} method
     * and then draws any additional debug information on top.
     *
     * @param g The {@link Graphics} context provided by the Swing framework.
     */
    @Override
    protected void paintComponent(Graphics g) {
        this.requestFocus(true);
        super.paintComponent(g);
        renderGame(g);
        renderInfo(g);
    }

    /**
     * Delegates the core game rendering to the main Game object.
     *
     * @param g The graphics context.
     */
    private void renderGame(Graphics g) {
        game.render(g);
    }

    /**
     * Renders debug and informational text, such as FPS, UPS, and the current player's name.
     *
     * @param g The graphics context.
     */
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
