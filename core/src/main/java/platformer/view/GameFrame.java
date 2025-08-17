package platformer.view;

import platformer.controller.GameFocusListener;
import platformer.core.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

/**
 * The main window container for the game.
 * <p>
 * This class extends {@link JFrame} and serves as the top-level frame of the application.
 * It is responsible for:
 * <ul>
 *     <li>Initializing the window with a title, icon, and default close operation.</li>
 *     <li>Holding and displaying the {@link GamePanel}, which is the primary drawing surface.</li>
 *     <li>Managing the transition between windowed and fullscreen modes.</li>
 *     <li>Attaching a {@link GameFocusListener} to handle window focus events, which is
 *         important for pausing the game or resetting player input when the window loses focus.</li>
 * </ul>
 */
@SuppressWarnings("FieldCanBeLocal")
public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private boolean isFullScreen = false;
    private Rectangle previousWindowBounds;
    private Dimension previousPanelSize;

    public GameFrame(Game game) {
        initFrame();
        initPanel(game);
        this.addWindowFocusListener(new GameFocusListener(game));
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                game.getCurrentState().exit();
                System.exit(0);
            }
        });
    }

    /**
     * Initializes the frame's properties such as title, icon, and default close behavior.
     */
    private void initFrame() {
        this.setTitle("Merciless Warrior");
        URL iconURL = getClass().getResource("/base/icon.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
        }
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Initializes and adds the {@link GamePanel} to this frame.
     * The panel is where all game rendering occurs.
     *
     * @param game The main {@link Game} instance.
     */
    private void initPanel(Game game) {
        this.gamePanel = new GamePanel(game);
        this.gamePanel.requestFocus();
        add(gamePanel);
        pack();
    }

    /**
     * Toggles the game frame between fullscreen and windowed mode by delegating
     * to the appropriate helper methods. This method acts as the public entry point
     * and controller for the fullscreen state change.
     */
    public void toggleFullScreen() {
        isFullScreen = !isFullScreen;
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (isFullScreen) enterFullScreen(gd);
        else exitFullScreen(gd);
        gamePanel.requestFocusInWindow();
    }

    /**
     * Handles the logic for transitioning the window into fullscreen mode.
     *
     * @param device The graphics device to use for fullscreen operations.
     */
    private void enterFullScreen(GraphicsDevice device) {
        saveCurrentWindowState();
        dispose();
        setUndecorated(true);
        setResizable(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        gamePanel.updateSize(screenSize.width, screenSize.height);

        if (device.isFullScreenSupported()) device.setFullScreenWindow(this);
        else {
            // Fallback for systems that don't support exclusive fullscreen mode.
            setBounds(device.getDefaultConfiguration().getBounds());
            setVisible(true);
        }
    }

    /**
     * Handles the logic for transitioning the window out of fullscreen and back to windowed mode.
     *
     * @param device The graphics device used for fullscreen operations.
     */
    private void exitFullScreen(GraphicsDevice device) {
        if (device.isFullScreenSupported()) device.setFullScreenWindow(null);
        dispose();
        setUndecorated(false);
        setResizable(false);
        setVisible(true);
        restorePreviousWindowState();
    }

    /**
     * Saves the current size and position of the window and panel before entering fullscreen.
     */
    private void saveCurrentWindowState() {
        previousWindowBounds = getBounds();
        previousPanelSize = gamePanel.getCurrentSize();
    }

    /**
     * Restores the window and panel to their saved size and position after exiting fullscreen.
     */
    private void restorePreviousWindowState() {
        if (previousPanelSize != null)
            gamePanel.updateSize(previousPanelSize.width, previousPanelSize.height);
        if (previousWindowBounds != null) setBounds(previousWindowBounds);
        pack();
    }

    // Getters
    public boolean isFullScreen() {
        return isFullScreen;
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
