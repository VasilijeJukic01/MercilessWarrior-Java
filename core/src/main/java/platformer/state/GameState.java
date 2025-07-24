package platformer.state;

import platformer.audio.Audio;
import platformer.controller.GameStateController;
import platformer.core.Framework;
import platformer.core.Game;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.effects.EffectManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.TimeCycleManager;
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
import platformer.model.world.GameWorld;
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

    private GameContext context;

    private GameWorld world;
    private final GameStateController gameStateController;

    // Events
    private final List<EventHandler> eventHandlers = new ArrayList<>();

    private PerksManager perksManager;
    private QuestManager questManager;
    private OverlayManager overlayManager;
    private DialogueManager dialogueManager;
    private MinimapManager minimapManager;
    private TutorialManager tutorialManager;

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
        this.cameraX = getPlayer().getHitBox().x;
        this.cameraY = getPlayer().getHitBox().y;
    }

    // Init
    private void init() {
        this.bossInterface = new BossInterface();
        this.perksManager = new PerksManager();
        this.minimapManager = new MinimapManager(this);
        this.questManager = new QuestManager(this);
        this.overlayManager = new OverlayManager(this);
        this.dialogueManager = new DialogueManager(this);
        this.tutorialManager = new TutorialManager(this);

        this.context = new GameContext(
                this,
                new LevelManager(this),
                new EffectManager(),
                new RainManager(),
                new EnemyManager(this),
                new ObjectManager(this),
                new ProjectileManager(this),
                new SpellManager(this),
                new LightManager(this),
                new TimeCycleManager(),
                minimapManager,
                perksManager,
                questManager,
                tutorialManager
        );

        this.world = new GameWorld(context);

        this.getObjectManager().lateInit();
        this.getSpellManager().lateInit();
        this.questManager.registerObservers();

        initEventHandlers();

        this.getObjectManager().loadObjects(this.getLevelManager().getCurrentLevel());
        loadFromDatabase();
        calculateLevelOffset();
    }

    private void initEventHandlers() {
        this.eventHandlers.add(new LancerEventHandler(context));
        this.eventHandlers.add(new RoricEventHandler(context));
    }

    private void loadFromDatabase() {
        this.perksManager.loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
    }

    public void reloadSave() {
        getPlayer().activateMinimap(false);
        PerksBonus.getInstance().reset();
        this.perksManager = new PerksManager();
        this.getPlayer().getPlayerDataManager().loadPlayerData();
        this.getPlayer().getInventory().fillItems(Framework.getInstance().getAccount().getItems());
        this.perksManager.loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
        this.getLevelManager().loadSavePoint(Framework.getInstance().getAccount().getSpawn());
        this.overlayManager.reset();
        this.questManager.reset();
        reset();
        calculateLevelOffset();
    }

    private void calculateLevelOffset() {
        this.xMaxLevelOffset = getLevelManager().getCurrentLevel().getXMaxLevelOffset();
        this.yMaxLevelOffset = getLevelManager().getCurrentLevel().getYMaxLevelOffset();
    }

    private void goToLevel(int dI, int dJ, String message) {
        getPlayer().activateMinimap(false);
        getLevelManager().loadNextLevel(dI, dJ);
        String spawn = "";
        if (dI == 0 && dJ == 1) spawn = "LEFT";
        else if (dI == 0 && dJ == -1) spawn = "RIGHT";
        else if (dI == -1 && dJ == 0) spawn = "BOTTOM";
        else if (dI == 1 && dJ == 0) spawn = "UPPER";

        world.levelLoadReset(spawn);
        minimapManager.changeLevel();
        getPlayer().activateMinimap(true);
        calculateLevelOffset();
        overlayManager.reset();
        questManager.reset();
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

    private void updateCamera() {
        float targetX = (float) getPlayer().getHitBox().x - (GAME_WIDTH / 2.0f);
        float targetY = (float) getPlayer().getHitBox().y - (GAME_HEIGHT / 2.0f);

        cameraX += (targetX - cameraX) * CAMERA_LERP_FACTOR_X;
        cameraY += (targetY - cameraY) * CAMERA_LERP_FACTOR_Y;

        xLevelOffset = (int) cameraX;
        yLevelOffset = (int) cameraY;

        xLevelOffset = Math.max(0, Math.min(xLevelOffset, xMaxLevelOffset));
        yLevelOffset = Math.max(0, Math.min(yLevelOffset, yMaxLevelOffset));
    }

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

    @Override
    public void update() {
        updateScreenShake();
        updateFlash();
        checkPlayerDeath();
        if (state == PlayingState.PAUSE)
            overlayManager.update(PlayingState.PAUSE);
        else if (state == PlayingState.SAVE)
            overlayManager.update(PlayingState.SAVE);
        else if (state == PlayingState.GAME_OVER)
            overlayManager.update(PlayingState.GAME_OVER);
        else if (state == PlayingState.DYING) {
            getPlayer().update();
            getEffectManager().update();
        } else {
            handleGameState();
        }

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

        world.render(g, xLevelOffset, yLevelOffset, isDarkPhase);

        if (screenFlashAlpha > 0) {
            g.setColor(new Color(255, 255, 255, screenFlashAlpha));
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        }

        getPlayer().getPlayerStatusManager().getUserInterface().render(g);
        bossInterface.render(g);
        overlayManager.render(g);

        if (screenShakeDuration > 0) {
            g2d.translate(-shakeOffsetX, -shakeOffsetY);
        }
    }

    private void handleGameState() {
        try {
            checkLevelExit();
            world.update();
            minimapManager.update();
            updateCamera();
            updateEventHandlers();

            if (state == PlayingState.SHOP) overlayManager.update(PlayingState.SHOP);
            else if (state == PlayingState.BLACKSMITH) overlayManager.update(PlayingState.BLACKSMITH);
            else if (state == PlayingState.INVENTORY) overlayManager.update(PlayingState.INVENTORY);
            else if (state == PlayingState.CRAFTING) overlayManager.update(PlayingState.CRAFTING);
            else if (state == PlayingState.LOOTING) overlayManager.update(PlayingState.LOOTING);
            else if (state == PlayingState.QUEST) overlayManager.update(PlayingState.QUEST);
            else if (state == PlayingState.MINIMAP) overlayManager.update(PlayingState.MINIMAP);
            else if (state == PlayingState.TUTORIAL) overlayManager.update(PlayingState.TUTORIAL);
        } catch (Exception ignored) { }
    }

    private void checkLevelExit() {
        if (getPlayer().checkAction(PlayerAction.DASH)) return;
        int exitStatus = isEntityOnExit(getLevelManager().getCurrentLevel(), getPlayer().getHitBox());
        if (exitStatus == RIGHT_EXIT) goToRightLevel();
        else if (exitStatus == LEFT_EXIT) goToLeftLevel();
        else if (exitStatus == UPPER_EXIT) goToUpperLevel();
        else if (exitStatus == BOTTOM_EXIT) goToBottomLevel();
    }

    private void checkPlayerDeath() {
        boolean dying = getPlayer().checkAction(PlayerAction.DYING);
        boolean gameOver = getPlayer().checkAction(PlayerAction.GAME_OVER);
        if (dying) setOverlay(PlayingState.DYING);
        if (gameOver) setOverlay(PlayingState.GAME_OVER);
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
    public void mouseClicked(MouseEvent e) {
        gameStateController.mouseClicked(e);
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
        world.reset();
        overlayManager.reset();
        bossInterface.reset();
        eventHandlers.forEach(EventHandler::reset);
        if (!isRespawning) minimapManager.reset();
        isRespawning = false;
        isDarkPhase = false;
    }

    @Override
    public void windowFocusLost(WindowEvent e) {
        getPlayer().resetDirections();
    }

    public PlayingState getActiveState() {
        if (state == PlayingState.DYING) return null;
        return state;
    }

    public Player getPlayer() {
        return world.getPlayer();
    }

    public EnemyManager getEnemyManager() {
        return world.getEnemyManager();
    }

    public ObjectManager getObjectManager() {
        return world.getObjectManager();
    }

    public LevelManager getLevelManager() {
        return world.getLevelManager();
    }

    public ProjectileManager getProjectileManager() {
        return world.getProjectileManager();
    }

    public SpellManager getSpellManager() {
        return world.getSpellManager();
    }

    public EffectManager getEffectManager() {
        return world.getEffectManager();
    }

    public RainManager getRainManager() {
        return world.getRainManager();
    }

    public LightManager getLightManager() {
        return world.getLightManager();
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

    public TutorialManager getTutorialManager() {
        return tutorialManager;
    }

    public BossInterface getBossInterface() {
        return bossInterface;
    }

    public List<EventHandler> getEventHandlers() {
        return eventHandlers;
    }

    public void setRespawning(boolean respawning) {
        isRespawning = respawning;
    }

    public void setOverlay(PlayingState newOverlay) {
        if (state == PlayingState.PAUSE) {
            Audio.getInstance().getAudioPlayer().unpauseSounds();
            getEnemyManager().unpauseRoricTimer();
            eventHandlers.forEach(EventHandler::unpause);
        }
        this.state = newOverlay;
        if (state == PlayingState.PAUSE) {
            Audio.getInstance().getAudioPlayer().pauseSounds();
            getEnemyManager().pauseRoricTimer();
            eventHandlers.forEach(EventHandler::pause);
        }
    }

    public boolean isDarkPhase() {
        return isDarkPhase;
    }

    public void setDarkPhase(boolean darkPhase) {
        isDarkPhase = darkPhase;
    }
}
