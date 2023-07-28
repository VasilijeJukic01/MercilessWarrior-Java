package platformer.view;

import platformer.controller.GameFocusListener;
import platformer.core.Game;

import javax.swing.*;
import java.net.URL;

@SuppressWarnings("FieldCanBeLocal")
public class GameFrame extends JFrame {

    private GamePanel gamePanel;

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

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
