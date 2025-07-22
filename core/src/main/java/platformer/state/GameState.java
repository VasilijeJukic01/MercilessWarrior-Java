package platformer.state;

import platformer.audio.Audio;
import platformer.controller.GameStateController;
import platformer.core.Framework;
import platformer.core.Game;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.effects.EffectManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.lighting.LightManager;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.levels.LevelManager;
import platformer.model.minimap.MinimapManager;
import platformer.model.perks.PerksBonus;
import platformer.model.perks.PerksManager;
import platformer.model.quests.QuestManager;
import platformer.model.spells.SpellManager;
import platformer.model.tutorial.TutorialManager;
import platformer.observer.EventHandler;
import platformer.observer.events.LancerEventHandler;
import platformer.observer.events.RoricEventHandler;
import platformer.ui.dialogue.DialogueManager;
import platformer.ui.overlays.OverlayManager;
import platformer.ui.overlays.hud.BossInterface;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.isEntityOnExit;

/**
 * State of the game when the player is actively playing the game.
 * In this state, the player can move around the game world, interact with objects, fight enemies, and perform other game actions.
 */
public class GameState extends AbstractState implements State {

    private Player player;

    private final GameStateController gameStateController;

    // Events
    private final List<EventHandler> eventHandlers = new ArrayList<>();

    // Managers
    private LevelManager levelManager;
    private ObjectManager objectManager;
    private EnemyManager enemyManager;
    private ProjectileManager projectileManager;
    private SpellManager spellManager;
    private PerksManager perksManager;
    private QuestManager questManager;
    private OverlayManager overlayManager;
    private DialogueManager dialogueManager;
    private LightManager lightManager;
    private MinimapManager minimapManager;
    private TutorialManager tutorialManager;
    private EffectManager effectManager;
    private RainManager rainManager;

    // State
    private PlayingState state;
    private boolean isRespawning;
    private boolean isDarkPhase = false;

    // Camera
    private double cameraX, cameraY;

    private int xLevelOffset;
    private int xMaxLevelOffset;
    private int yLevelOffset;
    private int yMaxLevelOffset;

    private BossInterface bossInterface;

    // Effects
    private int screenFlashAlpha = 0;
    private final int flashFadeSpeed = 25;
    private int screenShakeDuration = 0;
    private double screenShakeIntensity = 0;
    private final Random random = new Random();

    public GameState(Game game) {
        super(game);
        init();
        this.gameStateController = new GameStateController(this);
        this.cameraX = player.getHitBox().x;
        this.cameraY = player.getHitBox().y;
    }

    // Init
    private void init() {
        this.bossInterface = new BossInterface();
        initManagers();
        initEventHandlers();
        initPlayer();
        loadStartLevel();
        loadFromDatabase();
        calculateLevelOffset();
    }

    private void initManagers() {
        this.perksManager = new PerksManager();
        this.levelManager = new LevelManager(this);
        this.effectManager = new EffectManager();
        this.rainManager = new RainManager();
        this.enemyManager = new EnemyManager(this);
        this.objectManager = new ObjectManager(this);
        this.projectileManager = new ProjectileManager(this);
        this.overlayManager = new OverlayManager(this);
        this.spellManager = new SpellManager(this);
        this.dialogueManager = new DialogueManager(this);
        this.lightManager = new LightManager(this);
        this.questManager = new QuestManager(this);
        this.minimapManager = new MinimapManager(this);
        this.tutorialManager = new TutorialManager(this);
    }

    private void initEventHandlers() {
        this.eventHandlers.add(new LancerEventHandler(this));
        this.eventHandlers.add(new RoricEventHandler(this, effectManager));
    }

