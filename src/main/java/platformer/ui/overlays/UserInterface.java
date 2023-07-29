package platformer.ui.overlays;

import platformer.model.entities.Cooldown;
import platformer.model.entities.Player;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.Constants.SCALE;

public class UserInterface {

    private final Player player;

    private final BufferedImage statusBar;
    private final BufferedImage portrait;
    private final int healthW = 134, staminaW = 134, expW = 149;
    private int healthWidth = (int)(healthW*SCALE), staminaWidth = (int)(staminaW*SCALE), expWidth = (int)(expW*SCALE);

    public UserInterface(Player player) {
        this.player = player;
        this.statusBar = Utils.getInstance().importImage("/images/playerHUD.png",-1,-1);
        this.portrait = Utils.getInstance().importImage("/images/portraitHUD.png",-1,-1);
    }

    private void updateBars(double currentHealth, double maxHealth, double currentStamina, double maxStamina, double currentExp, double expCap) {
        this.healthWidth = (int)((currentHealth / maxHealth) * (int)(healthW*SCALE));
        this.staminaWidth = (int)((currentStamina / maxStamina) * (int)(staminaW*SCALE));
        this.expWidth = (int)((currentExp / expCap) * (int)(expW*SCALE));
    }

    public void update(double currentHealth, double maxHealth, double currentStamina, double maxStamina, double currentExp, double expCap) {
        updateBars(currentHealth, maxHealth, currentStamina, maxStamina, currentExp, expCap);
    }

    public void render(Graphics g) {
        g.drawImage(statusBar, (int)(10*SCALE), (int)(15*SCALE), (int)(192*SCALE), (int)(92*SCALE), null);
        renderStatusBar(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int)(10*SCALE)));
        g.drawString(""+player.getCoins(), (int)(93*SCALE), (int)(82*SCALE));
        int xStr = (int)(GAME_WIDTH-(125*SCALE)), yStr = (int)(10*SCALE);
        g.drawString("Attack Cooldown: "+Math.round(player.getCooldown()[Cooldown.ATTACK.ordinal()]*100.0)/100.0, xStr, yStr);
        g.drawString("Block Cooldown:  "+Math.round(player.getCooldown()[Cooldown.BLOCK.ordinal()]*100.0)/100.0, xStr, 2*yStr);
        g.drawString("Dash Cooldown:   "+Math.round(player.getCooldown()[Cooldown.DASH.ordinal()]*100.0)/100.0, xStr, 3*yStr);
        g.setFont(new Font("Arial", Font.BOLD, (int)(7*SCALE)));
        g.drawString("Lvl: "+player.getLevel(), (int)(170*SCALE), (int)(67*SCALE));
        g.drawImage(portrait, (int)(18*SCALE), (int)(22*SCALE), (int)(40*SCALE), (int)(40*SCALE), null);
    }

    private void renderStatusBar(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int)(64.5*SCALE), (int)(27*SCALE), healthWidth, (int)(12*SCALE));
        g.setColor(Color.BLUE);
        g.fillRect((int)(64.5*SCALE), (int)(44*SCALE), staminaWidth, (int)(12*SCALE));
        g.setColor(Color.GREEN);
        g.fillRect((int)(48.5*SCALE), (int)(61*SCALE), expWidth, (int)(7*SCALE));
    }


}
