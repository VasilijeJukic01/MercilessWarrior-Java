package platformer.model.gameObjects.objects.traps;

import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.model.levels.LvlTriggerType;
import platformer.model.levels.Trigger;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static platformer.constants.Constants.*;

/**
 * High-speed spinning runic sawblade trap.
 * Place a KILL_ZONE or EFFECT trigger at the destination tile in the same row or column (and optionally one on the saw's start tile) to define its exact patrol track.
 */
public class SawTrap extends GameObject implements Interactable {

    private static final double SPIN_SPEED = 0.085;
    private static final double BASE_PATROL_SPEED = 0.024;
    private static final int MAX_SEARCH_TILES = 16;

    private boolean trackInitialized = false;
    private boolean stationary = true;
    private double trackMinX, trackMaxX;
    private double trackMinY, trackMaxY;
    private double centerX, centerY;
    private double patrolStepSpeed = BASE_PATROL_SPEED;

    private double patrolPhase = -Math.PI / 2.0;
    private double spinAngle = 0.0;
    private int sparkTick = 0;

    private List<Trigger> levelTriggers = Collections.emptyList();
    private EffectManager effectManager;
    private final Random rand = new Random();

    public SawTrap(int xPos, int yPos) {
        super(ObjType.SAW_TRAP, xPos, yPos);
        this.centerX = xPos + (TILES_SIZE / 2.0);
        this.centerY = yPos + (TILES_SIZE / 2.0);
        this.trackMinX = this.trackMaxX = this.centerX;
        this.trackMinY = this.trackMaxY = this.centerY;
        initHitBox(SAW_TRAP_HB_SIZE, SAW_TRAP_HB_SIZE);
        syncHitboxToCenter();
    }

