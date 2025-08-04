package platformer.model.spells;

import platformer.model.entities.Direction;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.player.Player;
import platformer.model.levels.Level;
import platformer.model.spells.types.*;
import platformer.state.types.GameState;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.getGroundY;

/**
 * This class is responsible for managing all the spells in the game.
 * It holds references to all the spells and provides methods for updating and rendering them.
 */
public class SpellManager {

    private final GameState gameState;

    private final List<Spell> activeSpells = new ArrayList<>();
    private final Flame flame;

    private boolean isTelegraphingArrowRain = false;
    private Point telegraphPosition;

    private final Random rand = new Random();

    public SpellManager(GameState gameState) {
        this.gameState = gameState;
        this.flame = new Flame(SpellType.FLAME_1, 0, 0, FLAME_WID, FLAME_HEI);
    }

    // Init
    public void lateInit() {
        initBossSpells();
    }

    public void initBossSpells() {
        activeSpells.clear();
        activeSpells.addAll(gameState.getLevelManager().getCurrentLevel().getSpells(Lightning.class));
        activeSpells.addAll(gameState.getLevelManager().getCurrentLevel().getSpells(Flash.class));
    }

    // Core
    public void update() {
        flame.update(gameState.getPlayer());
        activeSpells.removeIf(spell -> !isStaticSpell(spell) && !spell.isActive());
        for (Spell spell : activeSpells) {
            spell.update(gameState.getPlayer());
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (flame.isActive()) flame.render(g, xLevelOffset, yLevelOffset);
        List<Spell> snapshot = new ArrayList<>(activeSpells);
        for (Spell spell : snapshot) {
            if (spell.isActive()) spell.render(g, xLevelOffset, yLevelOffset);
        }
        renderArrowRainTelegraph(g, xLevelOffset, yLevelOffset);
    }

    // Render
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

    // Activators
    public void activateLightnings() {
        activeSpells.stream()
                .filter(spell -> spell instanceof Lightning)
                .forEach(spell -> {
                    spell.setAnimIndex(0);
                    spell.setActive(true);
                });
        gameState.getLightManager().setCurrentAmbientAlpha(0);
    }

    public void activateFlashes() {
        List<Flash> flashes = activeSpells.stream()
                .filter(spell -> spell instanceof Flash)
                .map(spell -> (Flash) spell)
                .toList();

        if (flashes.isEmpty()) return;
        int n = flashes.size();
        int k = rand.nextInt(n);
        flashes.get(k).setAnimIndex(0);
        flashes.get(k).setActive(true);
        flashes.get(n - k - 1).setAnimIndex(0);
        flashes.get(n - k - 1).setActive(true);
    }

    public void activateRoricBeam(Roric roric) {
        Direction dir = roric.getDirection();
        activeSpells.add(new RoricBeam(SpellType.RORIC_BEAM, (int)roric.getHitBox().x, (int)roric.getHitBox().y, dir));
    }

    public void activateArrowRain() {
        if (telegraphPosition == null) return;
        int xPos = telegraphPosition.x;
        double groundY = getGroundY(telegraphPosition.x, telegraphPosition.y, gameState.getLevelManager().getCurrentLevel().getLvlData());
        int yPos = (int) (groundY - (405 * SCALE));
        activeSpells.add(new ArrowRain(SpellType.ARROW_RAIN, xPos, yPos));
    }

    public void spawnSkyBeam() {
        Level currentLevel = gameState.getLevelManager().getCurrentLevel();
        int levelWidthInTiles = currentLevel.getLevelTilesWidth();
        int maxPixelX = levelWidthInTiles * TILES_SIZE;
        int xPos = rand.nextInt(maxPixelX);
        int yPos = 0;
        activeSpells.add(new RoricSkyBeam(SpellType.RORIC_SKY_BEAM, xPos, yPos, false));
    }

    public void spawnSkyBeamAt(int xPos) {
        int xOffset = (int) (24 * SCALE);
        int yPos = 0;
        activeSpells.add(new RoricSkyBeam(SpellType.RORIC_SKY_BEAM, xPos - xOffset, yPos, true));
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
        activeSpells.forEach(spell -> {
            if (isStaticSpell(spell)) spell.reset();
        });
        activeSpells.removeIf(spell -> !isStaticSpell(spell));
        isTelegraphingArrowRain = false;
    }

    public Flame getFlames() {
        return flame;
    }
}
