package platformer.state.types;

import lombok.Getter;
import lombok.Setter;
import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.controller.GameStateController;
import platformer.core.Account;
import platformer.core.Framework;
import platformer.core.Game;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.EventBus;
import platformer.event.events.*;
import platformer.event.events.effects.ScreenShakeEvent;
import platformer.event.events.lancer.LancerAuraEvent;
import platformer.event.events.lancer.LancerDashSlashEvent;
import platformer.event.events.lancer.LancerTeleportEvent;
import platformer.event.events.roric.RoricCloneEvent;
import platformer.event.events.roric.RoricEffectEvent;
import platformer.event.events.roric.RoricPhaseChangeEvent;
import platformer.event.events.roric.RoricTeleportEvent;
import platformer.event.listeners.QuestSystemListener;
import platformer.model.effects.EffectManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.TimeCycleManager;
import platformer.model.effects.lighting.LightManager;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.projectiles.ProjectileFactory;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.levels.LevelManager;
import platformer.model.minimap.MinimapManager;
import platformer.model.perks.PerksBonus;
import platformer.model.perks.PerksManager;
import platformer.model.quests.QuestManager;
import platformer.model.spells.SpellManager;
import platformer.model.tutorial.TutorialManager;
import platformer.model.world.GameWorld;
import platformer.event.EventHandler;
import platformer.event.handlers.GameFlowEventHandler;
import platformer.event.handlers.LancerEventHandler;
import platformer.event.handlers.RoricEventHandler;
import platformer.model.dialogue.DialogueManager;
import platformer.state.AbstractState;
import platformer.state.State;
import platformer.storage.OfflineStorageStrategy;
import platformer.storage.StorageStrategy;
import platformer.ui.overlays.OverlayManager;
import platformer.ui.overlays.hud.BossInterface;
import platformer.view.Camera;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.isEntityOnExit;

/**
 * State of the game when the player is actively playing the game.
 * In this state, the player can move around the game world, interact with objects, fight enemies, and perform other game actions.
 */
public class GameState extends AbstractState implements State {

    @Getter private GameContext context;

    private GameWorld world;
    @Getter private final Camera camera;
    private final GameStateController gameStateController;
    private final ScreenEffectsManager screenEffectsManager;

    // Events
    @Getter private final List<EventHandler> eventHandlers = new ArrayList<>();

    // Managers
    @Getter private OverlayManager overlayManager;

    // State
    private PlayingState state;
    private boolean isRespawning;
    @Getter @Setter private boolean isDarkPhase;

    @Getter private BossInterface bossInterface;

    public GameState(Game game) {
        super(game);
        this.screenEffectsManager = new ScreenEffectsManager(game);
        init();
        this.gameStateController = new GameStateController(context);
        this.camera = new Camera(getPlayer().getHitBox().x, getPlayer().getHitBox().y);
        this.camera.updateLevelBounds(getLevelManager().getCurrentLevel());
    }

    // Init
    private void init() {
        this.bossInterface = new BossInterface();

        PerksManager perksManager = new PerksManager();
        QuestManager questManager = new QuestManager();
        TutorialManager tutorialManager = new TutorialManager();
        DialogueManager dialogueManager = new DialogueManager();
        LevelManager levelManager = new LevelManager();
        EffectManager effectManager = new EffectManager();
        RainManager rainManager = new RainManager();
        EnemyManager enemyManager = new EnemyManager();
        ObjectManager objectManager = new ObjectManager();
        ProjectileManager projectileManager = new ProjectileManager();
        SpellManager spellManager = new SpellManager();
        LightManager lightManager = new LightManager();
        TimeCycleManager timeCycleManager = new TimeCycleManager();
        MinimapManager minimapManager = new MinimapManager();

        this.context = new GameContext();
        context.setGameState(this);
        context.setLevelManager(levelManager);
        context.setEffectManager(effectManager);
        context.setRainManager(rainManager);
        context.setEnemyManager(enemyManager);
        context.setObjectManager(objectManager);
        context.setProjectileManager(projectileManager);
        context.setSpellManager(spellManager);
        context.setLightManager(lightManager);
        context.setTimeCycleManager(timeCycleManager);
        context.setMinimapManager(minimapManager);
        context.setPerksManager(perksManager);
        context.setQuestManager(questManager);
        context.setTutorialManager(tutorialManager);
        context.setScreenEffectsManager(screenEffectsManager);
        context.setDialogueManager(dialogueManager);

        this.overlayManager = new OverlayManager(this);

        perksManager.wire(context);
        questManager.wire(context);
        tutorialManager.wire(context);
        dialogueManager.wire(context);
        levelManager.wire(context);
        minimapManager.wire(context);
        objectManager.wire(context);
        enemyManager.wire(context);
        projectileManager.wire(context);
        spellManager.wire(context);
        lightManager.wire(context);

        ProjectileFactory.init(projectileManager);
        enemyManager.injectScreenEffectsManager(screenEffectsManager);
        this.world = new GameWorld(context);

        initEventListeners();
        loadFromDatabase();
    }

