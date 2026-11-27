package platformer.model.gameObjects.objects;

import platformer.debug.DebugSettings;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.physics.CollisionDetector;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static platformer.constants.Constants.*;

/**
 * Procedural molten magma basin supporting arbitrary connected polyomino shapes (stepped basins, ceiling overhangs, and multi-opening pools).
 * Renders behind foreground terrain with solid edge bleed so ragged mossy tile fringes sit cleanly in front of the magma with zero gaps.
 */
public class Lava extends GameObject implements Interactable {

    private static final double RIPPLE_TRAVEL_SPEED = 0.22 * SCALE;
    private static final double SPLASH_DEPTH = 7.0 * SCALE;
    private static final int SAMPLE_SPACING = Math.max(2, (int) (2.5 * SCALE));
    private static final double SOLID_BLEED_CORE = 8.5 * SCALE;
    private static final double EDGE_BLEED_OUT = 12.0 * SCALE;

    // Palette
    private static final Color MAGMA_UPPER_COLOR = new Color(240, 75, 12, 255);
    private static final Color MAGMA_MID_COLOR = new Color(150, 22, 5, 255);
    private static final Color MAGMA_DEEP_COLOR = new Color(36, 7, 5, 255);
    private static final Color CRUST_SLAG_COLOR = new Color(38, 10, 8, 235);

    private static class MagmaPulse {
        final double startX;
        final int dir;
        final double speed;
        double amplitude;
        final double decay;
        final double radius;
        double age = 0;

        MagmaPulse(double startX, int dir, double speed, double amplitude, double decay, double radius) {
            this.startX = startX;
            this.dir = dir;
            this.speed = speed;
            this.amplitude = amplitude;
            this.decay = decay;
            this.radius = radius;
        }
    }

    private static class SurfaceSegment {
        final int tileRow;
        final int startCol;
        final int endCol;
        final int localLeftX;
        final int localRightX;
        final int pixelWidth;
        final double worldSurfaceY;
        final boolean solidLeft;
        final boolean solidRight;
        final int numSamples;
        final double[] renderX;
        final double[] renderY;
        final double[] blisterX;
        final double[] blisterProgress;
        final double[] blisterSpeed;
        final int[] blisterMaxRadius;
        final Queue<MagmaPulse> pulses = new ConcurrentLinkedQueue<>();

        SurfaceSegment(int tileRow, int startCol, int endCol, int worldOriginY, int surfaceYOffset, boolean solidLeft, boolean solidRight, int padLeft, int padRight, Random rand) {
            this.tileRow = tileRow;
            this.startCol = startCol;
            this.endCol = endCol;
            this.localLeftX = startCol * TILES_SIZE;
            this.localRightX = (endCol + 1) * TILES_SIZE;
            this.pixelWidth = localRightX - localLeftX;
            this.worldSurfaceY = worldOriginY + tileRow * TILES_SIZE + surfaceYOffset;
            this.solidLeft = solidLeft;
            this.solidRight = solidRight;

            int renderWidth = pixelWidth + (solidLeft ? padLeft : 0) + (solidRight ? padRight : 0);
            this.numSamples = Math.max(16, (renderWidth / SAMPLE_SPACING) + 2);
            this.renderX = new double[numSamples];
            this.renderY = new double[numSamples];

            int tilesSpan = endCol - startCol + 1;
            int blisterCount = Math.max(2, tilesSpan * 2);
            this.blisterX = new double[blisterCount];
            this.blisterProgress = new double[blisterCount];
            this.blisterSpeed = new double[blisterCount];
            this.blisterMaxRadius = new int[blisterCount];
            for (int i = 0; i < blisterCount; i++) {
                resetBlister(i, true, rand);
            }
        }

        void resetBlister(int i, boolean randomPhase, Random rand) {
            double margin = Math.min(12 * SCALE, pixelWidth * 0.25);
            blisterX[i] = margin + rand.nextDouble() * Math.max(4, pixelWidth - margin * 2);
            blisterProgress[i] = randomPhase ? rand.nextDouble() : 0.0;
            blisterSpeed[i] = 0.005 + rand.nextDouble() * 0.006;
            blisterMaxRadius[i] = Math.max(3, (int) ((3.5 + rand.nextDouble() * 3.0) * SCALE));
        }
    }

    private final boolean[][] tileMask;
    private final int tilesWide, tilesHigh;
    private final int originTileX, originTileY;
    private final int poolWidth, poolHeight;
    private final int surfaceYOffset;
    private final int padLeft, padRight, padBottom, topHeadroom;
    private final int bufW, bufH;

    private final boolean[][] openSurfaceMask;
    private final List<SurfaceSegment> surfaceSegments = new ArrayList<>();
    private final List<Rectangle2D.Double> hitBoxStrips = new ArrayList<>();
    private final List<Point> lightAnchorPoints = new ArrayList<>();

