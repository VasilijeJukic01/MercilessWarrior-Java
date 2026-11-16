package platformer.model.effects;

import platformer.core.Game;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import static platformer.constants.Constants.*;

/**
 * Physics-driven screen-space impact engine managing spring-damper camera shake, FOV zoom-punches, rotational micro-roll, radial shockwaves, and multi-stage lightning strobes.
 */
public class ScreenEffectsManager {

    private final Game game;
    private final Random random = new Random();

    // Spring-Damper Camera Physics
    private double trauma = 0.0;
    private double springX = 0.0, springY = 0.0;
    private double velX = 0.0, velY = 0.0;
    private double zoomKick = 0.0, zoomVel = 0.0;
    private double rollAngle = 0.0, rollVel = 0.0;
    private double rumbleTime = 0.0;

    private static final double SPRING_STIFFNESS = 0.18;
    private static final double SPRING_DAMPING = 0.80;
    private static final double ZOOM_STIFFNESS = 0.14;
    private static final double ZOOM_DAMPING = 0.78;
    private static final double ROLL_STIFFNESS = 0.16;
    private static final double ROLL_DAMPING = 0.80;

    private AffineTransform savedWorldTransform;

    // Multi-Stage Flash & Vignette Pulse
    private float flashAlpha = 0.0f;
    private float flashDecay = 6.0f;
    private Color flashColor = new Color(185, 245, 255);
    private int secondaryStrobeDelay = -1;
    private int secondaryStrobeAlpha = 0;

    // Expanding Radial Shockwave Rings
    private final Queue<Shockwave> shockwaves = new ConcurrentLinkedQueue<>();

    // Cutscene Visuals & Hit Stop
    private boolean letterboxActive = false;
    private float letterboxProgress = 0.0f;
    private float cutsceneFadeAlpha = 0.0f;

    // Hit Stop
    private int hitStopDuration = 0;

    private static class Shockwave {
        final double centerX, centerY;
        final double maxRadius;
        final int maxTicks;
        final Color color;
        int tick = 0;