    private void initPlayer() {
        this.player = new Player(PLAYER_X, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, enemyManager, objectManager, projectileManager, minimapManager, effectManager);
        this.player.loadLvlData(levelManager.getCurrentLevel().getLvlData());
        this.player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn("LEFT"));
    }

    // Save
    private void loadFromDatabase() {
        this.perksManager.loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
    }

    public void reloadSave() {
        player.activateMinimap(false);
        PerksBonus.getInstance().reset();
        this.perksManager = new PerksManager();
        this.player.getPlayerDataManager().loadPlayerData();
        this.player.getInventory().fillItems(Framework.getInstance().getAccount().getItems());
        this.perksManager.loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
        this.levelManager.loadSavePoint(Framework.getInstance().getAccount().getSpawn());
        this.overlayManager.reset();
        this.questManager.reset();
        reset();
        calculateLevelOffset();
    }

    private void calculateLevelOffset() {
        this.xMaxLevelOffset = levelManager.getCurrentLevel().getXMaxLevelOffset();
        this.yMaxLevelOffset = levelManager.getCurrentLevel().getYMaxLevelOffset();
    }

    private void loadStartLevel() {
        this.enemyManager.loadEnemies(levelManager.getCurrentLevel());
        this.objectManager.loadObjects(levelManager.getCurrentLevel());
    }

    // Level Flow
    private void goToLevel(int I, int J, String message) {
        player.activateMinimap(false);
        levelManager.loadNextLevel(I, J);
        String spawn = "";
        if (I == 0 && J == 1) spawn = "LEFT";
        else if (I == 0 && J == -1) spawn = "RIGHT";
        else if (I == -1 && J == 0) spawn = "BOTTOM";
        else if (I == 1 && J == 0) spawn = "UPPER";
        levelLoadReset(spawn);
        Logger.getInstance().notify(message, Message.NOTIFICATION);
    }

    private void goToRightLevel() {
        goToLevel(0, 1, "Right level loaded.");
    }

    private void goToLeftLevel() {
        goToLevel(0, -1, "Left level loaded.");
    }

    private void goToUpperLevel() {
        goToLevel(-1, 0, "Upper level loaded.");
    }

    private void goToBottomLevel() {
        goToLevel(1, 0, "Bottom level loaded.");
    }

    private void levelLoadReset(String spawn) {
        levelReset();
        enemyManager.loadEnemies(levelManager.getCurrentLevel());
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn(spawn));
        calculateLevelOffset();
        overlayManager.reset();
        questManager.reset();
    }

    /**
     * Updates the camera position based on the player's position.
     * The camera smoothly follows the player using linear interpolation (LARP).
     */
    private void updateCamera() {
        float targetX = (float)player.getHitBox().x - (GAME_WIDTH / 2.0f);
        float targetY = (float)player.getHitBox().y - (GAME_HEIGHT / 2.0f);

        cameraX += (targetX - cameraX) * CAMERA_LERP_FACTOR_X;
        cameraY += (targetY - cameraY) * CAMERA_LERP_FACTOR_Y;

        xLevelOffset = (int)cameraX;
        yLevelOffset = (int)cameraY;

        xLevelOffset = Math.max(0, Math.min(xLevelOffset, xMaxLevelOffset));
        yLevelOffset = Math.max(0, Math.min(yLevelOffset, yMaxLevelOffset));
    }

    // Effect
    public void triggerScreenFlash() {
        this.screenFlashAlpha = 200;
    }

    public void triggerScreenShake(int duration, double intensity) {
        if (!game.getSettings().isScreenShake()) return;
        this.screenShakeDuration = duration;
        this.screenShakeIntensity = intensity;
    }

    private void updateEventHandlers() {
        eventHandlers.forEach(EventHandler::continuousUpdate);
    }

    // Core
    @Override
    public void update() {
        updateScreenShake();
        updateFlash();
        rainManager.update();
        checkPlayerDeath();
        if (state == PlayingState.PAUSE)
            overlayManager.update(PlayingState.PAUSE);
        else if (state == PlayingState.SAVE)
            overlayManager.update(PlayingState.SAVE);
        else if (state == PlayingState.GAME_OVER)
            overlayManager.update(PlayingState.GAME_OVER);
        else if (state == PlayingState.DYING) {
            player.update();
            effectManager.update();
        }
        else handleGameState();

        updateEventHandlers();

        if (state == PlayingState.DIALOGUE)
            overlayManager.update(PlayingState.DIALOGUE);
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int shakeOffsetX = 0;
        int shakeOffsetY = 0;

        if (screenShakeDuration > 0) {
            shakeOffsetX = (int) ((random.nextDouble() - 0.5) * screenShakeIntensity);
            shakeOffsetY = (int) ((random.nextDouble() - 0.5) * screenShakeIntensity);
            g2d.translate(shakeOffsetX, shakeOffsetY);
        }

        g.drawImage(levelManager.getCurrentBackground(), 0, 0, null);
        effectManager.renderAmbientEffects(g);
        rainManager.render(g);
        this.levelManager.render(g, xLevelOffset, yLevelOffset);
        this.objectManager.render(g, xLevelOffset, yLevelOffset);
        if (isDarkPhase) {
            enemyManager.render(g, xLevelOffset, yLevelOffset);
            projectileManager.render(g, xLevelOffset, yLevelOffset);
        }
        this.lightManager.render(g, xLevelOffset, yLevelOffset);
        this.effectManager.renderBackgroundEffects(g, xLevelOffset, yLevelOffset);
        if (!isDarkPhase) this.enemyManager.render(g, xLevelOffset, yLevelOffset);
        this.player.render(g, xLevelOffset, yLevelOffset);
        this.objectManager.secondRender(g, xLevelOffset, yLevelOffset);
        if (!isDarkPhase) this.projectileManager.render(g, xLevelOffset, yLevelOffset);
        this.spellManager.render(g, xLevelOffset, yLevelOffset);
        this.effectManager.renderForegroundEffects(g, xLevelOffset, yLevelOffset);

        // TODO: Move this to somewhere else
        if (screenFlashAlpha > 0) {
            g.setColor(new Color(255, 255, 255, screenFlashAlpha));
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        }

        this.player.getPlayerStatusManager().getUserInterface().render(g);
        this.bossInterface.render(g);
        this.overlayManager.render(g);

        if (screenShakeDuration > 0) {
            g2d.translate(-shakeOffsetX, -shakeOffsetY);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        gameStateController.mouseClicked(e);
    }

    private void handleGameState() {
        try {
            checkLevelExit();
            updateManagers();
        }
        catch (Exception ignored) {}
    }

    private void checkLevelExit() {
        if (player.checkAction(PlayerAction.DASH)) return;
        int exitStatus = isEntityOnExit(levelManager.getCurrentLevel(), player.getHitBox());

        if (exitStatus == RIGHT_EXIT) goToRightLevel();
        else if (exitStatus == LEFT_EXIT) goToLeftLevel();
        else if (exitStatus == UPPER_EXIT) goToUpperLevel();
        else if (exitStatus == BOTTOM_EXIT) goToBottomLevel();
    }

    private void checkPlayerDeath() {
        boolean dying = player.checkAction(PlayerAction.DYING);
        boolean gameOver = player.checkAction(PlayerAction.GAME_OVER);
        if (dying) setOverlay(PlayingState.DYING);
        if (gameOver) setOverlay(PlayingState.GAME_OVER);
    }

    private void updateManagers() {
        updateCamera();
        enemyManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        objectManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        projectileManager.update(levelManager.getCurrentLevel().getLvlData(), player);
        lightManager.update();
        spellManager.update();
        minimapManager.update();
        effectManager.update();
        player.update();
        if (state == PlayingState.SHOP) overlayManager.update(PlayingState.SHOP);
        else if (state == PlayingState.BLACKSMITH) overlayManager.update(PlayingState.BLACKSMITH);
        else if (state == PlayingState.INVENTORY) overlayManager.update(PlayingState.INVENTORY);
        else if (state == PlayingState.CRAFTING) overlayManager.update(PlayingState.CRAFTING);
        else if (state == PlayingState.LOOTING) overlayManager.update(PlayingState.LOOTING);
        else if (state == PlayingState.QUEST) overlayManager.update(PlayingState.QUEST);
        else if (state == PlayingState.MINIMAP) overlayManager.update(PlayingState.MINIMAP);
        else if (state == PlayingState.TUTORIAL) overlayManager.update(PlayingState.TUTORIAL);
    }

    private void updateFlash() {
        if (screenFlashAlpha > 0) {
            screenFlashAlpha -= flashFadeSpeed;
            if (screenFlashAlpha < 0) screenFlashAlpha = 0;
        }
    }

    private void updateScreenShake() {
        if (screenShakeDuration > 0) screenShakeDuration--;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        gameStateController.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        gameStateController.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        gameStateController.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        gameStateController.mouseDragged(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        gameStateController.keyPressed(e, state);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        gameStateController.keyReleased(e, state);
    }

    @Override
    public void reset() {
        state = null;
        enemyManager.reset();
        player.reset();
        objectManager.reset();
        projectileManager.reset();
        spellManager.reset();
        overlayManager.reset();
        effectManager.reset();
        lightManager.reset();
        rainManager.reset();
        bossInterface.reset();
        eventHandlers.forEach(EventHandler::reset);
        if (!isRespawning) minimapManager.reset();
        isRespawning = false;
    }

    private void levelReset() {
        state = null;
        enemyManager.reset();
        objectManager.reset();
    }

    @Override
    public void windowFocusLost(WindowEvent e) {
        player.resetDirections();
    }

    // Getters & Setters
    public PlayingState getActiveState() {
        if (state == PlayingState.DYING) return null;
        return state;
    }

    public Player getPlayer() {
        return player;
    }

    public EnemyManager getEnemyManager() {
        return enemyManager;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public SpellManager getSpellManager() {
        return spellManager;
    }

    public PerksManager getPerksManager() {
        return perksManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public MinimapManager getMinimapManager() {
        return minimapManager;
    }

    public OverlayManager getOverlayManager() {
        return overlayManager;
    }

    public DialogueManager getDialogueManager() {
        return dialogueManager;
    }

    public LightManager getLightManager() {
        return lightManager;
    }

    public TutorialManager getTutorialManager() {
        return tutorialManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public RainManager getRainManager() {
        return rainManager;
    }

    public BossInterface getBossInterface() {
        return bossInterface;
    }

    public void setRespawning(boolean respawning) {
        isRespawning = respawning;
    }

    public void setOverlay(PlayingState newOverlay) {
        if (state == PlayingState.PAUSE) Audio.getInstance().getAudioPlayer().unpauseSounds();
        this.state = newOverlay;
        if (state == PlayingState.PAUSE) Audio.getInstance().getAudioPlayer().pauseSounds();
    }

    public boolean isDarkPhase() {
        return isDarkPhase;
    }

    public void setDarkPhase(boolean darkPhase) {
        isDarkPhase = darkPhase;
    }

    public List<EventHandler> getEventHandlers() {
        return eventHandlers;
    }
}
