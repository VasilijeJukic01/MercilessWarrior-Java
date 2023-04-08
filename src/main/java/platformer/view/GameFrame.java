package platformer.view;

import platformer.controller.GameFocusListener;
import platformer.core.Game;

import javax.swing.*;

@SuppressWarnings("FieldCanBeLocal")
public class GameFrame extends JFrame {

    private final GamePanel gamePanel;

    public GameFrame(Game game) {
        this.setTitle("Merciless Warrior");
        this.setIconImage(new ImageIcon("src/main/resources/images/icon.png").getImage());
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.gamePanel = new GamePanel(game);
        this.gamePanel.requestFocus();

        this.add(gamePanel);
        this.pack();

        this.addWindowFocusListener(new GameFocusListener(game));
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