    private double[] emberX = new double[0];
    private double[] emberY = new double[0];
    private double[] emberVy = new double[0];
    private double[] emberPhase = new double[0];
    private float[] emberAlpha = new float[0];
    private int[] emberSegmentIdx = new int[0];

    private BufferedImage poolBuffer;
    private BufferedImage staticMask;
    private boolean geometryBuilt = false;

    private double waveTime = 0.0;
    private boolean playerSubmerged = false;
    private double lastPlayerX = -1;
    private boolean wasPlayerInAir = false;
    private int wadeTick = 0;
    private int slashCooldown = 0;

    private EffectManager effectManager;
    private final Random rand = new Random();

    public Lava(int xPos, int yPos, boolean[][] tileMask, int[][] levelData) {
        super(ObjType.LAVA, xPos, yPos);
        this.tileMask = tileMask;
        this.tilesWide = tileMask.length;
        this.tilesHigh = tileMask[0].length;
        this.originTileX = xPos / TILES_SIZE;
        this.originTileY = yPos / TILES_SIZE;
        this.poolWidth = tilesWide * TILES_SIZE;
        this.poolHeight = tilesHigh * TILES_SIZE;
        this.surfaceYOffset = (int) (9 * SCALE);

        this.padLeft = (int) Math.ceil(EDGE_BLEED_OUT);
        this.padRight = (int) Math.ceil(EDGE_BLEED_OUT);
        this.padBottom = (int) Math.ceil(EDGE_BLEED_OUT);
        this.topHeadroom = (int) (16 * SCALE);
        this.bufW = poolWidth + padLeft + padRight;
        this.bufH = poolHeight + topHeadroom + padBottom;
        this.openSurfaceMask = new boolean[tilesWide][tilesHigh];

        initHitBox(poolWidth, poolHeight);
        this.hitBox.x = xPos;
        this.hitBox.y = yPos;

        if (levelData != null) {
            buildGeometry(levelData);
        }
    }

    private void buildGeometry(int[][] levelData) {
        geometryBuilt = true;
        surfaceSegments.clear();
        hitBoxStrips.clear();
        lightAnchorPoints.clear();

        for (int ly = 0; ly < tilesHigh; ly++) {
            for (int lx = 0; lx < tilesWide; lx++) {
                if (!tileMask[lx][ly]) continue;
                boolean lavaAbove = (ly > 0 && tileMask[lx][ly - 1]);
                boolean solidAbove = isSolidWorldTile(originTileX + lx, originTileY + ly - 1, levelData);
                openSurfaceMask[lx][ly] = !lavaAbove && !solidAbove;
            }
        }

        for (int ly = 0; ly < tilesHigh; ly++) {
            int lx = 0;
            while (lx < tilesWide) {
                if (!openSurfaceMask[lx][ly]) {
                    lx++;
                    continue;
                }
                int startCol = lx;
                while (lx + 1 < tilesWide && openSurfaceMask[lx + 1][ly]) {
                    lx++;
                }
                int endCol = lx;
                boolean solidLeft = isSolidWorldTile(originTileX + startCol - 1, originTileY + ly, levelData);
                boolean solidRight = isSolidWorldTile(originTileX + endCol + 1, originTileY + ly, levelData);
                surfaceSegments.add(new SurfaceSegment(ly, startCol, endCol, yPos, surfaceYOffset, solidLeft, solidRight, padLeft, padRight, rand));
                lx++;
            }
        }

        buildHitBoxStrips();
        buildLightAnchors();
        initEmbers();
        staticMask = buildStaticMask(levelData);
    }

    private void buildHitBoxStrips() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (int ly = 0; ly < tilesHigh; ly++) {
            int lx = 0;
            while (lx < tilesWide) {
                if (!tileMask[lx][ly]) {
                    lx++;
                    continue;
                }
                boolean isOpen = openSurfaceMask[lx][ly];
                int startCol = lx;
                while (lx + 1 < tilesWide && tileMask[lx + 1][ly] && openSurfaceMask[lx + 1][ly] == isOpen) {
                    lx++;
                }
                int endCol = lx;
                double sx = xPos + startCol * TILES_SIZE;
                double sw = (endCol - startCol + 1) * TILES_SIZE;
                double sy = yPos + ly * TILES_SIZE + (isOpen ? surfaceYOffset : 0);
                double sh = TILES_SIZE - (isOpen ? surfaceYOffset : 0);
                hitBoxStrips.add(new Rectangle2D.Double(sx, sy, sw, sh));

                if (sx < minX) minX = sx;
                if (sy < minY) minY = sy;
                if (sx + sw > maxX) maxX = sx + sw;
                if (sy + sh > maxY) maxY = sy + sh;
                lx++;
            }
        }

