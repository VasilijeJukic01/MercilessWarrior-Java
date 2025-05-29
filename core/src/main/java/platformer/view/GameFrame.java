package platformer.view;

import platformer.controller.GameFocusListener;
import platformer.core.Game;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * The main frame of the game.
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
    }

    private void initFrame() {
        this.setTitle("Merciless Warrior");
        URL iconURL = getClass().getResource("/images/icon.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            setIconImage(icon.getImage());
        }
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initPanel(Game game) {
        this.gamePanel = new GamePanel(game);
        this.gamePanel.requestFocus();
        add(gamePanel);
        pack();
    }

    /**
     * Toggles the game frame between full screen and windowed mode.
     * If entering full screen, it saves the current window bounds and panel size.
     * If exiting full screen, it restores the previous bounds and panel size.
     */
    public void toggleFullScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        if (!isFullScreen) {
            previousWindowBounds = getBounds();
            previousPanelSize = gamePanel.getCurrentSize();

            setResizable(true);
            dispose();
            setUndecorated(true);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;
            int screenHeight = screenSize.height;

            if (gd.isFullScreenSupported()) {
                gamePanel.updateSize(screenWidth, screenHeight);
                gd.setFullScreenWindow(this);
            }
            else {
                gamePanel.updateSize(screenWidth, screenHeight);
                setBounds(0, 0, screenWidth, screenHeight);
                setVisible(true);
            }

            isFullScreen = true;
        }
        else {
            if (gd.isFullScreenSupported()) gd.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            setResizable(false);

            if (previousPanelSize != null)
                gamePanel.updateSize(previousPanelSize.width, previousPanelSize.height);

            if (previousWindowBounds != null) setBounds(previousWindowBounds);

            setVisible(true);
            pack();
            isFullScreen = false;
        }
        gamePanel.requestFocus();
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
