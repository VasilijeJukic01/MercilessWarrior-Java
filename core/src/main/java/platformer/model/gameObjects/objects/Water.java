package platformer.model.gameObjects.objects;

import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import static platformer.constants.Constants.*;

/**
 * Procedural 2D water pool that triggers a deep U-crater and outward-rolling waves on every jump-off, surface crossing, and floor landing inside the pool.
 */
public class Water extends GameObject implements Interactable {

    // Speed of the outward-rolling left & right waves
    private static final double RIPPLE_TRAVEL_SPEED = 0.54 * SCALE;
    // Depth of the U-crater when jumping / landing in the water
    private static final double SPLASH_DEPTH = 12.0 * SCALE;
    private static final int SAMPLE_SPACING = Math.max(2, (int) (2.0 * SCALE));

    // Palette
    private static final Color BACK_WAVE_COLOR = new Color(20, 105, 130, 95);
    private static final Color WATER_TOP_COLOR = new Color(42, 180, 205, 130);
    private static final Color WATER_BOTTOM_COLOR = new Color(8, 36, 54, 195);
    private static final Color BUBBLE_COLOR = new Color(190, 248, 255, 85);

    private static class WavePulse {
        final double startX;
        // 0 = center crater, -1 = left-rolling wave, +1 = right-rolling wave
        final int dir;
        final double speed;
        double amplitude;
        final double decay;
        final double radius;
        double age = 0;

        WavePulse(double startX, int dir, double speed, double amplitude, double decay, double radius) {
            this.startX = startX;
            this.dir = dir;
            this.speed = speed;
            this.amplitude = amplitude;
            this.decay = decay;
            this.radius = radius;
        }
    }

    private final Queue<WavePulse> activePulses = new ConcurrentLinkedQueue<>();

    private final int poolWidth, poolHeight;
    private final int surfaceYOffset;

    private final int numSamples;
    private final double[] renderX, renderY;

    // Ambient
    private final double[] bubbleX, bubbleY;
    private final double[] bubbleSpeed;
    private final int[] bubbleSize;

    private double waveTime = 0.0;
    private boolean solidLeft = false, solidRight = false;
    private boolean edgeChecked = false;

    private boolean playerSubmerged = false;
    private double lastPlayerX = -1;
    private double lastPlayerMaxY = -1;
    private boolean wasPlayerInAir = false;
    private int wadeTick = 0;
    private int slashCooldown = 0;

    private EffectManager effectManager;
    private final Random rand = new Random();

    public Water(int xPos, int yPos, int tilesWide, int tilesHigh) {
        super(ObjType.WATER, xPos, yPos);
        this.poolWidth = tilesWide * TILES_SIZE;
        this.poolHeight = tilesHigh * TILES_SIZE + (int) (4 * SCALE);
        this.surfaceYOffset = (int) (7 * SCALE);

        initHitBox(poolWidth, poolHeight - surfaceYOffset);
        this.hitBox.x = xPos;
        this.hitBox.y = yPos + surfaceYOffset;

        this.numSamples = Math.max(24, (poolWidth / SAMPLE_SPACING) + 2);
        this.renderX = new double[numSamples];
        this.renderY = new double[numSamples];

        int bubbleCount = Math.max(5, tilesWide * 2);
        this.bubbleX = new double[bubbleCount];
        this.bubbleY = new double[bubbleCount];
        this.bubbleSpeed = new double[bubbleCount];
        this.bubbleSize = new int[bubbleCount];
        initBubbles();
    }

    // Init
    private void initBubbles() {
        for (int i = 0; i < bubbleX.length; i++) {
            resetBubble(i, true);
        }
    }

    private void resetBubble(int i, boolean randomY) {
        bubbleX[i] = xPos + 6 * SCALE + rand.nextDouble() * Math.max(8, poolWidth - 12 * SCALE);
        double minY = hitBox.y + 6 * SCALE;
        double maxY = yPos + poolHeight - 4 * SCALE;
        bubbleY[i] = randomY ? (minY + rand.nextDouble() * Math.max(4, maxY - minY)) : maxY;
        bubbleSpeed[i] = (0.10 + rand.nextDouble() * 0.14) * SCALE;
        bubbleSize[i] = Math.max(2, (int) ((1.5 + rand.nextDouble() * 1.8) * SCALE));
    }

