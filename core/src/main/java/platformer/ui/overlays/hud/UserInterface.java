package platformer.ui.overlays.hud;

import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.Cooldown;
import platformer.model.entities.player.Player;
import platformer.model.minimap.MinimapManager;
import platformer.utils.Utils;


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
        this.statusBar = Utils.getInstance().importImage(PLAYER_HUD,-1,-1);
        this.portrait = Utils.getInstance().importImage(PLAYER_PORTRAIT,-1,-1);
    }

    private void loadAbilities() {
        BufferedImage sheet = Utils.getInstance().importImage(PLAYER_ABILITY_SHEET,180,45);
        this.abilities.add(new AbilitySlot(sheet.getSubimage(0, 0, ABILITY_SLOT_W, ABILITY_SLOT_H), Cooldown.ATTACK, COOLDOWN_SLOT_X, COOLDOWN_SLOT_Y));
        this.abilities.add(new AbilitySlot(sheet.getSubimage(ABILITY_SLOT_W, 0, ABILITY_SLOT_W, ABILITY_SLOT_H), Cooldown.BLOCK, COOLDOWN_SLOT_X + COOLDOWN_SLOT_SPACING, COOLDOWN_SLOT_Y));
        this.abilities.add(new AbilitySlot(sheet.getSubimage(2*ABILITY_SLOT_W, 0, ABILITY_SLOT_W, ABILITY_SLOT_H), Cooldown.DASH, COOLDOWN_SLOT_X + 2 * COOLDOWN_SLOT_SPACING, COOLDOWN_SLOT_Y));
        this.abilities.add(new AbilitySlot(sheet.getSubimage(3*ABILITY_SLOT_W, 0, ABILITY_SLOT_W, ABILITY_SLOT_H), Cooldown.SPELL, COOLDOWN_SLOT_X + 3 * COOLDOWN_SLOT_SPACING, COOLDOWN_SLOT_Y));
    }

    private void loadTimeCycleIcons() {
        this.timeCycleIcons = new BufferedImage[4];
        BufferedImage sprite = Utils.getInstance().importImage(TIME_CYCLE_ICONS, -1, -1);
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
        renderCooldown(g);
        renderLevelInfo(g);
        g.drawImage(portrait, PORT_X, PORT_Y, PORT_WID, PORT_HEI, null);
        renderCooldown(g);
        quickUsePanel.render(g);
        if (!minimapVisible) renderUnknownLocation(g);
        else minimapPanel.render(g);
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
        abilities.forEach(ability -> ability.render(g, player));
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

}
