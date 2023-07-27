package platformer.view;

import platformer.controller.GameKeyListener;
import platformer.controller.GameMouseListener;
import platformer.core.Game;

import javax.swing.*;
import java.awt.*;

import static platformer.constants.Constants.*;

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
        Dimension dimension = new Dimension(GAME_WIDTH, GAME_HEIGHT);
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
        g.setFont(new Font("Arial", Font.BOLD, (int)(10*SCALE)));
        g.drawString(game.getAccount().getName(), (int)(1.5*SCALE), (int)(10*SCALE));
        g.drawString("FPS: "+game.getCurrentFps(), (int)(1.5*SCALE), (int)(20*SCALE));
        g.drawString("UPS: "+game.getCurrentUpdates(), (int)(50*SCALE), (int)(20*SCALE));
    }

    public Game getGame() {
        return game;
    }
}