    public void setEffectManager(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    // Core
    @Override
    public void update() {
        if (slashCooldown > 0) slashCooldown--;
        waveTime += 0.016;

        // Advance active wave pulses
        Iterator<WavePulse> it = activePulses.iterator();
        while (it.hasNext()) {
            WavePulse p = it.next();
            p.age += 1.0;
            p.amplitude *= p.decay;
            double currentX = p.startX + p.dir * p.speed * p.age;
            if (currentX < -p.radius || currentX > poolWidth + p.radius || Math.abs(p.amplitude) < 0.15 * SCALE) {
                it.remove();
            }
        }

        // Update bubbles
        for (int i = 0; i < bubbleX.length; i++) {
            bubbleY[i] -= bubbleSpeed[i];
            bubbleX[i] += Math.sin(waveTime * 1.5 + i) * (0.08 * SCALE);
            if (bubbleY[i] <= hitBox.y + 4 * SCALE) {
                resetBubble(i, false);
            }
        }
    }

    @Override
    public void update(int[][] levelData) {
        update();
        if (!edgeChecked && levelData != null) {
            edgeChecked = true;
            int leftTileX = (xPos / TILES_SIZE) - 1;
            int rightTileX = ((xPos + poolWidth) / TILES_SIZE);
            int topTileY = yPos / TILES_SIZE;
            if (topTileY >= 0 && topTileY < levelData[0].length) {
                solidLeft = (leftTileX >= 0) && CollisionDetector.isTileSolid(leftTileX, topTileY, levelData);
                solidRight = (rightTileX < levelData.length) && CollisionDetector.isTileSolid(rightTileX, topTileY, levelData);
            }
        }
    }

    public void updatePlayerPhysics(Player player) {
        if (player == null) return;
        Rectangle2D.Double pBox = player.getHitBox();
        boolean intersectingNow = hitBox.intersects(pBox);

        if (intersectingNow && !playerSubmerged) {
            // Player just entered the water hitbox from outside
            playerSubmerged = true;
            double px = pBox.getCenterX();
            plunge(px, 1.15, true);
            lastPlayerX = px;
            lastPlayerMaxY = pBox.getMaxY();
            wasPlayerInAir = player.isInAir();
        }
        else if (intersectingNow) {
            double px = pBox.getCenterX();
            double currentMaxY = pBox.getMaxY();
            boolean inAirNow = player.isInAir();

            // Player jumped while standing in the pool
            if (!wasPlayerInAir && inAirNow && player.getAirSpeed() < -0.5) {
                plunge(px, 0.90, true);
            }
            // Player landed on the floor inside the pool after a jump!
            else if (wasPlayerInAir && !inAirNow) {
                plunge(px, 1.25, true);
            }
            // Player's feet crossed the top surface downward while airborne
            else if (inAirNow && lastPlayerMaxY != -1 && lastPlayerMaxY <= hitBox.y + 8 * SCALE && currentMaxY > hitBox.y + 8 * SCALE) {
                plunge(px, 1.20, true);
            }

            // Horizontal walking or dashing pushes wake waves
            double dx = (lastPlayerX != -1) ? (px - lastPlayerX) : 0;
            if (Math.abs(dx) > 0.2) {
                int dir = dx > 0 ? 1 : -1;
                boolean dashing = player.checkAction(PlayerAction.DASH);
                wadeTick++;
                int interval = dashing ? 8 : 18;
                if (wadeTick >= interval) {
                    wadeTick = 0;
                    spawnWakePulse(px, dir, dashing ? 0.95 : 0.45);
                    if (dashing || rand.nextDouble() < 0.55) {
                        spawnDirectionalSplash(px, hitBox.y, dashing ? 6 : 2, -dir);
                    }
                }
            }
            lastPlayerX = px;
            lastPlayerMaxY = currentMaxY;
            wasPlayerInAir = inAirNow;
        }
        else if (playerSubmerged) {
            // Player completely exited the water hitbox
            playerSubmerged = false;
            double px = pBox.getCenterX();
            if (player.isInAir() || player.getAirSpeed() < 0) {
                plunge(px, 0.85, true);
            }
            lastPlayerX = -1;
            lastPlayerMaxY = -1;
            wasPlayerInAir = false;
        }
    }

    /**
     * Spawns a highly visible U-crater at worldX and two smooth wave crests that roll.
     */
    public void plunge(double worldX, double intensity, boolean withParticles) {
        double localX = Math.max(8 * SCALE, Math.min(poolWidth - 8 * SCALE, worldX - xPos));
        double depth = SPLASH_DEPTH * intensity;

        // Wide U-crater under the player
        double craterRadius = PLAYER_HB_WID * 1.6;
        activePulses.add(new WavePulse(localX, 0, 0.0, depth, 0.982, craterRadius));

        // left and right traveling wave crests that roll outward across the pool
        double startOffset = PLAYER_HB_WID * 0.45;
        double waveRadius = 22.0 * SCALE;
        double waveHeight = -depth * 0.75;
        activePulses.add(new WavePulse(localX - startOffset, -1, RIPPLE_TRAVEL_SPEED, waveHeight, 0.993, waveRadius));
        activePulses.add(new WavePulse(localX + startOffset,  1, RIPPLE_TRAVEL_SPEED, waveHeight, 0.993, waveRadius));

        // Secondary trailing trough behind each outward wave so you see a full crest + trough rolling out
        double trailOffset = PLAYER_HB_WID * 0.15;
        activePulses.add(new WavePulse(localX - trailOffset, -1, RIPPLE_TRAVEL_SPEED * 0.82, depth * 0.45, 0.991, waveRadius * 0.85));
        activePulses.add(new WavePulse(localX + trailOffset,  1, RIPPLE_TRAVEL_SPEED * 0.82, depth * 0.45, 0.991, waveRadius * 0.85));

        if (withParticles) spawnTwinSplashArcs(worldX, hitBox.y, 11);
    }

    private void spawnWakePulse(double worldX, int moveDir, double intensity) {
        double localX = Math.max(6 * SCALE, Math.min(poolWidth - 6 * SCALE, worldX - xPos));
        double height = -(5.5 * SCALE) * intensity;
        // Push a bow-wave crest ahead of the player and a small dip behind
        activePulses.add(new WavePulse(localX + moveDir * (6 * SCALE), moveDir, RIPPLE_TRAVEL_SPEED, height, 0.990, 16.0 * SCALE));
        activePulses.add(new WavePulse(localX - moveDir * (6 * SCALE), -moveDir, RIPPLE_TRAVEL_SPEED * 0.75, -height * 0.65, 0.988, 14.0 * SCALE));
    }

    private double evaluateSurfaceDisplacement(double localX) {
        double totalY = 0.0;

        for (WavePulse p : activePulses) {
            double center = p.startX + p.dir * p.speed * p.age;
            double dist = Math.abs(localX - center);
            if (dist >= p.radius) continue;

            // Cosine bell curve (U-shaped when positive, smooth wave crest when negative)
            double norm = dist / p.radius;
            double cosVal = Math.cos(norm * (Math.PI / 2.0));
            double shape = cosVal * cosVal;

            if (p.dir == 0) {
                // Center crater dips down into a U-bowl, then gently rebounds once before vanishing
                double rebound = Math.cos(p.age * 0.055);
                totalY += p.amplitude * shape * rebound;
            }
            else {
                // Outward traveling waves ramp in smoothly over the first 8 ticks
                double fadeIn = Math.min(1.0, p.age / 8.0);
                totalY += p.amplitude * shape * fadeIn;
            }
        }

        double maxDisp = Math.min(hitBox.height * 0.72, 22.0 * SCALE);
        return Math.max(-maxDisp, Math.min(maxDisp, totalY));
    }

    private void spawnTwinSplashArcs(double worldX, double surfaceY, int countPerSide) {
        if (effectManager == null) return;
        double offset = PLAYER_HB_WID * 0.45;
        effectManager.spawnDustParticles(worldX - offset, surfaceY, countPerSide, DustType.WATER_SPLASH, -1, null);
        effectManager.spawnDustParticles(worldX + offset, surfaceY, countPerSide, DustType.WATER_SPLASH, 1, null);
    }

    private void spawnDirectionalSplash(double worldX, double surfaceY, int count, int flipSign) {
        if (effectManager != null) {
            effectManager.spawnDustParticles(worldX, surfaceY, count, DustType.WATER_SPLASH, flipSign, null);
        }
    }

    public void onAttackSlash(Rectangle2D.Double attackBox, int flipSign) {
        if (slashCooldown > 0) return;
        if (attackBox.y > hitBox.y + 24 * SCALE) return;

        slashCooldown = 14;
        double slashX = Math.max(hitBox.x + 8, Math.min(hitBox.getMaxX() - 8, attackBox.getCenterX()));
        plunge(slashX, 0.95, true);
    }

    private double getIdleWaveOffset(int i) {
        double xNorm = (double) i / (numSamples - 1);
        double wallTaper = Math.sin(xNorm * Math.PI);
        double w1 = Math.sin(waveTime * 0.8 - xNorm * Math.PI * 2.0) * (0.80 * SCALE);
        double w2 = Math.cos(waveTime * 0.55 + xNorm * Math.PI * 1.5) * (0.50 * SCALE);
        return (w1 + w2) * (0.25 + 0.75 * wallTaper);
    }

    @Override
    public void onEnter(Player player) {
        updatePlayerPhysics(player);
    }

    @Override
    public void onIntersect(Player player) {
        updatePlayerPhysics(player);
    }

    @Override
    public void onExit(Player player) {
        updatePlayerPhysics(player);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int padLeft = solidLeft ? (int) (3 * SCALE) : 0;
        int padRight = solidRight ? (int) (3 * SCALE) : 0;
        double drawX = xPos - padLeft - xLevelOffset;
        double drawW = poolWidth + padLeft + padRight;
        double baseSurfaceY = hitBox.y - yLevelOffset;
        double bottomY = yPos + poolHeight + (2 * SCALE) - yLevelOffset;

        if (drawX + drawW < -64 || drawX > GAME_WIDTH + 64 || bottomY < -64 || baseSurfaceY > GAME_HEIGHT + 64) {
            return;
        }

        double stepX = drawW / (numSamples - 1);
        double minSurfaceY = baseSurfaceY;

        for (int i = 0; i < numSamples; i++) {
            renderX[i] = drawX + i * stepX;
            double localX = (i * stepX) - padLeft;
            double wallEdgeTaper = Math.min(1.0, Math.min(i, (numSamples - 1) - i) / 3.0);
            double waveDisp = evaluateSurfaceDisplacement(localX) * (0.25 + 0.75 * wallEdgeTaper);

            double targetY = baseSurfaceY + getIdleWaveOffset(i) + waveDisp;
            renderY[i] = Math.min(bottomY - 3 * SCALE, targetY);
            if (renderY[i] < minSurfaceY) minSurfaceY = renderY[i];
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Secondary parallax background wave layer
        Path2D.Double backWavePath = buildBackWavePath(drawX, drawW, baseSurfaceY, bottomY, stepX);
        g2d.setColor(BACK_WAVE_COLOR);
        g2d.fill(backWavePath);

        // Smooth front water body path
        Path2D.Double surfaceCurve = buildSmoothCurve(renderX, renderY, 0.0);
        Path2D.Double waterBody = new Path2D.Double(surfaceCurve);
        waterBody.lineTo(renderX[numSamples - 1], bottomY);
        waterBody.lineTo(renderX[0], bottomY);
        waterBody.closePath();

        GradientPaint bodyPaint = new GradientPaint(0, (float) (minSurfaceY - 2 * SCALE), WATER_TOP_COLOR, 0, (float) bottomY, WATER_BOTTOM_COLOR);
        g2d.setPaint(bodyPaint);
        g2d.fill(waterBody);
        Shape oldClip = g2d.getClip();
        g2d.clip(waterBody);

        renderCausticRays(g2d, drawX, drawW, minSurfaceY, bottomY);
        renderFeatheredSurfaceShimmer(g2d, surfaceCurve);

        // Rising underwater micro-bubbles
        g2d.setColor(BUBBLE_COLOR);
        for (int i = 0; i < bubbleX.length; i++) {
            int bx = (int) (bubbleX[i] - xLevelOffset);
            int by = (int) (bubbleY[i] - yLevelOffset);
            g2d.fillOval(bx, by, bubbleSize[i], bubbleSize[i]);
        }

        renderEdgeShading(g2d, (int) drawX, (int) drawW, (int) (minSurfaceY - 6 * SCALE), (int) bottomY);
        g2d.setClip(oldClip);

        LinearGradientPaint crestPaint = new LinearGradientPaint(
                (float) drawX, 0f, (float) (drawX + drawW), 0f,
                new float[]{0.0f, 0.08f, 0.92f, 1.0f},
                new Color[]{new Color(195, 248, 255, 20), new Color(210, 252, 255, 170), new Color(210, 252, 255, 170), new Color(195, 248, 255, 20)}
        );
        g2d.setPaint(crestPaint);
        g2d.setStroke(new BasicStroke(1.1f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(surfaceCurve);

        g2d.dispose();
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
    }

    private void renderFeatheredSurfaceShimmer(Graphics2D g2d, Path2D.Double surfaceCurve) {
        g2d.setColor(new Color(120, 235, 250, 22));
        g2d.setStroke(new BasicStroke(9.0f * SCALE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2d.draw(surfaceCurve);
        g2d.setColor(new Color(150, 245, 255, 28));
        g2d.setStroke(new BasicStroke(4.5f * SCALE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2d.draw(surfaceCurve);
    }

    private Path2D.Double buildSmoothCurve(double[] xs, double[] ys, double yOffset) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(xs[0], ys[0] + yOffset);
        for (int i = 0; i < numSamples - 1; i++) {
            double midX = (xs[i] + xs[i + 1]) * 0.5;
            double midY = (ys[i] + ys[i + 1]) * 0.5 + yOffset;
            if (i == 0) path.lineTo(midX, midY);
            else path.quadTo(xs[i], ys[i] + yOffset, midX, midY);
        }
        path.lineTo(xs[numSamples - 1], ys[numSamples - 1] + yOffset);
        return path;
    }

    private Path2D.Double buildBackWavePath(double drawX, double drawW, double baseSurfaceY, double bottomY, double stepX) {
        Path2D.Double backPath = new Path2D.Double();
        double backLift = -1.8 * SCALE;
        for (int i = 0; i < numSamples; i++) {
            double bx = drawX + i * stepX;
            double xNorm = (double) i / (numSamples - 1);
            double wallTaper = Math.sin(xNorm * Math.PI);
            double phaseWave = Math.sin(waveTime * 0.65 + xNorm * Math.PI * 2.0) * (1.0 * SCALE) * wallTaper;
            double frontDelta = renderY[i] - baseSurfaceY;
            double by = baseSurfaceY + backLift + phaseWave - (frontDelta * 0.25);
            by = Math.min(bottomY - 3 * SCALE, by);
            if (i == 0) backPath.moveTo(bx, by);
            else backPath.lineTo(bx, by);
        }
        backPath.lineTo(drawX + drawW, bottomY);
        backPath.lineTo(drawX, bottomY);
        backPath.closePath();
        return backPath;
    }

    private void renderCausticRays(Graphics2D g2d, double drawX, double drawW, double topY, double bottomY) {
        int rayCount = Math.max(3, (int) (drawW / (40 * SCALE)));
        double spacing = drawW / rayCount;
        double rayHeight = (bottomY - topY) * 0.88;

        for (int i = 0; i < rayCount; i++) {
            double sway = Math.sin(waveTime * 0.7 + i * 1.7) * (7 * SCALE);
            double rx = drawX + (i + 0.35) * spacing + sway;
            double topW = (7 + 3 * Math.sin(waveTime + i)) * SCALE;
            double botW = topW * 2.1;
            double slant = (12 + 4 * Math.cos(waveTime * 0.5 + i)) * SCALE;

            Path2D.Double ray = new Path2D.Double();
            ray.moveTo(rx - topW * 0.5, topY - 4 * SCALE);
            ray.lineTo(rx + topW * 0.5, topY - 4 * SCALE);
            ray.lineTo(rx + botW * 0.5 - slant, topY + rayHeight);
            ray.lineTo(rx - botW * 0.5 - slant, topY + rayHeight);
            ray.closePath();

            GradientPaint rayPaint = new GradientPaint(
                    0, (float) topY, new Color(150, 245, 255, 30),
                    0, (float) (topY + rayHeight), new Color(60, 190, 220, 0)
            );
            g2d.setPaint(rayPaint);
            g2d.fill(ray);
        }
    }

    private void renderEdgeShading(Graphics2D g2d, int drawX, int drawW, int topY, int bottomY) {
        int fadeH = (int) ((bottomY - topY) * 0.35);
        int fadeY = bottomY - fadeH;
        GradientPaint bottomShadow = new GradientPaint(0, fadeY, new Color(0, 0, 0, 0), 0, bottomY, new Color(0, 8, 14, 150));
        g2d.setPaint(bottomShadow);
        g2d.fillRect(drawX, fadeY, drawW, fadeH);

        int sideW = (int) (8 * SCALE);
        if (solidLeft) {
            GradientPaint leftShadow = new GradientPaint(drawX, 0, new Color(0, 8, 14, 130), drawX + sideW, 0, new Color(0, 0, 0, 0));
            g2d.setPaint(leftShadow);
            g2d.fillRect(drawX, topY, sideW, bottomY - topY);
        }
        if (solidRight) {
            int rightEdge = drawX + drawW;
            GradientPaint rightShadow = new GradientPaint(rightEdge - sideW, 0, new Color(0, 0, 0, 0), rightEdge, 0, new Color(0, 8, 14, 130));
            g2d.setPaint(rightShadow);
            g2d.fillRect(rightEdge - sideW, topY, sideW, bottomY - topY);
        }
    }

    @Override
    public String getInteractionPrompt() {
        return null;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}