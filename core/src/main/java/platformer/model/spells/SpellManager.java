package platformer.model.spells;

import platformer.animation.Animation;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.player.Player;
import platformer.model.levels.Level;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.FLASH_SHEET;
import static platformer.constants.FilePaths.LIGHTNING_SHEET;

/**
 * This class is responsible for managing all the spells in the game.
 * It holds references to all the spells and provides methods for updating and rendering them.
 */
public class SpellManager {

    private final GameState gameState;

    // Animations
    private BufferedImage[] lightningAnimations;
    private BufferedImage[] flashAnimations;
    private BufferedImage[] roricBeamAnimations;
    private BufferedImage[] arrowRainAnimations;
    private BufferedImage[] skyBeamAnimations;

    // Spells
    private final Flame flame;
    private List<Lightning> bossLightnings;
    private List<Flash> bossFlashes;
    private final List<RoricBeam> roricBeams;
    private final List<ArrowRain> arrowRains;
    private final List<RoricSkyBeam> skyBeams;

    // Flags
    private boolean spellHit;

    private boolean isSkyBeamActive = false;
    private int skyBeamSpawnTimer = 0;
    private final int skyBeamSpawnCooldown = 200;

    private final Random rand = new Random();

    public SpellManager(GameState gameState) {
        this.gameState = gameState;
        this.flame = new Flame(SpellType.FLAME_1, 0, 0, FLAME_WID, FLAME_HEI);
        this.roricBeams = new ArrayList<>();
        this.arrowRains = new ArrayList<>();
        this.skyBeams = new ArrayList<>();
        initSpellAnimations();
        initBossSpells();
    }

