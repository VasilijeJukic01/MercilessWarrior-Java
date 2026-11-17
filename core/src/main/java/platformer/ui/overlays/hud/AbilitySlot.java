package platformer.ui.overlays.hud;

import lombok.Getter;
import platformer.controller.KeyboardController;
import platformer.core.Framework;
import platformer.model.entities.Cooldown;
import platformer.model.entities.player.Player;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.COOLDOWN_SLOT_SIZE;

/**
 * Represents a slot in the HUD for displaying an ability's icon and its cooldown status.
 * It handles rendering the ability icon, cooldown overlay, and associated key bindings.
 */
@Getter
public class AbilitySlot {

    private final BufferedImage image;
    private final int xPos, yPos;
    private final Cooldown cooldownType;
    private double maxCooldown = 0;

    public AbilitySlot(BufferedImage image, Cooldown cooldownType, int xPos, int yPos) {
        int iconSize = COOLDOWN_SLOT_SIZE - scale(4);
        this.image = ImageUtils.resizeImage(image, iconSize, iconSize);
        this.cooldownType = cooldownType;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void render(Graphics g, Player player) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        double currentCooldown = player.getCooldown()[cooldownType.ordinal()];
        boolean ready = currentCooldown <= 0;
        Color borderCol = ready ? new Color(65, 195, 190, 175) : new Color(60, 75, 80, 160);

        UIButtonRenderer.drawRunicSlot(g2d, xPos, yPos, COOLDOWN_SLOT_SIZE, null, borderCol, false);
        int pad = scale(2);
        g2d.drawImage(image, xPos + pad, yPos + pad, null);
        renderCooldownOverlay(g2d, currentCooldown);
        renderKeybindBadge(g2d);
        g2d.dispose();
    }

    private void renderCooldownOverlay(Graphics2D g2d, double currentCooldown) {
        if (currentCooldown > 0) {
            if (maxCooldown == 0 || currentCooldown > maxCooldown) {
                maxCooldown = currentCooldown;
            }
            double cooldownPercentage = Math.min(1.0, currentCooldown / maxCooldown);
            int arcAngle = (int) Math.ceil(360 * cooldownPercentage);
            Shape oldClip = g2d.getClip();
            g2d.setClip(new RoundRectangle2D.Double(xPos + 1, yPos + 1, COOLDOWN_SLOT_SIZE - 2, COOLDOWN_SLOT_SIZE - 2, scale(4), scale(4)));
            g2d.setColor(new Color(6, 12, 15, 185));
            g2d.fillArc(xPos - scale(10), yPos - scale(10), COOLDOWN_SLOT_SIZE + scale(20), COOLDOWN_SLOT_SIZE + scale(20), 90, -arcAngle);
            g2d.setClip(oldClip);
        }
        else maxCooldown = 0;
    }

    private void renderKeybindBadge(Graphics2D g2d) {
        KeyboardController kc = Framework.getInstance().getKeyboardController();
        String keyText = switch (cooldownType) {
            case ATTACK -> kc.getKeyName("Attack");
            case BLOCK -> kc.getKeyName("Shield");
            case DASH -> kc.getKeyName("Dash");
            case SPELL -> kc.getKeyName("Fireball");
        };
        if (keyText == null || keyText.isEmpty()) return;

        int badgeH = scale(8.5);
        g2d.setFont(UIButtonRenderer.getFont(6.0f * SCALE));
        FontMetrics fm = g2d.getFontMetrics();
        int badgeW = Math.max(badgeH, fm.stringWidth(keyText) + scale(3));
        int bx = xPos + COOLDOWN_SLOT_SIZE - badgeW + scale(1);
        int by = yPos + COOLDOWN_SLOT_SIZE - badgeH + scale(1);

        g2d.setColor(new Color(12, 22, 26, 240));
        g2d.fillRoundRect(bx, by, badgeW, badgeH, scale(3), scale(3));
        g2d.setColor(new Color(55, 215, 210, 160));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRoundRect(bx, by, badgeW, badgeH, scale(3), scale(3));

        g2d.setColor(new Color(220, 248, 248));
        int tx = bx + (badgeW - fm.stringWidth(keyText)) / 2;
        int ty = by + (badgeH + (int) (fm.getAscent() * 0.72f)) / 2;
        g2d.drawString(keyText, tx, ty);
    }
}