package platformer.model.levels;

import platformer.animation.Animation;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.effects.particles.AmbientParticle;
import platformer.model.entities.effects.particles.AmbientParticleFactory;
import platformer.model.entities.effects.particles.AmbientParticleType;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

/**
 * This class is managing the levels in the game.
 * It holds references to all the levels and provides methods for loading and rendering them.
 * <p>
 * It uses the Flyweight pattern for managing Particle objects to save memory.
 * The ParticleFactory class is used to create and manage Particle objects.
 */
public class LevelManager {

    private final GameState gameState;
    private final LevelObjectManager levelObjectManager;

    private BufferedImage[] levelSprite;
    private final Level[][] levels = new Level[MAX_LEVELS][MAX_LEVELS];
    private int levelIndexI = 0, levelIndexJ = 0;

    // Particle Flyweight
    private final AmbientParticle[] ambientParticles;
    private final AmbientParticleFactory ambientParticleFactory;

    public LevelManager(GameState gameState) {
        this.gameState = gameState;
        this.ambientParticleFactory = new AmbientParticleFactory();
        this.ambientParticles = loadParticles();
        this.levelObjectManager = new LevelObjectManager();
        loadForestSprite();
        buildLevels();
    }

    // Init
    private void loadForestSprite() {
        BufferedImage img = Utils.getInstance().importImage(FOREST_SPRITE, -1, -1);
        levelSprite = new BufferedImage[MAX_TILE_VALUE];
        for (int i = 0; i < FOREST_SPRITE_ROW; i++) {
            for (int j = 0; j < FOREST_SPRITE_COL; j++) {
                int index = j*FOREST_SPRITE_COL + i;
                levelSprite[index] = img.getSubimage(i*FOREST_SPRITE_W, j*FOREST_SPRITE_H, FOREST_SPRITE_W, FOREST_SPRITE_H);
            }
        }
    }

