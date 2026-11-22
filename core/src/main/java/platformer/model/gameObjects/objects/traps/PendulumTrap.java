package platformer.model.gameObjects.objects.traps;

import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Ceiling-mounted swinging pendulum scythe trap.
 * Keeps the top mossy stone ceiling bracket embedded flush into the ceiling tile while swinging the chain and blade around the iron ring pivot.
 * Only the blade head deals hazard damage.
 */
public class PendulumTrap extends GameObject implements Interactable {

    private static final double MAX_SWING_ANGLE_RAD = Math.toRadians(54.0);
    private static final double SWING_SPEED = 0.022;
    private static final double PIVOT_Y_RATIO = 0.145;
    private static final double BLADE_CENTER_Y_RATIO = 0.80;
    private static final int CEILING_EMBED_OFFSET_Y = scale(10);

    private double swingPhase;
    private double currentAngle = 0.0;

    private BufferedImage bracketImage;
    private BufferedImage chainBladeImage;
    private int bracketDrawH;
    private int pivotLocalY;

    public PendulumTrap(int xPos, int yPos) {
        super(ObjType.PENDULUM_TRAP, xPos, yPos);
        this.swingPhase = ((xPos / (double) TILES_SIZE) * 0.85) % (Math.PI * 2.0);
        initHitBox(PENDULUM_BLADE_HB_SIZE, PENDULUM_BLADE_HB_SIZE);
        updateBladeHitboxPosition();
    }

    private void ensureSplitSprites(BufferedImage fullSprite) {
        if (bracketImage != null || fullSprite == null) return;
        int srcW = fullSprite.getWidth();
        int srcH = fullSprite.getHeight();
        int splitSrcY = Math.max(1, (int) Math.round(srcH * PIVOT_Y_RATIO));

        this.bracketImage = fullSprite.getSubimage(0, 0, srcW, splitSrcY);
        this.chainBladeImage = fullSprite.getSubimage(0, Math.max(0, splitSrcY - 12), srcW, srcH - Math.max(0, splitSrcY - 12));
        this.bracketDrawH = (int) Math.round(PENDULUM_HEI * PIVOT_Y_RATIO);
        this.pivotLocalY = bracketDrawH - scale(3);
    }

    @Override
    public void update() {
        swingPhase += SWING_SPEED;
        if (swingPhase > Math.PI * 20.0) {
            swingPhase -= Math.PI * 20.0;
        }
        currentAngle = MAX_SWING_ANGLE_RAD * Math.sin(swingPhase);
        updateBladeHitboxPosition();
    }

    private void updateBladeHitboxPosition() {
        double pivotWorldX = xPos + (TILES_SIZE / 2.0);
        double pivotWorldY = (yPos - CEILING_EMBED_OFFSET_Y) + (PENDULUM_HEI * PIVOT_Y_RATIO);
        double armLength = PENDULUM_HEI * (BLADE_CENTER_Y_RATIO - PIVOT_Y_RATIO);

        double bladeCenterX = pivotWorldX - Math.sin(currentAngle) * armLength;
        double bladeCenterY = pivotWorldY + Math.cos(currentAngle) * armLength;

        hitBox.x = bladeCenterX - (hitBox.width / 2.0);
        hitBox.y = bladeCenterY - (hitBox.height / 2.0);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        if (animations == null || animations.length == 0 || animations[0] == null) return;
        ensureSplitSprites(animations[0]);

        int pivotScreenX = (int) Math.round(xPos + (TILES_SIZE / 2.0) - xLevelOffset);
        int topScreenY = yPos - CEILING_EMBED_OFFSET_Y - yLevelOffset;
        int pivotScreenY = topScreenY + pivotLocalY;

        int drawLeftX = pivotScreenX - (PENDULUM_WID / 2);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform old = g2d.getTransform();
        g2d.translate(pivotScreenX, pivotScreenY);
        g2d.rotate(currentAngle);
        int chainDrawH = PENDULUM_HEI - bracketDrawH + scale(3);
        g2d.drawImage(chainBladeImage, -PENDULUM_WID / 2, 0, PENDULUM_WID, chainDrawH, null);
        g2d.setTransform(old);

        g2d.drawImage(bracketImage, drawLeftX, topScreenY, PENDULUM_WID, bracketDrawH + scale(2), null);
        g2d.dispose();

        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
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
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}