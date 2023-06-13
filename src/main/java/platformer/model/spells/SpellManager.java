package platformer.model.spells;

import platformer.animation.AnimationUtils;
import platformer.model.Tiles;
import platformer.model.entities.Player;
import platformer.state.PlayingState;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class SpellManager {

    private final PlayingState playingState;
    private final Flames flames;
    private final BufferedImage[] lightningAnimations;
    private List<Lightning> bossLightnings;

    public SpellManager(PlayingState playingState) {
        this.playingState = playingState;
        Rectangle2D.Double hitBox = playingState.getPlayer().getHitBox();
        this.flames = new Flames(SpellType.FLAME_1, (int)hitBox.x, (int)hitBox.x,  (int)(hitBox.width*2.5),  (int)(hitBox.height-8.5*Tiles.SCALE.getValue()));
        this.lightningAnimations = AnimationUtils.getInstance().loadLightningAnimations();
        gatherLightnings();
    }

    private void updateFlames() {
        Player player = playingState.getPlayer();
        flames.getHitBox().x = playingState.getPlayer().getHitBox().x - flames.getXOffset();
        flames.getHitBox().y = playingState.getPlayer().getHitBox().y - flames.getYOffset();
        if (player.getFlipSign() == 1) flames.getHitBox().x = flames.getHitBox().x + flames.getHitBox().width + (int)(38*Tiles.SCALE.getValue());
        flames.setAlive(playingState.getPlayer().getSpellState() != 0);
    }

    private void updateLightnings() {
        for (Lightning bossLightning : bossLightnings) {
            bossLightning.updateAnimation();
        }
    }


    // Core
    public void update() {
        updateFlames();
        updateLightnings();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (flames.isAlive()) flames.render(g, xLevelOffset, yLevelOffset);
        renderLightnings(g, xLevelOffset, yLevelOffset);
    }

    private void renderLightnings(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Lightning bossLightning : bossLightnings) {
            if (bossLightning.isAlive()) {
                bossLightning.render(g, xLevelOffset, yLevelOffset);
                int x = (int) bossLightning.getHitBox().x - xLevelOffset - (int)(bossLightning.getWidth()/3.8);
                int y = (int) bossLightning.getHitBox().y - yLevelOffset+1;
                g.drawImage(lightningAnimations[bossLightning.getAnimIndex()], x, y, bossLightning.getWidth(), bossLightning.getHeight(), null);
            }
        }
    }

    public void activateLightnings() {
        for (Lightning bossLightning : bossLightnings) {
            bossLightning.setAnimIndex(0);
            bossLightning.setAlive(true);
        }
    }

    public void gatherLightnings() {
        this.bossLightnings = playingState.getLevelManager().getCurrentLevel().getLightnings();
    }

    public Flames getFlames() {
        return flames;
    }
}