    /**
     * Creates and registers all event listeners with the EventBus.
     * This is the central location for wiring up the event-driven systems.
     */
    private void initEventListeners() {
        EventBus eventBus = EventBus.getInstance();

        QuestSystemListener questListener = new QuestSystemListener(context.getQuestManager());
        eventBus.register(EnemyDefeatedEvent.class, questListener::onEnemyDefeated);
        eventBus.register(CrateDestroyedEvent.class, questListener::onCrateDestroyed);
        eventBus.register(PerkUnlockedEvent.class, questListener::onPerkUnlocked);
        eventBus.register(ItemPurchasedEvent.class, questListener::onItemPurchased);

        LancerEventHandler lancerHandler = new LancerEventHandler(context);
        eventBus.register(LancerTeleportEvent.class, lancerHandler::onLancerTeleport);
        eventBus.register(LancerAuraEvent.class, lancerHandler::onLancerAura);
        eventBus.register(LancerDashSlashEvent.class, lancerHandler::onLancerDashSlash);
        eventBus.register(ScreenShakeEvent.class, lancerHandler::onScreenShake);

        RoricEventHandler roricHandler = new RoricEventHandler(context);
        eventBus.register(RoricPhaseChangeEvent.class, roricHandler::onPhaseChange);
        eventBus.register(RoricTeleportEvent.class, roricHandler::onRoricTeleport);
        eventBus.register(RoricCloneEvent.class, roricHandler::onRoricClone);
        eventBus.register(RoricEffectEvent.class, roricHandler::onRoricEffect);

        GameFlowEventHandler gameFlowManager = new GameFlowEventHandler(context);
        eventBus.register(FightInitiatedEvent.class, gameFlowManager::onFightInitiated);
        eventBus.register(BossDefeatedEvent.class, gameFlowManager::onBossDefeated);

        this.eventHandlers.add(roricHandler);
        this.eventHandlers.add(gameFlowManager);
    }

    private void loadFromDatabase() {
        context.getPerksManager().loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
    }

    public void reloadSave() {
        StorageStrategy strategy = Framework.getInstance().getStorageStrategy();
        if (strategy instanceof OfflineStorageStrategy) {
            ((OfflineStorageStrategy) strategy).loadLocalData();
        }
        getPlayer().activateMinimap(false);
        PerksBonus.getInstance().reset();
        context.setPerksManager(new PerksManager());
        this.getPlayer().getPlayerDataManager().loadPlayerData();
        this.getPlayer().getInventory().fillItems(Framework.getInstance().getAccount().getItems());
        context.getPerksManager().loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
        context.getLevelManager().loadSavePoint(Framework.getInstance().getAccount().getSpawn());
        this.overlayManager.reset();
        context.getQuestManager().reset();
        reset();
        camera.updateLevelBounds(getLevelManager().getCurrentLevel());
    }

    /**
     * Performs a soft refresh of the player's data after an online transaction.
     */
    public void refreshPlayerDataFromAccount() {
        Account currentAccount = Framework.getInstance().getAccount();
        getPlayer().getPlayerDataManager().refreshFromAccount(currentAccount);
    }

