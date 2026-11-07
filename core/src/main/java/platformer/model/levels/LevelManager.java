package platformer.model.levels;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.player.PlayerAction;
import platformer.model.levels.metadata.*;
import platformer.model.levels.render.DecorationRenderer;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

/**
 * Manages loading, caching, and rendering of levels in the world graph.
 */
public class LevelManager {

    private GameContext context;
    private final LevelObjectManager levelObjectManager;
    private final DecorationRenderer decorationRenderer;
    private final BackgroundManager backgroundManager;
    private final Map<Point, ObjectMetadata> decorationMetadata;

    private final Map<String, BufferedImage[]> tilesetSprites = new HashMap<>();
    private final Map<String, Map<Integer, DecorationMetadata>> decorationSets = new HashMap<>();
    private final Map<String, Level> levels = new HashMap<>();
    private WorldMapMetadata worldMap;

    private Level arenaLevel;
    private Level currentLevel;
    private String currentLevelId = "level00";
    private BufferedImage currentBackground;
    private LevelMetadata currentLevelMetadata, arenaLevelMetadata;

    // Effects
    private final Set<Point> touchedGrassTiles = new HashSet<>();

    public LevelManager() {
        this.decorationMetadata = new HashMap<>();
        this.backgroundManager = new BackgroundManager();
        this.currentBackground = backgroundManager.getDefaultBackground();
        this.levelObjectManager = new LevelObjectManager();
        this.decorationRenderer = new DecorationRenderer(levelObjectManager);
        loadWorldMap();
        loadTilesets();
        loadDecorationSets();
        buildArenaLevels();
    }

    public void wire(GameContext context) {
        this.context = context;
        this.currentLevel = getOrLoadLevel(currentLevelId);
        loadMetadata();
        loadBackground();
    }

    private void loadWorldMap() {
        try (InputStream is = getClass().getResourceAsStream(WORLD_MAP_META)) {
            if (is != null) {
                try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    this.worldMap = new Gson().fromJson(reader, WorldMapMetadata.class);
                    if (worldMap != null && worldMap.getStartLevel() != null) {
                        this.currentLevelId = worldMap.getStartLevel();
                    }
                }
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load world_map.json: " + e.getMessage(), Message.ERROR);
        }
        if (this.worldMap == null) this.worldMap = new WorldMapMetadata();
    }

    /**
     * Lazily loads a level by its string ID if it hasn't been loaded yet, and caches it.
     */
    private Level getOrLoadLevel(String levelId) {
        if (levels.containsKey(levelId)) return levels.get(levelId);

        BufferedImage levelImg = ImageUtils.importImage(LEVEL_SPRITES.replace("$", levelId), -1, -1);
        if (levelImg == null) {
            Logger.getInstance().notify("Level image not found for ID: " + levelId, Message.WARNING);
            return null;
        }

        LevelMetadata meta = loadMetadataForLevel(levelId);
        String tileset = (meta != null && meta.getTileset() != null) ? meta.getTileset() : "Forest";

        Level level = new Level(levelImg, tileset);
        levels.put(levelId, level);
        return level;
    }

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
     * This method builds the arena levels.
     * It loads the arena level image and creates a new Level object for it.
     */
    private void buildArenaLevels() {
        BufferedImage arenaImg = ImageUtils.importImage(LEVEL_SPRITES.replace("$", "levelarena1"), -1, -1);
        if (arenaImg != null) {
            arenaLevelMetadata = loadMetadataForLevel("levelarena1");
            String tileset = (arenaLevelMetadata != null && arenaLevelMetadata.getTileset() != null) ? arenaLevelMetadata.getTileset() : "Forest";
            arenaLevel = new Level(arenaImg, tileset);
        }
    }

    // Level flow
    /**
     * Resolves the underlying filename for a logical room ID. If a room has been restored, loads <levelId>_restored instead.
     */
    private String resolveEffectiveFileId(String levelId) {
        if (context != null && context.getWorldMemoryManager() != null) {
            if (context.getWorldMemoryManager().hasFlag("RESTORED_" + levelId)) {
                return levelId + "_restored";
            }
        }
        return levelId;
    }

    private void loadLevel() {
        String effectiveId = resolveEffectiveFileId(currentLevelId);
        Level newLevel = getOrLoadLevel(effectiveId);
        if (newLevel == null) {
            context.getGameState().getGame().startMenuState();
            return;
        }
        loadMetadata();
        context.getWorldMemoryManager().onRoomEntered(currentLevelId, newLevel);
        this.currentLevel = newLevel;
        context.getGameState().getPlayer().loadLvlData(newLevel.getLvlData());
        context.getEnemyManager().loadEnemies(newLevel);
        context.getObjectManager().loadObjects(newLevel);
        context.getSpellManager().initBossSpells();
        context.getMinimapManager().changeLevel();
        context.getGameState().getPlayer().activateMinimap(true);
        loadBackground();
    }

