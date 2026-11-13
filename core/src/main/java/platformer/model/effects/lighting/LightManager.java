package platformer.model.effects.lighting;

import platformer.core.GameContext;
import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.enemies.Enemy;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.objects.*;
import platformer.model.gameObjects.objects.Container;
import platformer.model.levels.Level;
import platformer.model.levels.metadata.DecorationMetadata;
import platformer.model.levels.metadata.LevelMetadata;
import platformer.model.levels.metadata.LightMetadata;
import platformer.state.types.GameState;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static platformer.constants.AnimConstants.LIGHT_ANIM_SPEED;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

/**
 * Manages all screen-wide lighting, dynamic light sources, and atmospheric color grading for the game.
 * <p>
 * This class orchestrates several layers of visual effects to build a dynamic environment:
 * <ul>
 *     <li><b>Ambient Darkness:</b> A base darkness layer whose opacity shifts with the in-game day/night cycle or boss fight overrides.</li>
 *     <li><b>Dynamic Light Sources:</b> Uses an off-screen low-resolution lightmap buffer ({@code 1 / LIGHTMAP_SCALE}) where moving light sources
 *         (the player, enemies, and interactive objects) erase darkness via {@link AlphaComposite#DstOut}.</li>
 *     <li><b>Static Room Light Caching:</b> Pre-bakes all stationary level lights (glowing crystals, lanterns, and JSON metadata lights)
 *         once per room into cached buffers ({@link #staticRoomWhiteMap} and {@link #staticRoomColorMap}). During gameplay, rendering
 *         dozens of static lights requires only a single sub-region blit per frame.</li>
 *     <li><b>Day/Night Cycle Tinting:</b> Applies a full-screen color wash that smoothly interpolates between night, dawn, day, and dusk keyframes.</li>
 *     <li><b>Cosmetic Bloom & Glows:</b> Renders pulsing radial glow sprites over active light-emitting objects like candles and the shop wagon.</li>
 *     <li><b>Special Flash Filters:</b> Handles temporary full-screen flashes and color washes triggered by lightning strikes or boss phases.</li>
 * </ul>
 *
 * @see TimeCycleManager
 * @see DecorationLightManager
 * @see GameState
 */
public class LightManager {

    private GameContext context;

    /**
     * Off-screen composite buffer where darkness, erased light masks, and colored light tints are assembled
     * at {@code 1 / LIGHTMAP_SCALE} resolution before being bilinearly upscaled to the screen.
     */
    private final BufferedImage lightmap;

    /**
     * Downscaling factor for the lightmap buffer. A factor of 4 reduces pixel fill-rate cost by 16x while preserving smooth radial gradients when drawn with bilinear interpolation.
     */
    private static final int LIGHTMAP_SCALE = 4;

    private BufferedImage orangeLight, whiteRadialLight;
    private BufferedImage playerLightTexture, enemyLightTexture, candleLightTexture, objectLightTexture, shopLightTexture;
    private final Map<String, BufferedImage> lightTextureCache = new HashMap<>();

    /**
     * Cached room-sized buffers holding pre-baked white cutout masks and colored glow tints for all stationary decorations and metadata lights in the current level.
     * Rebuilt only when the active level or restoration state changes.
     */
    private Level cachedStaticLevel = null;
    private boolean cachedRestoredState = false;
    private BufferedImage staticRoomWhiteMap = null;
    private BufferedImage staticRoomColorMap = null;
    private boolean hasStaticColoredLights = false;

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

