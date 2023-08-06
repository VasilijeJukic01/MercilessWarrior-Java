package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.*;

public class ArrowLauncher extends GameObject {

    private final int yTile;

    public ArrowLauncher(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        this.yTile = yPos/TILES_SIZE;
        initHitBox(ARROW_TRAP_HB_SIZE, ARROW_TRAP_HB_SIZE);
        if (objType == ObjType.ARROW_TRAP_LEFT) hitBox.x -= ARROW_TRAP_OFFSET;
        else if (objType == ObjType.ARROW_TRAP_RIGHT) hitBox.x += ARROW_TRAP_OFFSET;
        hitBox.y += (int)(2 * SCALE);
    }

    public void update() {
        if (animate) updateAnimation();
    }

    public int getYTile() {
        return yTile;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
