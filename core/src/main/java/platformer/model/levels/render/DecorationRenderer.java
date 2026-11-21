package platformer.model.levels.render;

import platformer.model.levels.Level;
import platformer.model.levels.LevelObjectManager;
import platformer.model.levels.metadata.DecorationMetadata;
import platformer.model.levels.metadata.ObjectMetadata;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static platformer.constants.Constants.*;

/**
 * Handles rendering of level decorations, including viewport culling, pre-baked high-res spatial transforms (including horizontal/vertical mirroring via negative scaleX/scaleY), and GPU-accelerated procedural wind sway animations.
 */
public class DecorationRenderer {

    private final LevelObjectManager levelObjectManager;
    private final Map<String, BufferedImage> transformedCache = new HashMap<>();
    private final BufferedImage underGlowTexture;

    public DecorationRenderer(LevelObjectManager levelObjectManager) {
        this.levelObjectManager = levelObjectManager;
        this.underGlowTexture = createUnderGlowTexture(scale(96));
    }

    private BufferedImage createUnderGlowTexture(int diameter) {
        int d = Math.max(4, diameter);
        BufferedImage tex = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tex.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RadialGradientPaint paint = new RadialGradientPaint(
                new Point2D.Float(d / 2.0f, d / 2.0f),
                d / 2.0f,
                new float[]{0.0f, 0.55f, 1.0f},
                new Color[]{new Color(0, 255, 230, 140), new Color(0, 200, 210, 55), new Color(0, 160, 190, 0)}
        );
        g2d.setPaint(paint);
        g2d.fillOval(0, 0, d, d);
        g2d.dispose();
        return tex;
    }

