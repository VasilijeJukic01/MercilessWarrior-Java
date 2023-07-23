package platformer.ui.overlays;

import platformer.model.Tiles;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Player;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class UserInterface {

    private final Player player;

    private final BufferedImage statusBar;
    private final BufferedImage portrait;
    private final int healthW = 134, staminaW = 134, expW = 149;
    private int healthWidth = (int)(healthW*Tiles.SCALE.getValue()), staminaWidth = (int)(staminaW*Tiles.SCALE.getValue()), expWidth = (int)(expW*Tiles.SCALE.getValue());

    public UserInterface(Player player) {
        this.player = player;
        this.statusBar = Utils.getInstance().importImage("src/main/resources/images/playerHUD.png",-1,-1);
        this.portrait = Utils.getInstance().importImage("src/main/resources/images/portraitHUD.png",-1,-1);
    }

    private void updateBars(double currentHealth, double maxHealth, double currentStamina, double maxStamina, double currentExp, double expCap) {
        this.healthWidth = (int)((currentHealth / maxHealth) * (int)(healthW*Tiles.SCALE.getValue()));
        this.staminaWidth = (int)((currentStamina / maxStamina) * (int)(staminaW*Tiles.SCALE.getValue()));
        this.expWidth = (int)((currentExp / expCap) * (int)(expW*Tiles.SCALE.getValue()));
    }

    public void update(double currentHealth, double maxHealth, double currentStamina, double maxStamina, double currentExp, double expCap) {
        updateBars(currentHealth, maxHealth, currentStamina, maxStamina, currentExp, expCap);
    }

    public void render(Graphics g) {
        g.drawImage(statusBar, (int)(10*Tiles.SCALE.getValue()), (int)(15*Tiles.SCALE.getValue()), (int)(192*Tiles.SCALE.getValue()), (int)(92*Tiles.SCALE.getValue()), null);
        renderStatusBar(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int)(10*Tiles.SCALE.getValue())));
        g.drawString(""+player.getCoins(), (int)(93*Tiles.SCALE.getValue()), (int)(82*Tiles.SCALE.getValue()));
        int xStr = (int)(Tiles.GAME_WIDTH.getValue()-(125*Tiles.SCALE.getValue())), yStr = (int)(10*Tiles.SCALE.getValue());
        g.drawString("Attack Cooldown: "+Math.round(player.getCooldown()[Cooldown.ATTACK.ordinal()]*100.0)/100.0, xStr, yStr);
        g.drawString("Block Cooldown:  "+Math.round(player.getCooldown()[Cooldown.BLOCK.ordinal()]*100.0)/100.0, xStr, 2*yStr);
        g.drawString("Dash Cooldown:   "+Math.round(player.getCooldown()[Cooldown.DASH.ordinal()]*100.0)/100.0, xStr, 3*yStr);
        g.setFont(new Font("Arial", Font.BOLD, (int)(7*Tiles.SCALE.getValue())));
        g.drawString("Lvl: "+player.getLevel(), (int)(170*Tiles.SCALE.getValue()), (int)(67*Tiles.SCALE.getValue()));
        g.drawImage(portrait, (int)(18*Tiles.SCALE.getValue()), (int)(22*Tiles.SCALE.getValue()), (int)(40*Tiles.SCALE.getValue()), (int)(40*Tiles.SCALE.getValue()), null);
    }

    private void renderStatusBar(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int)(64.5*Tiles.SCALE.getValue()), (int)(27*Tiles.SCALE.getValue()), healthWidth, (int)(12*Tiles.SCALE.getValue()));
        g.setColor(Color.BLUE);
        g.fillRect((int)(64.5*Tiles.SCALE.getValue()), (int)(44*Tiles.SCALE.getValue()), staminaWidth, (int)(12*Tiles.SCALE.getValue()));
        g.setColor(Color.GREEN);
        g.fillRect((int)(48.5*Tiles.SCALE.getValue()), (int)(61*Tiles.SCALE.getValue()), expWidth, (int)(7*Tiles.SCALE.getValue()));
    }


}
