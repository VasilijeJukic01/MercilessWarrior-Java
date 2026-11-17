package platformer.ui.overlays.hud;

import platformer.core.Framework;
import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.Cooldown;
import platformer.model.entities.player.Player;
import platformer.model.minimap.MinimapManager;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * This class is responsible for rendering the player's HUD.
 * It displays the player's health, stamina, experience, portrait, coins, level and cooldowns.
 */
public class UserInterface {

    private final Player player;
    private final QuickUsePanel quickUsePanel;
    private final MinimapPanel minimapPanel;
    private final TimeCycleManager timeCycleManager;
    private boolean minimapVisible = true;
    private boolean isInterior = false;

    private BufferedImage statusBar, portrait;
    private int healthWidth;
    private int staminaWidth;
    private int expWidth;

    private BufferedImage[] timeCycleIcons;
    private int timeCycleIconIndex;

    private final List<AbilitySlot> abilities;

    public UserInterface(Player player, MinimapManager minimapManager, TimeCycleManager timeCycleManager) {
        this.player = player;
        this.timeCycleManager = timeCycleManager;
        this.abilities = new ArrayList<>();
        this.quickUsePanel = new QuickUsePanel(player);
        this.minimapPanel = new MinimapPanel(minimapManager, RADAR_X, RADAR_Y, RADAR_WID, RADAR_HEI);
        init();
    }

    private void init() {
        this.healthWidth = (int)(HEALTH_WID * SCALE);
        this.staminaWidth = (int)(STAMINA_WID * SCALE);
        this.expWidth = (int)(EXP_WID * SCALE);
        loadHUD();
        loadAbilities();
        loadTimeCycleIcons();
    }

    private void loadHUD() {
        this.statusBar = ImageUtils.importImage(PLAYER_HUD,-1,-1);
        this.portrait = ImageUtils.importImage(PLAYER_PORTRAIT,-1,-1);
    }

    private void loadAbilities() {
        BufferedImage sheet = ImageUtils.importImage(PLAYER_ABILITY_SHEET,180,45);
        this.abilities.add(new AbilitySlot(sheet.getSubimage(0, 0, ABILITY_SLOT_W, ABILITY_SLOT_H), Cooldown.ATTACK, COOLDOWN_SLOT_X, COOLDOWN_SLOT_Y));
        this.abilities.add(new AbilitySlot(sheet.getSubimage(ABILITY_SLOT_W, 0, ABILITY_SLOT_W, ABILITY_SLOT_H), Cooldown.BLOCK, COOLDOWN_SLOT_X + COOLDOWN_SLOT_SPACING, COOLDOWN_SLOT_Y));
        this.abilities.add(new AbilitySlot(sheet.getSubimage(2*ABILITY_SLOT_W, 0, ABILITY_SLOT_W, ABILITY_SLOT_H), Cooldown.DASH, COOLDOWN_SLOT_X + 2 * COOLDOWN_SLOT_SPACING, COOLDOWN_SLOT_Y));
        this.abilities.add(new AbilitySlot(sheet.getSubimage(3*ABILITY_SLOT_W, 0, ABILITY_SLOT_W, ABILITY_SLOT_H), Cooldown.SPELL, COOLDOWN_SLOT_X + 3 * COOLDOWN_SLOT_SPACING, COOLDOWN_SLOT_Y));
    }

    private void loadTimeCycleIcons() {
        this.timeCycleIcons = new BufferedImage[4];
        BufferedImage sprite = ImageUtils.importImage(TIME_CYCLE_ICONS, -1, -1);
        if (sprite == null) return;
        for (int i = 0; i < 4; i++) {
            timeCycleIcons[i] = sprite.getSubimage(i * TIME_CYCLE_W, 0, TIME_CYCLE_W, TIME_CYCLE_H);
        }
    }