    public void renderLayer(Graphics g, Level level, Map<Integer, DecorationMetadata> decoSet, Map<Point, ObjectMetadata> objMetaMap, int xLevelOffset, int yLevelOffset, int layer, boolean useLively) {
        if (decoSet == null) return;

        double baseTime = System.currentTimeMillis() * 0.0022;
        boolean hasMetaInLevel = objMetaMap != null && !objMetaMap.isEmpty();

        int marginTiles = 6;
        int xStart = Math.max(0, (xLevelOffset / TILES_SIZE) - marginTiles);
        int xEnd = Math.min(level.getLvlData().length, ((xLevelOffset + GAME_WIDTH) / TILES_SIZE) + marginTiles);
        int yStart = Math.max(0, (yLevelOffset / TILES_SIZE) - marginTiles);
        int yEnd = Math.min(level.getLvlData()[0].length, ((yLevelOffset + GAME_HEIGHT) / TILES_SIZE) + marginTiles);

        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
                if (level.getLayerSpriteIndex(i, j) != layer) continue;

                DecorationMetadata decoMeta = decoSet.get(level.getDecoSpriteIndex(i, j));
                if (decoMeta == null) continue;
                ObjectMetadata objMeta = hasMetaInLevel ? objMetaMap.get(new Point(i, j)) : null;
                int customOffX = (objMeta != null) ? (int) (objMeta.getOffsetX() / 2.0 * SCALE) : 0;
                int customOffY = (objMeta != null) ? (int) (objMeta.getOffsetY() / 2.0 * SCALE) : 0;
                int offsetX = (int) (decoMeta.getXOffset() / 2.0 * SCALE) + customOffX, offsetY = (int) (decoMeta.getYOffset() / 2.0 * SCALE) + customOffY;
                int wid = (int) (decoMeta.getWid() / 2.0 * SCALE), hei = (int) (decoMeta.getHei() / 2.0 * SCALE);
                int xPos = (TILES_SIZE * i - xLevelOffset) + offsetX, yPos = (TILES_SIZE * j - yLevelOffset) + offsetY;

                boolean hasCustomTransform = objMeta != null && (objMeta.getRotation() != 0.0 || objMeta.getScaleX() != 1.0 || objMeta.getScaleY() != 1.0);
                double maxScale = hasCustomTransform ? Math.max(1.0, Math.max(Math.abs(objMeta.getScaleX()), Math.abs(objMeta.getScaleY()))) : 1.0;

                if (!isVisible(xPos, yPos, (int) (wid * maxScale), (int) (hei * maxScale))) continue;

                BufferedImage model = levelObjectManager.getModel(decoMeta.getId(), level.getTilesetName(), decoMeta.getFilename(), decoMeta.isFlipped(), useLively);
                if (model == null) continue;

                if (level.isDecorationCut(i, j)) {
                    drawCutStubble(g, model, xPos, yPos, wid, hei, objMeta);
                    continue;
                }

                if (decoMeta.getSway() != null) drawSwaying(g, model, decoMeta, objMeta, i, j, xPos, yPos, wid, hei, baseTime, level);
                else if (hasCustomTransform) drawCachedTransformed(g, model, decoMeta, objMeta, xPos, yPos, wid, hei, useLively);
                else g.drawImage(model, xPos, yPos, wid, hei, null);
            }
        }
    }

    private boolean isVisible(int x, int y, int wid, int hei) {
        return x + wid >= -192 && x <= GAME_WIDTH + 192 && y + hei >= -192 && y <= GAME_HEIGHT + 192;
    }

    /**
     * Pre-bakes a rotated/scaled/flipped static decoration with Bicubic interpolation once and caches the resulting BufferedImage,
     * so every frame only performs a fast hardware-accelerated drawImage call.
     */
    private void drawCachedTransformed(Graphics g, BufferedImage model, DecorationMetadata decoMeta, ObjectMetadata objMeta, int xPos, int yPos, int wid, int hei, boolean useLively) {
        double rot = objMeta.getRotation();
        double sx = objMeta.getScaleX() != 0.0 ? objMeta.getScaleX() : 1.0;
        double sy = objMeta.getScaleY() != 0.0 ? objMeta.getScaleY() : 1.0;

        String key = decoMeta.getId() + "_" + decoMeta.isFlipped() + "_" + useLively + "_" + rot + "_" + sx + "_" + sy;
        BufferedImage baked = transformedCache.computeIfAbsent(key, k -> bakeTransformedImage(model, wid, hei, rot, sx, sy));

        int centerX = xPos + wid / 2;
        int centerY = yPos + hei / 2;
        g.drawImage(baked, centerX - baked.getWidth() / 2, centerY - baked.getHeight() / 2, null);
    }

    private BufferedImage bakeTransformedImage(BufferedImage model, int wid, int hei, double rotationDeg, double sx, double sy) {
        double absSx = Math.abs(sx);
        double absSy = Math.abs(sy);
        double scaledW = wid * absSx, scaledH = hei * absSy;
        double rads = Math.toRadians(rotationDeg);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int boundW = Math.max(1, (int) Math.ceil(scaledW * cos + scaledH * sin) + 4);
        int boundH = Math.max(1, (int) Math.ceil(scaledH * cos + scaledW * sin) + 4);

        BufferedImage out = new BufferedImage(boundW, boundH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = out.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform at = new AffineTransform();
        at.translate(boundW / 2.0, boundH / 2.0);
        at.rotate(rads);
        at.scale(sx, sy);
        g2d.setTransform(at);
        g2d.drawImage(model, -wid / 2, -hei / 2, wid, hei, null);
        g2d.dispose();
        return out;
    }

    private void drawSwaying(Graphics g, BufferedImage model, DecorationMetadata decoMeta, ObjectMetadata objMeta, int i, int j, int xPos, int yPos, int wid, int hei, double baseTime, Level level) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(xPos + wid / 2.0, yPos + hei / 2.0);

        double flipSignX = 1.0;
        if (objMeta != null) {
            if (objMeta.getRotation() != 0.0) g2d.rotate(Math.toRadians(objMeta.getRotation()));
            double sx = objMeta.getScaleX() != 0.0 ? objMeta.getScaleX() : 1.0;
            double sy = objMeta.getScaleY() != 0.0 ? objMeta.getScaleY() : 1.0;
            if (sx < 0) flipSignX = -1.0;
            if (sx != 1.0 || sy != 1.0) g2d.scale(sx, sy);
        }

        double extraBend = level.getDecorationBend(i, j) * flipSignX;
        applySwayAndDraw(g2d, model, decoMeta, wid, hei, i, j, baseTime, extraBend, flipSignX);
        g2d.dispose();
    }

    private void drawCutStubble(Graphics g, BufferedImage model, int xPos, int yPos, int wid, int hei, ObjectMetadata objMeta) {
        int srcH = model.getHeight();
        int stubSrcY = (int) (srcH * 0.72);
        int stubDrawY = yPos + (int) (hei * 0.72);
        boolean flippedX = objMeta != null && objMeta.getScaleX() < 0;
        if (flippedX) {
            g.drawImage(model, xPos + wid, stubDrawY, xPos, yPos + hei, 0, stubSrcY, model.getWidth(), srcH, null);
        }
        else {
            g.drawImage(model, xPos, stubDrawY, xPos + wid, yPos + hei, 0, stubSrcY, model.getWidth(), srcH, null);
        }
    }

    private void applySwayAndDraw(Graphics2D g2d, BufferedImage model, DecorationMetadata decoMeta, int wid, int hei, int tileX, int tileY, double baseTime, double extraBend, double flipSignX) {
        String swayType = decoMeta.getSway();
        if (swayType == null) {
            g2d.drawImage(model, -wid / 2, -hei / 2, wid, hei, null);
        }
        else if (swayType.equalsIgnoreCase("BOTTOM")) {
            double sway = calculateOrganicSway(tileX, tileY, baseTime, 0.085) * flipSignX + extraBend;
            g2d.translate(0, hei / 2.0);
            g2d.shear(sway, 0);
            g2d.drawImage(model, -wid / 2, -hei, wid, hei, null);
        }
        else if (swayType.equalsIgnoreCase("BOTTOM_SLOW")) {
            double sway = calculateOrganicSway(tileX, tileY, baseTime * 0.55, 0.032) * flipSignX;
            g2d.translate(0, hei / 2.0);
            g2d.shear(sway, 0);
            g2d.drawImage(model, -wid / 2, -hei, wid, hei, null);
        }
        else if (swayType.equalsIgnoreCase("TOP")) {
            double sway = calculateOrganicSway(tileX, tileY, baseTime * 0.7, 0.045) * flipSignX + extraBend;
            g2d.translate(0, -hei / 2.0);
            g2d.shear(sway, 0);
            g2d.drawImage(model, -wid / 2, 0, wid, hei, null);
        }
        else if (swayType.equalsIgnoreCase("UNDER_FRINGE")) {
            drawUnderGrassFringe(g2d, model, decoMeta.getId(), wid, hei, tileX, tileY, baseTime, flipSignX);
        }
    }

    /**
     * Renders UNDER_GRASS decorations by keeping the upper 45% (soil and embedded rocks) anchored still,
     * while gently swaying the lower 55% (hanging roots, ferns, and moss) and pulsing bioluminescent nodes.
     */
    private void drawUnderGrassFringe(Graphics2D g2d, BufferedImage model, String id, int wid, int hei, int tileX, int tileY, double baseTime, double flipSignX) {
        int srcW = model.getWidth();
        int srcH = model.getHeight();
        double anchorRatio = 0.45;
        int splitSrcY = (int) Math.round(srcH * anchorRatio);
        int splitDstY = (int) Math.round(-hei / 2.0 + hei * anchorRatio);

        g2d.drawImage(model, -wid / 2, -hei / 2, wid / 2, splitDstY, 0, 0, srcW, splitSrcY, null);

        double sway = calculateOrganicSway(tileX, tileY, baseTime * 0.65, 0.065) * flipSignX;
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(0, splitDstY);
        g2d.shear(sway, 0);
        g2d.drawImage(model, -wid / 2, 0, wid / 2, hei / 2 - splitDstY, 0, splitSrcY, srcW, srcH, null);
        g2d.setTransform(oldTransform);

        if (id != null && (id.contains("GLOW") || id.contains("SHROOM"))) {
            int hash = (tileX * 73856093) ^ (tileY * 19349663);
            double phase = ((hash & 0xFFFF) / 65535.0) * Math.PI * 2.0;
            float pulse = (float) (0.18 + 0.22 * (0.5 + 0.5 * Math.sin(baseTime * 0.9 + phase)));
            Composite oldComp = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
            int glowW = (int) (wid * 0.85);
            int glowH = (int) (hei * 0.85);
            g2d.drawImage(underGlowTexture, -glowW / 2, -glowH / 2, glowW, glowH, null);
            g2d.setComposite(oldComp);
        }
    }

    /**
     * Generates a randomized, organic wind sway for a specific tile coordinate using a deterministic spatial hash and dual-harmonic sine waves.
     */
    private double calculateOrganicSway(int tileX, int tileY, double baseTime, double baseAmplitude) {
        int hash = (tileX * 73856093) ^ (tileY * 19349663);
        double rand1 = ((hash & 0xFFFF) / 65535.0);
        double rand2 = (((hash >> 8) & 0xFFFF) / 65535.0);
        double rand3 = (((hash >> 16) & 0xFFFF) / 65535.0);

        double speed = 0.70 + rand1 * 0.65;
        double phase = rand2 * Math.PI * 2.0;
        double ampScale = 0.75 + rand3 * 0.50;

        double primaryWave = Math.sin(baseTime * speed + phase);
        double gustWave = Math.sin(baseTime * (speed * 1.7) + phase * 0.5);
        return (primaryWave * 0.7 + gustWave * 0.3) * (baseAmplitude * ampScale);
    }
}