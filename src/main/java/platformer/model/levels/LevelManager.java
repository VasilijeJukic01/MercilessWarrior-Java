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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.FOREST_SPRITE;
import static platformer.constants.FilePaths.PARTICLE_SHEET;

public class LevelManager {

    private final GameState gameState;
    private final LevelObjectManager levelObjectManager;

    private BufferedImage[] levelSprite;
    private final List<Level> levels = new ArrayList<>();
    private int levelIndex = 0;

    private final Particle[] particles;
    private final ParticleFactory particleFactory;

    public LevelManager(GameState gameState) {
        this.gameState = gameState;
        this.particleFactory = new ParticleFactory();
        this.particles = loadParticles();
        this.levelObjectManager = new LevelObjectManager();
        loadFirstLayerSprite();
        buildLevels();
    }

    // Init
    private void loadFirstLayerSprite() {
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
        BufferedImage[] levelsLayer1 = getAllLevels("1");
        BufferedImage[] levelsLayer2 = getAllLevels("2");
        for (int i = 0; i < levelsLayer1.length; i++) {
            levels.add(new Level(levelsLayer1[i], levelsLayer2[i]));
        }
        Logger.getInstance().notify("Levels built successfully!", Message.NOTIFICATION);
    }

    private BufferedImage[] getAllLevels(String layer) {
        BufferedImage[] levels = new BufferedImage[3];
        for (int i = 0; i < levels.length; i++) {
            BufferedImage levelImg = Utils.getInstance().importImage("/images/levels/level"+(i+1)+".png", -1, -1);
            if (layer.equals("1")) {
                levels[i] = levelImg.getSubimage(0, 0, levelImg.getWidth()/2, levelImg.getHeight());
            }
            else {
                levels[i] = levelImg.getSubimage(levelImg.getWidth()/2, 0, levelImg.getWidth()/2, levelImg.getHeight());
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
        Level newLevel = levels.get(levelIndex);
        gameState.getPlayer().loadLvlData(newLevel.getLvlData());
        gameState.getEnemyManager().loadEnemies(newLevel);
        gameState.getObjectManager().loadObjects(newLevel);
        gameState.getSpellManager().initBossSpells();
    }

    public void loadNextLevel() {
        levelIndex++;
        if (levelIndex >= levels.size()) {
            gameState.getGame().startMenuState();
            return;
        }
        loadLevel();
    }

    public void loadPrevLevel() {
        levelIndex--;
        if (levelIndex < 0) {
            gameState.getGame().startMenuState();
            return;
        }
        loadLevel();
    }

    // Render
    private void renderDeco(Graphics g, int xLevelOffset, int yLevelOffset, int layer) {
        for (int i = 0; i < levels.get(levelIndex).getLvlData().length; i++) {
            for (int j = 0; j < levels.get(levelIndex).getLvlData()[0].length; j++) {
                int decorationIndex = levels.get(levelIndex).getDecoSpriteIndex(i, j);
                int layerIndex = levels.get(levelIndex).getLayerSpriteIndex(i, j);
                if (decorationIndex == -1) continue;
                int x = TILES_SIZE * i - xLevelOffset;
                int y = TILES_SIZE * j - yLevelOffset;
                LevelObject lvlObject = levelObjectManager.getLvlObjects()[decorationIndex];
                if (layerIndex == layer)
                    g.drawImage(lvlObject.getObjectModel(), x+lvlObject.getXOffset(), y+lvlObject.getYOffset(), lvlObject.getW(), lvlObject.getH(), null);
            }
        }
    }

    private void renderTerrain(Graphics g, int xLevelOffset, int yLevelOffset, boolean behind) {
        for (int i = 0; i < levels.get(levelIndex).getLvlData().length; i++) {
            for (int j = 0; j < levels.get(levelIndex).getLvlData()[0].length; j++) {
                int tileIndex = levels.get(levelIndex).getSpriteIndex(i, j);
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
        return levels.get(levelIndex);
    }

    public Particle[] getParticles() {
        return particles;
    }

}
