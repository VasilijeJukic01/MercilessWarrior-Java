package platformer.model.levels;

import platformer.animation.AnimUtils;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;
import platformer.model.entities.effects.Particle;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static platformer.constants.Constants.*;

public class LevelManager {

    private final GameState gameState;
    private final LevelObjectManager levelObjectManager;
    private BufferedImage[] levelSprite;
    private final ArrayList<Level> levels = new ArrayList<>();
    private int levelIndex = 0;
    private final Particle[] particles;

    public LevelManager(GameState gameState) {
        this.gameState = gameState;
        this.particles = AnimUtils.getInstance().loadParticles();
        this.levelObjectManager = new LevelObjectManager();
        loadFirstLayerSprite();
        buildLevels();
    }

    private void loadFirstLayerSprite() {
        BufferedImage temp = Utils.getInstance().importImage("/images/levels/mossyTiles.png", 224, 224);
        levelSprite = new BufferedImage[49];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                int index = j*7 + i;
                levelSprite[index] = temp.getSubimage(i*32, j*32, 32, 32);
            }
        }
    }

    private void buildLevels() {
        BufferedImage[] lvlsL1 = getAllLevels("1");
        BufferedImage[] lvlsL2 = getAllLevels("2");
        for (int i = 0; i < lvlsL1.length; i++) {
            levels.add(new Level(lvlsL1[i], lvlsL2[i]));
        }
        Logger.getInstance().notify("Levels built successfully!", Message.NOTIFICATION);
    }

    private BufferedImage[] getAllLevels(String layer) {
        BufferedImage[] levels = new BufferedImage[MAX_LEVELS];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = Utils.getInstance().importImage("/images/levels/level"+(i+1)+"_layer"+layer+".png", -1, -1);
        }
        return levels;
    }

    private void loadLevel() {
        Level newLevel = levels.get(levelIndex);
        gameState.getEnemyManager().loadEnemies(newLevel);
        gameState.getEnemyManager().reset();
        gameState.getPlayer().loadLvlData(newLevel.getLvlData());
        gameState.getObjectManager().loadObjects(newLevel);
        gameState.getSpellManager().initBossSpells();
    }

    public void loadNextLevel() {
        levelIndex++;
        if (levelIndex >= levels.size()) gameState.getGame().startMenuState();
        loadLevel();
    }

    public void loadPrevLevel() {
        levelIndex--;
        if (levelIndex < 0) gameState.getGame().startMenuState();
        loadLevel();
    }

    private void renderDeco(Graphics g, int xLevelOffset, int yLevelOffset, int lay) {
        for (int i = 0; i < levels.get(levelIndex).getLvlData().length; i++) {
            for (int j = 0; j < levels.get(levelIndex).getLvlData()[0].length; j++) {
                int index = levels.get(levelIndex).getDecoSpriteIndex(i, j);
                int layer = levels.get(levelIndex).getLayerSpriteIndex(i, j);
                if (index == -1) continue;
                int x = TILES_SIZE*i-xLevelOffset;
                int y = TILES_SIZE*j-yLevelOffset;
                LevelObject levelObject = levelObjectManager.getLvlObjects()[index];
                if (layer == lay) g.drawImage(levelObject.getObjectModel(), x+levelObject.getXOffset(), y+levelObject.getYOffset(), levelObject.getW(), levelObject.getH(), null);
            }
        }
    }

    private void renderTerrain(Graphics g, int xLevelOffset, int yLevelOffset, boolean behind) {
        for (int i = 0; i < levels.get(levelIndex).getLvlData().length; i++) {
            for (int j = 0; j < levels.get(levelIndex).getLvlData()[0].length; j++) {
                int index = levels.get(levelIndex).getSpriteIndex(i, j);
                if ((!behind && index < 255) || index == -1) continue;
                if (behind && index >= 255) continue;
                if (!behind) index -= 255;
                int x = TILES_SIZE*i-xLevelOffset;
                int y = TILES_SIZE*j-yLevelOffset;
                int w = TILES_SIZE+1;
                int h = TILES_SIZE+1;
                g.drawImage(levelSprite[index], x, y, w, h, null);
            }
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderDeco(g, xLevelOffset, yLevelOffset, 0);
        renderDeco(g, xLevelOffset, yLevelOffset, 1);
        g.setColor(new Color(1, 130, 120, 110));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        renderDeco(g, xLevelOffset, yLevelOffset, 2);
        renderTerrain(g, xLevelOffset, yLevelOffset, true);
        renderDeco(g, xLevelOffset, yLevelOffset, 3);
        renderTerrain(g, xLevelOffset, yLevelOffset, false);
        for (Particle particle : particles) {
            particle.render(g);
        }
    }

    public Level getCurrentLevel() {
        return levels.get(levelIndex);
    }

    public Particle[] getParticles() {
        return particles;
    }
}