    public LightManager() {
        this.lightmap = new BufferedImage(GAME_WIDTH / LIGHTMAP_SCALE, GAME_HEIGHT / LIGHTMAP_SCALE, BufferedImage.TYPE_INT_ARGB);
        this.ambientDarkness = new Color(0, 0, 0, currentAmbientAlpha);
        initLightImages();
        initLightTextures();
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    /**
     * Loads and prepares the sprite assets used for cosmetic bloom overlays on candles and shops.
     */
    private void initLightImages() {
        this.orangeLight = ImageUtils.importImage(ORANGE_GLOW, 2 * CANDLE_LIGHT_RADIUS, 2 * CANDLE_LIGHT_RADIUS);
        BufferedImage whiteRadialGlow = ImageUtils.importImage(WHITE_RADIAL_GLOW, 2 * CANDLE_LIGHT_RADIUS, 2 * CANDLE_LIGHT_RADIUS);
        RescaleOp rescaleOp = new RescaleOp(new float[]{1.0f, 1.0f, 1.0f, 0.5f}, new float[4], null);
        this.whiteRadialLight = rescaleOp.filter(whiteRadialGlow, null);
    }

    /**
     * Pre-renders the circular light cutout gradients into reusable {@link BufferedImage} textures at native lightmap scale.
     * <p>
     * Pre-baking gradients at initialization avoids constructing expensive {@link RadialGradientPaint} objects and avoids rescaling textures on the fly during the frame loop.
     */
    private void initLightTextures() {
        float[] coreFractions = new float[]{0f, 0.42f, 1f};
        Color[] coreColors = new Color[]{Color.WHITE, new Color(1.0f, 1.0f, 1.0f, 0.92f), new Color(1.0f, 1.0f, 1.0f, 0f)};

        int playerDiameter = Math.max(2, (int) (PLAYER_LIGHT_RADIUS * 2.5f) / LIGHTMAP_SCALE);
        playerLightTexture = createLightTexture(playerDiameter, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(1.0f, 1.0f, 1.0f, 0f)});

        int enemyDiameter = Math.max(2, (int) (PLAYER_LIGHT_RADIUS * 2.0f) / LIGHTMAP_SCALE);
        enemyLightTexture = createLightTexture(enemyDiameter, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(1.0f, 1.0f, 1.0f, 0f)});

        int candleDiameter = Math.max(2, (int) (CANDLE_LIGHT_RADIUS * 2.6f) / LIGHTMAP_SCALE);
        candleLightTexture = createLightTexture(candleDiameter, coreFractions, coreColors);

        int objectDiameter = Math.max(2, (int) (CANDLE_LIGHT_RADIUS * 1.9f) / LIGHTMAP_SCALE);
        objectLightTexture = createLightTexture(objectDiameter, coreFractions, coreColors);

