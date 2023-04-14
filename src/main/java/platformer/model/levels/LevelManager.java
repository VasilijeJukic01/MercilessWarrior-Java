package platformer.model.levels;

import platformer.model.Tiles;
import platformer.core.Game;
import platformer.model.entities.effects.Particle;
import platformer.state.PlayingState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class LevelManager {

    private final Game game;
    private final PlayingState playingState;
    private final LevelObjectManager levelObjectManager;
    private BufferedImage[] levelSprite;
    private final ArrayList<Level> levels = new ArrayList<>();
    private int levelIndex = 0;
    private Particle[] particles;

    private BufferedImage leftEnd, rightEnd;

    public LevelManager(Game game, PlayingState playingState) {
        this.game = game;
        this.playingState = playingState;
        this.particles = Utils.getInstance().loadParticles();
        this.levelObjectManager = new LevelObjectManager();
        loadFirstLayerSprite();
        buildLevels();
    }

    private void loadFirstLayerSprite() {
        BufferedImage temp = Utils.getInstance().importImage("src/main/resources/images/levels/mossyTiles.png", 224, 224);
        levelSprite = new BufferedImage[49];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                int index = j*7 + i;
                levelSprite[index] = temp.getSubimage(i*32, j*32, 32, 32);
            }
        }
        leftEnd = Utils.getInstance().importImage("src/main/resources/images/levels/leftEnd.png", 32, 32);
        rightEnd = Utils.getInstance().importImage("src/main/resources/images/levels/rightEnd.png", 32, 32);
    }

    private void buildLevels() {
        BufferedImage[] lvlsL1 = Utils.getInstance().getAllLevelsL1();
        BufferedImage[] lvlsL2 = Utils.getInstance().getAllLevelsL2();
        for (int i = 0; i < lvlsL1.length; i++) {
            levels.add(new Level(lvlsL1[i], lvlsL2[i]));
        }
    }

    private void loadLevel() {
        Level newLevel = levels.get(levelIndex);
        playingState.getEnemyManager().loadEnemies(newLevel);
        playingState.getEnemyManager().reset();
        playingState.getPlayer().loadLvlData(newLevel.getLvlData());
        playingState.getObjectManager().loadObjects(newLevel);
    }

    public void loadNextLevel() {
        levelIndex++;
        if (levelIndex >= levels.size()) game.startMenuState();
        loadLevel();
    }

    public void loadPrevLevel() {
        levelIndex--;
        if (levelIndex < 0) game.startMenuState();
        loadLevel();
    }

    private void renderDeco(Graphics g, int xLevelOffset, int yLevelOffset, int layer) {
        for (int i = 0; i < levels.get(levelIndex).getLvlData().length; i++) {
            for (int j = 0; j < levels.get(levelIndex).getLvlData()[0].length; j++) {
                int index = levels.get(levelIndex).getDecoSpriteIndex(i, j);
                if (index == -1 || index > 42) continue;
                int x = (int)(Tiles.TILES_SIZE.getValue()*i-xLevelOffset);
                int y =  (int)(Tiles.TILES_SIZE.getValue()*j-yLevelOffset);
                LevelObject levelObject = levelObjectManager.getLvlObjects()[index];
                if (levelObject.getLayer() == layer)
                    g.drawImage(levelObject.getObjectModel(), x, y+levelObject.getYOffset(), levelObject.getW(), levelObject.getH(), null);
            }
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderDeco(g, xLevelOffset, yLevelOffset, 0);
        renderDeco(g, xLevelOffset, yLevelOffset, 1);
        g.setColor(new Color(1, 130, 120, 110));
        g.fillRect(0, 0, (int)Tiles.GAME_WIDTH.getValue(), (int)Tiles.GAME_HEIGHT.getValue());
        for (int i = 0; i < levels.get(levelIndex).getLvlData().length; i++) {
            for (int j = 0; j < levels.get(levelIndex).getLvlData()[0].length; j++) {
                int index = levels.get(levelIndex).getSpriteIndex(i, j);
                if (index == -1) continue;
                if (index == -2) renderExit(g, xLevelOffset, yLevelOffset, rightEnd, i, j, true);
                else if (index == -3) renderExit(g, xLevelOffset, yLevelOffset, leftEnd, i, j, false);
                else {
                    int x = (int)(Tiles.TILES_SIZE.getValue()*i-xLevelOffset);
                    int y =  (int)(Tiles.TILES_SIZE.getValue()*j-yLevelOffset);
                    int w = (int)Tiles.TILES_SIZE.getValue()+1;
                    int h = (int)Tiles.TILES_SIZE.getValue()+1;
                    g.drawImage(levelSprite[index], x, y, w, h, null);
                }
            }
        }
        for (Particle particle : particles) {
            particle.render(g);
        }
        renderDeco(g, xLevelOffset, yLevelOffset, 2);
    }

    private void renderExit(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage object, int i, int j, boolean flag) {
        int k = flag ? (int)Tiles.SCALE.getValue()*10 : -(int)Tiles.SCALE.getValue()*10;
        int x =  (int)(Tiles.TILES_SIZE.getValue()*i-xLevelOffset), y = (int)(Tiles.TILES_SIZE.getValue()*j-yLevelOffset);
        int w = (int)Tiles.TILES_SIZE.getValue()+k, h = (int)Tiles.TILES_SIZE.getValue();
        g.drawImage(object, x, y, w, h, null);
    }

    public Level getCurrentLevel() {
        return levels.get(levelIndex);
    }

    public Particle[] getParticles() {
        return particles;
    }
}
