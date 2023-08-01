package platformer.model.spells;

import platformer.animation.AnimUtils;
import platformer.model.entities.Player;
import platformer.state.GameState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

public class SpellManager {

    private final GameState gameState;

    // Animations
    private final BufferedImage[] lightningAnimations;
    private final BufferedImage[] flashAnimations;

    // Spells
    private final Flames flames;
    private List<Lightning> bossLightnings;
    private List<Flash> bossFlashes;

    // Flags
    private boolean hitFlag;

    private final Random rand = new Random();

    public SpellManager(GameState gameState) {
        this.gameState = gameState;
        this.flames = new Flames(SpellType.FLAME_1, 0, 0, (int)(45*SCALE), (int)(35*SCALE));
        this.lightningAnimations = AnimUtils.getInstance().loadLightningAnimations();
        this.flashAnimations = AnimUtils.getInstance().loadFlashAnimations();
        gatherSpellPlacements();
    }

    // Checks
    public void checkLightningHit() {
        if (hitFlag) return;
        Player player = gameState.getPlayer();
        for (Lightning bossLightning : bossLightnings) {
            int aIndex = bossLightning.getAnimIndex();
            if (bossLightning.isAlive() && aIndex > 0 && aIndex < 5 && bossLightning.getHitBox().intersects(player.getHitBox())) {
                hitFlag = true;
                player.changeHealth(-20);
            }
        }
    }

    public void checkFlashHit() {
        if (hitFlag) return;
        Player player = gameState.getPlayer();
        for (Flash bossFlash : bossFlashes) {
            int aIndex = bossFlash.getAnimIndex();
            if (bossFlash.isAlive() && aIndex > 12 && aIndex < 16 && bossFlash.getHitBox().intersects(player.getHitBox())) {
                hitFlag = true;
                player.changeHealth(-10);
            }
        }
    }

    // Updates
    private void updateFlames() {
        Player player = gameState.getPlayer();
        flames.getHitBox().x = gameState.getPlayer().getHitBox().x - flames.getXOffset();
        flames.getHitBox().y = gameState.getPlayer().getHitBox().y - flames.getYOffset();
        if (player.getFlipSign() == 1) flames.getHitBox().x = flames.getHitBox().x + flames.getHitBox().width + (int)(38*SCALE);
        flames.setAlive(gameState.getPlayer().getSpellState() != 0);
    }

    private void updateLightnings() {
        for (Lightning bossLightning : bossLightnings) {
            if (bossLightning.isAlive()) bossLightning.updateAnimation();
        }
        for (Flash bossFlash : bossFlashes) {
            if (bossFlash.isAlive()) bossFlash.updateAnimation();
        }
        checkLightningHit();
        checkFlashHit();
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

    // Renders
    private void renderLightnings(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Lightning bossLightning : bossLightnings) {
            if (bossLightning.isAlive()) {
                bossLightning.render(g, xLevelOffset, yLevelOffset);
                int x = (int) bossLightning.getHitBox().x - xLevelOffset - (int)(bossLightning.getWidth()/3.8);
                int y = (int) bossLightning.getHitBox().y - yLevelOffset+1;
                g.drawImage(lightningAnimations[bossLightning.getAnimIndex()], x, y, bossLightning.getWidth(), bossLightning.getHeight(), null);
            }
        }
        for (Flash bossFlash : bossFlashes) {
            if (bossFlash.isAlive()) {
                bossFlash.render(g, xLevelOffset, yLevelOffset);
                int x = (int) bossFlash.getHitBox().x - xLevelOffset - (int)(bossFlash.getWidth()/2.4);
                int y = (int) bossFlash.getHitBox().y - yLevelOffset+1;
                g.drawImage(flashAnimations[bossFlash.getAnimIndex()], x, y, bossFlash.getWidth(), bossFlash.getHeight(), null);
            }
        }
    }

    // Activators
    public void activateLightnings() {
        hitFlag = false;
        for (Lightning bossLightning : bossLightnings) {
            bossLightning.setAnimIndex(0);
            bossLightning.setAlive(true);
        }
    }

    public void activateFlashes() {
        hitFlag = false;
        int n = bossFlashes.size();
        int k = rand.nextInt(n);
        bossFlashes.get(k).setAnimIndex(0);
        bossFlashes.get(k).setAlive(true);
        bossFlashes.get(n-k-1).setAnimIndex(0);
        bossFlashes.get(n-k-1).setAlive(true);

    }

    public void gatherSpellPlacements() {
        this.bossLightnings = gameState.getLevelManager().getCurrentLevel().getLightnings();
        this.bossFlashes = gameState.getLevelManager().getCurrentLevel().getFlashes();
    }

    public void reset() {
        for (Lightning bossLightning : bossLightnings) {
            bossLightning.reset();
        }
        for (Flash bossFlash : bossFlashes) {
            bossFlash.reset();
        }
    }

    public Flames getFlames() {
        return flames;
    }
}
