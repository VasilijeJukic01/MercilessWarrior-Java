package platformer.model.effects.decorations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.animation.AssetManager;
import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.effects.TimeCycleManager;
import platformer.model.levels.Level;
import platformer.model.levels.metadata.DecorationMetadata;
import platformer.model.levels.metadata.ObjectMetadata;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.DECORATION_EFFECTS_PATH;

/**
 * Data-driven manager that spawns and renders ambient micro-life effects on level decorations.
 */
public class DecorationEffectManager {

    private static final int MAX_ACTIVE_PARTICLES = 260;
    private static final int CULL_MARGIN_PX = scale(96);

    private static class ActiveEmitter {
        final double worldX, worldY, width, height;
        final List<DecorationEffectSpec> specs;
        final double speedJitter;

        ActiveEmitter(double worldX, double worldY, double width, double height, List<DecorationEffectSpec> specs, double speedJitter) {
            this.worldX = worldX;
            this.worldY = worldY;
            this.width = width;
            this.height = height;
            this.specs = specs;
            this.speedJitter = speedJitter;
        }
    }

    private static class DecoParticle {
        DecorationEffectType type;
        double x, y, baseX, baseY;
        double vx, vy, gravity;
        double size, maxSize;
        double phase, phaseSpeed, swayAmp;
        double angle, spinSpeed;
        int age, maxAge;
        float maxAlpha;
        Color color;
        boolean alive;
    }

    private GameContext context;
    private final Map<String, List<DecorationEffectSpec>> registry = new HashMap<>();
    private final List<ActiveEmitter> activeEmitters = new ArrayList<>();
    private final List<DecoParticle> particles = new ArrayList<>(MAX_ACTIVE_PARTICLES);
    private final Random rand = new Random();

    private final BufferedImage smokeCloudTexture;
    private boolean active = true;