        Shockwave(double centerX, double centerY, double maxRadius, int maxTicks, Color color) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.maxRadius = maxRadius;
            this.maxTicks = Math.max(1, maxTicks);
            this.color = color;
        }
    }

    public ScreenEffectsManager(Game game) {
        this.game = game;
    }

    public void update() {
        if (updateHitStop()) return;
        updateSpringPhysics();
        updateFlashAndStrobe();
        updateShockwaves();
        updateCutscene();
    }

    private void updateSpringPhysics() {
        rumbleTime += 0.42;

        if (trauma > 0) trauma = Math.max(0.0, trauma - 0.011);

        // Hooke's Law spring-damper integration for X/Y camera punch
        double forceX = -SPRING_STIFFNESS * springX;
        double forceY = -SPRING_STIFFNESS * springY;
        velX = (velX + forceX) * SPRING_DAMPING;
        velY = (velY + forceY) * SPRING_DAMPING;
        springX += velX;
        springY += velY;

        // Spring-damper for FOV Zoom-Punch
        double zoomForce = -ZOOM_STIFFNESS * zoomKick;
        zoomVel = (zoomVel + zoomForce) * ZOOM_DAMPING;
        zoomKick += zoomVel;
        if (Math.abs(zoomKick) < 0.0005 && Math.abs(zoomVel) < 0.0005) {
            zoomKick = 0;
            zoomVel = 0;
        }

        // Spring-damper for Rotational Roll
        double rollForce = -ROLL_STIFFNESS * rollAngle;
        rollVel = (rollVel + rollForce) * ROLL_DAMPING;
        rollAngle += rollVel;
        if (Math.abs(rollAngle) < 0.0002 && Math.abs(rollVel) < 0.0002) {
            rollAngle = 0;
            rollVel = 0;
        }
    }

    private void updateFlashAndStrobe() {
        if (secondaryStrobeDelay > 0) {
            secondaryStrobeDelay--;
            if (secondaryStrobeDelay == 0) {
                flashAlpha = Math.max(flashAlpha, secondaryStrobeAlpha);
                secondaryStrobeDelay = -1;
            }
        }
        if (flashAlpha > 0) {
            flashAlpha *= 0.89f;
            flashAlpha -= flashDecay * 0.25f;
            if (flashAlpha < 1.5f) {
                flashAlpha = 0;
            }
        }
    }

    private void updateShockwaves() {
        Iterator<Shockwave> it = shockwaves.iterator();
        while (it.hasNext()) {
            Shockwave sw = it.next();
            sw.tick++;
            if (sw.tick >= sw.maxTicks) {
                it.remove();
            }
        }
    }

    /**
     * Applies the spring-damper shake, zoom-punch, and rotational roll to the world layer.
     */
    public void beginFrame(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        savedWorldTransform = g2d.getTransform();

        boolean hasEffect = trauma > 0.001 || Math.abs(springX) > 0.2 || Math.abs(springY) > 0.2 || Math.abs(zoomKick) > 0.001;
        if (!hasEffect) return;

        // Quadratic trauma curve for organic rumble + physical spring displacement
        double traumaSq = trauma * trauma;
        double rumbleX = Math.sin(rumbleTime * 1.7) * traumaSq * (14.0 * SCALE);
        double rumbleY = Math.cos(rumbleTime * 2.3) * traumaSq * (14.0 * SCALE);

        double totalOffsetX = springX + rumbleX;
        double totalOffsetY = springY + rumbleY;
        double totalZoom = 1.0 + Math.max(0.0, zoomKick) + (traumaSq * 0.032);
        double totalRoll = rollAngle + (Math.sin(rumbleTime * 1.3) * traumaSq * Math.toRadians(1.1));

        double cx = GAME_WIDTH / 2.0;
        double cy = GAME_HEIGHT / 2.0;

        g2d.translate(cx + totalOffsetX, cy + totalOffsetY);
        g2d.scale(totalZoom, totalZoom);
        g2d.rotate(totalRoll);
        g2d.translate(-cx, -cy);
    }

    /**
     * Restores the world transform so the HUD stays crisp, then renders shockwaves and vignette flashes.
     */
    public void renderFlash(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (savedWorldTransform != null) {
            g2d.setTransform(savedWorldTransform);
            savedWorldTransform = null;
        }

        renderShockwaves(g2d);
        renderVignetteFlash(g2d);
    }

    private void renderShockwaves(Graphics2D g2d) {
        if (shockwaves.isEmpty()) return;
        Graphics2D gWave = (Graphics2D) g2d.create();
        gWave.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Shockwave sw : shockwaves) {
            float progress = (float) sw.tick / sw.maxTicks;
            float eased = 1.0f - (float) Math.pow(1.0 - progress, 3);
            float radius = (float) (sw.maxRadius * eased);
            if (radius < 4) continue;

            float alpha = (1.0f - progress) * 0.75f;
            int aCore = Math.max(0, Math.min(255, (int) (alpha * 230)));
            int aGlow = Math.max(0, Math.min(255, (int) (alpha * 110)));

            Color transparent = new Color(sw.color.getRed(), sw.color.getGreen(), sw.color.getBlue(), 0);
            Color glowCol = new Color(sw.color.getRed(), sw.color.getGreen(), sw.color.getBlue(), aGlow);
            Color rimCol = new Color(255, 255, 255, aCore);

            RadialGradientPaint ringPaint = new RadialGradientPaint(
                    new Point2D.Double(sw.centerX, sw.centerY), radius,
                    new float[]{0.0f, 0.72f, 0.88f, 0.95f, 1.0f},
                    new Color[]{transparent, transparent, glowCol, rimCol, transparent}
            );
            gWave.setPaint(ringPaint);
            int d = (int) (radius * 2);
            gWave.fillOval((int) (sw.centerX - radius), (int) (sw.centerY - radius), d, d);
        }
        gWave.dispose();
    }

    private void renderVignetteFlash(Graphics2D g2d) {
        if (flashAlpha <= 0) return;
        Graphics2D gFlash = (Graphics2D) g2d.create();

        int edgeAlpha = Math.max(0, Math.min(255, Math.round(flashAlpha)));
        int centerAlpha = Math.max(0, Math.min(255, Math.round(flashAlpha * 0.38f)));

        Point2D center = new Point2D.Float(GAME_WIDTH / 2.0f, GAME_HEIGHT / 2.0f);
        float radius = Math.max(GAME_WIDTH, GAME_HEIGHT) * 0.75f;

        Color cCenter = new Color(255, 255, 255, centerAlpha);
        Color cEdge = new Color(flashColor.getRed(), flashColor.getGreen(), flashColor.getBlue(), edgeAlpha);
        RadialGradientPaint vignette = new RadialGradientPaint(center, radius, new float[]{0.0f, 0.65f, 1.0f}, new Color[]{cCenter, cEdge, cEdge});
        gFlash.setPaint(vignette);
        gFlash.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        gFlash.dispose();
    }

    public void renderCutsceneEffects(Graphics g) {
        if (cutsceneFadeAlpha > 0.0f) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0, 0, 0, Math.min(255, (int) (cutsceneFadeAlpha * 255))));
            g2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
            g2d.dispose();
        }
        if (letterboxProgress > 0.0f) {
            int barHeight = (int) (28 * SCALE * letterboxProgress);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, GAME_WIDTH, barHeight);
            g.fillRect(0, GAME_HEIGHT - barHeight, GAME_WIDTH, barHeight);
        }
    }

    private void updateCutscene() {
        if (letterboxActive) letterboxProgress = Math.min(1.0f, letterboxProgress + 0.04f);
        else letterboxProgress = Math.max(0.0f, letterboxProgress - 0.04f);
    }

    private boolean updateHitStop() {
        if (hitStopDuration > 0) {
            hitStopDuration--;
            return true;
        }
        return false;
    }

    /**
     * Triggers a physical spring-damper camera slam, FOV zoom-punch, and rotational roll.
     */
    public void triggerShake(int duration, double intensity) {
        if (!game.getSettings().isScreenShake()) return;

        double normalized = Math.min(1.0, intensity / 16.0);
        this.trauma = Math.min(1.0, Math.max(this.trauma, normalized * 0.85 + 0.15));

        double angle = random.nextDouble() * Math.PI * 2.0;
        double impulse = intensity * SCALE * 1.15;
        this.velX += Math.cos(angle) * impulse;
        this.velY += Math.sin(angle) * impulse + (intensity >= 10.0 ? intensity * SCALE * 0.8 : 0);

        // FOV Zoom-Punch & Micro-Roll
        if (intensity >= 5.0) {
            this.zoomVel += Math.min(0.035, intensity * 0.0018);
            double rollDir = random.nextBoolean() ? 1.0 : -1.0;
            this.rollVel += rollDir * Math.toRadians(Math.min(1.6, intensity * 0.09));
        }
    }

    public void triggerFlash() {
        triggerLightningStrobe(new Color(200, 245, 255), 185, false);
    }

    /**
     * Triggers a radial vignette lightning flash with an optional secondary thunderbolt restrike.
     */
    public void triggerLightningStrobe(Color color, int initialAlpha, boolean doubleStrike) {
        this.flashColor = color;
        this.flashAlpha = Math.max(this.flashAlpha, initialAlpha);
        this.flashDecay = 8.0f;
        if (doubleStrike) {
            this.secondaryStrobeDelay = 9;
            this.secondaryStrobeAlpha = (int) (initialAlpha * 0.85);
        }
    }

    /**
     * Spawns an expanding radial shockwave ring in screen space.
     */
    public void triggerShockwave(double screenX, double screenY, double maxRadius, int durationTicks, Color color) {
        shockwaves.add(new Shockwave(screenX, screenY, maxRadius, durationTicks, color));
    }

    public void triggerHitStop(int durationFrames) {
        this.hitStopDuration = durationFrames;
    }

    public boolean isHitStopActive() {
        return hitStopDuration > 0;
    }

    public void setLetterboxActive(boolean active) {
        this.letterboxActive = active;
    }

    public float getCutsceneFadeAlpha() {
        return cutsceneFadeAlpha;
    }

    public void setCutsceneFadeAlpha(float alpha) {
        this.cutsceneFadeAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }

    // Reset
    public void reset() {
        trauma = 0;
        springX = springY = 0;
        velX = velY = 0;
        zoomKick = zoomVel = 0;
        rollAngle = rollVel = 0;
        flashAlpha = 0;
        secondaryStrobeDelay = -1;
        shockwaves.clear();
        letterboxActive = false;
        letterboxProgress = 0.0f;
        cutsceneFadeAlpha = 0.0f;
    }
}