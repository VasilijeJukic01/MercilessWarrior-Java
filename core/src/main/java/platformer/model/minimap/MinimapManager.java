package platformer.model.minimap;

import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.levels.Level;
import platformer.model.levels.LevelManager;
import platformer.model.levels.metadata.LevelMetadata;
import platformer.model.levels.metadata.MinimapIconMetadata;
import platformer.model.levels.metadata.WorldMapMetadata;
import platformer.model.minimap.astar.AStarPathfinding;
import platformer.model.quests.Quest;
import platformer.model.quests.QuestObjective;
import platformer.ui.components.slots.QuestSlot;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static platformer.constants.AnimConstants.MINIMAP_FLASH_ANIM_SPEED;
import static platformer.constants.AnimConstants.MINIMAP_ICON_SIZE;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

/**
 * MinimapManager handles minimap operations such as finding player spawn, level positions and updating player position.
 */
public class MinimapManager {

    public record QuestMinimapMarker(Point position, String questName, String objectiveDescription) { }

    private GameContext context;
    private int[][] minimapGray;

    private BufferedImage minimapImage;
    private BufferedImage[] minimapIcons;
    private final Map<Point, String> iconRoomCache = new HashMap<>();

    private BufferedImage minimap;
    private BufferedImage visualMinimap;
    private Point2D.Double playerLocation;
    private Point pinnedLocation;
    private Point lastPathStart = null;
    private List<Point> pathPoints = new ArrayList<>();
    private Map<String, Point> levelPositions;
    private final List<MinimapIcon> icons = new ArrayList<>();

    private int flashTick = 0;
    private boolean flashVisible = true;

    private MinimapIcon hoveredIcon = null;
    private MinimapIcon pinnedIcon = null;

    private final AStarPathfinding pathfinding = new AStarPathfinding();

    private boolean fogDirty = true;
    private int dirtyMinX = Integer.MAX_VALUE, dirtyMinY = Integer.MAX_VALUE;
    private int dirtyMaxX = -1, dirtyMaxY = -1;
    private float[][] explorationMap;
    private BufferedImage fogImage;