    public void setEffectManager(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void setLevelTriggers(List<Trigger> levelTriggers) {
        this.levelTriggers = (levelTriggers != null) ? levelTriggers : Collections.emptyList();
        this.trackInitialized = false;
    }

    private void initTrack(int[][] levelData) {
        trackInitialized = true;
        int startTileX = xPos / TILES_SIZE;
        int startTileY = yPos / TILES_SIZE;

        double embedOffsetX = 0;
        double embedOffsetY = 0;
        if (levelData != null) {
            boolean wallLeft = startTileX > 0 && CollisionDetector.isTileSolid(startTileX - 1, startTileY, levelData);
            boolean wallRight = startTileX + 1 < levelData.length && CollisionDetector.isTileSolid(startTileX + 1, startTileY, levelData);
            boolean floorBelow = startTileY + 1 < levelData[0].length && CollisionDetector.isTileSolid(startTileX, startTileY + 1, levelData);
            boolean ceilAbove = startTileY > 0 && CollisionDetector.isTileSolid(startTileX, startTileY - 1, levelData);

            if (wallLeft && !wallRight) embedOffsetX = -scale(8);
            else if (wallRight && !wallLeft) embedOffsetX = scale(8);
            if (ceilAbove && !floorBelow) embedOffsetY = -scale(8);
            else if (floorBelow && !ceilAbove) embedOffsetY = scale(8);
        }

        int minTileX = startTileX, maxTileX = startTileX;
        int minTileY = startTileY, maxTileY = startTileY;

        for (Trigger t : levelTriggers) {
            if (t.type() != LvlTriggerType.KILL_ZONE && t.type() != LvlTriggerType.EFFECT) continue;
            int tx = (int) Math.round(t.bounds().x / TILES_SIZE);
            int ty = (int) Math.round(t.bounds().y / TILES_SIZE);
            if (tx == startTileX && ty == startTileY) continue;

            // Vertical endpoint in the same column
            if (Math.abs(tx - startTileX) <= 1 && Math.abs(ty - startTileY) <= MAX_SEARCH_TILES) {
                minTileY = Math.min(minTileY, ty);
                maxTileY = Math.max(maxTileY, ty);
            }
            // Horizontal endpoint in the same row
            else if (Math.abs(ty - startTileY) <= 1 && Math.abs(tx - startTileX) <= MAX_SEARCH_TILES) {
                minTileX = Math.min(minTileX, tx);
                maxTileX = Math.max(maxTileX, tx);
            }
        }

        int spanX = maxTileX - minTileX;
        int spanY = maxTileY - minTileY;

        if (spanX == 0 && spanY == 0) {
            this.stationary = true;
            this.centerX = xPos + (TILES_SIZE / 2.0) + embedOffsetX;
            this.centerY = yPos + (TILES_SIZE / 2.0) + embedOffsetY;
            this.trackMinX = this.trackMaxX = this.centerX;
            this.trackMinY = this.trackMaxY = this.centerY;
            return;
        }

        this.stationary = false;
        if (spanY >= spanX) {
            double fixedX = xPos + (TILES_SIZE / 2.0) + embedOffsetX;
            this.trackMinX = this.trackMaxX = fixedX;
            this.trackMinY = minTileY * TILES_SIZE + (TILES_SIZE / 2.0);
            this.trackMaxY = maxTileY * TILES_SIZE + (TILES_SIZE / 2.0);
            this.patrolStepSpeed = BASE_PATROL_SPEED * (3.5 / Math.max(2.0, spanY));
            this.patrolPhase = (startTileY <= minTileY) ? (-Math.PI / 2.0) : (Math.PI / 2.0);
        }
        else {
            double fixedY = yPos + (TILES_SIZE / 2.0) + embedOffsetY;
            this.trackMinY = this.trackMaxY = fixedY;
            this.trackMinX = minTileX * TILES_SIZE + (TILES_SIZE / 2.0);
            this.trackMaxX = maxTileX * TILES_SIZE + (TILES_SIZE / 2.0);
            this.patrolStepSpeed = BASE_PATROL_SPEED * (3.5 / Math.max(2.0, spanX));
            this.patrolPhase = (startTileX <= minTileX) ? (-Math.PI / 2.0) : (Math.PI / 2.0);
        }
    }

    @Override
    public void update() {
        // Handled in update(int[][] levelData)
    }

    @Override
    public void update(int[][] levelData) {
        if (!trackInitialized && levelData != null) {
            initTrack(levelData);
        }

        spinAngle += SPIN_SPEED;
        if (spinAngle > Math.PI * 20.0) spinAngle -= Math.PI * 20.0;

        if (!stationary) {
            patrolPhase += patrolStepSpeed;
            if (patrolPhase > Math.PI * 20.0) patrolPhase -= Math.PI * 20.0;

            double t = 0.5 + 0.5 * Math.sin(patrolPhase);
            centerX = trackMinX + t * (trackMaxX - trackMinX);
            centerY = trackMinY + t * (trackMaxY - trackMinY);
        }
        syncHitboxToCenter();

        sparkTick++;
        if (sparkTick >= 18 && effectManager != null && rand.nextDouble() < 0.45) {
            sparkTick = 0;
            effectManager.spawnDustParticles(centerX, centerY, 1, DustType.IMPACT_SPARK, 0, null);
        }
    }

    private void syncHitboxToCenter() {
        hitBox.x = centerX - (hitBox.width / 2.0);
        hitBox.y = centerY - (hitBox.height / 2.0);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        if (animations == null || animations.length == 0 || animations[0] == null) return;
        BufferedImage blade = animations[0];

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (!stationary) {
            renderTrackRail(g2d, xLevelOffset, yLevelOffset);
        }

        int sx = (int) Math.round(centerX - xLevelOffset);
        int sy = (int) Math.round(centerY - yLevelOffset);

        AffineTransform old = g2d.getTransform();
        g2d.translate(sx, sy);
        g2d.rotate(spinAngle);
        g2d.drawImage(blade, -SAW_TRAP_SIZE / 2, -SAW_TRAP_SIZE / 2, SAW_TRAP_SIZE, SAW_TRAP_SIZE, null);
        g2d.setTransform(old);

        g2d.dispose();
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    private void renderTrackRail(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        int x1 = (int) Math.round(trackMinX - xLevelOffset);
        int y1 = (int) Math.round(trackMinY - yLevelOffset);
        int x2 = (int) Math.round(trackMaxX - xLevelOffset);
        int y2 = (int) Math.round(trackMaxY - yLevelOffset);

        g2d.setColor(new Color(12, 18, 22, 210));
        g2d.setStroke(new BasicStroke(4.5f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x1, y1, x2, y2);

        g2d.setColor(new Color(55, 225, 235, 120));
        g2d.setStroke(new BasicStroke(1.4f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x1, y1, x2, y2);

        int capR = scale(3.5);
        g2d.setColor(new Color(55, 235, 245, 190));
        g2d.fillOval(x1 - capR, y1 - capR, capR * 2, capR * 2);
        g2d.fillOval(x2 - capR, y2 - capR, capR * 2, capR * 2);
    }

    @Override
    public void onEnter(Player player) {
        player.triggerHazardRespawn(25);
    }

    @Override
    public void onIntersect(Player player) {
        player.triggerHazardRespawn(25);
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
        trackInitialized = false;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}