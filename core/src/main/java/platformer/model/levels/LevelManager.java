package platformer.model.levels;

import com.google.gson.Gson;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.metadata.LevelMetadata;
import platformer.model.levels.metadata.ObjectMetadata;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
    private final BackgroundManager backgroundManager;
    private final Map<Point, ObjectMetadata> decorationMetadata;

    private BufferedImage[] levelSprite;
    private final Level[][] levels = new Level[MAX_LEVELS][MAX_LEVELS];
    private Level arenaLevel;
    private Level currentLevel;
    private int levelIndexI = 0, levelIndexJ = 0;
    private BufferedImage currentBackground;
    private LevelMetadata currentLevelMetadata;

    public LevelManager(GameState gameState) {
        this.gameState = gameState;
        this.decorationMetadata = new HashMap<>();
        this.backgroundManager = new BackgroundManager();
        this.currentBackground = backgroundManager.getDefaultBackground();
        this.levelObjectManager = new LevelObjectManager();
        loadForestSprite();
        buildLevels();
        this.currentLevel = levels[levelIndexI][levelIndexJ];
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
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < levelsLayer1.length; j++) {
                if (levelsLayer1[i][j] != null)
                    levels[i][j] = new Level("level"+i+j, levelsLayer1[i][j], levelsLayer2[i][j]);
            }
        }
        buildArenaLevels();
        Logger.getInstance().notify("Levels built successfully!", Message.NOTIFICATION);
    }

    /**
     * This method builds the arena levels.
     * It loads the arena level image and creates a new Level object for it.
     */
    private void buildArenaLevels() {
        BufferedImage arenaImg = Utils.getInstance().importImage(LEVEL_SPRITES.replace("$", "arena1"), -1, -1);
        if (arenaImg != null) {
            BufferedImage arenaLayer1 = arenaImg.getSubimage(0, 0, arenaImg.getWidth()/2, arenaImg.getHeight());
            BufferedImage arenaLayer2 = arenaImg.getSubimage(arenaImg.getWidth()/2, 0, arenaImg.getWidth()/2, arenaImg.getHeight());
            arenaLevel = new Level("arena1", arenaLayer1, arenaLayer2);
        }
    }

    private BufferedImage[][] getAllLevels(String layer) {
        BufferedImage[][] levels = new BufferedImage[MAX_LEVELS][MAX_LEVELS];
        for (int i = 0; i < 3; i++) {
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

    // Level flow
    private void loadLevel() {
        Level newLevel = levels[levelIndexI][levelIndexJ];
        gameState.getPlayer().loadLvlData(newLevel.getLvlData());
        gameState.getEnemyManager().loadEnemies(newLevel);
        gameState.getObjectManager().loadObjects(newLevel);
        loadMetadata();
        gameState.getSpellManager().initBossSpells();
        gameState.getMinimapManager().changeLevel();
        gameState.getPlayer().activateMinimap(true);
        loadBackground();
    }

    public void loadNextLevel(int dI, int dJ) {
        levelIndexI += dI;
        levelIndexJ += dJ;
        if (levelIndexI < 0 || levelIndexI >= levels.length || levelIndexJ < 0 || levelIndexJ >= levels[0].length) {
            gameState.getGame().startMenuState();
        }
        else loadLevel();
    }

    private void loadBackground() {
        if (currentLevelMetadata != null && currentLevelMetadata.getBackgroundId() != null) {
            this.currentBackground = backgroundManager.getBackground(currentLevelMetadata.getBackgroundId());
        }
        else this.currentBackground = backgroundManager.getDefaultBackground();
    }

    private void loadMetadata() {
        decorationMetadata.clear();
        String levelName = "level" + levelIndexI + levelIndexJ;
        String jsonPath = "/meta/" + levelName + ".json";

        try (InputStream is = getClass().getResourceAsStream(jsonPath)) {
            if (is == null) {
                currentLevelMetadata = null;
                return;
            }
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                LevelMetadata metadata = gson.fromJson(reader, LevelMetadata.class);
                this.currentLevelMetadata = metadata;
                if (metadata != null && metadata.getDecorations() != null) {
                    for (ObjectMetadata meta : metadata.getDecorations()) {
                        decorationMetadata.put(new Point(meta.getX(), meta.getY()), meta);
                    }
                }
            }
        } catch (Exception e) {
            this.currentLevelMetadata = null;
            Logger.getInstance().notify("Could not load metadata for " + levelName + ": " + e.getMessage(), Message.ERROR);
        }
    }

    public void switchToArena() {
        if (arenaLevel != null) {
            this.currentLevel = arenaLevel;
        }
        else Logger.getInstance().notify("Arena level is not loaded!", Message.ERROR);
    }

    public void returnToMainMap() {
        this.currentLevel = levels[levelIndexI][levelIndexJ];
    }

    // Render
    private void renderDeco(Graphics g, int xLevelOffset, int yLevelOffset, int layer) {
        Level level = currentLevel;
        for (int i = 0; i < level.getLvlData().length; i++) {
            for (int j = 0; j < level.getLvlData()[0].length; j++) {
                int decorationIndex = level.getDecoSpriteIndex(i, j);
                int layerIndex = level.getLayerSpriteIndex(i, j);
                if (decorationIndex == -1) continue;
                LvlObjType lvlObj = LvlObjType.values()[decorationIndex];
                if (layerIndex == layer) {
                    BufferedImage model = levelObjectManager.getModels()[decorationIndex];
                    ObjectMetadata meta = decorationMetadata.get(new Point(i, j));
                    double rotation = (meta != null) ? meta.getRotation() : 0.0;
                    double scaleX = (meta != null) ? meta.getScaleX() : 1.0;
                    double scaleY = (meta != null) ? meta.getScaleY() : 1.0;

                    if (rotation == 0.0 && scaleX == 1.0 && scaleY == 1.0) {
                        int xPos = (TILES_SIZE * i - xLevelOffset) + (int) (lvlObj.getYOffset() * SCALE);
                        int yPos = (TILES_SIZE * j - yLevelOffset) + (int) (lvlObj.getXOffset() * SCALE);
                        g.drawImage(model, xPos, yPos, lvlObj.getWid(), lvlObj.getHei(), null);
                    }
                    else {
                        Graphics2D g2d = (Graphics2D) g.create();
                        int drawX = TILES_SIZE * i - xLevelOffset;
                        int drawY = TILES_SIZE * j - yLevelOffset;

                        int centerX = drawX + lvlObj.getWid() / 2;
                        int centerY = drawY + lvlObj.getHei() / 2;

                        g2d.translate(centerX, centerY);
                        g2d.rotate(Math.toRadians(rotation));
                        g2d.scale(scaleX, scaleY);

                        g2d.drawImage(model, -lvlObj.getWid() / 2, -lvlObj.getHei() / 2, lvlObj.getWid(), lvlObj.getHei(), null);
                        g2d.dispose();
                    }
                }
            }
        }
    }

    private void renderTerrain(Graphics g, int xLevelOffset, int yLevelOffset, boolean behind) {
        Level level = currentLevel;
        int xStart = xLevelOffset / TILES_SIZE;
        int xEnd = (xLevelOffset + GAME_WIDTH) / TILES_SIZE + 1;
        int yStart = yLevelOffset / TILES_SIZE;
        int yEnd = (yLevelOffset + GAME_HEIGHT) / TILES_SIZE + 1;
        xStart = Math.max(0, xStart);
        xEnd = Math.min(level.getLevelTilesWidth(), xEnd);
        yStart = Math.max(0, yStart);
        yEnd = Math.min(level.getLevelTilesHeight(), yEnd);

        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
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
        g.setColor(new Color(1, 130, 120, 60));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);                 // Green ambient layer
        renderDeco(g, xLevelOffset, yLevelOffset, 2);              // Third deco layer
        renderTerrain(g, xLevelOffset, yLevelOffset, true);       // Terrain behind layer
        renderDeco(g, xLevelOffset, yLevelOffset, 4);              // Fourth deco layer
        renderTerrain(g, xLevelOffset, yLevelOffset, false);      // Terrain normal layer
    }

    public Level getCurrentLevel() {
        return currentLevel;
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
                this.currentLevel = levels[levelIndexI][levelIndexJ];
                loadLevel();
                return;
            }
        }

        this.levelIndexI = Spawn.INITIAL.getLevelI();
        this.levelIndexJ = Spawn.INITIAL.getLevelJ();
        loadLevel();
    }

    // Getters
    public int getLevelIndexI() {
        return levelIndexI;
    }

    public int getLevelIndexJ() {
        return levelIndexJ;
    }

    public BufferedImage getCurrentBackground() {
        return currentBackground;
    }
}
