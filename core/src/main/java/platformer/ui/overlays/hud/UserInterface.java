package platformer.ui.overlays.hud;

import platformer.model.entities.Cooldown;
import platformer.model.entities.player.Player;
import platformer.model.minimap.MinimapManager;
import platformer.utils.Utils;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.AnimConstants.ABILITY_SLOT_H;
import static platformer.constants.AnimConstants.ABILITY_SLOT_W;
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

    private BufferedImage statusBar, portrait;
    private int healthWidth;
    private int staminaWidth;
    private int expWidth;

    private final List<AbilitySlot> abilities;

    public UserInterface(Player player, MinimapManager minimapManager) {
        this.player = player;
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

    private void updateBars(double currentHealth, double maxHealth, double currentStamina, double maxStamina, double currentExp, double expCap) {
        this.healthWidth = (int)((currentHealth / maxHealth) * (int)(HEALTH_WID * SCALE));
        this.staminaWidth = (int)((currentStamina / maxStamina) * (int)(STAMINA_WID * SCALE));
        this.expWidth = (int)((currentExp / expCap) * (int)(EXP_WID * SCALE));
    }

    // Core
    public void update(double currentHealth, double maxHealth, double currentStamina, double maxStamina, double currentExp, double expCap) {
        updateBars(currentHealth, maxHealth, currentStamina, maxStamina, currentExp, expCap);
    }

    public void render(Graphics g) {
        renderStatusBar(g);
        renderCoinsInfo(g);
        renderCooldown(g);
        renderLevelInfo(g);
        g.drawImage(portrait, PORT_X, PORT_Y, PORT_WID, PORT_HEI, null);
        renderCooldown(g);
        quickUsePanel.render(g);
        minimapPanel.render(g);
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

    private void renderLevelInfo(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        g.drawString("Lvl: "+player.getLevel(), LVL_X, LVL_Y);
    }

    private void renderCooldown(Graphics g) {
        abilities.forEach(ability -> ability.render(g, player));
    }

}
