package platformer.model.entities.effects.lighting;

import platformer.model.entities.enemies.Enemy;
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

import static platformer.constants.AnimConstants.LIGHT_ANIM_SPEED;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

/**
 * Manages the light effects in the game.
 * <p>
 * The light effects are used to create a dark atmosphere in the game. Effects are created by using gradients and images.
 */
public class LightManager {

    private final GameState gameState;

    private BufferedImage orangeLight, whiteLight, whiteRadialLight;
    private Ellipse2D playerLight;

    private Color[] gradient;
    private float[] gradientFraction;

    private int animTick = 0, alpha = 130, ambientAlpha = 130;
    private double pulseTimer = 0.0;

    private int flashDuration = 0;
    private float flashIntensity = 0.0f;

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

    /**
     * Initializes the gradient used for the light effects.
     * <p>
     * The gradient is used to create a smooth transition between the light and the dark areas.
     */
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
        if (alpha < ambientAlpha) updateFading();
        updatePulse();
        updateFlash();
    }

    private void updateFading() {
        animTick++;
        if (animTick >= LIGHT_ANIM_SPEED) {
            animTick = 0;
            alpha += 50;
            if (alpha > ambientAlpha) {
                alpha = ambientAlpha;
            }
        }
    }

    /**
     * Updates the pulse timer, which drives the sine wave for the light pulsation effect.
     * The timer increments in each frame, and resets to avoid growing indefinitely.
     */
    private void updatePulse() {
        pulseTimer += 0.02;
        if (pulseTimer > Math.PI * 2) {
            pulseTimer -= Math.PI * 2;
        }
    }

    /**
     * Manages the screen flash effect. If a flash is active, it rapidly decreases
     * the flash intensity over its duration, creating a quick burst of light that fades away.
     */
    private void updateFlash() {
        if (flashDuration > 0) {
            flashDuration--;
            flashIntensity *= 0.85f;
            if (flashDuration == 0) {
                flashIntensity = 0.0f;
            }
        }
    }

    // Render
    /**
     * Renders the light effects in the game. This includes the player's light, the light from candles, shops, and enemies.
     * It also handles the fading effect of the light.
     *
     * @param g Graphics object used for drawing the light effects.
     * @param xLevelOffset The horizontal offset of the level.
     * @param yLevelOffset The vertical offset of the level.
     */
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

            if (flashIntensity > 0) {
                g2d.setColor(new Color(1.0f, 1.0f, 1.0f, flashIntensity));
                g2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
            }
        }
        catch (Exception ignored) {}
    }

    /**
     * Applies light effects to various game objects such as the player, candles, shops, and enemies.
     *
     * @param g2d Graphics2D object used for drawing the light effects.
     * @param xLevelOffset The horizontal offset of the level.
     * @param yLevelOffset The vertical offset of the level.
     */
    private void glowObjects(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        playerFilter(g2d, xLevelOffset, yLevelOffset);
        candleFilter(g2d, xLevelOffset, yLevelOffset);
        gameState.getObjectManager().glowingRender(g2d, xLevelOffset, yLevelOffset);
        shopFilter(g2d, xLevelOffset, yLevelOffset);
        enemyFilter(g2d, xLevelOffset, yLevelOffset);
    }

    // Filters
    private void playerFilter(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        Player player = gameState.getPlayer();
        int playerX = (int) (player.getHitBox().x + player.getHitBox().width / 2 - xLevelOffset) - PLAYER_LIGHT_RADIUS;
        int playerY = (int) (player.getHitBox().y + player.getHitBox().height / 2 - yLevelOffset) - PLAYER_LIGHT_RADIUS;
        g2d.drawImage(whiteLight, playerX, playerY, null);
    }

    private void candleFilter(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        double pulseScale = 0.975 + 0.025 * Math.sin(pulseTimer);
        List<Candle> candles = gameState.getObjectManager().getObjects(Candle.class);
        for (Candle candle : candles) {
            int glowWidth = (int) (2 * CANDLE_LIGHT_RADIUS * pulseScale);
            int glowHeight = (int) (2 * CANDLE_LIGHT_RADIUS * pulseScale);
            int candleX = (int) (candle.getHitBox().x + candle.getHitBox().width / 2 - xLevelOffset) - glowWidth / 2;
            int candleY = (int) (candle.getHitBox().y + candle.getHitBox().height / 2 - yLevelOffset) - glowHeight / 2;
            g2d.drawImage(whiteRadialLight, candleX, candleY, glowWidth, glowHeight, null);
            gameState.getObjectManager().candleRender(g2d, xLevelOffset, yLevelOffset, candle);
            g2d.drawImage(orangeLight, candleX, candleY, glowWidth, glowHeight, null);
        }
    }

    private void shopFilter(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        List<Shop> shops = gameState.getObjectManager().getObjects(Shop.class);
        for (Shop shop : shops) {
            int shopX = (int) (shop.getHitBox().x + shop.getHitBox().width / 2 - xLevelOffset) - CANDLE_LIGHT_RADIUS;
            int shopY = (int) (shop.getHitBox().y + shop.getHitBox().height / 2 - yLevelOffset) - CANDLE_LIGHT_RADIUS;
            g2d.drawImage(orangeLight, shopX, shopY, null);
        }
    }

    private void enemyFilter(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        List<Enemy> enemies = gameState.getEnemyManager().getAllEnemies();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive() || !enemy.isVisible() || gameState.isDarkPhase()) continue;
            int enemyX = (int) (enemy.getHitBox().x + enemy.getHitBox().width / 2 - xLevelOffset) - PLAYER_LIGHT_RADIUS;
            int enemyY = (int) (enemy.getHitBox().y + enemy.getHitBox().height / 2 - yLevelOffset) - PLAYER_LIGHT_RADIUS;
            g2d.drawImage(whiteLight, enemyX, enemyY, null);
        }
    }

    // Player light
    /**
     * Fills a given area with a radial gradient to create a light effect.
     *
     * @param g2d Graphics2D object used for drawing the light effects.
     * @param area The area to be filled with the radial gradient.
     * @param radius The radius of the radial gradient.
     */
    private void fillWithRadialGradient(Graphics2D g2d, Area area, int radius) {
        RadialGradientPaint gradientPaint = new RadialGradientPaint(
                new Point(area.getBounds().x + radius, area.getBounds().y + radius),
                radius + 1,
                gradientFraction,
                gradient);
        g2d.setPaint(gradientPaint);
        g2d.fill(area);
    }

    /**
     * Creates the area of the player's light based on the player's position and the level offsets.
     *
     * @param xLevelOffset The horizontal offset of the level.
     * @param yLevelOffset The vertical offset of the level.
     * @return The area of the player's light.
     */
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

    /**
     * Calculates the area of the player's light based on the player's position and the level offsets.
     *
     * @param xLevelOffset The horizontal offset of the level.
     * @param yLevelOffset The vertical offset of the level.
     * @return The area of the player's light.
     */
    private Area calculatePlayerLight(int xLevelOffset, int yLevelOffset) {
        Player player = gameState.getPlayer();
        int playerX = (int) (player.getHitBox().x + player.getHitBox().width / 2 - xLevelOffset);
        int playerY = (int) (player.getHitBox().y + player.getHitBox().height / 2 - yLevelOffset);
        playerLight.setFrame(playerX - PLAYER_LIGHT_RADIUS, playerY - PLAYER_LIGHT_RADIUS, playerLight.getWidth(), playerLight.getHeight());
        return new Area(playerLight);
    }

    // Ambient
    /**
     * Sets the base ambient darkness level of the screen.
     *
     * @param darkness The alpha value for the dark overlay (0-255). Higher is darker.
     */
    public void setAmbientDarkness(int darkness) {
        this.ambientAlpha = darkness;
        this.alpha = darkness;
    }

    // Setters
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

}
