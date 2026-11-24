package platformer.model.world;

import lombok.Getter;
import platformer.core.GameClock;
import platformer.core.GameContext;
import platformer.model.combat.ClashManager;
import platformer.model.effects.EffectManager;
import platformer.model.effects.decorations.DecorationEffectManager;
import platformer.model.effects.lighting.LightManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.levels.LevelManager;
import platformer.model.levels.LvlTriggerType;
import platformer.model.levels.metadata.LevelMetadata;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.quests.QuestManager;
import platformer.model.spells.SpellManager;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Encapsulates all the active entities, objects, and environmental systems of a level.
 * This class acts as a container for the game's interactive world, handling the collective update and render calls for its components.
 */
@Getter
public class GameWorld {

    private final GameClock gameClock;
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
    private final QuestManager questManager;
    private final WorldMemoryManager worldMemoryManager;
    private final DecorationEffectManager decorationEffectManager;
    private final ClashManager clashManager;

    public GameWorld(GameContext context) {
        this.gameClock = context.getGameClock();
        this.levelManager = context.getLevelManager();
        this.effectManager = context.getEffectManager();
        this.rainManager = context.getRainManager();
        this.enemyManager = context.getEnemyManager();
        this.objectManager = context.getObjectManager();
        this.projectileManager = context.getProjectileManager();
        this.spellManager = context.getSpellManager();
        this.lightManager = context.getLightManager();
        this.timeCycleManager = context.getTimeCycleManager();
        this.questManager = context.getQuestManager();
        this.worldMemoryManager = context.getWorldMemoryManager();
        this.decorationEffectManager = context.getDecorationEffectManager();
        this.clashManager = context.getClashManager();

        this.player = new Player(PLAYER_X, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, context);
        this.player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
        this.player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn(LvlTriggerType.SPAWN_B));

        loadStartLevel();
    }

    private void loadStartLevel() {
        this.enemyManager.loadEnemies(levelManager.getCurrentLevel());
        this.objectManager.loadObjects(levelManager.getCurrentLevel());
        reinitializeParticles();
        this.decorationEffectManager.rebuildForCurrentLevel();
    }

    public void reinitializeParticles() {
        LevelMetadata metadata = levelManager.getCurrentLevelMetadata();
        boolean particlesEnabled = metadata == null || metadata.getAmbientParticlesEnabled() == null || metadata.getAmbientParticlesEnabled();
        effectManager.setAmbientEffectsActive(particlesEnabled);

        if (particlesEnabled) {
            int levelWidth = levelManager.getCurrentLevel().getLevelTilesWidth() * TILES_SIZE;
            int levelHeight = levelManager.getCurrentLevel().getLevelTilesHeight() * TILES_SIZE;
            effectManager.reinitializeAmbientParticles(levelWidth, levelHeight);
            // TODO: Temporary
            boolean isStormPeak = metadata != null && "FOREST_STORM_PEAK".equals(metadata.getBackgroundId());
            effectManager.setStormPeakMode(isStormPeak);
        }
    }

    // Core
    public void update() {
        if (gameClock != null) gameClock.tick();
        clashManager.update();
        if (clashManager.isActive()) {
            lightManager.update(timeCycleManager);
            effectManager.update();
            rainManager.update();
            return;
        }
        player.update();
        if (player.getUltimateHandler().isActive()) {
            lightManager.update(timeCycleManager);
            effectManager.update();
            rainManager.update();
            return;
        }
        levelManager.updateGrassInteraction(player);
        enemyManager.update(levelManager.getCurrentLevel().getLvlData(), player, objectManager.getActiveFollower());
        objectManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        projectileManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        timeCycleManager.update();
        lightManager.update(timeCycleManager);
        spellManager.update();
        effectManager.updateTimeOfDay(timeCycleManager);
        decorationEffectManager.update();
        effectManager.update();
        rainManager.update();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset, boolean isDarkPhase) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        renderBackground(xLevelOffset, yLevelOffset, g);

        effectManager.renderAmbientEffects(g, xLevelOffset, yLevelOffset);
        rainManager.render(g);
        levelManager.renderBackgroundLayers(g, xLevelOffset, yLevelOffset);
        objectManager.render(g, xLevelOffset, yLevelOffset);
        levelManager.renderForegroundTerrain(g, xLevelOffset, yLevelOffset);
        if (isDarkPhase) {
            enemyManager.render(g, xLevelOffset, yLevelOffset);
            projectileManager.render(g, xLevelOffset, yLevelOffset);
        }
        lightManager.render(g, xLevelOffset, yLevelOffset);
        effectManager.renderBackgroundEffects(g, xLevelOffset, yLevelOffset);
        if (!isDarkPhase) enemyManager.render(g, xLevelOffset, yLevelOffset);
        player.render(g, xLevelOffset, yLevelOffset);
        objectManager.secondRender(g, xLevelOffset, yLevelOffset);
        lightManager.renderPostProcessing(g);
        effectManager.renderAmbientEffectsAbove(g, xLevelOffset, yLevelOffset);
        decorationEffectManager.render(g, xLevelOffset, yLevelOffset);
        if (!isDarkPhase) projectileManager.render(g, xLevelOffset, yLevelOffset);
        spellManager.render(g, xLevelOffset, yLevelOffset);
        effectManager.renderForegroundEffects(g, xLevelOffset, yLevelOffset);
        player.getUltimateHandler().render(g, xLevelOffset, yLevelOffset);
        clashManager.render(g, xLevelOffset, yLevelOffset);
    }

    private void renderBackground(int xLevelOffset, int yLevelOffset, Graphics g) {
        BufferedImage bg = levelManager.getCurrentBackground();
        if (levelManager.getCurrentLevelMetadata().getTileset().equals("Invisible")) {
            int levelW = levelManager.getCurrentLevel().getLevelTilesWidth() * TILES_SIZE;
            int levelH = levelManager.getCurrentLevel().getLevelTilesHeight() * TILES_SIZE;
            g.drawImage(bg, -xLevelOffset, -yLevelOffset, levelW, levelH, null);
        }
        else {
            double parallaxScale = 1.15;
            int drawW = (int) (GAME_WIDTH * parallaxScale);
            int drawH = (int) (GAME_HEIGHT * parallaxScale);
            int maxX = Math.max(1, levelManager.getCurrentLevel().getXMaxLevelOffset());
            int maxY = Math.max(1, levelManager.getCurrentLevel().getYMaxLevelOffset());

            double progressX = Math.min(1.0, Math.max(0.0, (double) xLevelOffset / maxX));
            double progressY = Math.min(1.0, Math.max(0.0, (double) yLevelOffset / maxY));
            int bgX = (int) -((drawW - GAME_WIDTH) * progressX);
            int bgY = (int) -((drawH - GAME_HEIGHT) * progressY);

            g.drawImage(bg, bgX, bgY, drawW, drawH, null);
        }
    }

    // Reset
    /**
     * Resets transient combat, particle, weather, and lighting state without touching persistent progression.
     */
    public void clearTransientEffects() {
        projectileManager.reset();
        spellManager.reset();
        effectManager.reset();
        lightManager.reset();
        rainManager.reset();
        timeCycleManager.reset();
        decorationEffectManager.reset();
        clashManager.reset();
    }

    public void reset() {
        worldMemoryManager.reset();
        enemyManager.reset();
        player.reset();
        objectManager.reset();
        clearTransientEffects();
        reinitializeParticles();
    }
}