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
 * Procedural molten magma pool featuring churning subsurface lava veins, drifting dark obsidian crust plates, swelling surface blisters, alpha-feathered edge blending, and interactive ember splashes.
 */
public class Lava extends GameObject implements Interactable {

    private static final double RIPPLE_TRAVEL_SPEED = 0.22 * SCALE;
    private static final double SPLASH_DEPTH = 7.0 * SCALE;
    private static final int SAMPLE_SPACING = Math.max(2, (int) (2.5 * SCALE));

    // Palette
    private static final Color MAGMA_UPPER_COLOR = new Color(240, 75, 12, 255);
    private static final Color MAGMA_MID_COLOR = new Color(150, 22, 5, 255);
    private static final Color MAGMA_DEEP_COLOR = new Color(8, 3, 6, 255);
    private static final Color CRUST_SLAG_COLOR = new Color(38, 10, 8, 235);

    private static class MagmaPulse {
        final double startX;
        final int dir;
        final double speed;
        double amplitude;
        final double decay;
        final double radius;
        double age = 0;

        MagmaPulse(double startX, int dir, double speed, double amplitude, double decay, double radius) {
            this.startX = startX;
            this.dir = dir;
            this.speed = speed;
            this.amplitude = amplitude;
            this.decay = decay;
            this.radius = radius;
        }
    }

    private final Queue<MagmaPulse> activePulses = new ConcurrentLinkedQueue<>();

    private final int poolWidth, poolHeight;
    private final int surfaceYOffset;

    private final int numSamples;
    private final double[] renderX, renderY;

    // Surface Blisters
    private final double[] blisterX;
    private final double[] blisterProgress;
    private final double[] blisterSpeed;
    private final int[] blisterMaxRadius;

    // Flame Embers
    private final double[] emberX, emberY, emberVy, emberPhase;
    private final float[] emberAlpha;

    private BufferedImage poolBuffer;

    private double waveTime = 0.0;
    private boolean solidLeft = false, solidRight = false;
    private boolean edgeChecked = false;

    private boolean playerSubmerged = false;
    private double lastPlayerX = -1;
    private boolean wasPlayerInAir = false;
    private int wadeTick = 0;
    private int slashCooldown = 0;

    private EffectManager effectManager;
    private final Random rand = new Random();

    public Lava(int xPos, int yPos, int tilesWide, int tilesHigh) {
        super(ObjType.LAVA, xPos, yPos);
        this.poolWidth = tilesWide * TILES_SIZE;
        this.poolHeight = tilesHigh * TILES_SIZE + (int) (4 * SCALE);
        this.surfaceYOffset = (int) (9 * SCALE);

        initHitBox(poolWidth, poolHeight - surfaceYOffset);
        this.hitBox.x = xPos;
        this.hitBox.y = yPos + surfaceYOffset;

        this.numSamples = Math.max(20, (poolWidth / SAMPLE_SPACING) + 2);
        this.renderX = new double[numSamples];
        this.renderY = new double[numSamples];

        int blisterCount = Math.max(3, tilesWide * 2);
        this.blisterX = new double[blisterCount];
        this.blisterProgress = new double[blisterCount];
        this.blisterSpeed = new double[blisterCount];
        this.blisterMaxRadius = new int[blisterCount];
        initBlisters();

        int emberCount = Math.max(5, tilesWide * 3);
        this.emberX = new double[emberCount];
        this.emberY = new double[emberCount];
        this.emberVy = new double[emberCount];
        this.emberPhase = new double[emberCount];
        this.emberAlpha = new float[emberCount];
        initEmbers();
    }

    private void initBlisters() {
        for (int i = 0; i < blisterX.length; i++) {
            resetBlister(i, true);
        }
    }

    private void resetBlister(int i, boolean randomPhase) {
        double margin = 12 * SCALE;
        blisterX[i] = margin + rand.nextDouble() * Math.max(8, poolWidth - margin * 2);
        blisterProgress[i] = randomPhase ? rand.nextDouble() : 0.0;
        blisterSpeed[i] = 0.005 + rand.nextDouble() * 0.006;
        blisterMaxRadius[i] = Math.max(3, (int) ((3.5 + rand.nextDouble() * 3.0) * SCALE));
    }

