package platformer.model.levels;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.metadata.DecorationMetadata;
import platformer.model.levels.metadata.LevelMetadata;
import platformer.model.levels.metadata.ObjectMetadata;
import platformer.model.levels.metadata.TilesetMetadata;
import platformer.state.types.GameState;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private GameContext context;
    private final LevelObjectManager levelObjectManager;
    private final BackgroundManager backgroundManager;
    private final Map<Point, ObjectMetadata> decorationMetadata;

    private final Map<String, BufferedImage[]> tilesetSprites = new HashMap<>();
    private final Map<String, Map<Integer, DecorationMetadata>> decorationSets = new HashMap<>();
    private final Level[][] levels = new Level[MAX_LEVELS][MAX_LEVELS];
    private Level arenaLevel;
    private Level currentLevel;
    private int levelIndexI = 0, levelIndexJ = 0;
    private BufferedImage currentBackground;
    private LevelMetadata currentLevelMetadata, arenaLevelMetadata;

    public LevelManager() {
        this.decorationMetadata = new HashMap<>();
        this.backgroundManager = new BackgroundManager();
        this.currentBackground = backgroundManager.getDefaultBackground();
        this.levelObjectManager = new LevelObjectManager();
        loadTilesets();
        loadDecorationSets();
        buildLevels();
    }

    public void wire(GameContext context) {
        this.context = context;
        this.currentLevel = levels[levelIndexI][levelIndexJ];
    }

    // Init
    private void loadTilesets() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(TILESET_META)))) {
            Type listType = new TypeToken<List<TilesetMetadata>>(){}.getType();
            List<TilesetMetadata> tilesetMetadata = new Gson().fromJson(reader, listType);

            for (TilesetMetadata meta : tilesetMetadata) {
                BufferedImage img = ImageUtils.importImage(meta.getSpritePath(), -1, -1);
                if (img == null) continue;

                BufferedImage[] sprites = new BufferedImage[meta.getTileCount()];
                for (int i = 0; i < meta.getRows(); i++) {
                    for (int j = 0; j < meta.getColumns(); j++) {
                        int index = j * meta.getRows() + i;
                        if (index >= meta.getTileCount()) break;
                        sprites[index] = img.getSubimage(i * meta.getTileSizeInSprite(), j * meta.getTileSizeInSprite(), meta.getTileSizeInSprite(), meta.getTileSizeInSprite());
                    }
                }
                tilesetSprites.put(meta.getName(), sprites);
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load tilesets from JSON: " + e.getMessage(), Message.ERROR);
        }
    }

    private void loadDecorationSets() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(DECORATION_META)))) {
            Type type = new TypeToken<Map<String, List<DecorationMetadata>>>() {}.getType();
            Map<String, List<DecorationMetadata>> loadedSets = new Gson().fromJson(reader, type);

            for (Map.Entry<String, List<DecorationMetadata>> entry : loadedSets.entrySet()) {
                Map<Integer, DecorationMetadata> decoMap = new HashMap<>();
                for (DecorationMetadata meta : entry.getValue()) {
                    decoMap.put(meta.getColorValue(), meta);
                }
                decorationSets.put(entry.getKey(), decoMap);
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load decoration sets from JSON: " + e.getMessage(), Message.ERROR);
        }
    }

    /**
     * This method builds all the levels in the game.
     * It loads the images for each level and creates a new Level object for each one.
     * The Level objects are stored in a 2D array.
     */
    private void buildLevels() {
        BufferedImage[][] levelsImg = getAllLevels();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < levelsImg.length; j++) {
                if (levelsImg[i][j] != null) {
                    String levelName = "level" + i + j;
                    LevelMetadata meta = loadMetadataForLevel(levelName);
                    String tileset = (meta != null && meta.getTileset() != null) ? meta.getTileset() : "Forest";
                    levels[i][j] = new Level(levelName, levelsImg[i][j], tileset);
                }
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
        BufferedImage arenaImg = ImageUtils.importImage(LEVEL_SPRITES.replace("$", "arena1"), -1, -1);
        if (arenaImg != null) {
            arenaLevelMetadata = loadMetadataForLevel("levelarena1");
            String tileset = (arenaLevelMetadata != null && arenaLevelMetadata.getTileset() != null) ? arenaLevelMetadata.getTileset() : "Forest";
            arenaLevel = new Level("arena1", arenaImg, tileset);
        }
    }

    private BufferedImage[][] getAllLevels() {
        BufferedImage[][] levels = new BufferedImage[MAX_LEVELS][MAX_LEVELS];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < levels.length; j++) {
                BufferedImage levelImg = ImageUtils.importImage(LEVEL_SPRITES.replace("$", i+""+j), -1, -1);
                if (levelImg == null) continue;
                levels[i][j] = levelImg;
            }
        }
        return levels;
    }

    // Level flow
    private void loadLevel() {
        Level newLevel = levels[levelIndexI][levelIndexJ];
        this.currentLevel = newLevel;
        context.getGameState().getPlayer().loadLvlData(newLevel.getLvlData());
        context.getEnemyManager().loadEnemies(newLevel);
        context.getObjectManager().loadObjects(newLevel);
        loadMetadata();
        context.getSpellManager().initBossSpells();
        context.getMinimapManager().changeLevel();
        context.getGameState().getPlayer().activateMinimap(true);
        loadBackground();
    }

    public void loadNextLevel(int dI, int dJ) {
        GameState gameState = context.getGameState();
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

    private LevelMetadata loadMetadataForLevel(String levelName) {
        String jsonPath = "/meta/" + levelName + ".json";
        try (InputStream is = getClass().getResourceAsStream(jsonPath)) {
            if (is == null) return null;
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(reader, LevelMetadata.class);
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Could not load metadata for " + levelName + ": " + e.getMessage(), Message.ERROR);
            return null;
        }
    }

    private void loadMetadata() {
        decorationMetadata.clear();
        String levelName = "level" + levelIndexI + levelIndexJ;
        this.currentLevelMetadata = loadMetadataForLevel(levelName);
        if (currentLevelMetadata != null && currentLevelMetadata.getDecorations() != null) {
            for (ObjectMetadata meta : currentLevelMetadata.getDecorations()) {
                decorationMetadata.put(new Point(meta.getX(), meta.getY()), meta);
            }
        }
    }

    public void switchToArena() {
        if (arenaLevel != null) {
            this.currentLevel = arenaLevel;
            this.currentLevelMetadata = arenaLevelMetadata;
            loadBackground();
        }
        else Logger.getInstance().notify("Arena level is not loaded!", Message.ERROR);
    }

    public void returnToMainMap() {
        this.currentLevel = levels[levelIndexI][levelIndexJ];
        loadMetadata();
        loadBackground();
    }

    // Render
    private void renderDeco(Graphics g, int xLevelOffset, int yLevelOffset, int layer) {
        Level level = currentLevel;
        Map<Integer, DecorationMetadata> currentDecoSet = decorationSets.get(level.getTilesetName());
        if (currentDecoSet == null) return;

        for (int i = 0; i < level.getLvlData().length; i++) {
            for (int j = 0; j < level.getLvlData()[0].length; j++) {
                int decorationIndex = level.getDecoSpriteIndex(i, j);
                int layerIndex = level.getLayerSpriteIndex(i, j);
                if (decorationIndex == -1 || layerIndex != layer) continue;

                DecorationMetadata decoMeta = currentDecoSet.get(decorationIndex);
                if (decoMeta == null) continue;
                BufferedImage model = levelObjectManager.getModel(decoMeta.getId(), level.getTilesetName(), decoMeta.getFilename(), decoMeta.isFlipped());
                if (model == null) continue;

                ObjectMetadata meta = decorationMetadata.get(new Point(i, j));
                double rotation = (meta != null) ? meta.getRotation() : 0.0;
                double scaleX = (meta != null) ? meta.getScaleX() : 1.0;
                double scaleY = (meta != null) ? meta.getScaleY() : 1.0;
                int xPos = (TILES_SIZE * i - xLevelOffset) + (int) (decoMeta.getYOffset() * SCALE);
                int yPos = (TILES_SIZE * j - yLevelOffset) + (int) (decoMeta.getXOffset() * SCALE);

                if (rotation == 0.0 && scaleX == 1.0 && scaleY == 1.0) {
                    g.drawImage(model, xPos, yPos, decoMeta.getWid(), decoMeta.getHei(), null);
                }
                else {
                    Graphics2D g2d = (Graphics2D) g.create();
                    int drawX = TILES_SIZE * i - xLevelOffset;
                    int drawY = TILES_SIZE * j - yLevelOffset;
                    int centerX = drawX + decoMeta.getWid() / 2;
                    int centerY = drawY + decoMeta.getHei() / 2;

                    g2d.translate(centerX, centerY);
                    g2d.rotate(Math.toRadians(rotation));
                    g2d.scale(scaleX, scaleY);
                    g2d.drawImage(model, -decoMeta.getWid() / 2, -decoMeta.getHei() / 2, decoMeta.getWid(), decoMeta.getHei(), null);
                    g2d.dispose();
                }
            }
        }
    }

    private void renderTerrain(Graphics g, int xLevelOffset, int yLevelOffset, boolean behind) {
        Level level = currentLevel;
        BufferedImage[] currentTileset = tilesetSprites.get(level.getTilesetName());
        if (currentTileset == null) return;

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
                g.drawImage(currentTileset[tileIndex], x, y, size, size, null);
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
    public LevelMetadata getCurrentLevelMetadata() {
        return currentLevelMetadata;
    }

    public int getLevelIndexI() {
        return levelIndexI;
    }

    public int getLevelIndexJ() {
        return levelIndexJ;
    }

    public BufferedImage getCurrentBackground() {
        return currentBackground;
    }

    public boolean isInArena() {
        return currentLevel == arenaLevel;
    }
}
