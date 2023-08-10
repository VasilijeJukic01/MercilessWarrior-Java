package platformer.model.spells;

import platformer.animation.Animation;
import platformer.model.entities.player.Player;
import platformer.state.GameState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.FLASH_SHEET;
import static platformer.constants.FilePaths.LIGHTNING_SHEET;

public class SpellManager {

    private final GameState gameState;

    // Animations
    private BufferedImage[] lightningAnimations;
    private BufferedImage[] flashAnimations;

    // Spells
    private final Flame flame;
    private List<Lightning> bossLightnings;
    private List<Flash> bossFlashes;

    // Flags
    private boolean spellHit;

    private final Random rand = new Random();

    public SpellManager(GameState gameState) {
        this.gameState = gameState;
        this.flame = new Flame(SpellType.FLAME_1, 0, 0, FLAME_WID, FLAME_HEI);
        initSpellAnimations();
        initBossSpells();
    }

    // Init
    private void initSpellAnimations() {
        this.lightningAnimations = loadLightningAnimations();
        this.flashAnimations = loadFlashAnimations();
    }

    private BufferedImage[] loadLightningAnimations() {
        return Animation.getInstance().loadFromSprite(LIGHTNING_SHEET, 8, 0, LIGHTNING_WIDTH, LIGHTNING_HEIGHT, 0, LIGHTNING_W, LIGHTNING_H);
    }

    private BufferedImage[] loadFlashAnimations() {
        return Animation.getInstance().loadFromSprite(FLASH_SHEET, 16, 0, FLASH_WIDTH, FLASH_HEIGHT, 0, FLASH_W, FLASH_H);
    }

    public void initBossSpells() {
        this.bossLightnings = gameState.getLevelManager().getCurrentLevel().getSpells(Lightning.class);
        this.bossFlashes = gameState.getLevelManager().getCurrentLevel().getSpells(Flash.class);
    }

    // Checks
    public void checkLightningHit() {
        checkSpellHit(bossLightnings, 0, 5, -20);
    }

    public void checkFlashHit() {
        checkSpellHit(bossFlashes, 12, 16, -10);
    }

    private void checkSpellHit(List<? extends Spell> spells, int indexFrom, int indexTo, int dmg) {
        if (spellHit) return;
        Player player = gameState.getPlayer();
        for (Spell spell : spells) {
            int animIndex = spell.getAnimIndex();
            if (!spell.isActive()) continue;
            if (!spell.getHitBox().intersects(player.getHitBox())) continue;
            if (animIndex > indexFrom && animIndex < indexTo) {
                spellHit = true;
                player.changeHealth(dmg);
            }
        }
    }

    // Updates
    private void updateFlames() {
        Player player = gameState.getPlayer();

        if (player.getFlipSign() != 1) flame.getHitBox().x = player.getHitBox().x - FLAME_OFFSET_X;
        else flame.getHitBox().x = player.getHitBox().x + player.getHitBox().width + FLAME_OFFSET_X - FLAME_WID;

        flame.getHitBox().y = player.getHitBox().y - FLAME_OFFSET_Y;
        flame.setActive(gameState.getPlayer().getSpellState() != 0);
    }

    private void updateLightnings() {
        updateSpells(bossLightnings);
        updateSpells(bossFlashes);

        checkLightningHit();
        checkFlashHit();
    }

    private void updateSpells(List<? extends Spell> spells) {
        for (Spell spell : spells) {
            if (spell.isActive()) spell.updateAnimation();
        }
    }

    // Core
    public void update() {
        updateFlames();
        updateLightnings();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (flame.isActive()) flame.render(g, xLevelOffset, yLevelOffset);
        renderSpells(bossLightnings, lightningAnimations, g, xLevelOffset, yLevelOffset, LIGHTNING_OFFSET_X);
        renderSpells(bossFlashes, flashAnimations, g, xLevelOffset, yLevelOffset, FLASH_OFFSET_X);
    }

    // Render
    private void renderSpells(List<? extends Spell> spells, BufferedImage[] anim, Graphics g, int xLevelOffset, int yLevelOffset, int offset) {
        for (Spell spell : spells) {
            if (spell.isActive()) {
                spell.render(g, xLevelOffset, yLevelOffset);
                int x = (int) spell.getHitBox().x - xLevelOffset - offset;
                int y = (int) spell.getHitBox().y - yLevelOffset + 1;
                g.drawImage(anim[spell.getAnimIndex()], x, y, spell.getWidth(), spell.getHeight(), null);
            }
        }
    }

    // Activators
    public void activateLightnings() {
        spellHit = false;
        for (Lightning bossLightning : bossLightnings) {
            bossLightning.setAnimIndex(0);
            bossLightning.setActive(true);
        }
    }

    public void activateFlashes() {
        spellHit = false;
        int n = bossFlashes.size();
        int k = rand.nextInt(n);
        bossFlashes.get(k).setAnimIndex(0);
        bossFlashes.get(k).setActive(true);
        bossFlashes.get(n-k-1).setAnimIndex(0);
        bossFlashes.get(n-k-1).setActive(true);

    }

    public void reset() {
        resetSpells(bossLightnings);
        resetSpells(bossFlashes);
    }

    private void resetSpells(List<? extends Spell> spells) {
        for (Spell spell : spells) {
            spell.reset();
        }
    }

    public Flame getFlames() {
        return flame;
    }
}
