package platformer.model.levels;

import platformer.animation.Animation;
import platformer.debug.logger.Message;
import platformer.debug.logger.Logger;
import platformer.model.entities.effects.ParticleFactory;
import platformer.model.entities.effects.Particle;
import platformer.model.entities.effects.ParticleType;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.FOREST_SPRITE;
import static platformer.constants.FilePaths.PARTICLE_SHEET;

public class LevelManager {

    private final GameState gameState;
    private final LevelObjectManager levelObjectManager;

    private BufferedImage[] levelSprite;
    private final Level[][] levels = new Level[MAX_LEVELS][MAX_LEVELS];
    private int levelIndexI = 0, levelIndexJ = 0;

    // Particle Flyweight
    private final Particle[] particles;
    private final ParticleFactory particleFactory;

    public LevelManager(GameState gameState) {
        this.gameState = gameState;
        this.particleFactory = new ParticleFactory();
        this.particles = loadParticles();
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

    private void buildLevels() {
        BufferedImage[][] levelsLayer1 = getAllLevels("1");
        BufferedImage[][] levelsLayer2 = getAllLevels("2");
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < levelsLayer1.length; j++) {
                if (levelsLayer1[i][j] != null)
                    levels[i][j] = new Level(levelsLayer1[i][j], levelsLayer2[i][j]);
            }
        }
        Logger.getInstance().notify("Levels built successfully!", Message.NOTIFICATION);
    }

    private BufferedImage[][] getAllLevels(String layer) {
        BufferedImage[][] levels = new BufferedImage[4][4];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < levels.length; j++) {
                BufferedImage levelImg = Utils.getInstance().importImage("/images/levels/level"+i+j+".png", -1, -1);
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

    private Particle[] loadParticles() {
        Particle[] particles = new Particle[PARTICLES_CAP];
        Random rand = new Random();
        for (int i = 0; i < particles.length; i++) {
            int xPos = rand.nextInt(GAME_WIDTH-10) + 10;
            int yPos = rand.nextInt(GAME_HEIGHT-10) + 10;
            int size = (int)((rand.nextInt(15-5) + 5) * SCALE);
            String key = "DefaultParticle";
            BufferedImage[] images = Animation.getInstance().loadFromSprite(PARTICLE_SHEET, 8, 0, size, size, 0, PARTICLE_W, PARTICLE_H);
            ParticleType particleType = particleFactory.getParticleImage(key, images);
            particles[i] = new Particle(particleType, size, xPos, yPos);
        }
        return particles;
    }

    // Level flow
    private void loadLevel() {
        Level newLevel = levels[levelIndexI][levelIndexJ];
        gameState.getPlayer().loadLvlData(newLevel.getLvlData());
        gameState.getEnemyManager().loadEnemies(newLevel);
        gameState.getObjectManager().loadObjects(newLevel);
        gameState.getSpellManager().initBossSpells();
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
        Arrays.stream(particles).forEach(p -> p.render(g));
    }

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

    public void loadSavePoint(int spawnNumber) {
        if (spawnNumber == -1) {
            this.levelIndexI = 0;
            this.levelIndexJ = 0;
        }
        if (spawnNumber == 1) {
            this.levelIndexI = 0;
            this.levelIndexJ = 2;
        }
        loadLevel();
    }

    public Particle[] getParticles() {
        return particles;
    }

}
