package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.SCALE;

public class Blacksmith extends GameObject{

    private boolean active;

    public Blacksmith(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        int hbWid = (int)(43 * SCALE);
        int hbHei = (int)(45 * SCALE);
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(32 * SCALE);
        super.yOffset = (int)(20 * SCALE);
    }

    // Core
    public void update() {
        if (animate) updateAnimation();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, (int)(20*SCALE)));
            int infoX = (int)(hitBox.x-hitBox.width/8-xLevelOffset);
            int infoY = (int)(hitBox.y-yLevelOffset-5*SCALE);
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
