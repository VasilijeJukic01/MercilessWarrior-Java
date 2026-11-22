package platformer.model.gameObjects.objects.traps;

import platformer.debug.DebugSettings;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Ceiling-mounted toxic fungal spore sac.
 */
public class SporeTrap extends GameObject implements Interactable {

    private static final int CYCLE_TICKS = 360;
    private static final int FORMING_START_TICK = 230;
    private static final double DROP_GRAVITY = 0.045 * SCALE;
    private static final double DROP_MAX_SPEED = 2.6 * SCALE;
    private static final int TRAIL_SIZE = 6;
    private static final int CEILING_EMBED_Y = scale(12);

    private int cycleTick;
    private BufferedImage cleanTrimmedSprite;

    // Falling Venom Droplet State
    private boolean dropActive = false;
    private double dropX, dropY, dropVy;
    private final Rectangle2D.Double dropHitBox = new Rectangle2D.Double(0, 0, scale(11), scale(15));
    private final double[] trailY = new double[TRAIL_SIZE];
    private int trailCount = 0;

    private EffectManager effectManager;

    public SporeTrap(int xPos, int yPos) {
        super(ObjType.SPORE_TRAP, xPos, yPos);
        this.cycleTick = (int) ((xPos * 0.7) % CYCLE_TICKS);
        initHitBox(SPORE_TRAP_HB_WID, SPORE_TRAP_HB_HEI);
        super.xOffset = SPORE_TRAP_OFFSET_X;
        super.yOffset = SPORE_TRAP_OFFSET_Y;
        this.hitBox.x = xPos + xOffset;
        this.hitBox.y = yPos - CEILING_EMBED_Y + scale(14);
    }

