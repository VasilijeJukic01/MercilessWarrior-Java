package platformer.model.entities.effects;

import platformer.model.entities.player.Player;
import platformer.state.GameState;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import static platformer.constants.Constants.*;

public class LightManager {

    private final GameState gameState;

    private Ellipse2D playerLight;
    private Color[] gradient;
    private float[] gradientFraction;

    public LightManager(GameState gameState) {
        this.gameState = gameState;
        initGradient();
        initPlayerLight();
    }

    // Init
    private void initGradient() {
        this.gradient = new Color[5];
        this.gradientFraction = new float[5];
        gradient[0] = new Color(0, 0, 0, 0);
        gradient[1] = new Color(0, 0, 0, 10);
        gradient[2] = new Color(0, 0, 0, 20);
        gradient[3] = new Color(0, 0, 0, 40);
        gradient[4] = new Color(0, 0, 0, 120);

        gradientFraction[0] = 0f;
        gradientFraction[1] = 0.25f;
        gradientFraction[2] = 0.5f;
        gradientFraction[3] = 0.75f;
        gradientFraction[4] = 1f;
    }

    private void initPlayerLight() {
        this.playerLight = new Ellipse2D.Double(-PLAYER_LIGHT_RADIUS, -PLAYER_LIGHT_RADIUS, 2 * PLAYER_LIGHT_RADIUS, 2 * PLAYER_LIGHT_RADIUS);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        try {
            renderPlayerLight(g, xLevelOffset, yLevelOffset);
        }
        catch (Exception ignored) {}
    }

    // Render
    private void renderPlayerLight(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g;

        Area darkLayer = new Area(new Rectangle(0, 0, GAME_WIDTH, GAME_HEIGHT));
        Area playerArea = calculatePlayerLight(xLevelOffset, yLevelOffset);

        int xPos = playerArea.getBounds().x + PLAYER_LIGHT_RADIUS;
        int yPos = playerArea.getBounds().y + PLAYER_LIGHT_RADIUS;
        RadialGradientPaint paint = new RadialGradientPaint(xPos, yPos, 2 * PLAYER_LIGHT_RADIUS, gradientFraction, gradient);
        g2d.setPaint(paint);

        g2d.fill(darkLayer);
    }

    private Area calculatePlayerLight(int xLevelOffset, int yLevelOffset) {
        Player player = gameState.getPlayer();
        int playerX = (int) (player.getHitBox().x + player.getHitBox().width / 2 - xLevelOffset);
        int playerY = (int) (player.getHitBox().y + player.getHitBox().height / 2 - yLevelOffset);
        playerLight.setFrame(playerX - PLAYER_LIGHT_RADIUS, playerY - PLAYER_LIGHT_RADIUS, playerLight.getWidth(), playerLight.getHeight());
        return new Area(playerLight);
    }

}