    /**
     * Transitions to the specified target level ID in the world graph.
     */
    public void loadLevelById(String targetLevelId) {
        Level next = getOrLoadLevel(targetLevelId);
        if (next == null) context.getGameState().getGame().startMenuState();
        else {
            this.currentLevelId = targetLevelId;
            loadLevel();
        }
    }

    /**
     * Resolves the exit transition rule for the current room given a trigger type and tile coordinates.
     */
    public RoomExitMetadata resolveExit(LvlTriggerType triggerType, int tileX, int tileY) {
        if (worldMap == null || worldMap.getRooms() == null) return null;
        RoomNodeMetadata node = worldMap.getRooms().get(currentLevelId);
        if (node == null) return null;
        return node.findExit(triggerType, tileX, tileY);
    }

    /**
     * Returns the minimap top-left coordinates (mapX, mapY) for a level ID if configured in world_map.json.
     */
    public Point getConfiguredMinimapPosition(String levelId) {
        if (worldMap == null || worldMap.getRooms() == null) return null;
        RoomNodeMetadata node = worldMap.getRooms().get(levelId);
        if (node != null && node.getMapX() != null && node.getMapY() != null) {
            return new Point(node.getMapX(), node.getMapY());
        }
        return null;
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
        String effectiveId = resolveEffectiveFileId(currentLevelId);
        this.currentLevelMetadata = loadMetadataForLevel(effectiveId);
        loadMetadataDecorations();
    }

