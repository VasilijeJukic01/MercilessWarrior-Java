package platformer.ui.overlays;

import platformer.model.Tiles;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BossInterface {

    private final SpearWoman spearWoman;

    private final BufferedImage bossBar;

    public BossInterface(SpearWoman spearWoman) {
        this.spearWoman = spearWoman;
        this.bossBar = Utils.getInstance().importImage("src/main/resources/images/enemies/Bosses/BossHP.png", -1, -1);
    }

    public void render(Graphics g) {
        if (spearWoman.isAlive()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, (int)(15*Tiles.SCALE.getValue())));
            g.drawString("Lancer", (int)(Tiles.GAME_WIDTH.getValue()/2-20*Tiles.SCALE.getValue()), (int)(75 * Tiles.SCALE.getValue()));
            g.drawImage(bossBar, (int)(180 * Tiles.SCALE.getValue()), (int)(80 * Tiles.SCALE.getValue()), (int)(500 * Tiles.SCALE.getValue()), (int)(50 * Tiles.SCALE.getValue()), null);
            g.drawString("?", (int)(Tiles.GAME_WIDTH.getValue()/2), (int)(107 * Tiles.SCALE.getValue()));
        }
    }
}
