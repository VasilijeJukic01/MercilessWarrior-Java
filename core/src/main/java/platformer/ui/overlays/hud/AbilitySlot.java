package platformer.ui.overlays.hud;

import lombok.Getter;
import platformer.controller.KeyboardController;
import platformer.core.Framework;
import platformer.model.entities.Cooldown;
import platformer.model.entities.player.Player;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.FONT_MEDIUM;
import static platformer.constants.UI.COOLDOWN_SLOT_SIZE;

@Getter
public class AbilitySlot {

    private final BufferedImage image;
    private final int xPos, yPos;
    private final Cooldown cooldownType;
    private double maxCooldown = 0;

    public AbilitySlot(BufferedImage image, Cooldown cooldownType, int xPos, int yPos) {
        this.image = image;
        this.cooldownType = cooldownType;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void render(Graphics g, Player player) {
        g.setColor(Color.WHITE);
        g.drawRoundRect(xPos - 1, yPos - 1, COOLDOWN_SLOT_SIZE + 1, COOLDOWN_SLOT_SIZE + 1, 10, 10);
        g.drawImage(image, xPos, yPos, null);
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRoundRect(xPos - 1, yPos - 1, COOLDOWN_SLOT_SIZE + 1, COOLDOWN_SLOT_SIZE + 1, 10, 10);
        renderCooldownOverlay(g, player);
        renderKeys(g);
    }

    private void renderCooldownOverlay(Graphics g, Player player) {
        double currentCooldown = player.getCooldown()[cooldownType.ordinal()];
        if (currentCooldown > 0) {
            if (maxCooldown == 0) maxCooldown = currentCooldown;
            double cooldownPercentage = currentCooldown / maxCooldown;
            int arcAngle = (int) (360 * cooldownPercentage);
            g.setColor(new Color(0, 0, 0, 150));
            g.setClip(xPos, yPos, COOLDOWN_SLOT_SIZE, COOLDOWN_SLOT_SIZE);
            g.fillArc(xPos - 20, yPos - 20, 110, 110, 90, -arcAngle);
            g.setClip(null);
        }
        else maxCooldown = 0;
    }


    private void renderKeys(Graphics g) {
        KeyboardController keyboardController = Framework.getInstance().getKeyboardController();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        switch (cooldownType) {
            case ATTACK:
                g.drawString(keyboardController.getKeyName("Attack"), xPos + 5, yPos + COOLDOWN_SLOT_SIZE - 5);
                break;
            case BLOCK:
                g.drawString(keyboardController.getKeyName("Shield"), xPos + 5, yPos + COOLDOWN_SLOT_SIZE - 5);
                break;
            case DASH:
                g.drawString(keyboardController.getKeyName("Dash"), xPos + 5, yPos + COOLDOWN_SLOT_SIZE - 5);
                break;
            case SPELL:
                g.drawString(keyboardController.getKeyName("Fireball"), xPos + 5, yPos + COOLDOWN_SLOT_SIZE - 5);
                break;
            default: break;
        }
    }
}