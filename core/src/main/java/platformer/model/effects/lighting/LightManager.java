package platformer.model.effects.lighting;

import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.enemies.Enemy;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.objects.*;
import platformer.model.gameObjects.objects.Container;
import platformer.state.GameState;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.List;

import static platformer.constants.AnimConstants.LIGHT_ANIM_SPEED;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

/**
 * Manages all screen-wide lighting and color effects for the game.
 * <p>
 * This class is responsible for creating a dynamic and atmospheric environment by orchestrating several layers of visual effects:
 * <ul>
 *     <li><b>Ambient Darkness:</b> A base layer of darkness that changes with the in-game time.</li>
 *     <li><b>Dynamic Light Sources:</b> Uses a highly optimized "lightmap" technique to render soft, feathered light from the player, candles, and other objects.</li>
 *     <li><b>Day/Night Cycle Tinting:</b> Applies a full-screen color tint that smoothly transitions between dawn, day, dusk, and night.</li>
 *     <li><b>Glow and Bloom Effects:</b> Renders cosmetic glows on top of light sources to make them feel brighter and more intense.</li>
 *     <li><b>Special Effects:</b> Handles temporary, full-screen flashes.</li>
 * </ul>
 * @see TimeCycleManager
 * @see GameState
 */
public class LightManager {

    private final GameState gameState;

    /** An off-screen buffer where all light sources are erased from a darkness layer before being drawn. */
    private final BufferedImage lightmap;
    private static final int LIGHTMAP_SCALE = 2;

    private BufferedImage orangeLight, whiteRadialLight;
    private BufferedImage playerLightTexture, enemyLightTexture, candleLightTexture, objectLightTexture;

    private Color ambientDarkness;
    private int animTick = 0, fadeBackAnimTick = 0;
    private int currentAmbientAlpha = 130, ambientAlpha = 130;
    private double pulseTimer = 0.0;
    private boolean ambientDarknessOverridden = false;
    private boolean isFadingBack = false;

    private int flashDuration = 0;
    private float flashIntensity = 0.0f;
    private Color filterColor = null;

    // TimeCycle
    private double currentTimeOfDay = 0.5;
    private Color timeCycleTintColor = new Color(0, 0, 0, 0);
    private static final float MIN_CANDLE_GLOW_ALPHA = 0.2f;
    private static final float MAX_CANDLE_GLOW_ALPHA = 1.0f;

    public LightManager(GameState gameState) {
        this.gameState = gameState;
        this.lightmap = new BufferedImage(GAME_WIDTH / LIGHTMAP_SCALE, GAME_HEIGHT / LIGHTMAP_SCALE, BufferedImage.TYPE_INT_ARGB);
        this.ambientDarkness = new Color(0, 0, 0, currentAmbientAlpha);
        initLightImages();
        initLightTextures();
    }

    // Init
    private void initLightImages() {
        this.orangeLight = ImageUtils.importImage(ORANGE_GLOW, 2 * CANDLE_LIGHT_RADIUS, 2 * CANDLE_LIGHT_RADIUS);
        BufferedImage whiteRadialGlow = ImageUtils.importImage(WHITE_RADIAL_GLOW, 2 * CANDLE_LIGHT_RADIUS, 2 * CANDLE_LIGHT_RADIUS);
        RescaleOp rescaleOp = new RescaleOp(new float[]{1.0f, 1.0f, 1.0f, 0.5f}, new float[4], null);
        this.whiteRadialLight = rescaleOp.filter(whiteRadialGlow, null);
    }

    /**
     * Pre-renders the circular light gradients into reusable BufferedImage textures.
     * This is a core optimization that avoids creating expensive {@link RadialGradientPaint} objects every frame, significantly improving rendering performance.
     */
    private void initLightTextures() {
        int playerDiameter = (int)(PLAYER_LIGHT_RADIUS * 2.5f);
        playerLightTexture = createLightTexture(playerDiameter, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(1.0f, 1.0f, 1.0f, 0f)});