    /**
     * Fully refreshes all player-related data from the master account object.
     * This is called after loading a save to ensure the live player object reflects the new data.
     */
    public void refreshAllFromAccount() {
        if (world == null || world.getPlayer() == null) return;
        getPlayer().getPlayerDataManager().loadPlayerData();
        context.getPerksManager().reset();
        context.getPerksManager().loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
        getPlayer().getInventory().fillItems(Framework.getInstance().getAccount().getItems());
        Logger.getInstance().notify("Player data fully refreshed from loaded save.", Message.INFORMATION);
    }

    private void goToLevel(int dI, int dJ, String message) {
        getPlayer().activateMinimap(false);
        context.getLevelManager().loadNextLevel(dI, dJ);
        String spawn = "";
        if (dI == 0 && dJ == 1) spawn = "LEFT";
        else if (dI == 0 && dJ == -1) spawn = "RIGHT";
        else if (dI == -1 && dJ == 0) spawn = "BOTTOM";
        else if (dI == 1 && dJ == 0) spawn = "UPPER";

        world.levelLoadReset(spawn);
        context.getMinimapManager().changeLevel();
        getPlayer().activateMinimap(true);
        camera.updateLevelBounds(context.getLevelManager().getCurrentLevel());
        overlayManager.reset();
        context.getQuestManager().reset();
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

    private void updateEventHandlers() {
        eventHandlers.forEach(EventHandler::continuousUpdate);
    }

    @Override
    public void update() {
        screenEffectsManager.update();
        checkPlayerDeath();
        if (state != null && state != PlayingState.DIALOGUE && state != PlayingState.DYING) {
            overlayManager.update(state);
        }
        else if (state == PlayingState.DYING) {
            getPlayer().update();
            context.getEffectManager().update();
        }
        else handleGameState();
        if (state == PlayingState.DIALOGUE) overlayManager.update(PlayingState.DIALOGUE);
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        screenEffectsManager.beginFrame(g2d);
        world.render(g2d, camera.getXOffset(), camera.getYOffset(), isDarkPhase);
        screenEffectsManager.renderFlash(g2d);

        getPlayer().getPlayerStatusManager().getUserInterface().render(g);
        bossInterface.render(g);
        overlayManager.render(g);
    }

    @Override
    public void enter() {
        Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);
    }

    @Override
    public void exit() {
        Audio.getInstance().getAudioPlayer().stopSong();
        Audio.getInstance().getAudioPlayer().stopAmbience();
    }

    private void handleGameState() {
        try {
            checkLevelExit();
            updateEventHandlers();
            world.update();
            context.getMinimapManager().update();
            camera.update(getPlayer().getHitBox());
            overlayManager.update(state);
        } catch (Exception ignored) { }
    }

    private void checkLevelExit() {
        if (getPlayer().checkAction(PlayerAction.DASH)) return;
        int exitStatus = isEntityOnExit(context.getLevelManager().getCurrentLevel(), getPlayer().getHitBox());
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
        if (!isRespawning) context.getMinimapManager().reset();
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

    // Facade
    public Player getPlayer() {
        return world.getPlayer();
    }

    public ObjectManager getObjectManager() {
        return world.getObjectManager();
    }

    public LevelManager getLevelManager() {
        return world.getLevelManager();
    }

    // Getters and Setters
    public void setRespawning(boolean respawning) {
        isRespawning = respawning;
    }

    public void setOverlay(PlayingState newOverlay) {
        if (state == PlayingState.PAUSE) {
            Audio.getInstance().getAudioPlayer().unpauseSounds();
            Audio.getInstance().getAudioPlayer().unpauseSong();
            context.getEnemyManager().unpauseRoricTimer();
            eventHandlers.forEach(EventHandler::unpause);
        }
        this.state = newOverlay;
        if (state == PlayingState.PAUSE) {
            Audio.getInstance().getAudioPlayer().pauseSounds();
            Audio.getInstance().getAudioPlayer().pauseSong();
            context.getEnemyManager().pauseRoricTimer();
            eventHandlers.forEach(EventHandler::pause);
        }
    }
}