    public DecorationEffectManager() {
        this.smokeCloudTexture = createSoftSmokeCloudTexture(64);
        loadEffectRegistry();
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    private BufferedImage createSoftSmokeCloudTexture(int size) {
        BufferedImage img = ImageUtils.createCompatibleImage(size, size);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float[][] lobes = {
                {0.50f, 0.50f, 0.44f, 0.55f},
                {0.34f, 0.46f, 0.30f, 0.42f},
                {0.66f, 0.44f, 0.28f, 0.40f},
                {0.48f, 0.33f, 0.27f, 0.38f},
                {0.54f, 0.64f, 0.26f, 0.32f}
        };

        for (float[] lobe : lobes) {
            float cx = size * lobe[0];
            float cy = size * lobe[1];
            float r = size * lobe[2];
            int coreA = (int) (255 * lobe[3]);
            RadialGradientPaint p = new RadialGradientPaint(
                    new Point2D.Float(cx, cy), r,
                    new float[]{0.0f, 0.45f, 1.0f},
                    new Color[]{
                            new Color(210, 222, 226, coreA),
                            new Color(185, 200, 208, coreA / 2),
                            new Color(165, 180, 190, 0)
                    }
            );
            g2d.setPaint(p);
            g2d.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        }
        g2d.dispose();
        return img;
    }

    private void loadEffectRegistry() {
        try (InputStream is = AssetManager.getInstance().getResourceAsStream(DECORATION_EFFECTS_PATH)) {
            if (is == null) return;
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, List<DecorationEffectSpec>>>() {}.getType();
                Map<String, List<DecorationEffectSpec>> loaded = new Gson().fromJson(reader, type);
                if (loaded != null) {
                    registry.putAll(loaded);
                }
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load decoration_effects.json: " + e.getMessage(), Message.ERROR);
        }
    }

    /**
     * Scans the newly loaded room once and builds the compact list of active decoration emitters.
     */
    public void rebuildForCurrentLevel() {
        activeEmitters.clear();
        particles.clear();
        if (context == null || context.getLevelManager() == null) return;

        Level level = context.getLevelManager().getCurrentLevel();
        if (level == null) return;

        int[][] decoData = level.getDecoData();
        String tilesetName = level.getTilesetName();
        String currentLevelId = context.getLevelManager().getCurrentLevelId();
        boolean isRestored = context.getWorldMemoryManager() != null
                && context.getWorldMemoryManager().hasFlag("RESTORED_" + currentLevelId);

        for (int i = 0; i < decoData.length; i++) {
            for (int j = 0; j < decoData[0].length; j++) {
                int decoIndex = decoData[i][j];
                if (decoIndex == -1) continue;

                DecorationMetadata decoMeta = context.getLevelManager().getDecorationMetadata(tilesetName, decoIndex);
                if (decoMeta == null || decoMeta.getId() == null) continue;

                String lookupId = decoMeta.getId();
                List<DecorationEffectSpec> specs;
                if (isRestored && registry.containsKey(lookupId + "_LIVELY")) {
                    specs = registry.get(lookupId + "_LIVELY");
                }
                else {
                    specs = registry.get(lookupId);
                }
                if (specs == null || specs.isEmpty()) continue;

                ObjectMetadata objMeta = context.getLevelManager().getObjectMetadataAt(i, j);
                double scaleX = (objMeta != null && objMeta.getScaleX() != 0.0) ? Math.abs(objMeta.getScaleX()) : 1.0;
                double scaleY = (objMeta != null && objMeta.getScaleY() != 0.0) ? Math.abs(objMeta.getScaleY()) : 1.0;
                int customOffX = (objMeta != null) ? (int) (objMeta.getOffsetX() / 2.0 * SCALE) : 0;
                int customOffY = (objMeta != null) ? (int) (objMeta.getOffsetY() / 2.0 * SCALE) : 0;

                double baseW = (decoMeta.getWid() / 2.0) * SCALE;
                double baseH = (decoMeta.getHei() / 2.0) * SCALE;
                double drawW = baseW * scaleX;
                double drawH = baseH * scaleY;

                double centerX = (i * TILES_SIZE) + (decoMeta.getXOffset() / 2.0) * SCALE + customOffX + (baseW / 2.0);
                double centerY = (j * TILES_SIZE) + (decoMeta.getYOffset() / 2.0) * SCALE + customOffY + (baseH / 2.0);
                double worldX = centerX - (drawW / 2.0);
                double worldY = centerY - (drawH / 2.0);

                int hash = Math.abs((i * 73856093) ^ (j * 19349663));
                double jitter = 0.80 + ((hash & 0xFF) / 255.0) * 0.40;

                activeEmitters.add(new ActiveEmitter(worldX, worldY, drawW, drawH, specs, jitter));
            }
        }
    }

    /**
     * Updates active particles and spawns new ones only from emitters visible inside the camera viewport.
     */
    public void update() {
        if (!active || context == null || context.getCamera() == null) return;

        double density = 1.0;
        if (Framework.getInstance().getGame() != null && Framework.getInstance().getGame().getSettings() != null) {
            density = Framework.getInstance().getGame().getSettings().getParticleDensity();
        }
        if (density <= 0.01) {
            particles.clear();
            return;
        }

        TimeCycleManager timeManager = context.getTimeCycleManager();
        float dayFactor = (timeManager != null) ? timeManager.getDaylightFactor() : 0.5f;
        float nightFactor = (timeManager != null) ? timeManager.getNightFactor() : 0.5f;

        int camX = context.getCamera().getXOffset();
        int camY = context.getCamera().getYOffset();
        double viewLeft = camX - CULL_MARGIN_PX;
        double viewRight = camX + GAME_WIDTH + CULL_MARGIN_PX;
        double viewTop = camY - CULL_MARGIN_PX;
        double viewBottom = camY + GAME_HEIGHT + CULL_MARGIN_PX;

        int cap = Math.max(20, (int) (MAX_ACTIVE_PARTICLES * density));
        if (particles.size() < cap) {
            for (ActiveEmitter emitter : activeEmitters) {
                if (emitter.worldX + emitter.width < viewLeft || emitter.worldX > viewRight
                        || emitter.worldY + emitter.height < viewTop || emitter.worldY > viewBottom) {
                    continue;
                }
                for (DecorationEffectSpec spec : emitter.specs) {
                    if (particles.size() >= cap) break;
                    DecorationEffectType type = spec.getEffectType();
                    if (type == null) continue;

                    double timeWeight = 1.0;
                    if (spec.isNightOnly()) {
                        if (nightFactor < 0.15f) continue;
                        timeWeight = nightFactor;
                    }
                    else if (spec.isDayOnly()) {
                        if (dayFactor < 0.15f) continue;
                        timeWeight = dayFactor;
                    }

                    double chance = (spec.getRate() * 0.45) * density * timeWeight * emitter.speedJitter;
                    if (rand.nextDouble() < chance) {
                        spawnParticle(emitter, spec, type);
                    }
                }
            }
        }

        for (int i = particles.size() - 1; i >= 0; i--) {
            DecoParticle p = particles.get(i);
            updateParticle(p);
            if (!p.alive) {
                particles.remove(i);
            }
        }
    }

    private void spawnParticle(ActiveEmitter emitter, DecorationEffectSpec spec, DecorationEffectType type) {
        DecoParticle p = new DecoParticle();
        p.type = type;
        p.alive = true;
        p.age = 0;

        double spawnX, spawnY;
        if (spec.getOffsetX() != null && spec.getOffsetY() != null) {
            spawnX = emitter.worldX + emitter.width * spec.getOffsetX() + (rand.nextDouble() - 0.5) * (5.0 * SCALE);
            spawnY = emitter.worldY + emitter.height * spec.getOffsetY() + (rand.nextDouble() - 0.5) * (3.0 * SCALE);
        }
        else {
            double rx = 0.14 + rand.nextDouble() * 0.72;
            double ry = switch (spec.getArea() != null ? spec.getArea().toUpperCase() : "FULL") {
                case "TOP" -> 0.08 + rand.nextDouble() * 0.28;
                case "BOTTOM" -> 0.65 + rand.nextDouble() * 0.28;
                case "CENTER" -> 0.25 + rand.nextDouble() * 0.50;
                default -> 0.12 + rand.nextDouble() * 0.76;
            };
            spawnX = emitter.worldX + emitter.width * rx;
            spawnY = emitter.worldY + emitter.height * ry;
        }

        p.x = p.baseX = spawnX;
        p.y = p.baseY = spawnY;
        p.color = rand.nextBoolean() ? spec.getPrimaryColor() : spec.getSecondaryColor();

        double minS = Math.max(1.0, spec.getMinSize()) * SCALE;
        double maxS = Math.max(minS, spec.getMaxSize() * SCALE);
        p.size = minS + rand.nextDouble() * (maxS - minS);
        p.maxSize = p.size;
        p.phase = rand.nextDouble() * Math.PI * 2.0;

        switch (type) {
            case SPARKLE -> {
                p.maxAge = 140 + rand.nextInt(90);
                p.vx = 0;
                p.vy = -0.006 * SCALE;
                p.angle = rand.nextDouble() * Math.PI;
                p.spinSpeed = (rand.nextBoolean() ? 1 : -1) * 0.005;
                p.maxAlpha = 0.90f;
            }
            case SPORE_DRIFT -> {
                p.maxAge = 340 + rand.nextInt(220);
                p.vx = (rand.nextDouble() - 0.5) * 0.020 * SCALE;
                p.vy = -(0.025 + rand.nextDouble() * 0.042) * SCALE;
                p.phaseSpeed = 0.012 + rand.nextDouble() * 0.010;
                p.swayAmp = (4.0 + rand.nextDouble() * 6.0) * SCALE;
                p.maxAlpha = 0.80f;
            }
            case FIREFLY_ORBIT -> {
                p.maxAge = 420 + rand.nextInt(280);
                p.phaseSpeed = 0.009 + rand.nextDouble() * 0.008;
                p.swayAmp = (8.0 + rand.nextDouble() * 14.0) * SCALE;
                p.vy = (rand.nextDouble() - 0.5) * 0.018 * SCALE;
                p.maxAlpha = 0.90f;
            }
            case WATER_DRIP -> {
                p.maxAge = 135 + rand.nextInt(55);
                p.vx = 0;
                p.vy = 0.04 * SCALE;
                p.gravity = 0.016 * SCALE;
                p.maxAlpha = 0.78f;
            }
            case SOUL_WISP -> {
                p.maxAge = 320 + rand.nextInt(200);
                p.vy = -(0.040 + rand.nextDouble() * 0.055) * SCALE;
                p.phaseSpeed = 0.014 + rand.nextDouble() * 0.010;
                p.swayAmp = (5.0 + rand.nextDouble() * 9.0) * SCALE;
                p.maxAlpha = 0.48f;
            }
            case POLLEN_FLOAT -> {
                p.maxAge = 340 + rand.nextInt(240);
                p.vx = (0.025 + rand.nextDouble() * 0.045) * SCALE;
                p.vy = (0.012 + rand.nextDouble() * 0.028) * SCALE;
                p.phaseSpeed = 0.012 + rand.nextDouble() * 0.010;
                p.swayAmp = (4.0 + rand.nextDouble() * 7.0) * SCALE;
                p.angle = rand.nextDouble() * Math.PI * 2.0;
                p.spinSpeed = 0.010;
                p.maxAlpha = 0.72f;
            }
            case CHIMNEY_SMOKE -> {
                p.maxAge = 380 + rand.nextInt(160);
                p.vx = (0.040 + rand.nextDouble() * 0.045) * SCALE;
                p.vy = -(0.075 + rand.nextDouble() * 0.055) * SCALE;
                p.phaseSpeed = 0.010 + rand.nextDouble() * 0.008;
                p.swayAmp = (5.0 + rand.nextDouble() * 7.0) * SCALE;
                p.angle = rand.nextDouble() * Math.PI * 2.0;
                p.spinSpeed = (rand.nextBoolean() ? 1 : -1) * 0.004;
                p.size = (10.0 + rand.nextDouble() * 5.0) * SCALE;
                p.maxAlpha = 0.42f;
            }
            case HEARTH_SPARK -> {
                p.maxAge = 180 + rand.nextInt(120);
                p.vx = (rand.nextDouble() - 0.5) * 0.045 * SCALE;
                p.vy = -(0.070 + rand.nextDouble() * 0.080) * SCALE;
                p.phaseSpeed = 0.025;
                p.swayAmp = 2.5 * SCALE;
                p.maxAlpha = 0.88f;
            }
            case CAVE_DUST -> {
                p.maxAge = 360 + rand.nextInt(240);
                p.vx = (rand.nextDouble() - 0.5) * 0.018 * SCALE;
                p.vy = (rand.nextDouble() - 0.5) * 0.018 * SCALE;
                p.phaseSpeed = 0.008;
                p.swayAmp = 3.5 * SCALE;
                p.maxAlpha = 0.42f;
            }
            case LEAF_SHED -> {
                p.maxAge = 380 + rand.nextInt(220);
                p.vx = (0.025 + rand.nextDouble() * 0.040) * SCALE;
                p.vy = (0.055 + rand.nextDouble() * 0.055) * SCALE;
                p.phaseSpeed = 0.014 + rand.nextDouble() * 0.010;
                p.swayAmp = (8.0 + rand.nextDouble() * 12.0) * SCALE;
                p.angle = rand.nextDouble() * Math.PI * 2.0;
                p.spinSpeed = 0.010;
                p.maxAlpha = 0.82f;
            }
        }
        particles.add(p);
    }

    private void updateParticle(DecoParticle p) {
        p.age++;
        if (p.age >= p.maxAge) {
            p.alive = false;
            return;
        }
        p.phase += p.phaseSpeed;
        p.angle += p.spinSpeed;

        switch (p.type) {
            case SPARKLE -> p.y += p.vy;
            case FIREFLY_ORBIT -> {
                p.baseY += p.vy;
                p.x = p.baseX + Math.cos(p.phase) * p.swayAmp;
                p.y = p.baseY + Math.sin(p.phase * 1.5) * (p.swayAmp * 0.45);
            }
            case WATER_DRIP -> {
                p.vy += p.gravity;
                p.y += p.vy;
            }
            case CHIMNEY_SMOKE -> {
                p.baseX += p.vx;
                p.y += p.vy;
                p.x = p.baseX + Math.sin(p.phase) * p.swayAmp;
                p.size += 0.065 * SCALE;
            }
            default -> {
                p.baseX += p.vx;
                p.y += p.vy;
                p.x = p.baseX + Math.sin(p.phase) * p.swayAmp;
            }
        }
    }

    /**
     * Renders all active ambient decoration particles on top of the lighting layer.
     */
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active || particles.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < particles.size(); i++) {
            DecoParticle p = particles.get(i);
            double sx = p.x - xLevelOffset;
            double sy = p.y - yLevelOffset;
            if (sx < -64 || sx > GAME_WIDTH + 64 || sy < -64 || sy > GAME_HEIGHT + 64) continue;

            float life = (float) p.age / p.maxAge;
            float env = (float) Math.sin(life * Math.PI);
            float alpha = Math.max(0.0f, Math.min(1.0f, p.maxAlpha * env));
            if (alpha <= 0.02f) continue;

            int r = p.color.getRed(), gr = p.color.getGreen(), b = p.color.getBlue();
            int a = (int) (alpha * 255);

            switch (p.type) {
                case SPARKLE -> drawSparkleStar(g2d, sx, sy, p.size * (0.35 + 0.75 * env), p.angle, r, gr, b, a);
                case SPORE_DRIFT, FIREFLY_ORBIT, HEARTH_SPARK -> {
                    int halo = Math.max(3, (int) (p.size * 2.4));
                    g2d.setColor(new Color(r, gr, b, a / 3));
                    g2d.fillOval((int) sx - halo / 2, (int) sy - halo / 2, halo, halo);
                    int core = Math.max(2, (int) p.size);
                    g2d.setColor(new Color(Math.min(255, r + 55), Math.min(255, gr + 55), Math.min(255, b + 55), a));
                    g2d.fillOval((int) sx - core / 2, (int) sy - core / 2, core, core);
                }
                case WATER_DRIP -> {
                    int w = Math.max(2, (int) (p.size * 0.7));
                    int h = Math.max(3, (int) (p.size * 1.6));
                    g2d.setColor(new Color(r, gr, b, a));
                    g2d.fillOval((int) sx - w / 2, (int) sy - h / 2, w, h);
                }
                case SOUL_WISP -> {
                    int w = Math.max(2, (int) (p.size * 1.2));
                    int h = Math.max(4, (int) (p.size * 2.6));
                    g2d.setColor(new Color(r, gr, b, a / 2));
                    g2d.fillOval((int) sx - w, (int) sy - h / 2, w * 2, h);
                    g2d.setColor(new Color(Math.min(255, r + 40), Math.min(255, gr + 40), Math.min(255, b + 40), a));
                    g2d.fillOval((int) sx - w / 2, (int) sy - h / 3, w, (int) (h * 0.65));
                }
                case CHIMNEY_SMOKE -> {
                    Composite oldComp = g2d.getComposite();
                    AffineTransform oldTx = g2d.getTransform();
                    float smokeAlpha = Math.max(0.0f, Math.min(1.0f, p.maxAlpha * (1.0f - life) * Math.min(1.0f, life * 4.5f)));
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, smokeAlpha));
                    g2d.translate(sx, sy);
                    g2d.rotate(p.angle);
                    int s = Math.max(6, (int) p.size);
                    g2d.drawImage(smokeCloudTexture, -s / 2, -s / 2, s, s, null);
                    g2d.setTransform(oldTx);
                    g2d.setComposite(oldComp);
                }
                case LEAF_SHED, POLLEN_FLOAT -> {
                    AffineTransform old = g2d.getTransform();
                    g2d.translate(sx, sy);
                    g2d.rotate(p.angle);
                    int lw = Math.max(2, (int) (p.size * 1.5));
                    int lh = Math.max(2, (int) (p.size * 0.75));
                    g2d.setColor(new Color(r, gr, b, a));
                    g2d.fillOval(-lw / 2, -lh / 2, lw, lh);
                    g2d.setTransform(old);
                }
                case CAVE_DUST -> {
                    int s = Math.max(2, (int) p.size);
                    g2d.setColor(new Color(r, gr, b, a));
                    g2d.fillRect((int) sx, (int) sy, s, s);
                }
            }
        }

        g2d.dispose();
    }

    private void drawSparkleStar(Graphics2D g2d, double cx, double cy, double outerR, double rot, int r, int g, int b, int alpha) {
        double innerR = outerR * 0.25;
        Path2D.Double star = new Path2D.Double();
        for (int p = 0; p < 8; p++) {
            double a = rot + p * (Math.PI / 4.0);
            double rad = (p % 2 == 0) ? outerR : innerR;
            double px = cx + Math.cos(a) * rad;
            double py = cy + Math.sin(a) * rad;
            if (p == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }
        star.closePath();

        g2d.setColor(new Color(r, g, b, alpha / 2));
        int halo = Math.max(3, (int) (outerR * 1.4));
        g2d.fillOval((int) cx - halo / 2, (int) cy - halo / 2, halo, halo);
        g2d.setColor(new Color(255, 255, 255, alpha));
        g2d.fill(star);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) particles.clear();
    }

    public void reset() {
        particles.clear();
    }
}