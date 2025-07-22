package platformer.ui.overlays.hud;

import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.boss.Lancer;
import platformer.model.entities.enemies.boss.Roric;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.FilePaths.LANCER_BOSS_BAR;
import static platformer.constants.FilePaths.RORIC_BOSS_BAR;
import static platformer.constants.UI.*;

/**
 * This class is responsible for rendering the boss HUD.
 * It is a simple image that is displayed at the top of the screen when the boss is active.
 */
public class BossInterface {

    private boolean active;
    private Enemy boss;

    private final BufferedImage[] bossBars;

    public BossInterface() {
        this.bossBars = new BufferedImage[2];
        bossBars[0] = Utils.getInstance().importImage(LANCER_BOSS_BAR, -1, -1);
        bossBars[1] = Utils.getInstance().importImage(RORIC_BOSS_BAR, -1, -1);
    }

    public void render(Graphics g) {
        if (active) {
            if (boss instanceof Lancer) renderBossBar(g, bossBars[0]);
            else if (boss instanceof Roric) renderBossBar(g, bossBars[1]);
        }
    }

    private void renderBossBar(Graphics g, BufferedImage bossBar) {
        g.drawImage(bossBar, BOSS_BAR_X, BOSS_BAR_Y, BOSS_BAR_WID, BOSS_BAR_HEI, null);
    }

    public void reset() {
        this.active = false;
        this.boss = null;
    }

    public void injectBoss(Enemy boss) {
        this.boss = boss;
        this.active = true;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

}
