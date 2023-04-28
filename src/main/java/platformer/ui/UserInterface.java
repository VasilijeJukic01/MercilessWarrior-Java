package platformer.ui;

import platformer.model.Tiles;
import platformer.model.entities.Player;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class UserInterface {

    private final Player player;

    private final BufferedImage statusBar;
    private int healthWidth = (int)(150* Tiles.SCALE.getValue()), staminaWidth = (int)(115*Tiles.SCALE.getValue());

    public UserInterface(Player player) {
        this.player = player;
        this.statusBar = Utils.getInstance().importImage("src/main/resources/images/health_power_bar.png",-1,-1);
    }

    private void updateBars(double currentHealth, double maxHealth, double currentStamina, double maxStamina) {
        this.healthWidth = (int)((currentHealth / maxHealth) * (int)(150*Tiles.SCALE.getValue()));
        this.staminaWidth = (int)((currentStamina / maxStamina) * (int)(115*Tiles.SCALE.getValue()));
    }

    public void update(double currentHealth, double maxHealth, double currentStamina, double maxStamina) {
        updateBars(currentHealth, maxHealth, currentStamina, maxStamina);
    }

    public void render(Graphics g) {
        g.drawImage(statusBar,(int)(10*Tiles.SCALE.getValue()), (int)(15*Tiles.SCALE.getValue()), (int)(192*Tiles.SCALE.getValue()), (int)(58*Tiles.SCALE.getValue()), null);
        renderStatusBar(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Coins: "+player.getCoins(), (int)Tiles.GAME_WIDTH.getValue()-250, 20);
        g.drawString("Level: "+player.getLevel()+" | EXP: "+player.getExp(), (int)Tiles.GAME_WIDTH.getValue()-250, 40);
        g.drawString("Attack Cooldown: 0", (int)Tiles.GAME_WIDTH.getValue()-250, 60);
        g.drawString("Block Cooldown:  0", (int)Tiles.GAME_WIDTH.getValue()-250, 80);
        g.drawString("Dash Cooldown:   0", (int)Tiles.GAME_WIDTH.getValue()-250, 100);
    }

    private void renderStatusBar(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect((int)(44*Tiles.SCALE.getValue()), (int)(29*Tiles.SCALE.getValue()), healthWidth, (int)(4*Tiles.SCALE.getValue()));
        g.setColor(Color.BLUE);
        g.fillRect((int)(44*Tiles.SCALE.getValue()), (int)(48*Tiles.SCALE.getValue()), staminaWidth, (int)(4*Tiles.SCALE.getValue()));
    }


}