        if (minX < maxX && minY < maxY) {
            hitBox.setRect(minX, minY, maxX - minX, maxY - minY);
        }
    }

    private void buildLightAnchors() {
        for (int ly = 0; ly < tilesHigh; ly += 2) {
            addRowLightAnchors(ly);
        }
        if (tilesHigh > 1 && (tilesHigh - 1) % 2 == 1) {
            addRowLightAnchors(tilesHigh - 1);
        }
    }

    private void addRowLightAnchors(int ly) {
        int lx = 0;
        while (lx < tilesWide) {
            if (!tileMask[lx][ly]) {
                lx++;
                continue;
            }
            int startCol = lx;
            while (lx + 1 < tilesWide && tileMask[lx + 1][ly]) {
                lx++;
            }
            int endCol = lx;
            int cy = yPos + ly * TILES_SIZE + (openSurfaceMask[startCol][ly] ? (surfaceYOffset + TILES_SIZE / 4) : (TILES_SIZE / 2));
            for (int col = startCol; col <= endCol; col += 2) {
                lightAnchorPoints.add(new Point(xPos + col * TILES_SIZE + TILES_SIZE / 2, cy));
            }
            if ((endCol - startCol) % 2 == 1) {
                lightAnchorPoints.add(new Point(xPos + endCol * TILES_SIZE + TILES_SIZE / 2, cy));
            }
            lx++;
        }
    }

    private void initEmbers() {
        int totalSurfaceTiles = 0;
        for (SurfaceSegment seg : surfaceSegments) {
            totalSurfaceTiles += (seg.endCol - seg.startCol + 1);
        }
        int emberCount = (totalSurfaceTiles == 0) ? 0 : Math.max(5, totalSurfaceTiles * 3);
        this.emberX = new double[emberCount];
        this.emberY = new double[emberCount];
        this.emberVy = new double[emberCount];
        this.emberPhase = new double[emberCount];
        this.emberAlpha = new float[emberCount];
        this.emberSegmentIdx = new int[emberCount];
        for (int i = 0; i < emberCount; i++) {
            resetEmber(i, true);
        }
    }

    private void resetEmber(int i, boolean randomY) {
        if (surfaceSegments.isEmpty()) return;
        int segIdx = rand.nextInt(surfaceSegments.size());
        SurfaceSegment seg = surfaceSegments.get(segIdx);
        emberSegmentIdx[i] = segIdx;
        double margin = Math.min(8 * SCALE, seg.pixelWidth * 0.25);
        emberX[i] = xPos + seg.localLeftX + margin + rand.nextDouble() * Math.max(4, seg.pixelWidth - margin * 2);
        emberY[i] = seg.worldSurfaceY - (randomY ? rand.nextDouble() * 22 * SCALE : 0);
        emberVy[i] = (0.10 + rand.nextDouble() * 0.16) * SCALE;
        emberPhase[i] = rand.nextDouble() * Math.PI * 2;
        emberAlpha[i] = 0.70f + rand.nextFloat() * 0.30f;
    }

    /**
     * Pre-bakes the static polyomino alpha mask with full-opacity bleed under bordering mossy tile fringes and a soft fade inside the solid earth interior.
     */
    private BufferedImage buildStaticMask(int[][] levelData) {
        BufferedImage mask = ImageUtils.createCompatibleImage(bufW, bufH);
        int[] pixels = new int[bufW * bufH];
        double coreSq = SOLID_BLEED_CORE * SOLID_BLEED_CORE;
        double bleedOutSq = EDGE_BLEED_OUT * EDGE_BLEED_OUT;
        double fadeSpan = Math.max(1.0, EDGE_BLEED_OUT - SOLID_BLEED_CORE);

        for (int cy = -1; cy <= tilesHigh; cy++) {
            int py0 = Math.max(0, topHeadroom + cy * TILES_SIZE);
            int py1 = Math.min(bufH, topHeadroom + (cy + 1) * TILES_SIZE);
            if (py0 >= py1) continue;

            for (int cx = -1; cx <= tilesWide; cx++) {
                int px0 = Math.max(0, padLeft + cx * TILES_SIZE);
                int px1 = Math.min(bufW, padLeft + (cx + 1) * TILES_SIZE);
                if (px0 >= px1) continue;

                boolean cellIsLava = isLavaCell(cx, cy);
                boolean cellIsSolid = !cellIsLava && isSolidWorldTile(originTileX + cx, originTileY + cy, levelData);
                boolean cellIsHeadroom = !cellIsLava && !cellIsSolid && isOpenSurfaceCell(cx, cy + 1);

                if (cellIsLava) {
                    fillOpaqueRect(pixels, px0, py0, px1, py1);
                }
                else if (cellIsSolid) {
                    if (!hasAdjacentLava(cx, cy)) continue;
                    for (int py = py0; py < py1; py++) {
                        int rowOffset = py * bufW;
                        double gy = (py - topHeadroom) + 0.5;
                        for (int px = px0; px < px1; px++) {
                            double gx = (px - padLeft) + 0.5;
                            double minLavaSq = minDistSqToLavaNeighbors(cx, cy, gx, gy);
                            if (minLavaSq <= coreSq) {
                                pixels[rowOffset + px] = 0xFFFFFFFF;
                            }
                            else if (minLavaSq < bleedOutSq) {
                                double dLava = Math.sqrt(minLavaSq);
                                double t = Math.max(0.0, Math.min(1.0, (EDGE_BLEED_OUT - dLava) / fadeSpan));
                                double s = t * t * (3.0 - 2.0 * t);
                                int alpha = (int) Math.round(s * 255.0);
                                if (alpha > 0) {
                                    pixels[rowOffset + px] = (alpha << 24) | 0x00FFFFFF;
                                }
                            }
                        }
                    }
                }
                else if (cellIsHeadroom) {
                    int headPy0 = Math.max(py0, py1 - topHeadroom);
                    fillOpaqueRect(pixels, px0, headPy0, px1, py1);
                }
            }
        }

        mask.setRGB(0, 0, bufW, bufH, pixels, 0, bufW);
        return mask;
    }

    private void fillOpaqueRect(int[] pixels, int px0, int py0, int px1, int py1) {
        for (int py = py0; py < py1; py++) {
            Arrays.fill(pixels, py * bufW + px0, py * bufW + px1, 0xFFFFFFFF);
        }
    }

    private boolean isLavaCell(int cx, int cy) {
        return cx >= 0 && cx < tilesWide && cy >= 0 && cy < tilesHigh && tileMask[cx][cy];
    }

    private boolean isOpenSurfaceCell(int cx, int cy) {
        return cx >= 0 && cx < tilesWide && cy >= 0 && cy < tilesHigh && openSurfaceMask[cx][cy];
    }

    private boolean isSolidWorldTile(int wx, int wy, int[][] levelData) {
        if (levelData == null || wx < 0 || wx >= levelData.length || wy < 0 || wy >= levelData[0].length) {
            return false;
        }
        return CollisionDetector.isTileSolid(wx, wy, levelData);
    }

    private boolean hasAdjacentLava(int cx, int cy) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = cx + dx, ny = cy + dy;
                if (!isLavaCell(nx, ny)) continue;
                if (isOpenSurfaceCell(nx, ny) && cy < ny) continue;
                return true;
            }
        }
        return false;
    }

    private double minDistSqToLavaNeighbors(int cx, int cy, double gx, double gy) {
        double minSq = Double.MAX_VALUE;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = cx + dx, ny = cy + dy;
                if (!isLavaCell(nx, ny)) continue;
                if (isOpenSurfaceCell(nx, ny) && cy < ny) continue;

                double ry0 = ny * TILES_SIZE + (isOpenSurfaceCell(nx, ny) ? surfaceYOffset : 0);
                double dSq = distSqToRect(gx, gy, nx * TILES_SIZE, ry0, (nx + 1) * TILES_SIZE, (ny + 1) * TILES_SIZE);
                if (dSq < minSq) minSq = dSq;
            }
        }
        return minSq;
    }

    private double distSqToRect(double px, double py, double rx0, double ry0, double rx1, double ry1) {
        double dx = (px < rx0) ? (rx0 - px) : (px > rx1 ? (px - rx1) : 0.0);
        double dy = (py < ry0) ? (ry0 - py) : (py > ry1 ? (py - ry1) : 0.0);
        return dx * dx + dy * dy;
    }

    public void setEffectManager(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    @Override
    public boolean intersects(Rectangle2D.Double area) {
        if (area == null || !hitBox.intersects(area)) return false;
        if (hitBoxStrips.isEmpty()) return true;
        for (Rectangle2D.Double strip : hitBoxStrips) {
            if (strip.intersects(area)) return true;
        }
        return false;
    }

    public List<Point> getLightAnchorPoints() {
        return lightAnchorPoints;
    }

    @Override
    public void update() {
        if (slashCooldown > 0) slashCooldown--;
        waveTime += 0.010;

        for (SurfaceSegment seg : surfaceSegments) {
            Iterator<MagmaPulse> it = seg.pulses.iterator();
            while (it.hasNext()) {
                MagmaPulse p = it.next();
                p.age += 1.0;
                p.amplitude *= p.decay;
                double currentX = p.startX + p.dir * p.speed * p.age;
                if (currentX < -p.radius || currentX > seg.pixelWidth + p.radius || Math.abs(p.amplitude) < 0.12 * SCALE) {
                    it.remove();
                }
            }

            for (int i = 0; i < seg.blisterX.length; i++) {
                seg.blisterProgress[i] += seg.blisterSpeed[i];
                if (seg.blisterProgress[i] >= 1.0) {
                    if (seg.pulses.size() < 12) {
                        seg.pulses.add(new MagmaPulse(seg.blisterX[i], 0, 0.0, 1.8 * SCALE, 0.95, 9 * SCALE));
                    }
                    seg.resetBlister(i, false, rand);
                }
            }
        }

        for (int i = 0; i < emberX.length; i++) {
            emberY[i] -= emberVy[i];
            emberPhase[i] += 0.04;
            emberX[i] += Math.sin(emberPhase[i]) * (0.10 * SCALE);
            emberAlpha[i] -= 0.0055f;
            SurfaceSegment seg = surfaceSegments.get(emberSegmentIdx[i]);
            if (emberAlpha[i] <= 0.05f || emberY[i] < seg.worldSurfaceY - 28 * SCALE) {
                resetEmber(i, false);
            }
        }
    }

    @Override
    public void update(int[][] levelData) {
        if (!geometryBuilt && levelData != null) {
            buildGeometry(levelData);
        }
        update();
    }

    private SurfaceSegment findNearestSurface(double worldX, double worldY) {
        SurfaceSegment best = null;
        double bestDist = Double.MAX_VALUE;
        for (SurfaceSegment seg : surfaceSegments) {
            double segLeftWorld = xPos + seg.localLeftX;
            double segRightWorld = xPos + seg.localRightX;
            if (worldX < segLeftWorld - TILES_SIZE || worldX > segRightWorld + TILES_SIZE) continue;
            double dy = Math.abs(worldY - seg.worldSurfaceY);
            if (dy < TILES_SIZE * 2.0 && dy < bestDist) {
                bestDist = dy;
                best = seg;
            }
        }
        return best;
    }

    public void updatePlayerPhysics(Player player) {
        if (player == null) return;
        Rectangle2D.Double pBox = player.getHitBox();
        boolean intersectingNow = intersects(pBox);

        if (intersectingNow && !playerSubmerged) {
            playerSubmerged = true;
            double px = pBox.getCenterX();
            plunge(px, pBox.getCenterY(), 1.15, true);
            lastPlayerX = px;
            wasPlayerInAir = player.isInAir();
        }
        else if (intersectingNow) {
            double px = pBox.getCenterX();
            boolean inAirNow = player.isInAir();

            if (!wasPlayerInAir && inAirNow && player.getAirSpeed() < -0.5) {
                plunge(px, pBox.getCenterY(), 0.85, true);
            }
            else if (wasPlayerInAir && !inAirNow) {
                plunge(px, pBox.getCenterY(), 1.20, true);
            }

            double dx = (lastPlayerX != -1) ? (px - lastPlayerX) : 0;
            if (Math.abs(dx) > 0.2) {
                int dir = dx > 0 ? 1 : -1;
                wadeTick++;
                if (wadeTick >= 18) {
                    wadeTick = 0;
                    SurfaceSegment seg = findNearestSurface(px, pBox.getCenterY());
                    if (seg != null) {
                        double localX = Math.max(6 * SCALE, Math.min(seg.pixelWidth - 6 * SCALE, px - (xPos + seg.localLeftX)));
                        seg.pulses.add(new MagmaPulse(localX + dir * (6 * SCALE), dir, RIPPLE_TRAVEL_SPEED, -(3.5 * SCALE), 0.982, 15 * SCALE));
                        if (effectManager != null) {
                            effectManager.spawnDustParticles(px, seg.worldSurfaceY, 4, DustType.PLAYER_BURN, -dir, player);
                        }
                    }
                }
            }
            lastPlayerX = px;
            wasPlayerInAir = inAirNow;
        }
        else if (playerSubmerged) {
            playerSubmerged = false;
            plunge(pBox.getCenterX(), pBox.getCenterY(), 0.80, true);
            lastPlayerX = -1;
            wasPlayerInAir = false;
        }
    }

    public void plunge(double worldX, double worldY, double intensity, boolean withSparks) {
        SurfaceSegment seg = findNearestSurface(worldX, worldY);
        if (seg == null) return;

        double segLeftWorld = xPos + seg.localLeftX;
        double localX = Math.max(8 * SCALE, Math.min(seg.pixelWidth - 8 * SCALE, worldX - segLeftWorld));
        double depth = SPLASH_DEPTH * intensity;

        double craterRadius = PLAYER_HB_WID * 1.45;
        seg.pulses.add(new MagmaPulse(localX, 0, 0.0, depth, 0.972, craterRadius));

        double startOffset = PLAYER_HB_WID * 0.45;
        double waveRadius = 18.0 * SCALE;
        double waveHeight = -depth * 0.65;
        seg.pulses.add(new MagmaPulse(localX - startOffset, -1, RIPPLE_TRAVEL_SPEED, waveHeight, 0.985, waveRadius));
        seg.pulses.add(new MagmaPulse(localX + startOffset, 1, RIPPLE_TRAVEL_SPEED, waveHeight, 0.985, waveRadius));

        if (withSparks && effectManager != null) {
            effectManager.spawnDustParticles(worldX, seg.worldSurfaceY, 10, DustType.IMPACT_SPARK, 0, null);
            effectManager.spawnDustParticles(worldX, seg.worldSurfaceY, 8, DustType.PLAYER_BURN, 0, null);
        }
    }

    public void onAttackSlash(Rectangle2D.Double attackBox, int flipSign) {
        if (slashCooldown > 0) return;
        SurfaceSegment seg = findNearestSurface(attackBox.getCenterX(), attackBox.getCenterY());
        if (seg == null) return;
        if (attackBox.y > seg.worldSurfaceY + 24 * SCALE) return;

        slashCooldown = 16;
        double segLeftWorld = xPos + seg.localLeftX;
        double segRightWorld = xPos + seg.localRightX;
        double slashX = Math.max(segLeftWorld + 8, Math.min(segRightWorld - 8, attackBox.getCenterX()));
        plunge(slashX, seg.worldSurfaceY, 0.90, true);
    }

    private double evaluateSurfaceDisplacement(SurfaceSegment seg, double localX) {
        double totalY = 0.0;
        for (MagmaPulse p : seg.pulses) {
            double center = p.startX + p.dir * p.speed * p.age;
            double dist = Math.abs(localX - center);
            if (dist >= p.radius) continue;

            double norm = dist / p.radius;
            double cosVal = Math.cos(norm * (Math.PI / 2.0));
            double shape = cosVal * cosVal;

            if (p.dir == 0) {
                double rebound = Math.cos(p.age * 0.045);
                totalY += p.amplitude * shape * rebound;
            }
            else {
                double fadeIn = Math.min(1.0, p.age / 10.0);
                totalY += p.amplitude * shape * fadeIn;
            }
        }
        double maxDisp = Math.min(TILES_SIZE * 0.60, 14.0 * SCALE);
        return Math.max(-maxDisp, Math.min(maxDisp, totalY));
    }

    private double getIdleMagmaWaveOffset(int i, int numSamples) {
        double xNorm = (double) i / (numSamples - 1);
        double wallTaper = Math.sin(xNorm * Math.PI);
        double w1 = Math.sin(waveTime * 0.55 - xNorm * Math.PI * 2.0) * (0.75 * SCALE);
        double w2 = Math.cos(waveTime * 0.35 + xNorm * Math.PI * 3.0) * (0.45 * SCALE);
        return (w1 + w2) * (0.20 + 0.80 * wallTaper);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int screenX = xPos - padLeft - xLevelOffset;
        int screenY = yPos - topHeadroom - yLevelOffset;

        if (screenX + bufW < -64 || screenX > GAME_WIDTH + 64 || screenY + bufH < -64 || screenY > GAME_HEIGHT + 64) {
            return;
        }

        if (poolBuffer == null || poolBuffer.getWidth() != bufW || poolBuffer.getHeight() != bufH) {
            poolBuffer = ImageUtils.createCompatibleImage(bufW, bufH);
        }

        double minSurfaceY = topHeadroom + surfaceYOffset;
        if (!surfaceSegments.isEmpty()) {
            minSurfaceY = bufH;
            for (SurfaceSegment seg : surfaceSegments) {
                double segLeftBleed = seg.solidLeft ? padLeft : 0;
                double segRightBleed = seg.solidRight ? padRight : 0;
                double startBufX = padLeft + seg.localLeftX - segLeftBleed;
                double renderW = seg.pixelWidth + segLeftBleed + segRightBleed;
                double stepX = renderW / (seg.numSamples - 1);
                double baseSurfaceY = topHeadroom + seg.tileRow * TILES_SIZE + surfaceYOffset;
                double maxRowBottomY = topHeadroom + (seg.tileRow + 1) * TILES_SIZE - 3 * SCALE;

                for (int i = 0; i < seg.numSamples; i++) {
                    seg.renderX[i] = startBufX + i * stepX;
                    double localX = (i * stepX) - segLeftBleed;
                    double wallEdgeTaper = Math.min(1.0, Math.min(i, (seg.numSamples - 1) - i) / 3.0);
                    double waveDisp = evaluateSurfaceDisplacement(seg, localX) * (0.20 + 0.80 * wallEdgeTaper);
                    double targetY = baseSurfaceY + getIdleMagmaWaveOffset(i, seg.numSamples) + waveDisp;
                    seg.renderY[i] = Math.min(maxRowBottomY, targetY);
                    if (seg.renderY[i] < minSurfaceY) minSurfaceY = seg.renderY[i];
                }
            }
        }

        Graphics2D bG2d = poolBuffer.createGraphics();
        bG2d.setComposite(AlphaComposite.Clear);
        bG2d.fillRect(0, 0, bufW, bufH);
        bG2d.setComposite(AlphaComposite.SrcOver);
        bG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bG2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        LinearGradientPaint bodyPaint = new LinearGradientPaint(0f, (float) minSurfaceY, 0f, (float) bufH, new float[]{0.0f, 0.45f, 1.0f}, new Color[]{MAGMA_UPPER_COLOR, MAGMA_MID_COLOR, MAGMA_DEEP_COLOR});
        bG2d.setPaint(bodyPaint);
        bG2d.fillRect(0, 0, bufW, bufH);

        renderDeepMagmaCurrents(bG2d, minSurfaceY);

        for (SurfaceSegment seg : surfaceSegments) {
            Path2D.Double surfaceCurve = buildSmoothCurve(seg.renderX, seg.renderY, seg.numSamples);
            renderSurfaceVeins(bG2d, seg);

            bG2d.setColor(new Color(255, 145, 15, 115));
            bG2d.setStroke(new BasicStroke(7.0f * SCALE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            bG2d.draw(surfaceCurve);

            bG2d.setColor(new Color(255, 220, 65, 175));
            bG2d.setStroke(new BasicStroke(3.2f * SCALE, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            bG2d.draw(surfaceCurve);

            renderSurfaceBlisters(bG2d, seg);
            renderDriftingCrustPlates(bG2d, seg);
            clearAirAboveSurface(bG2d, seg, surfaceCurve);
        }

        if (staticMask != null) {
            bG2d.setComposite(AlphaComposite.DstIn);
            bG2d.drawImage(staticMask, 0, 0, null);
        }
        bG2d.dispose();

        g.drawImage(poolBuffer, screenX, screenY, null);

        if (emberX.length > 0) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < emberX.length; i++) {
                int ex = (int) (emberX[i] - xLevelOffset);
                int ey = (int) (emberY[i] - yLevelOffset);
                int alpha = Math.max(0, Math.min(255, (int) (emberAlpha[i] * 240)));
                int es = Math.max(2, (int) (1.8 * SCALE));
                g2d.setColor(new Color(255, 180, 40, alpha));
                g2d.fillOval(ex, ey, es, es);
            }
            g2d.dispose();
        }

        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    /**
     * Erases everything strictly above an open-air surface wave curve without touching adjacent elevated lava columns.
     */
    private void clearAirAboveSurface(Graphics2D bG2d, SurfaceSegment seg, Path2D.Double surfaceCurve) {
        double segLeftBufX = padLeft + seg.localLeftX;
        double segRightBufX = padLeft + seg.localRightX;
        double segTileTopY = topHeadroom + seg.tileRow * TILES_SIZE;
        double clearTopY = Math.max(0.0, segTileTopY - topHeadroom);

        Path2D.Double cutout = new Path2D.Double(surfaceCurve);
        if (seg.solidRight) {
            cutout.lineTo(segRightBufX + padRight, segTileTopY);
            cutout.lineTo(segRightBufX, segTileTopY);
        }
        cutout.lineTo(segRightBufX, clearTopY);
        cutout.lineTo(segLeftBufX, clearTopY);
        if (seg.solidLeft) {
            cutout.lineTo(segLeftBufX, segTileTopY);
            cutout.lineTo(segLeftBufX - padLeft, segTileTopY);
        }
        cutout.closePath();

        Composite oldComp = bG2d.getComposite();
        bG2d.setComposite(AlphaComposite.Clear);
        bG2d.fill(cutout);
        bG2d.setComposite(oldComp);
    }

    private void renderDeepMagmaCurrents(Graphics2D g2d, double minSurfaceY) {
        if (tilesHigh < 2) return;
        double spacing = 22.0 * SCALE;
        double startY = minSurfaceY + 26.0 * SCALE;
        int samples = Math.max(12, bufW / (int) (10 * SCALE));
        double stepX = (double) bufW / (samples - 1);

        int bandIdx = 0;
        for (double baseY = startY; baseY < bufH - 10 * SCALE; baseY += spacing, bandIdx++) {
            double depthNorm = Math.min(1.0, (baseY - minSurfaceY) / Math.max(1.0, bufH - minSurfaceY));
            int alpha = (int) Math.max(18, 46 * (1.0 - depthNorm * 0.55));
            Path2D.Double current = new Path2D.Double();
            double dir = (bandIdx % 2 == 0) ? 1.0 : -1.0;

            for (int i = 0; i < samples; i++) {
                double x = i * stepX;
                double xNorm = (double) i / (samples - 1);
                double undulation = Math.sin(waveTime * 0.55 * dir + xNorm * Math.PI * 3.0 + bandIdx * 1.4) * (2.6 * SCALE);
                if (i == 0) current.moveTo(x, baseY + undulation);
                else current.lineTo(x, baseY + undulation);
            }

            g2d.setColor(new Color(225, 72, 12, alpha));
            g2d.setStroke(new BasicStroke(2.2f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(current);
        }
    }

    private void renderSurfaceVeins(Graphics2D g2d, SurfaceSegment seg) {
        double[] depthOffsets = {7.5 * SCALE, 14.5 * SCALE};
        Color[] veinColors = {new Color(255, 155, 25, 70), new Color(215, 65, 10, 45)};
        float[] thicknesses = {2.6f * SCALE, 1.8f * SCALE};

        for (int v = 0; v < depthOffsets.length; v++) {
            Path2D.Double vein = new Path2D.Double();
            double baseDepth = depthOffsets[v];
            for (int i = 0; i < seg.numSamples; i++) {
                double xNorm = (double) i / (seg.numSamples - 1);
                double undulation = Math.sin(waveTime * (0.8 + v * 0.3) + xNorm * Math.PI * (3.5 + v)) * (2.2 * SCALE);
                double vy = seg.renderY[i] + baseDepth + undulation;
                if (i == 0) vein.moveTo(seg.renderX[i], vy);
                else vein.lineTo(seg.renderX[i], vy);
            }
            g2d.setColor(veinColors[v]);
            g2d.setStroke(new BasicStroke(thicknesses[v], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(vein);
        }
    }

    private void renderSurfaceBlisters(Graphics2D g2d, SurfaceSegment seg) {
        double segLeftBleed = seg.solidLeft ? padLeft : 0;
        double segRightBleed = seg.solidRight ? padRight : 0;
        double totalRenderW = seg.pixelWidth + segLeftBleed + segRightBleed;

        for (int i = 0; i < seg.blisterX.length; i++) {
            double prog = seg.blisterProgress[i];
            int idx = Math.max(0, Math.min(seg.numSamples - 1, (int) (((seg.blisterX[i] + segLeftBleed) / totalRenderW) * (seg.numSamples - 1))));
            int bx = (int) (padLeft + seg.localLeftX + seg.blisterX[i]);
            int by = (int) seg.renderY[idx];

            double swell = Math.sin(prog * Math.PI);
            int rx = Math.max(2, (int) (seg.blisterMaxRadius[i] * swell));
            int ry = Math.max(2, (int) (seg.blisterMaxRadius[i] * 0.65 * swell));

            g2d.setColor(new Color(255, 110, 15, (int) (190 * swell)));
            g2d.fillOval(bx - rx, by - ry, rx * 2, ry * 2);
            g2d.setColor(new Color(255, 240, 130, (int) (230 * swell)));
            g2d.fillOval(bx - rx / 2, by - ry / 2, rx, ry);
        }
    }

    private void renderDriftingCrustPlates(Graphics2D g2d, SurfaceSegment seg) {
        g2d.setStroke(new BasicStroke(2.2f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(CRUST_SLAG_COLOR);

        Path2D.Double slagSegment = new Path2D.Double();
        boolean inSegment = false;

        for (int i = 0; i < seg.numSamples; i++) {
            double xNorm = (double) i / (seg.numSamples - 1);
            double pattern = Math.sin(xNorm * Math.PI * 5.0 - waveTime * 0.35) + 0.5 * Math.cos(xNorm * Math.PI * 9.0 + waveTime * 0.2);
            if (pattern > 0.35) {
                if (!inSegment) {
                    slagSegment.moveTo(seg.renderX[i], seg.renderY[i]);
                    inSegment = true;
                }
                else slagSegment.lineTo(seg.renderX[i], seg.renderY[i]);
            }
            else if (inSegment) {
                g2d.draw(slagSegment);
                slagSegment.reset();
                inSegment = false;
            }
        }
        if (inSegment) g2d.draw(slagSegment);
    }

    private Path2D.Double buildSmoothCurve(double[] xs, double[] ys, int count) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(xs[0], ys[0]);
        for (int i = 0; i < count - 1; i++) {
            double midX = (xs[i] + xs[i + 1]) * 0.5;
            double midY = (ys[i] + ys[i + 1]) * 0.5;
            if (i == 0) path.lineTo(midX, midY);
            else path.quadTo(xs[i], ys[i], midX, midY);
        }
        path.lineTo(xs[count - 1], ys[count - 1]);
        return path;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(color);
        for (Rectangle2D.Double strip : hitBoxStrips) {
            g.drawRect((int) strip.x - xLevelOffset, (int) strip.y - yLevelOffset, (int) strip.width, (int) strip.height);
        }
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    @Override
    public void onEnter(Player player) {
        player.addAction(PlayerAction.LAVA);
        updatePlayerPhysics(player);
    }

    @Override
    public void onIntersect(Player player) {
        if (!player.checkAction(PlayerAction.LAVA)) {
            player.addAction(PlayerAction.LAVA);
        }
        updatePlayerPhysics(player);
    }

    @Override
    public void onExit(Player player) {
        player.removeAction(PlayerAction.LAVA);
        updatePlayerPhysics(player);
    }

    @Override
    public String getInteractionPrompt() {
        return null;
    }
}