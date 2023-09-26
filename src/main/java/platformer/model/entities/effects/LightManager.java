package platformer.model.entities.effects;

import platformer.model.entities.player.Player;
import platformer.model.gameObjects.objects.Candle;
import platformer.model.gameObjects.objects.Shop;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

public class LightManager {

    private final GameState gameState;

    private BufferedImage orangeLight, whiteLight, whiteRadialLight;

    private Ellipse2D playerLight;

    private Color[] gradient;
    private float[] gradientFraction;

    private final int animSpeed = 20;
    private int animTick = 0, alpha = 130;

    public LightManager(GameState gameState) {
        this.gameState = gameState;
        initLightImages();
        initGradient();
        initLights();
    }

    // Init
    private void initLightImages() {
        this.orangeLight = Utils.getInstance().importImage(ORANGE_GLOW, 2 * CANDLE_LIGHT_RADIUS, 2 * CANDLE_LIGHT_RADIUS);
        BufferedImage whiteGlow = Utils.getInstance().importImage(WHITE_GLOW, 2 * PLAYER_LIGHT_RADIUS, 2 * PLAYER_LIGHT_RADIUS);
        BufferedImage whiteRadialGlow = Utils.getInstance().importImage(WHITE_RADIAL_GLOW, 2 * CANDLE_LIGHT_RADIUS, 2 * CANDLE_LIGHT_RADIUS);
        RescaleOp rescaleOp = new RescaleOp(new float[]{1.0f, 1.0f, 1.0f, 0.5f}, new float[4], null);
        this.whiteLight = rescaleOp.filter(whiteGlow, null);
        this.whiteRadialLight = rescaleOp.filter(whiteRadialGlow, null);
    }

    private void initGradient() {
        this.gradient = new Color[5];
        this.gradientFraction = new float[5];
        gradient[0] = new Color(0, 0, 0, 0);
        gradient[1] = new Color(0, 0, 0, 10);
        gradient[2] = new Color(0, 0, 0, 20);
        gradient[3] = new Color(0, 0, 0, 40);
        gradient[4] = new Color(0, 0, 0, alpha);

        gradientFraction[0] = 0f;
        gradientFraction[1] = 0.25f;
        gradientFraction[2] = 0.5f;
        gradientFraction[3] = 0.75f;
        gradientFraction[4] = 1f;
    }

    private void initLights() {
        this.playerLight = new Ellipse2D.Double(0, 0, 2 * PLAYER_LIGHT_RADIUS, 2 * PLAYER_LIGHT_RADIUS);
    }

    // Update
    public void update() {
        if (alpha < 130) updateFading();
    }

    private void updateFading() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            alpha += 20;
        }
    }

    // Render
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        gradient[4] = new Color(0, 0, 0, alpha);
        try {
            Graphics2D g2d = (Graphics2D) g;
            Area darkLayer = new Area(new Rectangle(0, 0, GAME_WIDTH, GAME_HEIGHT));

            darkLayer.subtract(getPlayerLightArea(xLevelOffset, yLevelOffset));
            fillWithRadialGradient(g2d, getPlayerLightArea(xLevelOffset, yLevelOffset), PLAYER_LIGHT_RADIUS);

            g2d.setColor(new Color(0, 0, 0, alpha));
            g2d.fill(darkLayer);
            glowObjects(g2d, xLevelOffset, yLevelOffset);
        }
        catch (Exception ignored) {}
    }

    private void glowObjects(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        // Player filter
        Player player = gameState.getPlayer();
        int playerX = (int) (player.getHitBox().x + player.getHitBox().width / 2 - xLevelOffset) - PLAYER_LIGHT_RADIUS;
        int playerY = (int) (player.getHitBox().y + player.getHitBox().height / 2 - yLevelOffset) - PLAYER_LIGHT_RADIUS;
        g2d.drawImage(whiteLight, playerX, playerY, null);

        // Candle filter
        List<Candle> candles = gameState.getObjectManager().getObjects(Candle.class);
        for (Candle candle : candles) {
            int candleX = (int) (candle.getHitBox().x + candle.getHitBox().width / 2 - xLevelOffset) - CANDLE_LIGHT_RADIUS;
            int candleY = (int) (candle.getHitBox().y + candle.getHitBox().height / 2 - yLevelOffset) - CANDLE_LIGHT_RADIUS;
            g2d.drawImage(whiteRadialLight, candleX, candleY, null);
            gameState.getObjectManager().candleRender(g2d, xLevelOffset, yLevelOffset, candle);
            g2d.drawImage(orangeLight, candleX, candleY, null);
        }

        gameState.getObjectManager().glowingRender(g2d, xLevelOffset, yLevelOffset);

        // Shop filter
        List<Shop> shops = gameState.getObjectManager().getObjects(Shop.class);
        for (Shop shop : shops) {
            int shopX = (int) (shop.getHitBox().x + shop.getHitBox().width / 2 - xLevelOffset) - CANDLE_LIGHT_RADIUS;
            int shopY = (int) (shop.getHitBox().y + shop.getHitBox().height / 2 - yLevelOffset) - CANDLE_LIGHT_RADIUS;
            g2d.drawImage(orangeLight, shopX, shopY, null);
        }
    }

    private void fillWithRadialGradient(Graphics2D g2d, Area area, int radius) {
        RadialGradientPaint gradientPaint = new RadialGradientPaint(
                new Point(area.getBounds().x + radius, area.getBounds().y + radius),
                radius + 1,
                gradientFraction,
                gradient);
        g2d.setPaint(gradientPaint);
        g2d.fill(area);
    }

    private Area getPlayerLightArea(int xLevelOffset, int yLevelOffset) {
        Area playerArea = calculatePlayerLight(xLevelOffset, yLevelOffset);

        int xPos = playerArea.getBounds().x + PLAYER_LIGHT_RADIUS;
        int yPos = playerArea.getBounds().y + PLAYER_LIGHT_RADIUS;
        RadialGradientPaint paint = new RadialGradientPaint(xPos, yPos, 2 * PLAYER_LIGHT_RADIUS, gradientFraction, gradient);
        Graphics2D g2d = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        g2d.setPaint(paint);
        g2d.fill(playerArea);

        return new Area(playerArea);
    }

    private Area calculatePlayerLight(int xLevelOffset, int yLevelOffset) {
        Player player = gameState.getPlayer();
        int playerX = (int) (player.getHitBox().x + player.getHitBox().width / 2 - xLevelOffset);
        int playerY = (int) (player.getHitBox().y + player.getHitBox().height / 2 - yLevelOffset);
        playerLight.setFrame(playerX - PLAYER_LIGHT_RADIUS, playerY - PLAYER_LIGHT_RADIUS, playerLight.getWidth(), playerLight.getHeight());
        return new Area(playerLight);
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

}
