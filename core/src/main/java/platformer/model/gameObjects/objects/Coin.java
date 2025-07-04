package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Coin extends GameObject {

    private double floatOffset;
    private final int maxFloatOffset;
    private int floatDir = 1;

    private double airSpeed, xSpeed;
    private boolean inAir = true;

    public Coin(ObjType objType, int xPos, int yPos) {
        this(objType, xPos, yPos, 0, 0);
    }

    public Coin(ObjType objType, int xPos, int yPos, double xSpeed, double ySpeed) {
        super(objType, xPos, yPos);
        this.xSpeed = xSpeed;
        this.airSpeed = ySpeed;
        this.maxFloatOffset = (int)(5*SCALE);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(COIN_HB_SIZE, COIN_HB_SIZE);
        super.xOffset = COIN_OFFSET;
        super.yOffset = COIN_OFFSET;
    }

    private void updateFloating() {
        floatOffset += (0.065 * SCALE * floatDir);
        if (floatOffset >= maxFloatOffset) floatDir = -1;
        else if (floatOffset < 0) floatDir = 1;
        hitBox.y = yPos+floatOffset;
    }

    /**
     * Updates the coin's position based on simple physics simulation, including, air friction, and collisions with the level's terrain.
     * This method is only active while the coin is in the air.
     * <p>
     * The simulation uses basic kinematic equations for projectile motion under constant acceleration:
     * <ul>
     *     <li><b>Velocity Update:</b> {@code v = u + at}, where gravity 'g' is the constant acceleration 'a'.
     *         This is implemented as {@code airSpeed += COIN_GRAVITY}.</li>
     *     <li><b>Position Update:</b> {@code s = ut + (1/2)at^2}. In each frame (where t=1), this simplifies to
     *         updating the position by the current velocity: {@code hitBox.y += airSpeed}.</li>
     * </ul>
     * The collision handling works as follows:
     * <ul>
     *     <li><b>Vertical Collision:</b>
     *         <ul>
     *             <li>If the coin is moving upwards ({@code airSpeed < 0}) and hits a ceiling, its vertical velocity is reversed and dampened, simulating an inelastic collision.</li>
     *             <li>If the coin is moving downwards ({@code airSpeed > 0}) and hits a floor, its movement is halted, and it transitions to its idle floating state.</li>
     *         </ul>
     *     </li>
     *     <li><b>Horizontal Collision:</b> While in the air, if the coin hits a vertical wall, its horizontal velocity is also reversed and dampened to simulate a bounce.</li>
     * </ul>
     *
     * @param levelData The current level data, used for collision detection.
     */
    private void updatePhysics(int[][] levelData) {
        airSpeed += COIN_GRAVITY;
        if (Utils.getInstance().canMoveHere(hitBox.x, hitBox.y + airSpeed, hitBox.width, hitBox.height, levelData)) {
            hitBox.y += airSpeed;
        }
        else {
            if (airSpeed < 0) {
                hitBox.y = Utils.getInstance().getYPosOnTheCeil(hitBox, airSpeed);
                airSpeed *= -0.4;
            }
            else {
                hitBox.y = getCoinYPos(hitBox);
                inAir = false;
                xSpeed = 0;
                yPos = (int) hitBox.y;
                airSpeed = 0;
            }
        }

        if (inAir) {
            if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                hitBox.x += xSpeed;
            }
            // Bounce
            else xSpeed *= -0.4;
        }
    }

    private double getCoinYPos(Rectangle2D.Double hitBox) {
        int currentTile = (int) (hitBox.y / TILES_SIZE);
        return (currentTile + 1) * TILES_SIZE - hitBox.height - 1;
    }

    @Override
    public void update(int[][] levelData) {
        updateAnimation();
        if (inAir) updatePhysics(levelData);
        else updateFloating();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x, y;
        x = (int)hitBox.x - xOffset - xLevelOffset;
        y = (int)hitBox.y - yOffset - yLevelOffset;
        g.drawImage(animations[animIndex], x, y, COIN_WID, COIN_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
    }

    @Override
    public void update() {
        // Should be empty
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