    public MinimapManager() {
        init();
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    private void init() {
        this.minimapImage = ImageUtils.importImage(MINIMAP, -1, -1);
        this.explorationMap = new float[minimapImage.getWidth()][minimapImage.getHeight()];
        this.fogImage = new BufferedImage(minimapImage.getWidth(), minimapImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.minimap = new BufferedImage(minimapImage.getWidth(), minimapImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        this.levelPositions = new LinkedHashMap<>();
        loadMinimapIcons();
        this.minimapGray = ImageUtils.toGrayscale(minimapImage);
        colorizeMinimap();
        updateMinimap();
        this.visualMinimap = buildSmoothVisualMinimap(minimapImage);
        markFullFogDirty();
        updateFogImage();
    }

    private void markFullFogDirty() {
        this.dirtyMinX = 0;
        this.dirtyMinY = 0;
        this.dirtyMaxX = minimapImage.getWidth() - 1;
        this.dirtyMaxY = minimapImage.getHeight() - 1;
        this.fogDirty = true;
    }

    private void loadMinimapIcons() {
        this.minimapIcons = new BufferedImage[MINIMAP_ICONS_COUNT];
        BufferedImage image = ImageUtils.importImage(MINIMAP_ICONS, -1, -1);
        for (int i = 0; i < MINIMAP_ICONS_COUNT; i++) {
            minimapIcons[i] = image.getSubimage(i * MINIMAP_ICON_SIZE, 0, MINIMAP_ICON_SIZE, MINIMAP_ICON_SIZE);
        }
    }

    private Point getOrFindLevelPosition(String levelId) {
        if (levelPositions.containsKey(levelId)) {
            return levelPositions.get(levelId);
        }

        // Lookup coordinates in world_map.json
        Point configuredPos = context.getLevelManager().getConfiguredMinimapPosition(levelId);
        if (configuredPos != null) {
            levelPositions.put(levelId, configuredPos);
            return configuredPos;
        }

        // Fallback to SAD image matching
        BufferedImage levelImage = ImageUtils.importImage(LEVEL_SPRITES.replace("$", levelId), -1, -1);
        if (levelImage == null) {
            levelPositions.put(levelId, null);
            return null;
        }

        int width = levelImage.getWidth() / 3;
        BufferedImage img = levelImage.getSubimage(0, 0, width, levelImage.getHeight());
        Point position = findImageOnMinimap(img);
        levelPositions.put(levelId, position);
        return position;
    }

    private void colorizeMinimap() {
        for (int y = 0; y < minimapImage.getHeight(); y++) {
            for (int x = 0; x < minimapImage.getWidth(); x++) {
                int argb = minimapImage.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (r == 254 && g == 254 && b >= 0 && b < MinimapIconType.values().length && b != MinimapIconType.PLAYER.ordinal() && b != MinimapIconType.FLAG.ordinal()) {
                    icons.add(new MinimapIcon(new Point(x, y), MinimapIconType.values()[b]));
                    int neighborColor = sampleNeighborWalkableColor(x, y);
                    minimapImage.setRGB(x, y, neighborColor);
                }
            }
        }
    }

    private int sampleNeighborWalkableColor(int cx, int cy) {
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nx = cx + d[0], ny = cy + d[1];
            if (nx >= 0 && nx < minimapImage.getWidth() && ny >= 0 && ny < minimapImage.getHeight()) {
                int argb = minimapImage.getRGB(nx, ny);
                int a = (argb >>> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                if (a == 254 && !(r == 254 && g == 254)) {
                    return argb;
                }
            }
        }
        return new Color(63, 94, 77, 254).getRGB();
    }

    private BufferedImage buildSmoothVisualMinimap(BufferedImage src) {
        BufferedImage pass1 = scale2x(src);
        return scale2x(pass1);
    }

    private BufferedImage scale2x(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dst = new BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = src.getRGB(x, y);
                int b = (y > 0) ? src.getRGB(x, y - 1) : p;
                int hCol = (y < h - 1) ? src.getRGB(x, y + 1) : p;
                int d = (x > 0) ? src.getRGB(x - 1, y) : p;
                int f = (x < w - 1) ? src.getRGB(x + 1, y) : p;

                int e0 = p, e1 = p, e2 = p, e3 = p;
                if (b != hCol && d != f) {
                    e0 = (d == b) ? d : p;
                    e1 = (b == f) ? f : p;
                    e2 = (d == hCol) ? d : p;
                    e3 = (hCol == f) ? f : p;
                }

                dst.setRGB(x * 2, y * 2, e0);
                dst.setRGB(x * 2 + 1, y * 2, e1);
                dst.setRGB(x * 2, y * 2 + 1, e2);
                dst.setRGB(x * 2 + 1, y * 2 + 1, e3);
            }
        }
        return dst;
    }

    /**
     * Finds the position of a level image on the minimap using the Sum of Absolute Differences (SAD) technique.
     * <p>
     * Time complexity: O(W_minimap × H_minimap × W_level × H_level).
     * <p>
     * @param levelImage The smaller image (level) to locate on the larger image (minimap).
     * @return The top-left position of the level image on the minimap if found.
     */
    public Point findImageOnMinimap(BufferedImage levelImage) {
        int levelWidth = levelImage.getWidth(), levelHeight = levelImage.getHeight();
        int minimapHeight = minimapGray.length, minimapWidth = minimapGray[0].length;

        int[][] levelGray = ImageUtils.toGrayscale(levelImage);

        // Thread-safe SAD Tracking
        AtomicInteger minSAD = new AtomicInteger(Integer.MAX_VALUE);
        AtomicReference<Point> bestMatch = new AtomicReference<>(null);

        // Parallelized Sliding Window
        IntStream.range(0, minimapHeight - levelHeight + 1).parallel().forEach(y -> {
            for (int x = 0; x <= minimapWidth - levelWidth; x++) {
                // Computing SAD for the current window
                int sad = computeSAD(minimapGray, levelGray, x, y, levelWidth, levelHeight, minSAD.get());
                if (sad < minSAD.get()) {
                    minSAD.set(sad);
                    bestMatch.set(new Point(x, y));
                }
            }
        });

        return bestMatch.get();
    }

    /**
     * Computes the Sum of Absolute Differences (SAD) for a specific window in the minimap.
     * SAD is a pixel-by-pixel comparison metric (measures the difference between two images by summing the absolute differences of their pixel values).
     *
     * @param minimapGray Grayscale representation of the minimap image.
     * @param levelGray Grayscale representation of the level image.
     * @param startX Top-left X coordinate of the current window in the minimap.
     * @param startY Top-left Y coordinate of the current window in the minimap.
     * @param levelWidth Width of the level image.
     * @param levelHeight Height of the level image.
     * @param currentMinSAD Current minimum SAD (for early stopping).
     * @return The SAD value for the current window.
     */
    private int computeSAD(int[][] minimapGray, int[][] levelGray, int startX, int startY, int levelWidth, int levelHeight, int currentMinSAD) {
        int sad = 0;
        for (int j = 0; j < levelHeight; j++) {
            for (int i = 0; i < levelWidth; i++) {
                int minimapPixel = minimapGray[startY + j][startX + i];
                int levelPixel = levelGray[j][i];
                // Computing the absolute difference
                int dx = Math.abs(minimapPixel - levelPixel);
                sad += dx;

                // Early stopping
                if (sad > currentMinSAD) return Integer.MAX_VALUE;
            }
        }
        return sad;
    }

    /**
     * Updates the flashing effect for the player's position.
     */
    public void update() {
        flashTick++;
        if (flashTick >= MINIMAP_FLASH_ANIM_SPEED) {
            flashTick = 0;
            flashVisible = !flashVisible;
        }
        updateFogImage();
    }

    /**
     * Updates the player's position on the minimap based on their world coordinates.
     * This ensures the player's icon is accurately placed on the minimap, reflecting their position within the level.
     *
     * @param playerWorldX The player's X coordinate in world pixels.
     * @param playerWorldY The player's Y coordinate in world pixels.
     */
    public void updatePlayerPosition(double playerWorldX, double playerWorldY) {
        LevelManager levelManager = context.getLevelManager();
        Level currentLevel = levelManager.getCurrentLevel();
        int levelWidthInPixels = currentLevel.getLevelTilesWidth() * TILES_SIZE;
        int levelHeightInPixels = currentLevel.getLevelTilesHeight() * TILES_SIZE;

        String levelId = levelManager.getCurrentLevelId();
        Point levelPosOnMinimap = getOrFindLevelPosition(levelId);
        if (levelPosOnMinimap == null) return;

        int levelWidthOnMinimap = currentLevel.getLevelTilesWidth();
        int levelHeightOnMinimap = currentLevel.getLevelTilesHeight();

        double relativeX = playerWorldX / levelWidthInPixels;
        double relativeY = playerWorldY / levelHeightInPixels;

        double playerMapX = levelPosOnMinimap.x + (relativeX * levelWidthOnMinimap);
        double playerMapY = levelPosOnMinimap.y + (relativeY * levelHeightOnMinimap);

        if (playerLocation == null) playerLocation = new Point2D.Double(playerMapX, playerMapY);
        else playerLocation.setLocation(playerMapX, playerMapY);

        revealMapArea((int) playerLocation.x, (int) playerLocation.y);
        updatePinnedLocation();
    }

    private void revealMapArea(int centerX, int centerY) {
        int startX = Math.max(0, centerX - MAP_FOG_RADIUS);
        int endX = Math.min(explorationMap.length - 1, centerX + MAP_FOG_RADIUS);
        int startY = Math.max(0, centerY - MAP_FOG_RADIUS);
        int endY = Math.min(explorationMap[0].length - 1, centerY + MAP_FOG_RADIUS);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (explorationMap[x][y] < 1.0f && Point2D.distance(centerX, centerY, x, y) <= MAP_FOG_RADIUS) {
                    explorationMap[x][y] = 1.0f;
                    if (x < dirtyMinX) dirtyMinX = x;
                    if (x > dirtyMaxX) dirtyMaxX = x;
                    if (y < dirtyMinY) dirtyMinY = y;
                    if (y > dirtyMaxY) dirtyMaxY = y;
                    fogDirty = true;
                }
            }
        }
    }

    private void updatePinnedLocation() {
        if (pinnedLocation == null) return;
        Point playerIntLocation = getPlayerIntegerLocation();
        if (playerIntLocation != null && playerIntLocation.distance(pinnedLocation) <= 2.0) {
            unpin();
            return;
        }
        if (playerIntLocation != null && !playerIntLocation.equals(lastPathStart)) {
            lastPathStart = new Point(playerIntLocation);
            findPath();
        }
    }

    private void updateMinimap() {
        Graphics2D g2d = minimap.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.drawImage(minimapImage, 0, 0, null);
        g2d.dispose();
    }

    /**
     * Performs a fast separable 2-pass box blur strictly over the newly revealed dirty region, avoiding full-map ConvolveOp stalls.
     */
    public void updateFogImage() {
        if (!fogDirty || dirtyMaxX < dirtyMinX || dirtyMaxY < dirtyMinY) return;
        fogDirty = false;

        int width = minimapImage.getWidth();
        int height = minimapImage.getHeight();
        int r = Math.max(1, BLUR_KERNEL_SIZE / 2);

        int rx0 = Math.max(0, dirtyMinX - r);
        int ry0 = Math.max(0, dirtyMinY - r);
        int rx1 = Math.min(width - 1, dirtyMaxX + r);
        int ry1 = Math.min(height - 1, dirtyMaxY + r);

        int roiW = rx1 - rx0 + 1;
        int hy0 = Math.max(0, ry0 - r);
        int hy1 = Math.min(height - 1, ry1 + r);
        int hRows = hy1 - hy0 + 1;

        float[] horiz = new float[roiW * hRows];
        float invWindow = 1.0f / (2 * r + 1);

        // Pass 1: Horizontal box blur with edge clamping
        for (int y = hy0; y <= hy1; y++) {
            int rowBase = (y - hy0) * roiW;
            for (int x = rx0; x <= rx1; x++) {
                float sum = 0f;
                for (int dx = -r; dx <= r; dx++) {
                    int sx = Math.max(0, Math.min(width - 1, x + dx));
                    sum += (explorationMap[sx][y] < 1.0f) ? 255f : 0f;
                }
                horiz[rowBase + (x - rx0)] = sum * invWindow;
            }
        }

        // Pass 2: Vertical box blur writing directly into fogImage
        int fogRgb = MAP_FOG_COLOR.getRGB() & 0x00FFFFFF;
        for (int y = ry0; y <= ry1; y++) {
            for (int x = rx0; x <= rx1; x++) {
                float sum = 0f;
                int col = x - rx0;
                for (int dy = -r; dy <= r; dy++) {
                    int sy = Math.max(hy0, Math.min(hy1, y + dy));
                    sum += horiz[(sy - hy0) * roiW + col];
                }
                int alpha = Math.max(0, Math.min(255, Math.round(sum * invWindow)));
                fogImage.setRGB(x, y, (alpha << 24) | fogRgb);
            }
        }

        dirtyMinX = Integer.MAX_VALUE;
        dirtyMinY = Integer.MAX_VALUE;
        dirtyMaxX = -1;
        dirtyMaxY = -1;
    }

    public void loadExplorationData(String base64) {
        unpin();
        int width = minimapImage.getWidth();
        int height = minimapImage.getHeight();
        this.explorationMap = new float[width][height];
        if (base64 == null || base64.isEmpty()) {
            markFullFogDirty();
            updateFogImage();
            return;
        }
        try {
            int savedW = width;
            String payload = base64;
            if (base64.contains(":")) {
                String[] parts = base64.split(":", 2);
                String[] dims = parts[0].split(",");
                savedW = Integer.parseInt(dims[0]);
                payload = parts[1];
            }

            byte[] data = Base64.getDecoder().decode(payload);
            if (!base64.contains(":") && data.length != width * height && height > 0 && data.length % height == 0) {
                savedW = data.length / height;
            }
            for (int i = 0; i < data.length; i++) {
                int x = i % savedW;
                int y = i / savedW;
                if (x < width && y < height) {
                    this.explorationMap[x][y] = (data[i] & 0xFF) / 255.0f;
                }
            }
        } catch (Exception e) {
            this.explorationMap = new float[width][height];
            Logger.getInstance().notify("Could not load minimap exploration data. Resetting fog.", Message.WARNING);
        }
        markFullFogDirty();
        updateFogImage();
    }

    public String getExplorationDataForSave() {
        int width = explorationMap.length;
        int height = explorationMap[0].length;
        byte[] data = new byte[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y * width + x] = (byte) (explorationMap[x][y] * 255);
            }
        }
        return width + "," + height + ":" + Base64.getEncoder().encodeToString(data);
    }

    /**
     * Returns quest markers (with quest name and objective text) for all active quests whose current objective has a valid overworld room coordinate.
     */
    public List<QuestMinimapMarker> getActiveQuestMarkers() {
        List<QuestMinimapMarker> markers = new ArrayList<>();
        if (context == null || context.getQuestManager() == null) return markers;

        for (QuestSlot slot : context.getQuestManager().getSlots()) {
            Quest quest = slot.getQuest();
            if (quest == null || quest.isCompleted() || quest.getObjectives() == null) continue;
            int idx = quest.getActiveObjectiveIndex();
            if (idx >= 0 && idx < quest.getObjectives().size()) {
                QuestObjective obj = quest.getObjectives().get(idx);
                String targetRoomId = (obj.getMapLevelId() != null && !obj.getMapLevelId().isBlank()) ? obj.getMapLevelId() : obj.getLevelId();
                if (targetRoomId == null || targetRoomId.isBlank()) continue;
                Point roomPos = context.getLevelManager().getConfiguredMinimapPosition(targetRoomId);
                if (roomPos != null) {
                    Level lvl = context.getLevelManager().getLevel(targetRoomId);
                    int defaultX = (lvl != null) ? lvl.getLevelTilesWidth() / 2 : 20;
                    int defaultY = (lvl != null) ? lvl.getLevelTilesHeight() / 2 : 12;
                    int tx = obj.getTargetX() != null ? obj.getTargetX() : defaultX;
                    int ty = obj.getTargetY() != null ? obj.getTargetY() : defaultY;
                    markers.add(new QuestMinimapMarker(new Point(roomPos.x + tx, roomPos.y + ty), quest.getName(), obj.getDescription()));
                }
            }
        }
        return markers;
    }

    public List<Point> getActiveQuestMinimapTargets() {
        List<Point> points = new ArrayList<>();
        for (QuestMinimapMarker m : getActiveQuestMarkers()) {
            points.add(m.position());
        }
        return points;
    }

    public void changeLevel() {
        this.playerLocation = null;
    }

    // Path
    public void handlePinRequest(Point location, MinimapIcon icon) {
        if (icon != null) {
            if (icon.equals(pinnedIcon)) unpin();
            else {
                pinnedIcon = icon;
                pinnedLocation = location;
                findPath();
                Logger.getInstance().notify("Minimap icon pinned: " + icon.type(), Message.NOTIFICATION);
            }
        }
        else {
            if (pinnedLocation != null && location.distance(pinnedLocation) <= 2.0) unpin();
            else {
                pinnedIcon = null;
                pinnedLocation = location;
                findPath();
                Logger.getInstance().notify("Minimap location pinned at: (" + location.x + ", " + location.y + ")", Message.NOTIFICATION);
            }
        }
        lastPathStart = null;
    }

    public void unpin() {
        pinnedIcon = null;
        pinnedLocation = null;
        lastPathStart = null;
        pathPoints.clear();
    }

    public void findPath() {
        Point playerIntLocation = getPlayerIntegerLocation();
        if (playerIntLocation == null || pinnedLocation == null) {
            pathPoints.clear();
            return;
        }
        pathPoints = pathfinding.findPath(minimapImage, playerIntLocation, pinnedLocation);
    }

    public boolean isExplored(Point point) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int nx = point.x + dx;
                int ny = point.y + dy;
                if (nx >= 0 && nx < explorationMap.length && ny >= 0 && ny < explorationMap[0].length) {
                    if (explorationMap[nx][ny] >= 0.5f) return true;
                }
            }
        }
        return false;
    }

    // Hide
    /**
     * Determines whether a minimap icon should currently be displayed based on live world, boss, and follower state.
     */
    public boolean isIconVisible(MinimapIcon icon) {
        if (!isExplored(icon.position())) return false;
        if (context == null) return true;
        String roomId = iconRoomCache.computeIfAbsent(icon.position(), this::findOwningRoomId);
        if (roomId == null) return true;

        LevelMetadata meta = context.getLevelManager().getLevelMetadata(roomId);
        if (meta == null || meta.getMinimapIcons() == null) return true;
        boolean bossFightActive = context.getGameState() != null && context.getGameState().getBossInterface() != null && context.getGameState().getBossInterface().isActive() && roomId.equals(context.getLevelManager().getCurrentLevelId());
        for (MinimapIconMetadata rule : meta.getMinimapIcons()) {
            if (rule.getType() == icon.type()) {
                if (!rule.isConditionMet(context.getWorldMemoryManager(), context.getQuestManager(), bossFightActive)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String findOwningRoomId(Point iconPos) {
        WorldMapMetadata worldMap = context.getLevelManager().getWorldMap();
        if (worldMap == null || worldMap.getRooms() == null) return null;

        String bestRoom = null;
        int bestDistSq = Integer.MAX_VALUE;
        for (var entry : worldMap.getRooms().entrySet()) {
            String levelId = entry.getKey();
            Point roomPos = getOrFindLevelPosition(levelId);
            if (roomPos == null) continue;
            Level lvl = context.getLevelManager().getLevel(levelId);
            int w = (lvl != null) ? lvl.getLevelTilesWidth() : 45, h = (lvl != null) ? lvl.getLevelTilesHeight() : 30;
            if (iconPos.x >= roomPos.x && iconPos.x < roomPos.x + w && iconPos.y >= roomPos.y && iconPos.y < roomPos.y + h) {
                int cx = roomPos.x + w / 2;
                int cy = roomPos.y + h / 2;
                int distSq = (iconPos.x - cx) * (iconPos.x - cx) + (iconPos.y - cy) * (iconPos.y - cy);
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    bestRoom = levelId;
                }
            }
        }
        return bestRoom;
    }

    public void reset() {
        unpin();
        playerLocation = null;
    }

    // Getters & Setters
    public BufferedImage getMinimap() {
        return minimap;
    }

    public BufferedImage getVisualMinimap() {
        return visualMinimap != null ? visualMinimap : minimap;
    }

    public Point2D.Double getPlayerLocation() {
        return playerLocation;
    }

    private Point getPlayerIntegerLocation() {
        if (playerLocation == null) return null;
        return new Point((int)Math.round(playerLocation.x), (int)Math.round(playerLocation.y));
    }

    public Point getPinnedLocation() {
        return pinnedLocation;
    }

    public List<MinimapIcon> getIcons() {
        return icons;
    }

    public BufferedImage[] getMinimapIcons() {
        return minimapIcons;
    }

    public boolean isFlashVisible() {
        return flashVisible;
    }

    public List<Point> getPathPoints() {
        return pathPoints;
    }

    public void setHoveredIcon(MinimapIcon icon) {
        this.hoveredIcon = icon;
    }

    public MinimapIcon getHoveredIcon() {
        return hoveredIcon;
    }

    public MinimapIcon getPinnedIcon() {
        return pinnedIcon;
    }

    public BufferedImage getFogImage() {
        return fogImage;
    }
}