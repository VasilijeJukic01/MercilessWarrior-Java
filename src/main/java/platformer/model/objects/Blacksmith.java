package platformer.model.objects;

import platformer.model.Tiles;

import java.awt.*;

public class Blacksmith extends GameObject{

    private boolean active;

    public Blacksmith(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        int hbWid = (int)(43 * Tiles.SCALE.getValue());
        int hbHei = (int)(45 * Tiles.SCALE.getValue());
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(32 * Tiles.SCALE.getValue());
        super.yOffset = (int)(20 * Tiles.SCALE.getValue());
    }

    public void update() {
        if (animate) updateAnimation();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, (int)(20*Tiles.SCALE.getValue())));
            int infoX = (int)(hitBox.x-hitBox.width/8-xLevelOffset);
            int infoY = (int)(hitBox.y-yLevelOffset-5*Tiles.SCALE.getValue());
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
