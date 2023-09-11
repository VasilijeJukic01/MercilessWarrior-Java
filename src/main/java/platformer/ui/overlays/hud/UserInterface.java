package platformer.ui.overlays.hud;

import platformer.model.entities.Cooldown;
import platformer.model.entities.player.Player;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.PLAYER_HUD;
import static platformer.constants.FilePaths.PLAYER_PORTRAIT;
import static platformer.constants.UI.*;

public class UserInterface {

    private final Player player;

    private BufferedImage statusBar;
    private BufferedImage portrait;
    private int healthWidth;
    private int staminaWidth;
    private int expWidth;

    public UserInterface(Player player) {
        this.player = player;
        init();
    }

    private void init() {
        this.healthWidth = (int)(HEALTH_WID * SCALE);
        this.staminaWidth = (int)(STAMINA_WID * SCALE);
        this.expWidth = (int)(EXP_WID * SCALE);
        loadHUD();
    }

    private void loadHUD() {
        this.statusBar = Utils.getInstance().importImage(PLAYER_HUD,-1,-1);
        this.portrait = Utils.getInstance().importImage(PLAYER_PORTRAIT,-1,-1);
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
        renderCooldownInfo(g);
        renderLevelInfo(g);
        g.drawImage(portrait, PORT_X, PORT_Y, PORT_WID, PORT_HEI, null);
    }

    private void renderStatusBar(Graphics g) {
        g.drawImage(statusBar, HUD_X, HUD_Y, HUD_WID, HUD_HEI, null);
        g.setColor(Color.RED);
        g.fillRect(HP_X, HP_Y, healthWidth, HP_HEI);
        g.setColor(Color.BLUE);
        g.fillRect(ST_X, ST_Y, staminaWidth, ST_HEI);
        g.setColor(Color.GREEN);
        g.fillRect(XP_X, XP_Y, expWidth, XP_HEI);
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

    private void renderCooldownInfo(Graphics g) {
        int xStr = COOLDOWN_TXT_X, yStr = COOLDOWN_TXT_Y;
        g.drawString("Attack Cooldown: "+Math.round(player.getCooldown()[Cooldown.ATTACK.ordinal()]*100.0)/100.0, xStr, yStr);
        g.drawString("Block Cooldown:  "+Math.round(player.getCooldown()[Cooldown.BLOCK.ordinal()]*100.0)/100.0, xStr, 2*yStr);
        g.drawString("Dash Cooldown:   "+Math.round(player.getCooldown()[Cooldown.DASH.ordinal()]*100.0)/100.0, xStr, 3*yStr);
        g.drawString("Spell Cooldown:   "+Math.round(player.getCooldown()[Cooldown.SPELL.ordinal()]*100.0)/100.0, xStr, 4*yStr);
    }

}