    /**
     * This method builds all the levels in the game.
     * It loads the images for each level and creates a new Level object for each one.
     * The Level objects are stored in a 2D array.
     */
    private void buildLevels() {
        BufferedImage[][] levelsLayer1 = getAllLevels("1");
        BufferedImage[][] levelsLayer2 = getAllLevels("2");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < levelsLayer1.length; j++) {
                if (levelsLayer1[i][j] != null)
                    levels[i][j] = new Level("level"+i+j, levelsLayer1[i][j], levelsLayer2[i][j]);
            }
        }
        Logger.getInstance().notify("Levels built successfully!", Message.NOTIFICATION);
    }

    private BufferedImage[][] getAllLevels(String layer) {
        BufferedImage[][] levels = new BufferedImage[MAX_LEVELS][MAX_LEVELS];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < levels.length; j++) {
                BufferedImage levelImg = Utils.getInstance().importImage(LEVEL_SPRITES.replace("$", i+""+j), -1, -1);
                if (levelImg == null) continue;
                if (layer.equals("1")) {
                    levels[i][j] = levelImg.getSubimage(0, 0, levelImg.getWidth()/2, levelImg.getHeight());
                }
                else {
                    levels[i][j] = levelImg.getSubimage(levelImg.getWidth()/2, 0, levelImg.getWidth()/2, levelImg.getHeight());
                }
            }
        }
        return levels;
    }

    /**
     * This method creates and initializes the particles used in the game.
     * It uses the Flyweight pattern to manage the particles, which helps to save memory.
     * The ParticleFactory is used to create and manage the Particle objects.
     * @return An array of Particle objects.
     */
    private AmbientParticle[] loadParticles() {
        AmbientParticle[] ambientParticles = new AmbientParticle[PARTICLES_CAP];
        Random rand = new Random();
        for (int i = 0; i < ambientParticles.length; i++) {
            int xPos = rand.nextInt(GAME_WIDTH-10) + 10;
            int yPos = rand.nextInt(GAME_HEIGHT-10) + 10;
            int size = (int)((rand.nextInt(15-5) + 5) * SCALE);
            String key = "DefaultParticle";
            BufferedImage[] images = Animation.getInstance().loadFromSprite(PARTICLE_SHEET, DEFAULT_PARTICLE_FRAMES, 0, size, size, 0, PARTICLE_W, PARTICLE_H);
            AmbientParticleType ambientParticleType = ambientParticleFactory.getParticleImage(key, images);
            ambientParticles[i] = new AmbientParticle(ambientParticleType, size, xPos, yPos);
        }
        return ambientParticles;
    }

    // Level flow
    private void loadLevel() {
        Level newLevel = levels[levelIndexI][levelIndexJ];
        gameState.getPlayer().loadLvlData(newLevel.getLvlData());
        gameState.getEnemyManager().loadEnemies(newLevel);
        gameState.getObjectManager().loadObjects(newLevel);
        gameState.getSpellManager().initBossSpells();
        gameState.getMinimapManager().changeLevel(levelIndexI, levelIndexJ);
        gameState.getPlayer().activateMinimap(true);
    }

    public void loadNextLevel(int dI, int dJ) {
        levelIndexI += dI;
        levelIndexJ += dJ;
        if (levelIndexI < 0 || levelIndexI >= levels.length || levelIndexJ < 0 || levelIndexJ >= levels[0].length) {
            gameState.getGame().startMenuState();
        }
        else loadLevel();
    }

    // Render
    private void renderDeco(Graphics g, int xLevelOffset, int yLevelOffset, int layer) {
        Level level = levels[levelIndexI][levelIndexJ];
        for (int i = 0; i < level.getLvlData().length; i++) {
            for (int j = 0; j < level.getLvlData()[0].length; j++) {
                int decorationIndex = level.getDecoSpriteIndex(i, j);
                int layerIndex = level.getLayerSpriteIndex(i, j);
                if (decorationIndex == -1) continue;
                int x = TILES_SIZE * i - xLevelOffset;
                int y = TILES_SIZE * j - yLevelOffset;
                LvlObjType lvlObj = LvlObjType.values()[decorationIndex];
                if (layerIndex == layer) {
                    BufferedImage model = levelObjectManager.getModels()[decorationIndex];
                    int xPos = x + (int)(lvlObj.getYOffset() * SCALE);
                    int yPos = y + (int)(lvlObj.getXOffset() * SCALE);
                    g.drawImage(model, xPos, yPos, lvlObj.getWid(), lvlObj.getHei(), null);
                }

            }
        }
    }

    private void renderTerrain(Graphics g, int xLevelOffset, int yLevelOffset, boolean behind) {
        Level level = levels[levelIndexI][levelIndexJ];
        for (int i = 0; i < level.getLvlData().length; i++) {
            for (int j = 0; j < level.getLvlData()[0].length; j++) {
                int tileIndex = level.getSpriteIndex(i, j);
                if ((!behind && tileIndex < 255) || tileIndex == -1) continue; // Invalid index
                if (behind && tileIndex >= 255) continue; // Layer behind
                if (!behind) tileIndex -= 255;
                int x = TILES_SIZE * i - xLevelOffset;
                int y = TILES_SIZE * j - yLevelOffset;
                int size = TILES_SIZE+1;
                g.drawImage(levelSprite[tileIndex], x, y, size, size, null);
            }
        }
    }

    private void renderParticles(Graphics g) {
        Arrays.stream(ambientParticles).forEach(p -> p.render(g));
    }

    /**
     * This method is responsible for rendering the game level.
     * It renders the decorations, terrain, and particles of the level.
     * The rendering order is carefully managed to ensure correct layering of the game elements.
     * <p>
     * The rendering process is as follows:
     * 1. Render the first decoration layer. This includes any background elements that should appear behind all other game elements.
     * 2. Render the second decoration layer. This includes any elements that should appear behind the terrain but in front of the first decoration layer.
     * 3. Draw a green ambient layer over the entire game area. This gives the game a greenish tint.
     * 4. Render the third decoration layer. This includes any elements that should appear in front of the terrain but behind the player and enemies.
     * 5. Render the terrain layer that is behind the player and enemies. This includes any ground, walls, or other static elements that the player and enemies can interact with.
     * 6. Render the fourth decoration layer. This includes any elements that should appear in front of the player and enemies but behind the particles.
     * 7. Render the terrain layer that is in front of the player and enemies. This includes any ground, walls, or other static elements that the player and enemies can interact with.
     * 8. Render the particles. These are small, dynamic elements that add visual interest to the game, such as sparks or dust.
     *
     * @param g The Graphics object used for rendering. This is a standard Java Graphics object that provides methods for drawing onto the screen.
     * @param xLevelOffset The horizontal offset of the level. This is used for scrolling the level horizontally when the player moves.
     * @param yLevelOffset The vertical offset of the level. This is used for scrolling the level vertically when the player jumps or falls.
     */
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderDeco(g, xLevelOffset, yLevelOffset, 0);               // First deco layer
        renderDeco(g, xLevelOffset, yLevelOffset, 1);               // Second deco layer
        g.setColor(new Color(1, 130, 120, 110));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);                 // Green ambient layer
        renderDeco(g, xLevelOffset, yLevelOffset, 2);              // Third deco layer
        renderTerrain(g, xLevelOffset, yLevelOffset, true);       // Terrain behind layer
        renderDeco(g, xLevelOffset, yLevelOffset, 4);              // Fourth deco layer
        renderTerrain(g, xLevelOffset, yLevelOffset, false);      // Terrain normal layer
        renderParticles(g);
    }

    public Level getCurrentLevel() {
        return levels[levelIndexI][levelIndexJ];
    }

    /**
     * Loads the correct level and sets the current level indices based on a spawn ID.
     * @param spawnId the spawn number from the player's account.
     */
    public void loadSavePoint(int spawnId) {
        for (Spawn spawn : Spawn.values()) {
            if (spawn.getId() == spawnId) {
                this.levelIndexI = spawn.getLevelI();
                this.levelIndexJ = spawn.getLevelJ();
                loadLevel();
                return;
            }
        }

        this.levelIndexI = Spawn.INITIAL.getLevelI();
        this.levelIndexJ = Spawn.INITIAL.getLevelJ();
        loadLevel();
    }

    // Getters
    public AmbientParticle[] getParticles() {
        return ambientParticles;
    }

    public int getLevelIndexI() {
        return levelIndexI;
    }

    public int getLevelIndexJ() {
        return levelIndexJ;
    }
}
