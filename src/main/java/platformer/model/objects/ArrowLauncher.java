package platformer.model.objects;

import platformer.model.Tiles;

import java.awt.*;

public class ArrowLauncher extends GameObject {

    private final int yTile;

    public ArrowLauncher(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        this.yTile = yPos/(int)(Tiles.TILES_SIZE.getValue());
        int hbWid = (int)(32*Tiles.SCALE.getValue());
        int hbHei = (int)(32*Tiles.SCALE.getValue());
        initHitBox(hbWid, hbHei);
        if (objType == ObjType.ARROW_LAUNCHER_LEFT) hitBox.x -= (int)(6*Tiles.SCALE.getValue());
        else if (objType == ObjType.ARROW_LAUNCHER_RIGHT) hitBox.x += (int)(6*Tiles.SCALE.getValue());
        hitBox.y += (int)(2*Tiles.SCALE.getValue());
    }

    public void update() {
        if (animate) updateAnimation();
    }

    public int getYTile() {
        return yTile;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.BLUE);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