        int enemyDiameter = (int)(PLAYER_LIGHT_RADIUS * 2.0f);
        enemyLightTexture = createLightTexture(enemyDiameter, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(1.0f, 1.0f, 1.0f, 0f)});

        int candleDiameter = (int)(CANDLE_LIGHT_RADIUS * 2.6f);
        candleLightTexture = createLightTexture(candleDiameter, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(1.0f, 1.0f, 1.0f, 0f)});

        int objectDiameter = (int)(CANDLE_LIGHT_RADIUS * 1.6f);
        objectLightTexture = createLightTexture(objectDiameter, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(1.0f, 1.0f, 1.0f, 0f)});
    }

    /**
     * A helper method for {@link #initLightTextures()} that creates a single circular gradient texture.
     *
     * @param diameter The diameter of the light texture.
     * @param fractions The fractions for the gradient stops (e.g., {0f, 1f}).
     * @param colors The colors corresponding to the gradient stops.
     * @return A BufferedImage containing the pre-rendered light gradient.
     */
    private BufferedImage createLightTexture(int diameter, float[] fractions, Color[] colors) {
        BufferedImage texture = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Point2D center = new Point2D.Float(diameter / 2.0f, diameter / 2.0f);
        float radius = diameter / 2.0f;
        RadialGradientPaint p = new RadialGradientPaint(center, radius, fractions, colors);
        g2d.setPaint(p);
        g2d.fillRect(0, 0, diameter, diameter);
        g2d.dispose();
        return texture;
    }

    // Update
    /**
     * The main update loop for the LightManager.
     * It synchronizes with the {@link TimeCycleManager}.
     *
     * @param timeManager The manager that provides the current in-game time.
     */
    public void update(TimeCycleManager timeManager) {
        this.currentTimeOfDay = timeManager.getGameTimeNormalized();
        calculateTimeCycleEffects(timeManager);
        updateAmbientDarknessAndEffects();
        updatePulse();
        updateFlash();
    }

    /**
     * Calculates the natural, baseline color tint and ambient darkness for the current time of day.
     * It interpolates between predefined keyframes (Night, Dawn, Day, Dusk).
     *
     * @param timeManager The manager providing the current time.
     */
    private void calculateTimeCycleEffects(TimeCycleManager timeManager) {
        if (ambientDarknessOverridden) return;
        double time = timeManager.getGameTimeNormalized();
        int tintAlpha, darknessAlpha;
        Color tint;

        Color nightTint = new Color(0, 5, 40, 10);
        Color dawnTint = new Color(130, 140, 200);
        Color dayTint = new Color(255, 255, 230);
        Color duskTint = new Color(251, 169, 111);

        // Night to Dawn
        if (time >= 0 && time < 0.25) {
            float progress = (float)(time / 0.25);
            tint = interpolateColor(nightTint, dawnTint, progress);
            tintAlpha = interpolate(100, 35, progress);
            darknessAlpha = interpolate(220, 130, progress);
        }
        // Dawn to Day
        else if (time >= 0.25 && time < 0.50) {
            float progress = (float)((time - 0.25) / 0.25);
            tint = interpolateColor(dawnTint, dayTint, progress);
            tintAlpha = interpolate(35, 15, progress);
            darknessAlpha = interpolate(130, 80, progress);
        }
        // Day to Dusk
        else if (time >= 0.50 && time < 0.75) {
            float progress = (float)((time - 0.50) / 0.25);
            tint = interpolateColor(dayTint, duskTint, progress);
            tintAlpha = interpolate(15, 50, progress);
            darknessAlpha = interpolate(80, 160, progress);
        }
        // Dusk to Night
        else {
            float progress = (float)((time - 0.75) / 0.25);
            tint = interpolateColor(duskTint, nightTint, progress);
            tintAlpha = interpolate(50, 100, progress);
            darknessAlpha = interpolate(160, 220, progress);
        }
        this.timeCycleTintColor = new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), tintAlpha);
        this.ambientAlpha = darknessAlpha;
    }

    /**
     * Manages the ambient darkness, distinguishing between smooth day/night transitions, fast recovery from effects, and a persistent override state.
     */
    private void updateAmbientDarknessAndEffects() {
        if (filterColor != null) {
            animTick++;
            if (animTick >= LIGHT_ANIM_SPEED) {
                animTick = 0;
                filterColor = null;
            }
        }
        int targetAlpha = this.ambientAlpha;

        if (ambientDarknessOverridden) {
            if (currentAmbientAlpha < targetAlpha) {
                currentAmbientAlpha = Math.min(currentAmbientAlpha + 2, targetAlpha);
            }
        }
        // Faster recovery from effect
        else if (isFadingBack) {
            fadeBackAnimTick++;
            if (fadeBackAnimTick >= LIGHT_ANIM_SPEED / 2) {
                fadeBackAnimTick = 0;
                currentAmbientAlpha += 50;
                if (currentAmbientAlpha >= targetAlpha) {
                    currentAmbientAlpha = targetAlpha;
                    isFadingBack = false;
                }
            }
        }
        else {
            if (currentAmbientAlpha < targetAlpha) {
                currentAmbientAlpha = Math.min(currentAmbientAlpha + 2, targetAlpha);
            }
            else if (currentAmbientAlpha > targetAlpha) {
                currentAmbientAlpha = Math.max(currentAmbientAlpha - 2, targetAlpha);
            }
        }
        this.ambientDarkness = new Color(0, 0, 0, currentAmbientAlpha);
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
     * The main rendering method for the lighting system. It orchestrates the drawing of all lighting layers in the correct order to produce the final composite image.
     * <p>
     * The rendering order is:
     * <ol>
     *     <li>The dynamic lightmap (darkness with light sources "erased") is drawn.</li>
     *     <li>The time-of-day color tint is drawn over everything.</li>
     *     <li>Cosmetic glow effects are drawn on top of the tinted scene.</li>
     *     <li>Finally, any full-screen flash effects are drawn on the very top.</li>
     * </ol>
     * @param g Graphics object used for drawing the light effects.
     * @param xLevelOffset The horizontal camera offset of the level.
     * @param yLevelOffset The vertical camera offset of the level.
     */
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderLightmap(xLevelOffset, yLevelOffset);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(lightmap, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        renderTimeCycleTint(g);
        glowObjects((Graphics2D)g, xLevelOffset, yLevelOffset);
        renderFlashEffects(g);
    }

    /**
     * Renders the full-screen color tint based on the current time of day.
     *
     * @param g The graphics context.
     */
    private void renderTimeCycleTint(Graphics g) {
        if (ambientDarknessOverridden) return;
        if (timeCycleTintColor.getAlpha() > 0) {
            g.setColor(timeCycleTintColor);
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        }
    }

    /**
     * Renders any active top-layer flash effects.
     *
     * @param g The graphics context.
     */
    private void renderFlashEffects(Graphics g) {
        if (filterColor != null) {
            g.setColor(filterColor);
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        }
        if (flashIntensity > 0) {
            g.setColor(new Color(1.0f, 1.0f, 1.0f, flashIntensity));
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        }
    }

    /**
     * Constructs the lightmap by filling it with the current ambient darkness, then using the pre-rendered light textures
     * with a {@link AlphaComposite#DST_OUT} composite to "erase" the areas where light sources are present.
     * This all happens on an off-screen buffer for maximum performance.
     *
     * @param xLevelOffset The horizontal camera offset.
     * @param yLevelOffset The vertical camera offset.
     */
    private void renderLightmap(int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = lightmap.createGraphics();
        g2d.scale(1.0 / LIGHTMAP_SCALE, 1.0 / LIGHTMAP_SCALE);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g2d.setColor(ambientDarkness);
        g2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));

        drawLightSource(g2d, playerLightTexture, gameState.getPlayer().getHitBox(), xLevelOffset, yLevelOffset);
        renderAllLightSources(g2d, xLevelOffset, yLevelOffset);
        g2d.dispose();
    }

    private void renderAllLightSources(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        if (ambientDarkness.getAlpha() > 100) {
            renderCandleLights(g2d, xLevelOffset, yLevelOffset);
            renderGameObjectLights(g2d, xLevelOffset, yLevelOffset);
            renderEnemyLights(g2d, xLevelOffset, yLevelOffset);
        }
    }

    private void renderCandleLights(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        for (Candle candle : gameState.getObjectManager().getObjects(Candle.class)) {
            if (candle.isAlive()) drawLightSource(g2d, candleLightTexture, candle.getHitBox(), xLevelOffset, yLevelOffset);
        }
    }

    private void renderGameObjectLights(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        List<Class<?>> sources = getLightSourceClasses();
        for (Class<?> s : sources) renderLightsForClass(g2d, s, xLevelOffset, yLevelOffset);
    }

    @SuppressWarnings("unchecked")
    private void renderLightsForClass(Graphics2D g2d, Class<?> lightSourceClass, int xLevelOffset, int yLevelOffset) {
        List<GameObject> objects = (List<GameObject>) gameState.getObjectManager().getObjects(lightSourceClass);
        for (GameObject obj : objects) {
            if (obj.isAlive()) drawLightSource(g2d, objectLightTexture, obj.getHitBox(), xLevelOffset, yLevelOffset);
        }
    }

    private void renderEnemyLights(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        for (Enemy enemy : gameState.getEnemyManager().getAllEnemies()) {
            if (enemy.isAlive() && enemy.isVisible()) drawLightSource(g2d, enemyLightTexture, enemy.getHitBox(), xLevelOffset, yLevelOffset);
        }
    }

    /**
     * A highly optimized helper method that draws a pre-rendered light texture onto the lightmap.
     *
     * @param g2d The graphics context (usually of the lightmap).
     * @param texture The pre-rendered light texture to draw.
     * @param hitbox The hitbox of the object emitting the light.
     * @param xLevelOffset The horizontal camera offset.
     * @param yLevelOffset The vertical camera offset.
     */
    private void drawLightSource(Graphics2D g2d, BufferedImage texture, Rectangle2D.Double hitbox, int xLevelOffset, int yLevelOffset) {
        int x = (int) (hitbox.getCenterX() - xLevelOffset - texture.getWidth() / 2.0);
        int y = (int) (hitbox.getCenterY() - yLevelOffset - texture.getHeight() / 2.0);
        g2d.drawImage(texture, x, y, null);
    }

    /**
     * Renders all cosmetic "glow" effects for light-emitting objects. These are drawn on top of the main lighting pass.
     *
     * @param g2d The main graphics context.
     * @param xLevelOffset The horizontal camera offset.
     * @param yLevelOffset The vertical camera offset.
     */
    private void glowObjects(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        candleFilter(g2d, xLevelOffset, yLevelOffset);
        gameState.getObjectManager().glowingRender(g2d, xLevelOffset, yLevelOffset);
        shopFilter(g2d, xLevelOffset, yLevelOffset);
    }

    // Filters
    private <T extends GameObject> void glowFilter(Graphics2D g2d, int xLevelOffset, int yLevelOffset, Class<T> clazz, boolean isCandle) {
        double pulseScale = 0.975 + 0.025 * Math.sin(pulseTimer);
        List<T> objects = gameState.getObjectManager().getObjects(clazz);
        float glowAlpha = getSmoothGlowAlpha();

        for (T object : objects) {
            int glowWidth = (int) (2.5 * CANDLE_LIGHT_RADIUS * pulseScale);
            int glowHeight = (int) (2.5 * CANDLE_LIGHT_RADIUS * pulseScale);
            int x = (int) (object.getHitBox().getCenterX() - xLevelOffset) - glowWidth / 2;
            int y = (int) (object.getHitBox().getCenterY() - yLevelOffset) - glowHeight / 2;

            Composite originalComposite = g2d.getComposite();

            float whiteGlowAlpha = glowAlpha * 0.7f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, whiteGlowAlpha));
            g2d.drawImage(whiteRadialLight, x, y, glowWidth, glowHeight, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha));
            g2d.drawImage(orangeLight, x, y, glowWidth, glowHeight, null);
            g2d.setComposite(originalComposite);

            if (isCandle && object instanceof Candle candle)
                gameState.getObjectManager().candleRender(g2d, xLevelOffset, yLevelOffset, candle);
        }
    }

    private void candleFilter(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        glowFilter(g2d, xLevelOffset, yLevelOffset, Candle.class, true);
    }

    private void shopFilter(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        glowFilter(g2d, xLevelOffset, yLevelOffset, Shop.class, false);
    }

    /**
     * Calculates an oscillating alpha value for cosmetic glows based on the time of day.
     * This makes glows fade during the day and brighten at night in a natural, cyclical pattern.
     * <p>
     * <b>Flow:</b>
     * <ol>
     *     <li><b>Cosine Wave:</b> The function uses {@code Math.cos(currentTimeOfDay * 2 * Math.PI)}.
     *     As {@code currentTimeOfDay} goes from 0.0 (midnight) to 1.0 (next midnight), the input to the cosine
     *     function goes from 0 to 2π, completing one full wave. The cosine function naturally returns a value
     *     in the range [-1.0, 1.0]. It is at its peak (+1.0) at the start/end (midnight) and at its trough (-1.0)
     *     in the middle (noon).</li>

     *     <li><b>Normalization:</b> The cosine output of [-1.0, 1.0] is not a useful range for alpha values.
     *     We normalize it to the range [0.0, 1.0] by using the formula {@code (cos + 1) / 2.0}.
     *     This maps -1.0 to 0.0 (noon, minimum glow) and +1.0 to 1.0 (midnight, maximum glow).</li>
     *
     *     <li><b>Linear Interpolation (Lerp):</b> Finally, we map this normalized [0.0, 1.0] value to our desired
     *     alpha range, which is from {@link #MIN_CANDLE_GLOW_ALPHA} to {@link #MAX_CANDLE_GLOW_ALPHA}.
     *     The formula {@code min + normalized * (max - min)} is a standard linear interpolation, scaling the
     *     normalized value to the final output range.</li>
     * </ol>
     * The result is a smooth, continuous alpha value that is highest at night and lowest during the day.
     *
     * @return A float alpha value between {@link #MIN_CANDLE_GLOW_ALPHA} and {@link #MAX_CANDLE_GLOW_ALPHA}.
     */
    private float getSmoothGlowAlpha() {
        double cos = Math.cos(currentTimeOfDay * 2 * Math.PI);
        double normalized = (cos + 1) / 2.0;
        return (float) (MIN_CANDLE_GLOW_ALPHA + normalized * (MAX_CANDLE_GLOW_ALPHA - MIN_CANDLE_GLOW_ALPHA));
    }

    // Ambient
    /**
     * Forces the ambient darkness to a specific level, ignoring the day-night cycle.
     * This override will remain in effect until {@link #releaseAmbientDarkness()} is called.
     *
     * @param darkness The alpha value (0-255) to lock the ambient darkness to.
     */
    public void overrideAmbientDarkness(int darkness) {
        this.ambientDarknessOverridden = true;
        this.isFadingBack = false;
        this.ambientAlpha = darkness;
        this.currentAmbientAlpha = darkness;
    }

    /**
     * Releases the manual override on ambient darkness, allowing the day-night cycle to resume control of the lighting.
     */
    public void releaseAmbientDarkness() {
        this.ambientDarknessOverridden = false;
    }

    // Helper
    /**
     * Linearly interpolates between two integer values. Used for smooth transitions of alpha values.
     *
     * @param start The starting value.
     * @param end The ending value.
     * @param progress The progress of the interpolation (0.0 to 1.0).
     * @return The interpolated integer value.
     */
    private int interpolate(int start, int end, float progress) {
        return (int) (start + (end - start) * progress);
    }

    /**
     * Linearly interpolates between two Color objects, component by component (R, G, B).
     * @param c1 The starting color.
     * @param c2 The ending color.
     * @param progress The progress of the interpolation (0.0 to 1.0).
     * @return The new, interpolated Color.
     */
    private Color interpolateColor(Color c1, Color c2, float progress) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        return new Color(r, g, b);
    }

    /**
     * Returns a list of classes that represent light sources in the game.
     * This is used to dynamically render all light-emitting objects without hardcoding each type.
     *
     * @return A list of Class objects representing light sources.
     */
    private List<Class<?>> getLightSourceClasses() {
        return List.of(
                Container.class, Blocker.class, Dog.class, SmashTrap.class,
                SaveTotem.class, Shop.class, Blacksmith.class, Table.class,
                Board.class, JumpPad.class, Herb.class, RoricTrap.class
        );
    }

    // Setters
    /**
     * Activates a full-screen color filter and sets the ambient darkness to zero.
     * The effect will automatically fade back to normal over a short duration as handled by the updateFading() method.
     *
     * @param color The color of the filter to apply (alpha component is used for intensity).
     */
    public void setAlphaWithFilter(int alpha, Color color) {
        this.currentAmbientAlpha = alpha;
        this.filterColor = color;
        if (!ambientDarknessOverridden) {
            this.isFadingBack = true;
            this.fadeBackAnimTick = 0;
        }
    }

    public void setAlpha(int alpha) {
        this.currentAmbientAlpha = alpha;
        this.filterColor = null;
        if (!ambientDarknessOverridden) {
            this.isFadingBack = true;
            this.fadeBackAnimTick = 0;
        }
    }

    public void setCurrentAmbientAlpha(int currentAmbientAlpha) {
        this.currentAmbientAlpha = currentAmbientAlpha;
        if (!ambientDarknessOverridden) {
            this.isFadingBack = true;
            this.fadeBackAnimTick = 0;
        }
        this.filterColor = null;
    }

    public void reset() {
        releaseAmbientDarkness();
        this.ambientAlpha = 130;
        this.currentAmbientAlpha = 130;
        this.filterColor = null;
        this.flashIntensity = 0;
        this.flashDuration = 0;
        this.isFadingBack = false;
        this.fadeBackAnimTick = 0;
    }

}
