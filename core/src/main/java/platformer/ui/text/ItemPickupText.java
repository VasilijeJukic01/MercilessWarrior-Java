package platformer.ui.text;

import platformer.model.entities.player.Player;

import java.awt.*;

import static platformer.constants.Constants.*;

/**
 * Represents a fading text notification for item pickups that appears above the player's head.
 * The text fades out and moves upwards over time.
 */
public class ItemPickupText {

    private final Player player;
    private double initialY;
    private final String text;
    private final Color color;
    private float alpha = 1.0f;
    private boolean active = true;

    public ItemPickupText(String text, Player player, int yOffset, Color color) {
        this.text = text;
        this.player = player;
        this.initialY = player.getHitBox().y - yOffset;
        this.color = color;
    }

    public void update() {
        if (!active) return;
        this.initialY -= ITEM_TEXT_Y_SPEED;
        alpha -= ITEM_TEXT_FADE_SPEED;
        if (alpha <= 0) {
            alpha = 0;
            active = false;
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (int) (player.getHitBox().getCenterX() - textWidth / 2.0);

        g.drawString(text, x - xLevelOffset, (int) (initialY - yLevelOffset));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public boolean isActive() {
        return active;
    }

}