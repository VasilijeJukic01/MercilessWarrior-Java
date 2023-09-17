package platformer.ui.overlays.hud;

import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.FilePaths.BOSS_BAR;
import static platformer.constants.UI.*;

public class BossInterface {

    private boolean active;

    private final BufferedImage bossBar;

    public BossInterface() {
        this.bossBar = Utils.getInstance().importImage(BOSS_BAR, -1, -1);
    }

    public void render(Graphics g) {
        if (active) {
            g.drawImage(bossBar, BOSS_BAR_X, BOSS_BAR_Y, BOSS_BAR_WID, BOSS_BAR_HEI, null);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

}
