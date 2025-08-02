package platformer.model.spells;

import platformer.animation.SpriteManager;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.player.Player;
import platformer.model.levels.Level;
import platformer.state.GameState;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.FLASH_SHEET;
import static platformer.constants.FilePaths.LIGHTNING_SHEET;
import static platformer.physics.CollisionDetector.getGroundY;

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

    private boolean isSkyBeamActive = false;
    private int skyBeamSpawnTimer = 0;
    private static final int SKY_BEAM_SPAWN_COOLDOWN = 200;

    private boolean isTelegraphingArrowRain = false;
    private Point telegraphPosition;

    private final Random rand = new Random();

    public SpellManager(GameState gameState) {
        this.gameState = gameState;
        this.flame = new Flame(SpellType.FLAME_1, 0, 0, FLAME_WID, FLAME_HEI);
        this.roricBeams = new ArrayList<>();
        this.arrowRains = new ArrayList<>();
        this.skyBeams = new ArrayList<>();
    }

    // Init
    public void lateInit() {
        initSpellAnimations();
        initBossSpells();
    }

    private void initSpellAnimations() {
        this.lightningAnimations = loadLightningAnimations();
        this.flashAnimations = loadFlashAnimations();
        this.roricBeamAnimations = SpriteManager.getInstance().getRoricProjectileAnimations()[0];
        this.arrowRainAnimations = SpriteManager.getInstance().getRoricProjectileAnimations()[1];
        this.skyBeamAnimations = SpriteManager.getInstance().getSkyBeamAnimations();
    }

    private BufferedImage[] loadLightningAnimations() {
        return SpriteManager.getInstance().loadFromSprite(LIGHTNING_SHEET, 8, 0, LIGHTNING_WIDTH, LIGHTNING_HEIGHT, 0, LIGHTNING_W, LIGHTNING_H);
    }

    private BufferedImage[] loadFlashAnimations() {
        return SpriteManager.getInstance().loadFromSprite(FLASH_SHEET, 16, 0, FLASH_WIDTH, FLASH_HEIGHT, 0, FLASH_W, FLASH_H);
    }

    public void initBossSpells() {
        this.bossLightnings = gameState.getLevelManager().getCurrentLevel().getSpells(Lightning.class);
        this.bossFlashes = gameState.getLevelManager().getCurrentLevel().getSpells(Flash.class);
    }

    // Updates
    private void updateFlames() {
        Player player = gameState.getPlayer();

        if (player.getFlipSign() != 1) flame.getHitBox().x = player.getHitBox().x - FLAME_OFFSET_X;
        else flame.getHitBox().x = player.getHitBox().x + player.getHitBox().width + FLAME_OFFSET_X - FLAME_WID;

        flame.getHitBox().y = player.getHitBox().y - FLAME_OFFSET_Y;
        flame.setActive(gameState.getPlayer().getSpellState() != 0);
    }

    private void updateSkyBeams() {
        if (isSkyBeamActive) {
            skyBeamSpawnTimer++;
            if (skyBeamSpawnTimer >= SKY_BEAM_SPAWN_COOLDOWN) {
                skyBeamSpawnTimer = 0;
                spawnSkyBeam();
            }
        }
        updateSpells(skyBeams);
    }

    private void updateSpells(List<? extends Spell> spells) {
        Player player = gameState.getPlayer();
        spells.removeIf(spell -> !isStaticSpell(spell) && !spell.isActive());
        spells.forEach(spell -> spell.update(player));
    }

    // Core
    public void update() {
        updateFlames();
        updateSpells(bossLightnings);
        updateSpells(bossFlashes);
        updateSpells(roricBeams);
        updateSpells(arrowRains);
        updateSkyBeams();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (flame.isActive()) flame.render(g, xLevelOffset, yLevelOffset);
        renderSpells(bossLightnings, lightningAnimations, g, xLevelOffset, yLevelOffset, LIGHTNING_OFFSET_X);
        renderSpells(bossFlashes, flashAnimations, g, xLevelOffset, yLevelOffset, FLASH_OFFSET_X);
        renderRoricBeams(g, xLevelOffset, yLevelOffset);
        renderArrowRain(g, xLevelOffset, yLevelOffset);
        renderSkyBeams(g, xLevelOffset, yLevelOffset);
        renderArrowRainTelegraph(g, xLevelOffset, yLevelOffset);
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
                int x = (int) arrow.getHitBox().x - xLevelOffset - (int)(135 * SCALE);
                int y = (int) arrow.getHitBox().y - yLevelOffset;
                g.drawImage(arrowRainAnimations[arrow.getAnimIndex()], x, y, arrow.getWidth(), arrow.getHeight(), null);
                arrow.renderHitBox(g, xLevelOffset, yLevelOffset, Color.CYAN);
            }
        }
    }

    private void renderArrowRainTelegraph(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!isTelegraphingArrowRain || telegraphPosition == null) return;

        Graphics2D g2d = (Graphics2D) g.create();

        int width = (int) (RORIC_RAIN_HB_WID * 1.5);
        int height = (int) (12 * SCALE);
        int drawX = telegraphPosition.x - xLevelOffset - (width / 2);
        double groundY = getGroundY(telegraphPosition.x, telegraphPosition.y, gameState.getLevelManager().getCurrentLevel().getLvlData());
        int drawY = (int)groundY - yLevelOffset - (height / 2);
        float pulse = (float) (0.75 + 0.25 * Math.sin(System.currentTimeMillis() / 150.0));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));

        Color colorCenter = new Color(255, 40, 40, 180);
        Color colorEdge = new Color(180, 0, 20, 100);
        GradientPaint gp = new GradientPaint(drawX, drawY, colorEdge, drawX + width / 2, drawY, colorCenter, true);
        g2d.setPaint(gp);
        g2d.fillRoundRect(drawX, drawY, width, height, 10, 10);

        g2d.dispose();
    }

    // TODO: For refactoring later
    private void renderSkyBeams(Graphics g, int xLevelOffset, int yLevelOffset) {
        List<RoricSkyBeam> beamsSnapshot = new ArrayList<>(skyBeams);
        for (RoricSkyBeam beam : beamsSnapshot) {
            if (beam.isActive()) {
                int x = (int) beam.getHitBox().x - xLevelOffset;
                int y = (int) beam.getHitBox().y - yLevelOffset;
                BufferedImage frame = skyBeamAnimations[beam.getAnimIndex()];
                BufferedImage rotatedFrame = ImageUtils.rotateImage(frame, 90);
                int offset = beam.isTargeted() ? (int)(150 * SCALE) : (int)(67 * SCALE);
                int drawX = x + (beam.getWidth() - rotatedFrame.getWidth()) / 2 - offset;
                int drawY = y + (beam.getHeight() - rotatedFrame.getHeight()) / 2 - (int)(180 * SCALE);
                int renderWidth = beam.isTargeted() ? RORIC_BEAM_HEI + (int) (190 * SCALE) : RORIC_BEAM_HEI;
                g.drawImage(rotatedFrame, drawX, drawY, renderWidth, RORIC_BEAM_WID, null);
                beam.renderHitBox(g, xLevelOffset, yLevelOffset, Color.CYAN);
            }
        }
    }

    // Activators
    public void activateLightnings() {
        for (Lightning bossLightning : bossLightnings) {
            bossLightning.setAnimIndex(0);
            bossLightning.setActive(true);
        }
        gameState.getLightManager().setCurrentAmbientAlpha(0);
    }

    public void activateFlashes() {
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

    public void activateArrowRain() {
        if (telegraphPosition == null) return;
        int xPos = telegraphPosition.x;
        double groundY = getGroundY(telegraphPosition.x, telegraphPosition.y, gameState.getLevelManager().getCurrentLevel().getLvlData());
        int yPos = (int) (groundY - (405 * SCALE));
        arrowRains.add(new ArrowRain(SpellType.ARROW_RAIN, xPos, yPos));
    }

    public void spawnSkyBeam() {
        Level currentLevel = gameState.getLevelManager().getCurrentLevel();
        int levelWidthInTiles = currentLevel.getLevelTilesWidth();
        int maxPixelX = levelWidthInTiles * TILES_SIZE;
        int xPos = rand.nextInt(maxPixelX);
        int yPos = 0;
        skyBeams.add(new RoricSkyBeam(SpellType.RORIC_SKY_BEAM, xPos, yPos, false));
    }

    public void spawnSkyBeamAt(int xPos) {
        int xOffset = (int) (24 * SCALE);
        int yPos = 0;
        skyBeams.add(new RoricSkyBeam(SpellType.RORIC_SKY_BEAM, xPos - xOffset, yPos, true));
    }

    public void startSkyBeams() {
        this.isSkyBeamActive = true;
    }

    public void stopSkyBeams() {
        this.isSkyBeamActive = false;
    }

    public void startArrowRainTelegraph(Player player) {
        this.isTelegraphingArrowRain = true;
        this.telegraphPosition = new Point((int) player.getHitBox().getCenterX(), (int) player.getHitBox().getCenterY());
    }

    public void stopArrowRainTelegraph() {
        this.isTelegraphingArrowRain = false;
    }

    private boolean isStaticSpell(Spell spell) {
        return spell instanceof Lightning || spell instanceof Flash;
    }

    public void reset() {
        resetSpells(bossLightnings);
        resetSpells(bossFlashes);
        roricBeams.clear();
        arrowRains.clear();
        skyBeams.clear();
        isSkyBeamActive = false;
        isTelegraphingArrowRain = false;
        skyBeamSpawnTimer = 0;
    }

    private void resetSpells(List<? extends Spell> spells) {
        spells.forEach(Spell::reset);
    }

    public Flame getFlames() {
        return flame;
    }
}