    private void initEmbers() {
        for (int i = 0; i < emberX.length; i++) {
            resetEmber(i, true);
        }
    }

    private void resetEmber(int i, boolean randomY) {
        double margin = 8 * SCALE;
        emberX[i] = xPos + margin + rand.nextDouble() * Math.max(8, poolWidth - margin * 2);
        emberY[i] = hitBox.y - (randomY ? rand.nextDouble() * 22 * SCALE : 0);
        emberVy[i] = (0.10 + rand.nextDouble() * 0.16) * SCALE;
        emberPhase[i] = rand.nextDouble() * Math.PI * 2;
        emberAlpha[i] = 0.70f + rand.nextFloat() * 0.30f;
    }

    public void setEffectManager(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    @Override
    public void update() {
        if (slashCooldown > 0) slashCooldown--;
        waveTime += 0.010;

        Iterator<MagmaPulse> it = activePulses.iterator();
        while (it.hasNext()) {
            MagmaPulse p = it.next();
            p.age += 1.0;
            p.amplitude *= p.decay;
            double currentX = p.startX + p.dir * p.speed * p.age;
            if (currentX < -p.radius || currentX > poolWidth + p.radius || Math.abs(p.amplitude) < 0.12 * SCALE) {
                it.remove();
            }
        }

        for (int i = 0; i < blisterX.length; i++) {
            blisterProgress[i] += blisterSpeed[i];
            if (blisterProgress[i] >= 1.0) {
                if (activePulses.size() < 12) {
                    activePulses.add(new MagmaPulse(blisterX[i], 0, 0.0, 1.8 * SCALE, 0.95, 9 * SCALE));
                }
                resetBlister(i, false);
            }
        }

        for (int i = 0; i < emberX.length; i++) {
            emberY[i] -= emberVy[i];
            emberPhase[i] += 0.04;
            emberX[i] += Math.sin(emberPhase[i]) * (0.10 * SCALE);
            emberAlpha[i] -= 0.0055f;
            if (emberAlpha[i] <= 0.05f || emberY[i] < hitBox.y - 28 * SCALE) {
                resetEmber(i, false);
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
            playerSubmerged = true;
            double px = pBox.getCenterX();
            plunge(px, 1.15, true);
            lastPlayerX = px;
            wasPlayerInAir = player.isInAir();
        }
        else if (intersectingNow) {
            double px = pBox.getCenterX();
            boolean inAirNow = player.isInAir();

            if (!wasPlayerInAir && inAirNow && player.getAirSpeed() < -0.5) {
                plunge(px, 0.85, true);
            }
            else if (wasPlayerInAir && !inAirNow) {
                plunge(px, 1.20, true);
            }

            double dx = (lastPlayerX != -1) ? (px - lastPlayerX) : 0;
            if (Math.abs(dx) > 0.2) {
                int dir = dx > 0 ? 1 : -1;
                wadeTick++;
                if (wadeTick >= 18) {
                    wadeTick = 0;
                    double localX = Math.max(6 * SCALE, Math.min(poolWidth - 6 * SCALE, px - xPos));
                    activePulses.add(new MagmaPulse(localX + dir * (6 * SCALE), dir, RIPPLE_TRAVEL_SPEED, -(3.5 * SCALE), 0.982, 15 * SCALE));
                    if (effectManager != null) {
                        effectManager.spawnDustParticles(px, hitBox.y, 4, DustType.PLAYER_BURN, -dir, player);
                    }
                }
            }
            lastPlayerX = px;
            wasPlayerInAir = inAirNow;
        }
        else if (playerSubmerged) {
            playerSubmerged = false;
            plunge(pBox.getCenterX(), 0.80, true);
            lastPlayerX = -1;
            wasPlayerInAir = false;
        }
    }

    public void plunge(double worldX, double intensity, boolean withSparks) {
        double localX = Math.max(8 * SCALE, Math.min(poolWidth - 8 * SCALE, worldX - xPos));
        double depth = SPLASH_DEPTH * intensity;

        double craterRadius = PLAYER_HB_WID * 1.45;
        activePulses.add(new MagmaPulse(localX, 0, 0.0, depth, 0.972, craterRadius));

        double startOffset = PLAYER_HB_WID * 0.45;
        double waveRadius = 18.0 * SCALE;
        double waveHeight = -depth * 0.65;
        activePulses.add(new MagmaPulse(localX - startOffset, -1, RIPPLE_TRAVEL_SPEED, waveHeight, 0.985, waveRadius));
        activePulses.add(new MagmaPulse(localX + startOffset,  1, RIPPLE_TRAVEL_SPEED, waveHeight, 0.985, waveRadius));

        if (withSparks && effectManager != null) {
            effectManager.spawnDustParticles(worldX, hitBox.y, 10, DustType.IMPACT_SPARK, 0, null);
            effectManager.spawnDustParticles(worldX, hitBox.y, 8, DustType.PLAYER_BURN, 0, null);
        }
    }

    public void onAttackSlash(Rectangle2D.Double attackBox, int flipSign) {
        if (slashCooldown > 0) return;
        if (attackBox.y > hitBox.y + 24 * SCALE) return;

        slashCooldown = 16;
        double slashX = Math.max(hitBox.x + 8, Math.min(hitBox.getMaxX() - 8, attackBox.getCenterX()));
        plunge(slashX, 0.90, true);
    }

    private double evaluateSurfaceDisplacement(double localX) {
        double totalY = 0.0;
        for (MagmaPulse p : activePulses) {
            double center = p.startX + p.dir * p.speed * p.age;
            double dist = Math.abs(localX - center);
            if (dist >= p.radius) continue;

            double norm = dist / p.radius;
            double cosVal = Math.cos(norm * (Math.PI / 2.0));
            double shape = cosVal * cosVal;

            if (p.dir == 0) {
                double rebound = Math.cos(p.age * 0.045);
                totalY += p.amplitude * shape * rebound;
            }
            else {
                double fadeIn = Math.min(1.0, p.age / 10.0);
                totalY += p.amplitude * shape * fadeIn;
            }
        }
        double maxDisp = Math.min(hitBox.height * 0.60, 14.0 * SCALE);
        return Math.max(-maxDisp, Math.min(maxDisp, totalY));
    }

    private double getIdleMagmaWaveOffset(int i) {
        double xNorm = (double) i / (numSamples - 1);
        double wallTaper = Math.sin(xNorm * Math.PI);
        double w1 = Math.sin(waveTime * 0.55 - xNorm * Math.PI * 2.0) * (0.75 * SCALE);
        double w2 = Math.cos(waveTime * 0.35 + xNorm * Math.PI * 3.0) * (0.45 * SCALE);
        return (w1 + w2) * (0.20 + 0.80 * wallTaper);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int padLeft = solidLeft ? (int) (6 * SCALE) : 0;
        int padRight = solidRight ? (int) (6 * SCALE) : 0;
        int topHeadroom = (int) (16 * SCALE);

        int bufW = poolWidth + padLeft + padRight;
        int bufH = poolHeight + topHeadroom + (int) (4 * SCALE);
        int screenX = xPos - padLeft - xLevelOffset;
        int screenY = yPos - topHeadroom - yLevelOffset;

        if (screenX + bufW < -64 || screenX > GAME_WIDTH + 64 || screenY + bufH < -64 || screenY > GAME_HEIGHT + 64) {
            return;
        }

        if (poolBuffer == null || poolBuffer.getWidth() != bufW || poolBuffer.getHeight() != bufH) {
            poolBuffer = new BufferedImage(bufW, bufH, BufferedImage.TYPE_INT_ARGB);
        }

        double baseSurfaceY = surfaceYOffset + topHeadroom;
        double stepX = (double) bufW / (numSamples - 1);
        double minSurfaceY = baseSurfaceY;

        for (int i = 0; i < numSamples; i++) {
            renderX[i] = i * stepX;
            double localX = (i * stepX) - padLeft;
            double wallEdgeTaper = Math.min(1.0, Math.min(i, (numSamples - 1) - i) / 3.0);
            double waveDisp = evaluateSurfaceDisplacement(localX) * (0.20 + 0.80 * wallEdgeTaper);

            double targetY = baseSurfaceY + getIdleMagmaWaveOffset(i) + waveDisp;
            renderY[i] = Math.min(bufH - 3 * SCALE, targetY);
            if (renderY[i] < minSurfaceY) minSurfaceY = renderY[i];
        }

        Graphics2D bG2d = poolBuffer.createGraphics();
        bG2d.setComposite(AlphaComposite.Clear);
        bG2d.fillRect(0, 0, bufW, bufH);
        bG2d.setComposite(AlphaComposite.SrcOver);
        bG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bG2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Main molten magma body
        Path2D.Double surfaceCurve = buildSmoothCurve(renderX, renderY, 0.0);
        Path2D.Double lavaBody = new Path2D.Double(surfaceCurve);
        lavaBody.lineTo(renderX[numSamples - 1], bufH);
        lavaBody.lineTo(renderX[0], bufH);
        lavaBody.closePath();

        LinearGradientPaint bodyPaint = new LinearGradientPaint(
                0f, (float) minSurfaceY, 0f, (float) bufH,
                new float[]{0.0f, 0.45f, 1.0f},
                new Color[]{MAGMA_UPPER_COLOR, MAGMA_MID_COLOR, MAGMA_DEEP_COLOR}
        );
        bG2d.setPaint(bodyPaint);
        bG2d.fill(lavaBody);

        Shape oldClip = bG2d.getClip();
        bG2d.clip(lavaBody);

        // Horizontal subsurface magma veins
        renderSubsurfaceMagmaVeins(bG2d);

        // Molten sub-crust glow band
        bG2d.setColor(new Color(255, 145, 15, 115));
        bG2d.setStroke(new BasicStroke(7.0f * SCALE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        bG2d.draw(surfaceCurve);

        bG2d.setColor(new Color(255, 220, 65, 175));
        bG2d.setStroke(new BasicStroke(3.2f * SCALE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        bG2d.draw(surfaceCurve);

        // Surface blisters along the crust
        renderSurfaceBlisters(bG2d, padLeft, 0);
        renderDriftingCrustPlates(bG2d);
        bG2d.setClip(oldClip);
        applyAlphaFeatherMask(bG2d, bufW, bufH, (int) minSurfaceY);
        bG2d.dispose();

        g.drawImage(poolBuffer, screenX, screenY, null);

        // Embers
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < emberX.length; i++) {
            int ex = (int) (emberX[i] - xLevelOffset);
            int ey = (int) (emberY[i] - yLevelOffset);
            int alpha = Math.max(0, Math.min(255, (int) (emberAlpha[i] * 240)));
            int es = Math.max(2, (int) (1.8 * SCALE));
            g2d.setColor(new Color(255, 180, 40, alpha));
            g2d.fillOval(ex, ey, es, es);
        }
        g2d.dispose();

        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    private void applyAlphaFeatherMask(Graphics2D bG2d, int bufW, int bufH, int topY) {
        bG2d.setComposite(AlphaComposite.DstIn);

        Color opaque = new Color(0, 0, 0, 255);
        Color semi = new Color(0, 0, 0, 110);
        Color transparent = new Color(0, 0, 0, 0);

        int bottomFadeH = (int) (8 * SCALE);
        int bottomFadeY = bufH - bottomFadeH;
        LinearGradientPaint bottomMask = new LinearGradientPaint(0f, bottomFadeY, 0f, bufH, new float[]{0.0f, 0.5f, 1.0f}, new Color[]{opaque, semi, transparent});
        bG2d.setPaint(bottomMask);
        bG2d.fillRect(0, bottomFadeY, bufW, bottomFadeH);

        int sideW = (int) (8 * SCALE);
        if (solidLeft) {
            LinearGradientPaint leftMask = new LinearGradientPaint(0f, 0f, sideW, 0f, new float[]{0.0f, 0.5f, 1.0f}, new Color[]{transparent, semi, opaque});
            bG2d.setPaint(leftMask);
            bG2d.fillRect(0, 0, sideW, bufH);
        }
        if (solidRight) {
            int rightStart = bufW - sideW;
            LinearGradientPaint rightMask = new LinearGradientPaint(rightStart, 0f, bufW, 0f, new float[]{0.0f, 0.5f, 1.0f}, new Color[]{opaque, semi, transparent});
            bG2d.setPaint(rightMask);
            bG2d.fillRect(rightStart, 0, sideW, bufH);
        }

        bG2d.setComposite(AlphaComposite.SrcOver);
    }

    private void renderSubsurfaceMagmaVeins(Graphics2D g2d) {
        double[] depthOffsets = {7.5 * SCALE, 14.5 * SCALE};
        Color[] veinColors = {new Color(255, 155, 25, 70), new Color(215, 65, 10, 45)};
        float[] thicknesses = {2.6f * SCALE, 1.8f * SCALE};
        for (int v = 0; v < depthOffsets.length; v++) {
            Path2D.Double vein = new Path2D.Double();
            double baseDepth = depthOffsets[v];
            for (int i = 0; i < numSamples; i++) {
                double xNorm = (double) i / (numSamples - 1);
                double undulation = Math.sin(waveTime * (0.8 + v * 0.3) + xNorm * Math.PI * (3.5 + v)) * (2.2 * SCALE);
                double vy = renderY[i] + baseDepth + undulation;
                if (i == 0) vein.moveTo(renderX[i], vy);
                else vein.lineTo(renderX[i], vy);
            }
            g2d.setColor(veinColors[v]);
            g2d.setStroke(new BasicStroke(thicknesses[v], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(vein);
        }
    }

    private void renderSurfaceBlisters(Graphics2D g2d, int padLeft, double drawX) {
        for (int i = 0; i < blisterX.length; i++) {
            double prog = blisterProgress[i];
            int idx = Math.max(0, Math.min(numSamples - 1, (int) (((blisterX[i] + padLeft) / (poolWidth + padLeft * 2.0)) * (numSamples - 1))));
            int bx = (int) (drawX + padLeft + blisterX[i]);
            int by = (int) renderY[idx];

            double swell = Math.sin(prog * Math.PI);
            int rx = Math.max(2, (int) (blisterMaxRadius[i] * swell));
            int ry = Math.max(2, (int) (blisterMaxRadius[i] * 0.65 * swell));

            g2d.setColor(new Color(255, 110, 15, (int) (190 * swell)));
            g2d.fillOval(bx - rx, by - ry, rx * 2, ry * 2);
            g2d.setColor(new Color(255, 240, 130, (int) (230 * swell)));
            g2d.fillOval(bx - rx / 2, by - ry / 2, rx, ry);
        }
    }

    private void renderDriftingCrustPlates(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(2.2f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(CRUST_SLAG_COLOR);

        Path2D.Double slagSegment = new Path2D.Double();
        boolean inSegment = false;

        for (int i = 0; i < numSamples; i++) {
            double xNorm = (double) i / (numSamples - 1);
            double pattern = Math.sin(xNorm * Math.PI * 5.0 - waveTime * 0.35) + 0.5 * Math.cos(xNorm * Math.PI * 9.0 + waveTime * 0.2);
            if (pattern > 0.35) {
                if (!inSegment) {
                    slagSegment.moveTo(renderX[i], renderY[i]);
                    inSegment = true;
                }
                else slagSegment.lineTo(renderX[i], renderY[i]);
            }
            else if (inSegment) {
                g2d.draw(slagSegment);
                slagSegment.reset();
                inSegment = false;
            }
        }
        if (inSegment) g2d.draw(slagSegment);
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

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    @Override
    public void onEnter(Player player) {
        player.addAction(PlayerAction.LAVA);
        updatePlayerPhysics(player);
    }

    @Override
    public void onIntersect(Player player) {
        if (!player.checkAction(PlayerAction.LAVA)) {
            player.addAction(PlayerAction.LAVA);
        }
        updatePlayerPhysics(player);
    }

    @Override
    public void onExit(Player player) {
        player.removeAction(PlayerAction.LAVA);
        updatePlayerPhysics(player);
    }

    @Override
    public String getInteractionPrompt() {
        return null;
    }
}