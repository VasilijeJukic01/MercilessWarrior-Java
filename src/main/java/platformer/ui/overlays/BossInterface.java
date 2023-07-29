package platformer.ui.overlays;

import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.Constants.SCALE;

public class BossInterface {

    private final SpearWoman spearWoman;

    private final BufferedImage bossBar;

    public BossInterface(SpearWoman spearWoman) {
        this.spearWoman = spearWoman;
        this.bossBar = Utils.getInstance().importImage("/images/enemies/Bosses/BossHP.png", -1, -1);
    }

    public void render(Graphics g) {
        if (spearWoman.isAlive()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, (int)(15*SCALE)));
            g.drawString("Lancer", (int)(GAME_WIDTH/2-20*SCALE), (int)(75 * SCALE));
            g.drawImage(bossBar, (int)(180 * SCALE), (int)(80 * SCALE), (int)(500 * SCALE), (int)(50 * SCALE), null);
            g.drawString("?", GAME_WIDTH/2, (int)(107 * SCALE));
        }
    }
}
