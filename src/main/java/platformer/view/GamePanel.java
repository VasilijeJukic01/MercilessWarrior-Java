package platformer.view;

import platformer.model.Tiles;
import platformer.controller.GameKeyListener;
import platformer.controller.GameMouseListener;
import platformer.core.Game;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("FieldCanBeLocal")
public class GamePanel extends JPanel {

    private final Game game;

    public GamePanel(Game game) {
        this.game = game;

        GameMouseListener mouseListener = new GameMouseListener(this);
        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseListener);
        this.addKeyListener(new GameKeyListener(this));
        this.setFocusable(true);

        initPanelSize();
    }

    private void initPanelSize() {
        Dimension dimension = new Dimension((int)Tiles.GAME_WIDTH.getValue(), (int)Tiles.GAME_HEIGHT.getValue());
        this.setMinimumSize(dimension);
        this.setPreferredSize(dimension);
        this.setMinimumSize(dimension);
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.requestFocus(true);
        super.paintComponent(g);
        game.render(g);
        g.setColor(new Color(255, 255, 255));
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("FPS: "+game.getCurrentFps(), 3, 20);
        g.drawString("UPS: "+game.getCurrentUpdates(), 100, 20);
    }

    public Game getGame() {
        return game;
    }
}
