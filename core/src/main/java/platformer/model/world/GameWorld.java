package platformer.model.world;

import lombok.Getter;
import platformer.core.GameContext;
import platformer.model.effects.EffectManager;
import platformer.model.effects.lighting.LightManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.levels.LevelManager;
import platformer.model.levels.metadata.LevelMetadata;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.spells.SpellManager;

import java.awt.*;

import static platformer.constants.Constants.*;

/**
 * Encapsulates all the active entities, objects, and environmental systems of a level.
 * This class acts as a container for the game's interactive world, handling the collective update and render calls for its components.
 */
@Getter
public class GameWorld {

    private final Player player;
    private final LevelManager levelManager;
    private final ObjectManager objectManager;
    private final EnemyManager enemyManager;
    private final ProjectileManager projectileManager;
    private final SpellManager spellManager;
    private final EffectManager effectManager;
    private final RainManager rainManager;
    private final TimeCycleManager timeCycleManager;
    private final LightManager lightManager;

    public GameWorld(GameContext context) {
        this.levelManager = context.getLevelManager();
        this.effectManager = context.getEffectManager();
        this.rainManager = context.getRainManager();
        this.enemyManager = context.getEnemyManager();
        this.objectManager = context.getObjectManager();
        this.projectileManager = context.getProjectileManager();
        this.spellManager = context.getSpellManager();
        this.lightManager = context.getLightManager();
        this.timeCycleManager = context.getTimeCycleManager();

        this.player = new Player(PLAYER_X, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, context);
        this.player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
        this.player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn("LEFT"));

        loadStartLevel();
    }

    private void loadStartLevel() {
        this.enemyManager.loadEnemies(levelManager.getCurrentLevel());
        this.objectManager.loadObjects(levelManager.getCurrentLevel());
        reinitializeParticles();
    }

    private void reinitializeParticles() {
        LevelMetadata metadata = levelManager.getCurrentLevelMetadata();
        boolean particlesEnabled = metadata == null || metadata.getAmbientParticlesEnabled() == null || metadata.getAmbientParticlesEnabled();
        effectManager.setAmbientEffectsActive(particlesEnabled);

        if (particlesEnabled) {
            int levelWidth = levelManager.getCurrentLevel().getLevelTilesWidth() * TILES_SIZE;
            int levelHeight = levelManager.getCurrentLevel().getLevelTilesHeight() * TILES_SIZE;
            effectManager.reinitializeAmbientParticles(levelWidth, levelHeight);
        }
    }

    // Core
    public void update() {
        player.update();
        enemyManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        objectManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        projectileManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        timeCycleManager.update();
        lightManager.update(timeCycleManager);
        spellManager.update();
        effectManager.update();
        rainManager.update();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset, boolean isDarkPhase) {
        g.drawImage(levelManager.getCurrentBackground(), 0, 0, null);
        effectManager.renderAmbientEffects(g, xLevelOffset, yLevelOffset);
        rainManager.render(g);
        levelManager.render(g, xLevelOffset, yLevelOffset);
        objectManager.render(g, xLevelOffset, yLevelOffset);
        if (isDarkPhase) {
            enemyManager.render(g, xLevelOffset, yLevelOffset);
            projectileManager.render(g, xLevelOffset, yLevelOffset);
        }
        lightManager.render(g, xLevelOffset, yLevelOffset);
        effectManager.renderBackgroundEffects(g, xLevelOffset, yLevelOffset);
        effectManager.renderAmbientEffectsAbove(g, xLevelOffset, yLevelOffset);
        if (!isDarkPhase) enemyManager.render(g, xLevelOffset, yLevelOffset);
        player.render(g, xLevelOffset, yLevelOffset);
        objectManager.secondRender(g, xLevelOffset, yLevelOffset);
        if (!isDarkPhase) projectileManager.render(g, xLevelOffset, yLevelOffset);
        spellManager.render(g, xLevelOffset, yLevelOffset);
        effectManager.renderForegroundEffects(g, xLevelOffset, yLevelOffset);
    }

    // Reset
    public void reset() {
        enemyManager.reset();
        player.reset();
        objectManager.reset();
        projectileManager.reset();
        spellManager.reset();
        effectManager.reset();
        lightManager.reset();
        rainManager.reset();
        reinitializeParticles();
    }

    public void levelLoadReset(String spawn) {
        enemyManager.reset();
        objectManager.reset();
        spellManager.initBossSpells();
        enemyManager.loadEnemies(levelManager.getCurrentLevel());
        objectManager.loadObjects(levelManager.getCurrentLevel());
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn(spawn));
        reinitializeParticles();
    }
}