    private void updateBars(double currentHealth, double maxHealth, double currentStamina, double maxStamina, double currentExp, double expCap) {
        this.healthWidth = (int)((currentHealth / maxHealth) * (int)(HEALTH_WID * SCALE));
        this.staminaWidth = (int)((currentStamina / maxStamina) * (int)(STAMINA_WID * SCALE));
        this.expWidth = (int)((currentExp / expCap) * (int)(EXP_WID * SCALE));
    }

    private void updateTimeCycle() {
        if (timeCycleManager.isDawn()) timeCycleIconIndex = 0;
        else if (timeCycleManager.isDay()) timeCycleIconIndex = 1;
        else if (timeCycleManager.isDusk()) timeCycleIconIndex = 2;
        else timeCycleIconIndex = 3;
    }

    // Core
    public void update(double currentHealth, double maxHealth, double currentStamina, double maxStamina, double currentExp, double expCap) {
        updateBars(currentHealth, maxHealth, currentStamina, maxStamina, currentExp, expCap);
        updateTimeCycle();
    }

    public void render(Graphics g) {
        renderStatusBar(g);
        renderCoinsInfo(g);
        renderTimeCycle(g);
        renderLevelInfo(g);
        g.drawImage(portrait, PORT_X, PORT_Y, PORT_WID, PORT_HEI, null);
        if (!isInterior) {
            renderCooldown(g);
            quickUsePanel.render(g);
            if (!minimapVisible) renderUnknownLocation(g);
            else minimapPanel.render(g);
        }
    }

    private void renderStatusBar(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g.drawImage(statusBar, HUD_X, HUD_Y, HUD_WID, HUD_HEI, null);

        GradientPaint healthGradient = new GradientPaint(HP_X, HP_Y, new Color(110, 0, 0), HP_X + healthWidth, HP_Y, Color.RED);
        g2d.setPaint(healthGradient);
        g2d.fillRect(HP_X, HP_Y, healthWidth, HP_HEI);

        GradientPaint staminaGradient = new GradientPaint(ST_X, ST_Y, new Color(0, 0, 115), ST_X + staminaWidth, ST_Y, Color.BLUE);
        g2d.setPaint(staminaGradient);
        g2d.fillRect(ST_X, ST_Y, staminaWidth, ST_HEI);

        GradientPaint expGradient = new GradientPaint(XP_X, XP_Y, new Color(0, 90, 0), XP_X + expWidth, XP_Y, Color.GREEN);
        g2d.setPaint(expGradient);
        g2d.fillRect(XP_X, XP_Y, expWidth, XP_HEI);
    }

    private void renderCoinsInfo(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(""+player.getCoins(), COINS_X, COINS_Y);
    }

    private void renderTimeCycle(Graphics g) {
        if (timeCycleIcons != null && timeCycleIcons[timeCycleIconIndex] != null) {
            g.drawImage(timeCycleIcons[timeCycleIconIndex], TIME_CYCLE_ICON_X, TIME_CYCLE_ICON_Y, TIME_CYCLE_ICON_WID, TIME_CYCLE_ICON_HEI, null);
        }
    }

    private void renderLevelInfo(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        g.drawString("Lvl: "+player.getLevel(), LVL_X, LVL_Y);
    }

    private void renderCooldown(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int pad = scale(4);
        int barW = 3 * COOLDOWN_SLOT_SPACING + COOLDOWN_SLOT_SIZE + pad * 2;
        int barH = COOLDOWN_SLOT_SIZE + pad * 2;
        UIButtonRenderer.drawRunicSubPanel(g2d, COOLDOWN_SLOT_X - pad, COOLDOWN_SLOT_Y - pad, barW, barH);

        abilities.forEach(ability -> ability.render(g2d, player));
        renderUltimateGauge(g2d, COOLDOWN_SLOT_X - pad, COOLDOWN_SLOT_Y + COOLDOWN_SLOT_SIZE + pad + scale(4), barW);

        g2d.dispose();
    }

