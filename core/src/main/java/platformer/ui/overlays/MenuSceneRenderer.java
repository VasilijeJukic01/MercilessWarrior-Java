package platformer.ui.overlays;

import platformer.core.GameContext;
import platformer.model.effects.EffectManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.TimeCycleManager;
import platformer.model.effects.lighting.LightManager;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.levels.Level;
import platformer.model.levels.LevelManager;
import platformer.model.levels.LvlTriggerType;
import platformer.model.minimap.MinimapManager;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.quests.QuestManager;
import platformer.model.spells.SpellManager;
import platformer.model.world.WorldMemoryManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * Renders the main menu background scene by running the 'menu' level through the core engine managers.
 */
public class MenuSceneRenderer {

    private static final String MENU_LEVEL_ID = "menu";

    private final LevelManager levelManager;
    private final ObjectManager objectManager;
    private final EnemyManager enemyManager;
    private final EffectManager effectManager;
    private final RainManager rainManager;
    private final LightManager lightManager;
    private final TimeCycleManager timeCycleManager;
    private final Player player;

    private final Random rand = new Random();
    private int lightningCooldown = 1600;
    private int doubleFlashTimer = -1;

    public MenuSceneRenderer() {
        GameContext menuContext = new GameContext();

        this.levelManager = new LevelManager();
        this.objectManager = new ObjectManager();
        this.enemyManager = new EnemyManager();
        this.effectManager = new EffectManager();
        this.rainManager = new RainManager();
        this.lightManager = new LightManager();
        this.timeCycleManager = new TimeCycleManager();

        ProjectileManager projectileManager = new ProjectileManager();
        SpellManager spellManager = new SpellManager();
        MinimapManager minimapManager = new MinimapManager();
        QuestManager questManager = new QuestManager();
        WorldMemoryManager worldMemoryManager = new WorldMemoryManager();

        menuContext.setLevelManager(levelManager);
        menuContext.setObjectManager(objectManager);
        menuContext.setEnemyManager(enemyManager);
        menuContext.setEffectManager(effectManager);
        menuContext.setRainManager(rainManager);
        menuContext.setLightManager(lightManager);
        menuContext.setTimeCycleManager(timeCycleManager);
        menuContext.setProjectileManager(projectileManager);
        menuContext.setSpellManager(spellManager);
        menuContext.setMinimapManager(minimapManager);
        menuContext.setQuestManager(questManager);
        menuContext.setWorldMemoryManager(worldMemoryManager);

        levelManager.wire(menuContext);
        worldMemoryManager.wire(menuContext);
        questManager.wire(menuContext);
        minimapManager.wire(menuContext);
        objectManager.wire(menuContext);
        enemyManager.wire(menuContext);
        projectileManager.wire(menuContext);
        spellManager.wire(menuContext);
        lightManager.wire(menuContext);

        this.player = new Player(PLAYER_X, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, menuContext);
        menuContext.setPlayer(player);

        initMenuLevel();
    }

    private void initMenuLevel() {
        levelManager.loadLevelById(MENU_LEVEL_ID);
        Level menuLevel = levelManager.getCurrentLevel();

        player.loadLvlData(menuLevel.getLvlData());
        player.setSpawn(menuLevel.getPlayerSpawn(LvlTriggerType.SPAWN_A));
        enemyManager.loadEnemies(menuLevel);
        objectManager.loadObjects(menuLevel);

        effectManager.setAmbientEffectsActive(true);
        effectManager.reinitializeAmbientParticles(GAME_WIDTH, GAME_HEIGHT);
        rainManager.startRaining();
    }

    public void update() {
        Level current = levelManager.getCurrentLevel();
        if (current == null) return;

        player.update();
        objectManager.update(current.getLvlData(), player);
        updateNightStorm();
        lightManager.update(timeCycleManager);
        effectManager.updateTimeOfDay(timeCycleManager);
        effectManager.update();
        rainManager.update();
    }

    private void updateNightStorm() {
        if (doubleFlashTimer > 0) {
            doubleFlashTimer--;
            if (doubleFlashTimer == 0) {
                lightManager.setAlphaWithFilter(40, new Color(180, 215, 255, 65));
                doubleFlashTimer = -1;
            }
        }

        lightningCooldown--;
        if (lightningCooldown <= 0) {
            lightManager.setAlphaWithFilter(75, new Color(170, 205, 255, 45));
            if (rand.nextBoolean()) {
                doubleFlashTimer = 25 + rand.nextInt(20);
            }
            lightningCooldown = 1800 + rand.nextInt(1600);
        }
    }

    public void render(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        BufferedImage bg = levelManager.getCurrentBackground();
        if (bg != null) {
            g.drawImage(bg, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
        }

        effectManager.renderAmbientEffects(g, 0, 0);
        rainManager.render(g);
        levelManager.render(g, 0, 0);
        objectManager.render(g, 0, 0);
        lightManager.render(g, 0, 0);
        effectManager.renderBackgroundEffects(g, 0, 0);
        effectManager.renderAmbientEffectsAbove(g, 0, 0);
        player.render(g, 0, 0);
        objectManager.secondRender(g, 0, 0);
        lightManager.renderPostProcessing(g);
        effectManager.renderForegroundEffects(g, 0, 0);
    }
}