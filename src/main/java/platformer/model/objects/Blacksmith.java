package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.*;

public class Blacksmith extends GameObject{

    private boolean active;

    public Blacksmith(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(BLACKSMITH_HB_WID, BLACKSMITH_HB_HEI);
        super.xOffset = BLACKSMITH_OFFSET_X;
        super.yOffset = BLACKSMITH_OFFSET_Y;
    }

    // Core
    public void update() {
        if (animate) updateAnimation();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, FONT_BIG));
            int infoX = (int)(hitBox.x - hitBox.width / 8 - xLevelOffset);
            int infoY = (int)(hitBox.y - yLevelOffset - 5 * SCALE);
            g.drawString("SHOP", infoX, infoY);
        }
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
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