        int shopDiameter = Math.max(2, (int) (CANDLE_LIGHT_RADIUS * 3.2f) / LIGHTMAP_SCALE);
        shopLightTexture = createLightTexture(shopDiameter, coreFractions, coreColors);
    }

    /**
     * Creates a single circular alpha-mask gradient texture used with {@link AlphaComposite#DstOut} to erase darkness.
     *
     * @param diameter  The diameter of the light texture in lightmap pixels.
     * @param fractions The normalized gradient stop positions (e.g., {@code {0f, 1f}}).
     * @param colors    The colors corresponding to each gradient stop.
     * @return A {@link BufferedImage} containing the pre-rendered radial gradient.
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

    /**
     * Creates a pre-rendered circular gradient texture for a colored light source (such as glowing crystals or lanterns).
     * <p>
     * Uses a 3-stop falloff ({@code 0.0 -> 0.5 -> 1.0}) so adjacent overlapping lights blend without creating harsh saturation rings.
     *
     * @param diameter The diameter of the light texture in lightmap pixels.
     * @param color    The base RGB color of the light source.
     * @return A {@link BufferedImage} containing the pre-rendered colored light gradient.
     */
    private BufferedImage createColoredLightTexture(int diameter, Color color) {
        if (diameter <= 0) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        BufferedImage texture = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Point2D center = new Point2D.Float(diameter / 2.0f, diameter / 2.0f);
        float radius = diameter / 2.0f;

        Color centerCol = new Color(color.getRed(), color.getGreen(), color.getBlue(), 190);
        Color midCol = new Color(color.getRed(), color.getGreen(), color.getBlue(), 75);
        Color transparentColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        Color[] colors = {centerCol, midCol, transparentColor};
        float[] fractions = {0.0f, 0.5f, 1.0f};

        RadialGradientPaint p = new RadialGradientPaint(center, radius, fractions, colors);
        g2d.setPaint(p);
        g2d.fillOval(0, 0, diameter, diameter);
        g2d.dispose();
        return texture;
    }

    // Update
    /**
     * Main update loop for the lighting system.
     * Synchronizes with the {@link TimeCycleManager} and updates ambient darkness transitions, glow pulsation, and screen flashes.
     *
     * @param timeManager The manager providing the normalized in-game time of day.
     */
    public void update(TimeCycleManager timeManager) {
        this.currentTimeOfDay = timeManager.getGameTimeNormalized();
        calculateTimeCycleEffects(timeManager);
        updateAmbientDarknessAndEffects();
        updatePulse();
        updateFlash();
    }

    /**
     * Calculates the baseline color tint and target ambient darkness for the current time of day.
     * Smoothly interpolates between four keyframe periods: Night, Dawn, Day, and Dusk.
     *
     * @param timeManager The manager providing the current normalized time of day.
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
            float progress = (float) (time / 0.25);
            tint = interpolateColor(nightTint, dawnTint, progress);
            tintAlpha = interpolate(100, 35, progress);
            darknessAlpha = interpolate(220, 130, progress);
        }
        // Dawn to Day
        else if (time >= 0.25 && time < 0.50) {
            float progress = (float) ((time - 0.25) / 0.25);
            tint = interpolateColor(dawnTint, dayTint, progress);
            tintAlpha = interpolate(35, 15, progress);
            darknessAlpha = interpolate(130, 80, progress);
        }
        // Day to Dusk
        else if (time >= 0.50 && time < 0.75) {
            float progress = (float) ((time - 0.50) / 0.25);
            tint = interpolateColor(dayTint, duskTint, progress);
            tintAlpha = interpolate(15, 50, progress);
            darknessAlpha = interpolate(80, 160, progress);
        }
        // Dusk to Night
        else {
            float progress = (float) ((time - 0.75) / 0.25);
            tint = interpolateColor(duskTint, nightTint, progress);
            tintAlpha = interpolate(50, 100, progress);
            darknessAlpha = interpolate(160, 220, progress);
        }
        this.timeCycleTintColor = new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), tintAlpha);
        this.ambientAlpha = darknessAlpha;
    }

    /**
     * Updates the current ambient darkness alpha, handling gradual day/night transitions, rapid recovery after lightning flashes ({@code isFadingBack}), and persistent boss/interior overrides.
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
                currentAmbientAlpha = Math.min(currentAmbientAlpha + 5, targetAlpha);
            }
        }
        // Faster recovery from lightning/spell flash effects
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
        } else {
            if (currentAmbientAlpha < targetAlpha) {
                currentAmbientAlpha = Math.min(currentAmbientAlpha + 2, targetAlpha);
            } else if (currentAmbientAlpha > targetAlpha) {
                currentAmbientAlpha = Math.max(currentAmbientAlpha - 2, targetAlpha);
            }
        }
        this.ambientDarkness = new Color(0, 0, 0, currentAmbientAlpha);
    }

    /**
     * Advances the phase timer that drives the sine-wave pulsation of candle and shop glows.
     */
    private void updatePulse() {
        pulseTimer += 0.02;
        if (pulseTimer > Math.PI * 2) {
            pulseTimer -= Math.PI * 2;
        }
    }

    /**
     * Updates active screen flash effects, exponentially decaying the flash intensity until it reaches zero.
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
     * Renders the lighting pass over the world terrain and objects.
     * <p>
     * The rendering sequence is:
     * <ol>
     *     <li>Ensures the current room's stationary decoration/metadata lights are baked into {@link #staticRoomWhiteMap} and {@link #staticRoomColorMap}.</li>
     *     <li>Assembles the per-frame {@link #lightmap} at {@code 1 / LIGHTMAP_SCALE} resolution (combining ambient darkness, dynamic entity cutouts, and the static room light maps).</li>
     *     <li>Upscales the composite {@link #lightmap} to full screen resolution using bilinear interpolation.</li>
     *     <li>Draws pulsing cosmetic bloom sprites ({@link #glowObjects}) on top of candles and shops.</li>
     * </ol>
     *
     * @param g            The graphics context used for rendering.
     * @param xLevelOffset The horizontal camera offset in world pixels.
     * @param yLevelOffset The vertical camera offset in world pixels.
     */
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        ensureStaticRoomLightsBuilt();
        prepareLightAndColorMaps(xLevelOffset, yLevelOffset);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(lightmap, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        glowObjects(g2d, xLevelOffset, yLevelOffset);
    }

    /**
     * Renders full-screen atmospheric color grading (time-of-day tint) and flash overlays after all world entities are drawn.
     *
     * @param g The graphics context.
     */
    public void renderPostProcessing(Graphics g) {
        renderTimeCycleTint(g);
        renderFlashEffects(g);
    }

    /**
     * Lazily builds and caches the full-room static light buffers ({@link #staticRoomWhiteMap} and {@link #staticRoomColorMap}) for the active level.
     * <p>
     * Stationary light sources (such as glowing cave crystals, bioluminescent mushrooms, village buildings, and JSON metadata lights)
     * never move during gameplay. By baking all of them once into a pair of room-sized {@link BufferedImage}s at {@code 1 / LIGHTMAP_SCALE}
     * resolution when the player enters the room, the engine avoids iterating the tilemap and drawing dozens of radial gradients every frame.
     */
    private void ensureStaticRoomLightsBuilt() {
        Level currentLevel = context.getLevelManager().getCurrentLevel();
        if (currentLevel == null) return;
        boolean isRestored = context.getWorldMemoryManager() != null && context.getWorldMemoryManager().hasFlag("RESTORED_" + context.getLevelManager().getCurrentLevelId());

        if (currentLevel == cachedStaticLevel && isRestored == cachedRestoredState && staticRoomWhiteMap != null) {
            return;
        }

        cachedStaticLevel = currentLevel;
        cachedRestoredState = isRestored;
        hasStaticColoredLights = false;

        int roomLmW = Math.max(1, (currentLevel.getLevelTilesWidth() * TILES_SIZE) / LIGHTMAP_SCALE);
        int roomLmH = Math.max(1, (currentLevel.getLevelTilesHeight() * TILES_SIZE) / LIGHTMAP_SCALE);
        staticRoomWhiteMap = new BufferedImage(roomLmW, roomLmH, BufferedImage.TYPE_INT_ARGB);
        staticRoomColorMap = new BufferedImage(roomLmW, roomLmH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D wG2d = staticRoomWhiteMap.createGraphics();
        Graphics2D cG2d = staticRoomColorMap.createGraphics();
        try {
            int[][] decoData = currentLevel.getDecoData();
            String tilesetName = currentLevel.getTilesetName();
            for (int j = 0; j < decoData[0].length; j++) {
                for (int i = 0; i < decoData.length; i++) {
                    int decoIndex = decoData[i][j];
                    if (decoIndex != -1) {
                        DecorationMetadata decoMeta = context.getLevelManager().getDecorationMetadata(tilesetName, decoIndex);
                        if (decoMeta != null) {
                            String lookupId = decoMeta.getId();
                            if (isRestored && DecorationLightManager.getInstance().getLightData(lookupId + "_LIVELY") != null) {
                                lookupId = lookupId + "_LIVELY";
                            }
                            DecorationLightData lightData = DecorationLightManager.getInstance().getLightData(lookupId);
                            if (lightData != null) {
                                bakeSingleDecorationLight(wG2d, cG2d, lightData, decoMeta, i, j);
                                hasStaticColoredLights = true;
                            }
                        }
                    }
                }
            }
            LevelMetadata meta = context.getLevelManager().getCurrentLevelMetadata();
            if (meta != null && meta.getLights() != null) {
                for (LightMetadata light : meta.getLights()) {
                    bakeMetadataLight(wG2d, cG2d, light);
                    hasStaticColoredLights = true;
                }
            }
        } finally {
            wG2d.dispose();
            cG2d.dispose();
        }
    }

    /**
     * Bakes a single decoration's white cutout mask and colored glow gradient into the room's static light buffers,
     * accounting for any custom sub-tile offsets or scale multipliers defined in {@link platformer.model.levels.metadata.ObjectMetadata}.
     */
    private void bakeSingleDecorationLight(Graphics2D wG2d, Graphics2D cG2d, DecorationLightData lightData, DecorationMetadata decoMeta, int tileX, int tileY) {
        try {
            var objMeta = context.getLevelManager().getObjectMetadataAt(tileX, tileY);
            double scaleMult = (objMeta != null) ? Math.max(objMeta.getScaleX(), objMeta.getScaleY()) : 1.0;
            if (scaleMult <= 0) scaleMult = 1.0;

            int lmRadius = Math.max(2, (int) (lightData.getLightRadius() * SCALE * scaleMult) / LIGHTMAP_SCALE);
            int lmDiameter = lmRadius * 2;

            Color lightColor = Color.decode(lightData.getLightColor());
            String colorCacheKey = lightColor.getRGB() + "_" + lmRadius;
            String whiteCacheKey = "white_" + lmRadius;
            BufferedImage coloredTex = lightTextureCache.computeIfAbsent(colorCacheKey, k -> createColoredLightTexture(lmDiameter, lightColor));
            BufferedImage whiteTex = lightTextureCache.computeIfAbsent(whiteCacheKey, k -> createLightTexture(lmDiameter, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(1.0f, 1.0f, 1.0f, 0f)}));

            int customOffX = (objMeta != null) ? (int) (objMeta.getOffsetX() / 2.0 * SCALE) : 0;
            int customOffY = (objMeta != null) ? (int) (objMeta.getOffsetY() / 2.0 * SCALE) : 0;
            int offsetX = (int) (decoMeta.getXOffset() / 2.0 * SCALE) + customOffX, offsetY = (int) (decoMeta.getYOffset() / 2.0 * SCALE) + customOffY;
            int decoWid = (int) (decoMeta.getWid() / 2.0 * SCALE), decoHei = (int) (decoMeta.getHei() / 2.0 * SCALE);
            int centerX = (tileX * TILES_SIZE + offsetX + decoWid / 2) / LIGHTMAP_SCALE;
            int centerY = (tileY * TILES_SIZE + offsetY + decoHei / 2) / LIGHTMAP_SCALE;

            cG2d.drawImage(coloredTex, centerX - lmRadius, centerY - lmRadius, null);
            wG2d.drawImage(whiteTex, centerX - lmRadius, centerY - lmRadius, null);
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Bakes a point light defined in the level's JSON metadata (such as interior tavern lights) into the static room buffers.
     */
    private void bakeMetadataLight(Graphics2D wG2d, Graphics2D cG2d, LightMetadata light) {
        try {
            int lmRadius = Math.max(2, (int) (light.getRadius() * SCALE) / LIGHTMAP_SCALE);
            int lmDiameter = lmRadius * 2;
            Color lightColor = Color.decode(light.getColor());
            String colorCacheKey = lightColor.getRGB() + "_" + lmRadius;
            String whiteCacheKey = "white_" + lmRadius;
            BufferedImage coloredTex = lightTextureCache.computeIfAbsent(colorCacheKey, k -> createColoredLightTexture(lmDiameter, lightColor));
            BufferedImage whiteTex = lightTextureCache.computeIfAbsent(whiteCacheKey, k -> createLightTexture(lmDiameter, new float[]{0f, 1f}, new Color[]{Color.WHITE, new Color(1.0f, 1.0f, 1.0f, 0f)}));
            int centerX = (light.getX() * TILES_SIZE + TILES_SIZE / 2) / LIGHTMAP_SCALE;
            int centerY = (light.getY() * TILES_SIZE + TILES_SIZE / 2) / LIGHTMAP_SCALE;
            cG2d.drawImage(coloredTex, centerX - lmRadius, centerY - lmRadius, null);
            wG2d.drawImage(whiteTex, centerX - lmRadius, centerY - lmRadius, null);
        } catch (Exception ignored) {}
    }

    /**
     * Constructs the per-frame {@link #lightmap} on an off-screen buffer:
     * <ol>
     *     <li>Fills the buffer with the current {@link #ambientDarkness} using {@link AlphaComposite#Src} (replacing both clear and fill in one pass).</li>
     *     <li>Switches to {@link AlphaComposite#DstOut} to punch out light holes for the player, dynamic objects, and the pre-baked {@link #staticRoomWhiteMap}.</li>
     *     <li>Overlays the pre-baked {@link #staticRoomColorMap} at {@code 0.26f} opacity so colored lights softly tint the illuminated areas.</li>
     * </ol>
     *
     * @param xLevelOffset The horizontal camera offset in world pixels.
     * @param yLevelOffset The vertical camera offset in world pixels.
     */
    private void prepareLightAndColorMaps(int xLevelOffset, int yLevelOffset) {
        int lmW = lightmap.getWidth();
        int lmH = lightmap.getHeight();
        int srcX = xLevelOffset / LIGHTMAP_SCALE;
        int srcY = yLevelOffset / LIGHTMAP_SCALE;

        Graphics2D lightmapG2d = lightmap.createGraphics();
        try {
            lightmapG2d.setComposite(AlphaComposite.Src);
            lightmapG2d.setColor(ambientDarkness);
            lightmapG2d.fillRect(0, 0, lmW, lmH);

            lightmapG2d.setComposite(AlphaComposite.DstOut);
            drawLightSource(lightmapG2d, playerLightTexture, context.getPlayer().getHitBox(), xLevelOffset, yLevelOffset);
            renderAllLightSources(lightmapG2d, xLevelOffset, yLevelOffset);

            if (hasStaticColoredLights && staticRoomWhiteMap != null) {
                lightmapG2d.drawImage(staticRoomWhiteMap, -srcX, -srcY, null);
                lightmapG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.26f));
                lightmapG2d.drawImage(staticRoomColorMap, -srcX, -srcY, null);
            }
        } finally {
            lightmapG2d.dispose();
        }
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
     * Renders any active top-layer flash or color filter effects.
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

    private void renderAllLightSources(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        if (ambientDarkness.getAlpha() > 100) {
            renderCandleLights(g2d, xLevelOffset, yLevelOffset);
            renderGameObjectLights(g2d, xLevelOffset, yLevelOffset);
            renderEnemyLights(g2d, xLevelOffset, yLevelOffset);
        }
    }

    private void renderCandleLights(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        for (Candle candle : context.getObjectManager().getObjects(Candle.class)) {
            if (candle.isAlive()) drawLightSource(g2d, candleLightTexture, candle.getHitBox(), xLevelOffset, yLevelOffset);
        }
    }

    private void renderGameObjectLights(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        List<Class<?>> sources = getLightSourceClasses();
        for (Class<?> s : sources) renderLightsForClass(g2d, s, xLevelOffset, yLevelOffset);
    }

    @SuppressWarnings("unchecked")
    private void renderLightsForClass(Graphics2D g2d, Class<?> lightSourceClass, int xLevelOffset, int yLevelOffset) {
        List<GameObject> objects = (List<GameObject>) context.getObjectManager().getObjects((Class<? extends GameObject>) lightSourceClass);
        BufferedImage tex = (lightSourceClass == Shop.class) ? shopLightTexture : objectLightTexture;
        for (GameObject obj : objects) {
            if (obj.isAlive()) drawLightSource(g2d, tex, obj.getHitBox(), xLevelOffset, yLevelOffset);
        }
    }

    private void renderEnemyLights(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        for (Enemy enemy : context.getEnemyManager().getAllEnemies()) {
            if (enemy.isAlive() && enemy.isVisible()) drawLightSource(g2d, enemyLightTexture, enemy.getHitBox(), xLevelOffset, yLevelOffset);
        }
    }

    /**
     * Draws a pre-rendered light cutout texture onto the low-resolution lightmap at 1:1 scale.
     *
     * @param g2d          The graphics context of the lightmap buffer.
     * @param texture      The pre-rendered light texture to draw.
     * @param hitbox       The world-space hitbox of the entity or object emitting the light.
     * @param xLevelOffset The horizontal camera offset in world pixels.
     * @param yLevelOffset The vertical camera offset in world pixels.
     */
    private void drawLightSource(Graphics2D g2d, BufferedImage texture, Rectangle2D.Double hitbox, int xLevelOffset, int yLevelOffset) {
        int x = (int) ((hitbox.getCenterX() - xLevelOffset) / LIGHTMAP_SCALE - texture.getWidth() / 2.0);
        int y = (int) ((hitbox.getCenterY() - yLevelOffset) / LIGHTMAP_SCALE - texture.getHeight() / 2.0);
        g2d.drawImage(texture, x, y, null);
    }

    /**
     * Renders cosmetic bloom overlays for light-emitting game objects on top of the main lighting pass.
     *
     * @param g2d          The main screen graphics context.
     * @param xLevelOffset The horizontal camera offset.
     * @param yLevelOffset The vertical camera offset.
     */
    private void glowObjects(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        candleFilter(g2d, xLevelOffset, yLevelOffset);
        shopFilter(g2d, xLevelOffset, yLevelOffset);
    }

    // Filters
    private <T extends GameObject> void glowFilter(Graphics2D g2d, int xLevelOffset, int yLevelOffset, Class<T> clazz, boolean isCandle) {
        double pulseScale = 0.975 + 0.025 * Math.sin(pulseTimer);
        List<T> objects = context.getObjectManager().getObjects(clazz);
        float glowAlpha = getSmoothGlowAlpha();

        for (T object : objects) {
            if (!object.isAlive()) continue;
            int glowWidth = (int) (2.5 * CANDLE_LIGHT_RADIUS * pulseScale);
            int glowHeight = (int) (2.5 * CANDLE_LIGHT_RADIUS * pulseScale);
            int x = (int) (object.getHitBox().getCenterX() - xLevelOffset) - glowWidth / 2;
            int y = (int) (object.getHitBox().getCenterY() - yLevelOffset) - glowHeight / 2;

            Composite originalComposite = g2d.getComposite();
            float whiteGlowAlpha = glowAlpha * 0.6f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, whiteGlowAlpha));
            g2d.drawImage(whiteRadialLight, x, y, glowWidth, glowHeight, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha * 0.85f));
            g2d.drawImage(orangeLight, x, y, glowWidth, glowHeight, null);
            g2d.setComposite(originalComposite);
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
     *     As {@code currentTimeOfDay} goes from 0.0 (midnight) to 1.0 (next midnight), the input to the cosine function goes from 0 to 2π, completing one full wave.
     *     The cosine function naturally returns a value in the range [-1.0, 1.0]. It is at its peak (+1.0) at the start/end (midnight) and at its trough (-1.0) in the middle (noon).</li>
     *     <li><b>Normalization:</b> The cosine output of [-1.0, 1.0] is normalized to the range [0.0, 1.0] using the formula {@code (cos + 1) / 2.0}.
     *     This maps -1.0 to 0.0 (noon, minimum glow) and +1.0 to 1.0 (midnight, maximum glow).</li>
     *     <li><b>Linear Interpolation (Lerp):</b> Finally, this normalized [0.0, 1.0] value is mapped to the desired
     *     alpha range from {@link #MIN_CANDLE_GLOW_ALPHA} to {@link #MAX_CANDLE_GLOW_ALPHA} using {@code min + normalized * (max - min)}.</li>
     * </ol>
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
     * This override remains in effect until {@link #releaseAmbientDarkness()} is called.
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
     * Linearly interpolates between two integer values.
     *
     * @param start    The starting value.
     * @param end      The ending value.
     * @param progress The interpolation progress (0.0 to 1.0).
     * @return The interpolated integer value.
     */
    private int interpolate(int start, int end, float progress) {
        return (int) (start + (end - start) * progress);
    }

    /**
     * Linearly interpolates between two {@link Color} objects, component by component (R, G, B).
     *
     * @param c1       The starting color.
     * @param c2       The ending color.
     * @param progress The interpolation progress (0.0 to 1.0).
     * @return The newly interpolated {@link Color}.
     */
    private Color interpolateColor(Color c1, Color c2, float progress) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        return new Color(r, g, b);
    }

    /**
     * Returns the list of {@link GameObject} classes that emit dynamic light cutouts in the game world.
     *
     * @return A list of light-emitting {@link GameObject} classes.
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
     * Activates a temporary full-screen color filter and sets the ambient darkness to the given alpha.
     * The darkness automatically fades back to normal over a short duration via {@link #updateAmbientDarknessAndEffects()}.
     *
     * @param alpha The temporary ambient darkness alpha (0-255).
     * @param color The color filter to apply over the screen.
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

    /**
     * Forces the static room light cache to be rebuilt on the next frame.
     */
    public void invalidateStaticRoomLights() {
        this.cachedStaticLevel = null;
    }

    /**
     * Resets all lighting states, flash timers, and cached room light maps back to their default values.
     */
    public void reset() {
        releaseAmbientDarkness();
        this.ambientAlpha = 130;
        this.currentAmbientAlpha = 130;
        this.filterColor = null;
        this.flashIntensity = 0;
        this.flashDuration = 0;
        this.isFadingBack = false;
        this.fadeBackAnimTick = 0;
        this.cachedStaticLevel = null;
    }
}