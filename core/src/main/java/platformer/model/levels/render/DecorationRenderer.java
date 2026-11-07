package platformer.model.levels.render;

import platformer.model.levels.Level;
import platformer.model.levels.LevelObjectManager;
import platformer.model.levels.metadata.DecorationMetadata;
import platformer.model.levels.metadata.ObjectMetadata;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

import static platformer.constants.Constants.*;

/**
 * Handles rendering of level decorations, including viewport culling, spatial transforms, and procedural wind sway animations.
 */
public class DecorationRenderer {

    private final LevelObjectManager levelObjectManager;

    public DecorationRenderer(LevelObjectManager levelObjectManager) {
        this.levelObjectManager = levelObjectManager;
    }

    public void renderLayer(Graphics g, Level level, Map<Integer, DecorationMetadata> decoSet, Map<Point, ObjectMetadata> objMetaMap, int xLevelOffset, int yLevelOffset, int layer, boolean useLively) {
        if (decoSet == null) return;

        double baseTime = System.currentTimeMillis() * 0.0022;
        for (int i = 0; i < level.getLvlData().length; i++) {
            for (int j = 0; j < level.getLvlData()[0].length; j++) {
                if (level.getLayerSpriteIndex(i, j) != layer) continue;

                DecorationMetadata decoMeta = decoSet.get(level.getDecoSpriteIndex(i, j));
                if (decoMeta == null) continue;

                int xPos = (TILES_SIZE * i - xLevelOffset) + (int) (decoMeta.getYOffset() * SCALE);
                int yPos = (TILES_SIZE * j - yLevelOffset) + (int) (decoMeta.getXOffset() * SCALE);
                if (!isVisible(xPos, yPos, decoMeta.getWid(), decoMeta.getHei())) continue;

                BufferedImage model = levelObjectManager.getModel(decoMeta.getId(), level.getTilesetName(), decoMeta.getFilename(), decoMeta.isFlipped(), useLively);
                if (model == null) continue;
                if (level.isDecorationCut(i, j)) {
                    drawCutStubble(g, model, xPos, yPos, decoMeta.getWid(), decoMeta.getHei());
                    continue;
                }
                ObjectMetadata objMeta = objMetaMap.get(new Point(i, j));
                if (!requiresTransform(objMeta, decoMeta)) {
                    g.drawImage(model, xPos, yPos, decoMeta.getWid(), decoMeta.getHei(), null);
                }
                else drawTransformed(g, model, decoMeta, objMeta, i, j, xPos, yPos, xLevelOffset, yLevelOffset, baseTime, level);
            }
        }
    }

    private boolean isVisible(int x, int y, int wid, int hei) {
        return x + wid >= -128 && x <= GAME_WIDTH + 128 && y + hei >= -128 && y <= GAME_HEIGHT + 128;
    }

    private boolean requiresTransform(ObjectMetadata objMeta, DecorationMetadata decoMeta) {
        if (decoMeta.getSway() != null) return true;
        if (objMeta == null) return false;
        return objMeta.getRotation() != 0.0 || objMeta.getScaleX() != 1.0 || objMeta.getScaleY() != 1.0;
    }

    private void drawTransformed(Graphics g, BufferedImage model, DecorationMetadata decoMeta, ObjectMetadata objMeta, int i, int j, int xPos, int yPos, int xLevelOffset, int yLevelOffset, double baseTime, Level level) {
        int wid = decoMeta.getWid();
        int hei = decoMeta.getHei();
        Graphics2D g2d = (Graphics2D) g.create();

        int baseX = (objMeta != null) ? (TILES_SIZE * i - xLevelOffset) : xPos;
        int baseY = (objMeta != null) ? (TILES_SIZE * j - yLevelOffset) : yPos;

        g2d.translate(baseX + wid / 2.0, baseY + hei / 2.0);
        if (objMeta != null) {
            g2d.rotate(Math.toRadians(objMeta.getRotation()));
            g2d.scale(objMeta.getScaleX(), objMeta.getScaleY());
        }

        double extraBend = level.getDecorationBend(i, j);
        applySwayAndDraw(g2d, model, decoMeta.getSway(), wid, hei, i, j, baseTime, extraBend);
        g2d.dispose();
    }

    private void drawCutStubble(Graphics g, BufferedImage model, int xPos, int yPos, int wid, int hei) {
        int srcH = model.getHeight();
        int stubSrcY = (int) (srcH * 0.72);
        int stubDrawY = yPos + (int) (hei * 0.72);
        g.drawImage(model, xPos, stubDrawY, xPos + wid, yPos + hei, 0, stubSrcY, model.getWidth(), srcH, null);
    }

    private void applySwayAndDraw(Graphics2D g2d, BufferedImage model, String swayType, int wid, int hei, int tileX, int tileY, double baseTime, double extraBend) {
        if (swayType == null) {
            g2d.drawImage(model, -wid / 2, -hei / 2, wid, hei, null);
        }
        else if (swayType.equalsIgnoreCase("BOTTOM")) {
            double sway = calculateOrganicSway(tileX, tileY, baseTime, 0.085) + extraBend;
            g2d.translate(0, hei / 2.0);
            g2d.shear(sway, 0);
            g2d.drawImage(model, -wid / 2, -hei, wid, hei, null);
        }
        else if (swayType.equalsIgnoreCase("TOP")) {
            double sway = calculateOrganicSway(tileX, tileY, baseTime * 0.7, 0.045);
            g2d.translate(0, -hei / 2.0);
            g2d.shear(sway, 0);
            g2d.drawImage(model, -wid / 2, 0, wid, hei, null);
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