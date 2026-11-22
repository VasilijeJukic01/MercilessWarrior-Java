package platformer.model.gameObjects.objects.traps;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Spike extends GameObject implements Interactable {

    private static final int GLINT_CYCLE_TICKS = 680;
    private static final int GLINT_ACTIVE_TICKS = 22;

    private final Direction direction;
    private int glintTick;
    private int glintToothIndex;

    public Spike(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);

        switch (objType) {
            case SPIKE_DOWN -> this.direction = Direction.DOWN;
            case SPIKE_LEFT -> this.direction = Direction.LEFT;
            case SPIKE_RIGHT -> this.direction = Direction.RIGHT;
            default -> this.direction = Direction.UP;
        }
        int hash = Math.abs((xPos * 31) ^ (yPos * 17));
        this.glintTick = hash % GLINT_CYCLE_TICKS;
        this.glintToothIndex = hash % 3;
        calculateHitboxAndOffsets();
    }

    private void calculateHitboxAndOffsets() {
        switch (direction) {
            case UP -> {
                initHitBox(SPIKE_HB_WID, SPIKE_HB_HEI);
                hitBox.x = xPos + (TILES_SIZE - SPIKE_HB_WID) / 2.0;
                hitBox.y = yPos + (TILES_SIZE - SPIKE_HB_HEI);
            }
            case DOWN -> {
                initHitBox(SPIKE_HB_WID, SPIKE_HB_HEI);
                hitBox.x = xPos + (TILES_SIZE - SPIKE_HB_WID) / 2.0;
                hitBox.y = yPos;
            }
            case LEFT -> {
                initHitBox(SPIKE_HB_HEI, SPIKE_HB_WID);
                hitBox.x = xPos + (TILES_SIZE - SPIKE_HB_HEI);
                hitBox.y = yPos + (TILES_SIZE - SPIKE_HB_WID) / 2.0;
            }
            case RIGHT -> {
                initHitBox(SPIKE_HB_HEI, SPIKE_HB_WID);
                hitBox.x = xPos;
                hitBox.y = yPos + (TILES_SIZE - SPIKE_HB_WID) / 2.0;
            }
            default -> {}
        }
    }

    @Override
    public void update() {
        glintTick++;
        if (glintTick >= GLINT_CYCLE_TICKS) {
            glintTick = 0;
            glintToothIndex = (glintToothIndex + 1) % 3;
        }
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int drawX = xPos - xLevelOffset;
        int drawY = yPos - yLevelOffset;
        int drawWid = SPIKE_WID;
        int drawHei = SPIKE_HEI;
        int centerAlign = (TILES_SIZE - SPIKE_WID) / 2;

        switch (direction) {
            case UP -> {
                drawX += centerAlign;
                drawY += (TILES_SIZE - SPIKE_HEI) + scale(12);
            }
            case DOWN -> {
                drawX += centerAlign;
                drawY -= scale(14);
            }
            case LEFT -> {
                drawWid = SPIKE_HEI;
                drawHei = SPIKE_WID;
                drawX += (TILES_SIZE - SPIKE_HEI) + scale(12);
                drawY += centerAlign;
            }
            case RIGHT -> {
                drawWid = SPIKE_HEI;
                drawHei = SPIKE_WID;
                drawX -= scale(12);
                drawY += centerAlign;
            }
            default -> {}
        }

        g.drawImage(animations[0], drawX, drawY, drawWid, drawHei, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    /**
     * Renders an occasional, razor-sharp metallic glint directly on one of the steel blade tips.
     */
    public void renderTipGlint(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (glintTick >= GLINT_ACTIVE_TICKS) return;

        int baseScreenX = xPos - xLevelOffset;
        int baseScreenY = yPos - yLevelOffset;
        if (baseScreenX < -TILES_SIZE || baseScreenX > GAME_WIDTH + TILES_SIZE
                || baseScreenY < -TILES_SIZE || baseScreenY > GAME_HEIGHT + TILES_SIZE) {
            return;
        }

        double progress = (double) glintTick / GLINT_ACTIVE_TICKS;
        double intensity = Math.sin(progress * Math.PI);
        if (intensity <= 0.05) return;

        double[] tipFractions = {0.18, 0.50, 0.82};
        Point tip = getTipScreenPosition(baseScreenX, baseScreenY, tipFractions[glintToothIndex]);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double outerR = (1.5 + 3.5 * intensity) * SCALE;
        double innerR = 0.65 * SCALE;
        int alpha = Math.max(0, Math.min(255, (int) (235 * intensity)));

        Path2D.Double star = new Path2D.Double();
        for (int p = 0; p < 8; p++) {
            double angle = p * (Math.PI / 4.0);
            double r = (p % 2 == 0) ? outerR : innerR;
            double px = tip.x + Math.cos(angle) * r;
            double py = tip.y + Math.sin(angle) * r;
            if (p == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }
        star.closePath();

        g2d.setColor(new Color(220, 250, 255, alpha));
        g2d.fill(star);
        g2d.dispose();
    }

    private Point getTipScreenPosition(int baseScreenX, int baseScreenY, double frac) {
        int centerAlign = (TILES_SIZE - SPIKE_WID) / 2;
        int along = (int) Math.round(centerAlign + frac * SPIKE_WID);
        return switch (direction) {
            case UP -> new Point(baseScreenX + along, baseScreenY + (TILES_SIZE - SPIKE_HEI) + scale(15));
            case DOWN -> new Point(baseScreenX + along, baseScreenY + SPIKE_HEI - scale(16));
            case LEFT -> new Point(baseScreenX + (TILES_SIZE - SPIKE_HEI) + scale(15), baseScreenY + along);
            case RIGHT -> new Point(baseScreenX + SPIKE_HEI - scale(16), baseScreenY + along);
            default -> new Point(baseScreenX + along, baseScreenY);
        };
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
        player.triggerHazardRespawn(20);
    }

    @Override
    public void onIntersect(Player player) {
        player.triggerHazardRespawn(20);
    }

    @Override
    public void onExit(Player player) {

    }

    @Override
    public String getInteractionPrompt() {
        return null;
    }
}