    private void renderUltimateGauge(Graphics2D g2d, int x, int y, int width) {
        if (player.getUltimateHandler() == null) return;

        double current = player.getUltimateHandler().getCurrentCharge();
        double max = Math.max(1.0, player.getUltimateHandler().getMaxCharge());
        double ratio = Math.min(1.0, current / max);
        boolean ready = player.getUltimateHandler().isReady();

        int height = scale(14);
        UIButtonRenderer.drawRunicSubPanel(g2d, x, y, width, height);

        int barInsetX = x + scale(18);
        int barInsetY = y + scale(3);
        int barMaxW = width - scale(22);
        int barH = height - scale(6);
        int fillW = (int) Math.round(barMaxW * ratio);

        g2d.setColor(new Color(8, 14, 18, 220));
        g2d.fillRoundRect(barInsetX, barInsetY, barMaxW, barH, scale(3), scale(3));

        if (fillW > 0) {
            Color cStart = ready ? new Color(255, 175, 25) : new Color(0, 155, 180);
            Color cEnd = ready ? new Color(255, 240, 110) : new Color(65, 245, 240);
            GradientPaint gp = new GradientPaint(barInsetX, barInsetY, cStart, barInsetX + fillW, barInsetY, cEnd);
            g2d.setPaint(gp);
            g2d.fillRoundRect(barInsetX, barInsetY, fillW, barH, scale(3), scale(3));
        }

        if (ready) {
            float pulse = (float) (0.65 + 0.35 * Math.sin(System.currentTimeMillis() * 0.012));
            g2d.setColor(new Color(255, 220, 75, (int) (185 * pulse)));
            g2d.setStroke(new BasicStroke(1.4f * SCALE));
            g2d.drawRoundRect(x - 1, y - 1, width + 2, height + 2, scale(6), scale(6));
        }

        int emblemCx = x + scale(9);
        int emblemCy = y + height / 2;
        int r = scale(4);
        Polygon diamond = new Polygon(
                new int[]{emblemCx, emblemCx + r, emblemCx, emblemCx - r},
                new int[]{emblemCy - r, emblemCy, emblemCy + r, emblemCy},
                4
        );
        g2d.setColor(ready ? new Color(255, 220, 75) : new Color(55, 225, 220));
        g2d.fillPolygon(diamond);

        String ultKey = Framework.getInstance().getKeyboardController().getKeyName("Ultimate").toUpperCase();
        String label = ready ? "ULTIMATE [" + ultKey + "]" : (int) Math.round(ratio * 100) + "%";
        g2d.setFont(UIButtonRenderer.getFont(6.5f * SCALE));
        FontMetrics fm = g2d.getFontMetrics();
        int tx = barInsetX + (barMaxW - fm.stringWidth(label)) / 2;
        int ty = barInsetY + (barH + (int) (fm.getAscent() * 0.72f)) / 2;

        g2d.setColor(new Color(8, 12, 14, 230));
        g2d.drawString(label, tx + 1, ty + 1);
        g2d.setColor(ready ? new Color(255, 250, 200) : new Color(215, 245, 245));
        g2d.drawString(label, tx, ty);
    }

    private void renderUnknownLocation(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Composite originalComposite = g2d.getComposite();
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(RADAR_X, RADAR_Y, RADAR_WID, RADAR_HEI, 10, 10);
        g2d.setComposite(originalComposite);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(RADAR_X, RADAR_Y, RADAR_WID, RADAR_HEI, 10, 10);
        g.setColor(new Color(200, 200, 200, 200));
        g.setFont(new Font("Arial", Font.ITALIC, FONT_MEDIUM));
        String text = "Unknown Location";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        int textX = RADAR_X + (RADAR_WID - textWidth) / 2;
        int textY = RADAR_Y + (RADAR_HEI - fm.getHeight()) / 2 + fm.getAscent();

        g.drawString(text, textX, textY);
    }

    public void setMinimapVisible(boolean visible) {
        this.minimapVisible = visible;
    }

    public void setInterior(boolean interior) {
        this.isInterior = interior;
    }

}
