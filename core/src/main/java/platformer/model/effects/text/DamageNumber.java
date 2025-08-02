package platformer.model.effects.text;

import java.awt.*;

import static platformer.constants.Constants.*;

/**
 * Represents a damage number displayed on the screen.
 * The number fades out and moves upwards over time.
 */
public class DamageNumber {

    private final double x, y;
    private final String text;
    private final Color color;
    private float alpha = 1.0f;
    private boolean active = true;
    private double dy = 0;

    public DamageNumber(String text, double x, double y, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    // Core
    public void update() {
        if (!active) return;
        dy -= DAMAGE_TEXT_Y_SPEED;
        alpha -= DAMAGE_TEXT_FADE_SPEED;
        if (alpha <= 0) {
            alpha = 0;
            active = false;
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, FONT_DAMAGE));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, (int) (x - textWidth/2.0 - xLevelOffset), (int) (y + dy - yLevelOffset));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public boolean isActive() {
        return active;
    }
}
