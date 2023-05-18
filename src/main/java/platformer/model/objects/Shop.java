package platformer.model.objects;

import platformer.model.Tiles;

import java.awt.*;

public class Shop extends GameObject {

    private boolean showText;

    public Shop(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        int hbWid = (int)(154 * Tiles.SCALE.getValue());
        int hbHei = (int)(132 * Tiles.SCALE.getValue());
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(1 * Tiles.SCALE.getValue());
        super.yOffset = (int)(1 * Tiles.SCALE.getValue());
    }

    public void update() {
        if (animate) updateAnimation();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (showText) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            int infoX = (int)(hitBox.x+hitBox.width/3-xLevelOffset);
            int infoY = (int)(hitBox.y-yLevelOffset+25*Tiles.SCALE.getValue());
            g.drawString("SHOP", infoX, infoY);
        }
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.ORANGE);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }
}