    private void loadMetadataDecorations() {
        decorationMetadata.clear();
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

    public void setInteriorLevel(Level interiorLevel, LevelMetadata meta) {
        this.currentLevel = interiorLevel;
        this.currentLevelMetadata = meta;
        loadMetadataDecorations();
        loadBackground();
    }

    public void restoreMainMap() {
        String effectiveId = resolveEffectiveFileId(currentLevelId);
        this.currentLevel = getOrLoadLevel(effectiveId);
        loadMetadata();
        loadBackground();

        context.getLightManager().releaseAmbientDarkness();
        boolean particles = currentLevelMetadata == null || currentLevelMetadata.getAmbientParticlesEnabled() == null || currentLevelMetadata.getAmbientParticlesEnabled();
        context.getEffectManager().setAmbientEffectsActive(particles);
    }

    /**
     * Hot-swaps the current room to its restored variant during a fade-to-black cutscene, loading all newly placed objects, NPCs, and metadata.
     */
    public void reloadCurrentLevelVariant() {
        String effectiveId = resolveEffectiveFileId(currentLevelId);
        Level variantLevel = getOrLoadLevel(effectiveId);
        if (variantLevel == null) return;

        this.currentLevel = variantLevel;
        loadMetadata();
        loadBackground();

        context.getPlayer().loadLvlData(variantLevel.getLvlData());
        context.getEnemyManager().loadEnemies(variantLevel);
        context.getObjectManager().loadObjects(variantLevel);
        context.getSpellManager().initBossSpells();
        context.getCamera().updateLevelBounds(variantLevel);

        boolean particles = currentLevelMetadata == null || currentLevelMetadata.getAmbientParticlesEnabled() == null || currentLevelMetadata.getAmbientParticlesEnabled();
        context.getEffectManager().setAmbientEffectsActive(particles);
    }

    public void checkDecorationCut(Rectangle2D.Double attackBox) {
        Level level = currentLevel;
        Map<Integer, DecorationMetadata> currentDecoSet = decorationSets.get(level.getTilesetName());
        if (currentDecoSet == null) return;

        int startX = Math.max(0, (int) (attackBox.x / TILES_SIZE) - 2);
        int endX = Math.min(level.getLvlData().length - 1, (int) ((attackBox.x + attackBox.width) / TILES_SIZE) + 2);
        int startY = Math.max(0, (int) (attackBox.y / TILES_SIZE) - 2);
        int endY = Math.min(level.getLvlData()[0].length - 1, (int) ((attackBox.y + attackBox.height) / TILES_SIZE) + 2);
        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                if (level.isDecorationCut(i, j)) continue;
                DecorationMetadata decoMeta = currentDecoSet.get(level.getDecoSpriteIndex(i, j));
                if (decoMeta == null || !decoMeta.isCuttable()) continue;
                int worldX = (TILES_SIZE * i) + (int) (decoMeta.getYOffset() * SCALE);
                int worldY = (TILES_SIZE * j) + (int) (decoMeta.getXOffset() * SCALE);
                Rectangle2D.Double decoBounds = new Rectangle2D.Double(worldX, worldY, decoMeta.getWid(), decoMeta.getHei());
                if (attackBox.intersects(decoBounds)) {
                    level.markDecorationCut(i, j);
                    int slashDir = context.getPlayer() != null ? context.getPlayer().getFlipSign() : 0;
                    context.getEffectManager().spawnDustParticles(decoBounds.getCenterX(), decoBounds.getCenterY(), 14, DustType.HERB_CUT, slashDir, null);
                }
            }
        }
    }

    /**
     * Updates interactive grass bending and spawns rustle particles when the player moves through foliage.
     */
    public void updateGrassInteraction(platformer.model.entities.player.Player player) {
        Level level = currentLevel;
        if (level == null || player == null) return;

        level.getDecorationBends().entrySet().removeIf(entry -> {
            double decayed = entry.getValue() * 0.90;
            if (Math.abs(decayed) < 0.005) return true;
            entry.setValue(decayed);
            return false;
        });

        Map<Integer, DecorationMetadata> currentDecoSet = decorationSets.get(level.getTilesetName());
        if (currentDecoSet == null) return;

        Rectangle2D.Double pBox = player.getHitBox();
        boolean isMoving = Math.abs(player.getHorizontalSpeed()) > 0.1;
        boolean isDashing = player.checkAction(PlayerAction.DASH);
        int startX = Math.max(0, (int) (pBox.x / TILES_SIZE) - 1);
        int endX = Math.min(level.getLvlData().length - 1, (int) ((pBox.x + pBox.width) / TILES_SIZE) + 1);
        int startY = Math.max(0, (int) (pBox.y / TILES_SIZE) - 1);
        int endY = Math.min(level.getLvlData()[0].length - 1, (int) ((pBox.y + pBox.height) / TILES_SIZE) + 1);

        Set<Point> currentlyIntersecting = new HashSet<>();
        for (int i = startX; i <= endX; i++) {
            for (int j = startY; j <= endY; j++) {
                if (level.isDecorationCut(i, j)) continue;
                DecorationMetadata decoMeta = currentDecoSet.get(level.getDecoSpriteIndex(i, j));
                if (decoMeta == null) continue;
                if (decoMeta.getSway() != null && !decoMeta.getSway().equalsIgnoreCase("BOTTOM")) continue;

                int worldX = (TILES_SIZE * i) + (int) (decoMeta.getYOffset() * SCALE);
                int worldY = (TILES_SIZE * j) + (int) (decoMeta.getXOffset() * SCALE);
                Rectangle2D.Double decoBounds = new Rectangle2D.Double(worldX, worldY, decoMeta.getWid(), decoMeta.getHei());
                if (pBox.intersects(decoBounds)) {
                    Point pt = new Point(i, j);
                    currentlyIntersecting.add(pt);
                    if (isMoving) {
                        double targetBend = -player.getFlipSign() * (isDashing ? 0.35 : 0.22);
                        level.setDecorationBend(pt, targetBend);
                        if (!touchedGrassTiles.contains(pt) && decoMeta.isCuttable()) {
                            int leafCount = isDashing ? 4 : 2;
                            context.getEffectManager().spawnDustParticles(decoBounds.getCenterX(), decoBounds.getCenterY(), leafCount, DustType.HERB_CUT, player.getFlipSign(), null);
                        }
                    }
                }
            }
        }
        touchedGrassTiles.clear();
        touchedGrassTiles.addAll(currentlyIntersecting);
    }

    // Render
    private void renderDeco(Graphics g, int xLevelOffset, int yLevelOffset, int layer) {
        Map<Integer, DecorationMetadata> currentDecoSet = decorationSets.get(currentLevel.getTilesetName());
        boolean useLively = context.getWorldMemoryManager() != null && context.getWorldMemoryManager().hasFlag("RESTORED_" + currentLevelId);
        decorationRenderer.renderLayer(g, currentLevel, currentDecoSet, decorationMetadata, xLevelOffset, yLevelOffset, layer, useLively);
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
        if (context.getInteriorManager() == null || !context.getInteriorManager().isInterior()) {
            g.setColor(new Color(1, 130, 120, 60));
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);  // Green ambient layer
        }
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
                this.currentLevelId = spawn.getLevelId();
                loadLevel();
                return;
            }
        }

        this.currentLevelId = Spawn.INITIAL.getLevelId();
        loadLevel();
    }

    // Getters
    public DecorationMetadata getDecorationMetadata(String tilesetName, int colorValue) {
        Map<Integer, DecorationMetadata> set = decorationSets.get(tilesetName);
        return (set != null) ? set.get(colorValue) : null;
    }

    public LevelMetadata getCurrentLevelMetadata() {
        return currentLevelMetadata;
    }

    public Level getLevel(String levelId) {
        return levels.get(levelId);
    }

    public String getCurrentLevelId() {
        return currentLevelId;
    }

    public BufferedImage getCurrentBackground() {
        return currentBackground;
    }

    public boolean isInArena() {
        return currentLevel == arenaLevel;
    }
}