    // Init
    private void initSpellAnimations() {
        this.lightningAnimations = loadLightningAnimations();
        this.flashAnimations = loadFlashAnimations();
        this.roricBeamAnimations = Animation.getInstance().loadRoricProjectiles()[0];
        this.arrowRainAnimations = Animation.getInstance().loadRoricProjectiles()[1];
        this.skyBeamAnimations = Animation.getInstance().createSkyBeamAnimation();
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

    private void updateRoricBeam() {
        List<RoricBeam> toRemove = new ArrayList<>();
        for (RoricBeam beam : roricBeams) {
            if (beam.isActive()) {
                beam.update(gameState.getPlayer());
            } else {
                toRemove.add(beam);
            }
        }
        roricBeams.removeAll(toRemove);
    }

    private void updateArrowRain() {
        List<ArrowRain> toRemove = new ArrayList<>();
        for (ArrowRain arrow : arrowRains) {
            if (arrow.isActive()) {
                arrow.update(gameState.getPlayer());
                arrow.updateAnimation();
            } else {
                toRemove.add(arrow);
            }
        }
        arrowRains.removeAll(toRemove);
    }

    private void updateSpells(List<? extends Spell> spells) {
        spells.stream()
                .filter(Spell::isActive)
                .forEach(Spell::updateAnimation);
    }

    private void updateSkyBeams() {
        if (isSkyBeamActive) {
            skyBeamSpawnTimer++;
            if (skyBeamSpawnTimer >= skyBeamSpawnCooldown) {
                skyBeamSpawnTimer = 0;
                spawnSkyBeam();
            }
        }

        List<RoricSkyBeam> toRemove = new ArrayList<>();
        for (RoricSkyBeam beam : skyBeams) {
            if (beam.isActive()) beam.update(gameState.getPlayer());
            else toRemove.add(beam);
        }
        skyBeams.removeAll(toRemove);
    }

    // Core
    public void update() {
        updateFlames();
        updateLightnings();
        updateRoricBeam();
        updateArrowRain();
        updateSkyBeams();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (flame.isActive()) flame.render(g, xLevelOffset, yLevelOffset);
        renderSpells(bossLightnings, lightningAnimations, g, xLevelOffset, yLevelOffset, LIGHTNING_OFFSET_X);
        renderSpells(bossFlashes, flashAnimations, g, xLevelOffset, yLevelOffset, FLASH_OFFSET_X);
        renderRoricBeams(g, xLevelOffset, yLevelOffset);
        renderArrowRain(g, xLevelOffset, yLevelOffset);
        renderSkyBeams(g, xLevelOffset, yLevelOffset);
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

    private void renderRoricBeams(Graphics g, int xLevelOffset, int yLevelOffset) {
        List<RoricBeam> beamsSnapshot = new ArrayList<>(roricBeams);
        for (RoricBeam beam : beamsSnapshot) {
            if (beam.isActive()) {
                int fS = (beam.getDirection() == Direction.LEFT) ? -1 : 1;
                int x = (int) beam.getHitBox().x - xLevelOffset;
                int y = (int) beam.getHitBox().y - yLevelOffset - (int) (64 * SCALE);

                if (fS == -1) x += RORIC_BEAM_WID;

                g.drawImage(roricBeamAnimations[beam.getAnimIndex()], x, y, fS * RORIC_BEAM_WID, RORIC_BEAM_HEI, null);
                beam.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
            }
        }
    }

    private void renderArrowRain(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (ArrowRain arrow : arrowRains) {
            if (arrow.isActive()) {
                int x = (int) arrow.getHitBox().x - xLevelOffset - (int)(127 * SCALE);
                int y = (int) arrow.getHitBox().y - yLevelOffset;
                g.drawImage(arrowRainAnimations[arrow.getAnimIndex()], x, y, arrow.getWidth(), arrow.getHeight(), null);
                arrow.renderHitBox(g, xLevelOffset, yLevelOffset, Color.CYAN);
            }
        }
    }

    private void renderSkyBeams(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (RoricSkyBeam beam : skyBeams) {
            if (beam.isActive()) {
                int x = (int) beam.getHitBox().x - xLevelOffset;
                int y = (int) beam.getHitBox().y - yLevelOffset;
                BufferedImage frame = skyBeamAnimations[beam.getAnimIndex()];
                BufferedImage rotatedFrame = Utils.getInstance().rotateImage(frame, 90);
                int drawX = x + (beam.getWidth() - rotatedFrame.getWidth()) / 2 - (int)(67 * SCALE);
                int drawY = y + (beam.getHeight() - rotatedFrame.getHeight()) / 2 - (int)(180 * SCALE);
                g.drawImage(rotatedFrame, drawX, drawY, null);
                beam.renderHitBox(g, xLevelOffset, yLevelOffset, Color.CYAN);
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
        gameState.getLightManager().setAlpha(0);
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

    public void activateRoricBeam(Roric roric) {
        Direction dir = roric.getDirection();
        roricBeams.add(new RoricBeam(SpellType.RORIC_BEAM, (int)roric.getHitBox().x, (int)roric.getHitBox().y, dir));
    }

    public void activateArrowRain(Player player) {
        int xPos = (int) (player.getHitBox().getCenterX());
        double groundY = Utils.getInstance().getGroundY(player.getHitBox().getCenterX(), player.getHitBox().y, gameState.getLevelManager().getCurrentLevel().getLvlData());
        int yPos = (int) (groundY - (390 * SCALE));
        arrowRains.add(new ArrowRain(SpellType.ARROW_RAIN, xPos, yPos));
    }

    private void spawnSkyBeam() {
        Level currentLevel = gameState.getLevelManager().getCurrentLevel();
        int levelWidthInTiles = currentLevel.getLevelTilesWidth();
        int maxPixelX = levelWidthInTiles * TILES_SIZE;
        int xPos = rand.nextInt(maxPixelX);
        int yPos = 0;
        skyBeams.add(new RoricSkyBeam(SpellType.RORIC_SKY_BEAM, xPos, yPos));
    }

    public void startSkyBeams() {
        this.isSkyBeamActive = true;
    }

    public void stopSkyBeams() {
        this.isSkyBeamActive = false;
    }

    public void reset() {
        resetSpells(bossLightnings);
        resetSpells(bossFlashes);
        roricBeams.clear();
        arrowRains.clear();
        skyBeams.clear();
        isSkyBeamActive = false;
        skyBeamSpawnTimer = 0;
    }

    private void resetSpells(List<? extends Spell> spells) {
        spells.forEach(Spell::reset);
    }

    public Flame getFlames() {
        return flame;
    }
}