    public void setEffectManager(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    private void ensurePrepared(BufferedImage raw) {
        if (cleanTrimmedSprite != null || raw == null) return;
        int w = raw.getWidth(), h = raw.getHeight();
        BufferedImage cleaned = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        int minX = w, minY = h, maxX = 0, maxY = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = raw.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a <= 28) continue;

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                boolean isLightHalo = (r > 180 && g > 180 && b > 180) && (Math.abs(r - g) < 22 && Math.abs(g - b) < 22);
                if (isLightHalo) continue;

                cleaned.setRGB(x, y, argb);
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        }
        if (minX <= maxX && minY <= maxY) {
            cleanTrimmedSprite = cleaned.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
        }
        else cleanTrimmedSprite = raw;
    }

    @Override
    public void update() {
        // Handled in update(int[][] levelData)
    }

    @Override
    public void update(int[][] levelData) {
        cycleTick++;
        if (cycleTick >= CYCLE_TICKS) {
            cycleTick = 0;
            spawnVenomDrop();
        }

        if (dropActive) updateVenomDrop(levelData);
    }

    private double getSacTipWorldY() {
        return (yPos - CEILING_EMBED_Y) + SPORE_TRAP_HEI - scale(2);
    }

    private void spawnVenomDrop() {
        dropActive = true;
        dropX = hitBox.getCenterX();
        dropY = getSacTipWorldY() + scale(3);
        dropVy = 0.35 * SCALE;
        trailCount = 0;
        syncDropHitbox();
    }

    private void updateVenomDrop(int[][] levelData) {
        for (int i = Math.min(trailCount, TRAIL_SIZE - 1); i > 0; i--) {
            trailY[i] = trailY[i - 1];
        }
        trailY[0] = dropY;
        if (trailCount < TRAIL_SIZE) trailCount++;

        dropVy = Math.min(DROP_MAX_SPEED, dropVy + DROP_GRAVITY);
        dropY += dropVy;
        syncDropHitbox();

        if (levelData != null && CollisionDetector.isSolid(dropX, dropHitBox.getMaxY(), levelData)) {
            burstVenomDrop();
        }
    }

    private void syncDropHitbox() {
        dropHitBox.x = dropX - dropHitBox.width / 2.0;
        dropHitBox.y = dropY - dropHitBox.height / 2.0;
    }

    public void checkPlayerDropHit(Player player) {
        if (!dropActive || player == null) return;
        if (dropHitBox.intersects(player.getHitBox())) {
            player.changeHealth(-18, this);
            burstVenomDrop();
        }
    }

    private void burstVenomDrop() {
        dropActive = false;
        if (effectManager != null) {
            effectManager.spawnDustParticles(dropX, dropY, 10, DustType.HERB_CUT, 0, null);
        }
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        if (animations == null || animations.length == 0 || animations[0] == null) return;
        ensurePrepared(animations[0]);

        int anchorCenterX = (int) Math.round(hitBox.getCenterX() - xLevelOffset);
        int topCeilingY = yPos - CEILING_EMBED_Y - yLevelOffset;
        int drawX = anchorCenterX - (SPORE_TRAP_WID / 2);

        g.drawImage(cleanTrimmedSprite, drawX, topCeilingY, SPORE_TRAP_WID, SPORE_TRAP_HEI, null);
        renderBioluminescentPulseAndFormingDrop(g, anchorCenterX, topCeilingY);

        if (dropActive) {
            renderVenomDrop(g, xLevelOffset, yLevelOffset);
        }
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.GREEN);
    }

    /**
     * Renders an internal bioluminescent pulse over the lower bulb and a forming liquid droplet at the tip before it detaches.
     */
    private void renderBioluminescentPulseAndFormingDrop(Graphics g, int centerX, int topY) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int bulbCenterY = topY + (int) (SPORE_TRAP_HEI * 0.68);
        int tipY = topY + SPORE_TRAP_HEI - scale(2);

        float chargeProgress = 0.0f;
        if (cycleTick >= FORMING_START_TICK) {
            chargeProgress = (float) (cycleTick - FORMING_START_TICK) / (CYCLE_TICKS - FORMING_START_TICK);
        }
        float breath = (float) (0.25 + 0.20 * Math.sin(cycleTick * 0.05) + 0.55 * chargeProgress);
        int glowR = (int) ((16 + 8 * chargeProgress) * SCALE);
        int glowAlpha = Math.min(185, (int) (breath * 165));

        RadialGradientPaint sacGlow = new RadialGradientPaint(
                new Point2D.Float(centerX, bulbCenterY), glowR,
                new float[]{0.0f, 0.6f, 1.0f},
                new Color[]{new Color(170, 255, 120, glowAlpha), new Color(60, 220, 80, glowAlpha / 2), new Color(60, 20, 110, 0)}
        );
        g2d.setPaint(sacGlow);
        g2d.fillOval(centerX - glowR, bulbCenterY - glowR, glowR * 2, glowR * 2);

        if (chargeProgress > 0.02f) {
            double hw = (1.5 + 3.6 * chargeProgress) * SCALE;
            double hh = (2.0 + 5.8 * chargeProgress) * SCALE;
            double stretchOffset = (chargeProgress * chargeProgress) * (3.5 * SCALE);
            double dropCenterY = tipY + stretchOffset;

            Path2D.Double formingTear = new Path2D.Double();
            formingTear.moveTo(centerX, tipY - scale(1));
            formingTear.quadTo(centerX + hw * 1.25, dropCenterY + hh * 0.25, centerX, dropCenterY + hh);
            formingTear.quadTo(centerX - hw * 1.25, dropCenterY + hh * 0.25, centerX, tipY - scale(1));
            formingTear.closePath();

            g2d.setColor(new Color(95, 250, 95, 235));
            g2d.fill(formingTear);
            if (chargeProgress > 0.4f) {
                g2d.setColor(new Color(230, 255, 210, 220));
                g2d.fillOval(centerX - scale(1.5), (int) (dropCenterY + hh * 0.2), scale(3), scale(3));
            }
        }

        g2d.dispose();
    }

    private void renderVenomDrop(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int sx = (int) Math.round(dropX - xLevelOffset);
        int sy = (int) Math.round(dropY - yLevelOffset);

        for (int i = 0; i < trailCount; i++) {
            float ratio = 1.0f - ((float) i / TRAIL_SIZE);
            int ty = (int) Math.round(trailY[i] - yLevelOffset);
            int r = Math.max(2, (int) (4.2 * SCALE * ratio));
            g2d.setColor(new Color(80, 255, 110, (int) (145 * ratio)));
            g2d.fillOval(sx - r, ty - r, r * 2, r * 2);
        }

        int glowR = scale(13);
        RadialGradientPaint glow = new RadialGradientPaint(
                new Point2D.Float(sx, sy), glowR,
                new float[]{0.0f, 0.55f, 1.0f},
                new Color[]{new Color(210, 255, 160, 235), new Color(65, 235, 90, 160), new Color(90, 20, 140, 0)}
        );
        g2d.setPaint(glow);
        g2d.fillOval(sx - glowR, sy - glowR, glowR * 2, glowR * 2);

        double hw = 5.2 * SCALE;
        double hh = 7.8 * SCALE;
        Path2D.Double tear = new Path2D.Double();
        tear.moveTo(sx, sy - hh);
        tear.quadTo(sx + hw * 1.25, sy + hh * 0.25, sx, sy + hh);
        tear.quadTo(sx - hw * 1.25, sy + hh * 0.25, sx, sy - hh);
        tear.closePath();

        g2d.setColor(new Color(95, 250, 95, 245));
        g2d.fill(tear);
        g2d.setColor(new Color(230, 255, 210, 240));
        g2d.fillOval(sx - scale(2), sy - scale(1), scale(4), scale(4));

        g2d.dispose();
    }

    @Override
    public void onEnter(Player player) {
        player.changeHealth(-15, this);
    }

    @Override
    public void onIntersect(Player player) {
        player.changeHealth(-15, this);
    }

    @Override
    public void onExit(Player player) {

    }

    @Override
    public String getInteractionPrompt() {
        return null;
    }

    @Override
    public void reset() {
        super.reset();
        dropActive = false;
        trailCount = 0;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
        if (dropActive && DebugSettings.getInstance().isDebugMode()) {
            g.setColor(Color.YELLOW);
            g.drawRect((int) dropHitBox.x - xLevelOffset, (int) dropHitBox.y - yLevelOffset, (int) dropHitBox.width, (int) dropHitBox.height);
        }
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}