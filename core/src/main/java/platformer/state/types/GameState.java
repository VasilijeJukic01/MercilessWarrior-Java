package platformer.state.types;

import lombok.Getter;
import lombok.Setter;
import platformer.audio.Audio;
import platformer.controller.GameStateController;
import platformer.core.Account;
import platformer.core.Framework;
import platformer.core.Game;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.effects.EffectManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.ScreenEffectsManager;
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
import platformer.observer.events.GameFlowEventHandler;
import platformer.observer.events.LancerEventHandler;
import platformer.observer.events.RoricEventHandler;
import platformer.model.dialogue.DialogueManager;
import platformer.state.AbstractState;
import platformer.state.State;
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

    private GameContext context;

    private GameWorld world;
    @Getter private final Camera camera;
    private final GameStateController gameStateController;
    private final ScreenEffectsManager screenEffectsManager;

    // Events
    @Getter private final List<EventHandler> eventHandlers = new ArrayList<>();

    // Managers
    @Getter private PerksManager perksManager;
    @Getter private QuestManager questManager;
    @Getter private OverlayManager overlayManager;
    @Getter private DialogueManager dialogueManager;
    @Getter private MinimapManager minimapManager;
    @Getter private TutorialManager tutorialManager;

    // State
    private PlayingState state;
    private boolean isRespawning;
    @Getter @Setter private boolean isDarkPhase;

    @Getter private BossInterface bossInterface;

    public GameState(Game game) {
        super(game);
        this.screenEffectsManager = new ScreenEffectsManager(game);
        init();
        this.gameStateController = new GameStateController(this);
        this.camera = new Camera(getPlayer().getHitBox().x, getPlayer().getHitBox().y);
        this.camera.updateLevelBounds(getLevelManager().getCurrentLevel());
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
                tutorialManager,
                screenEffectsManager
        );

        this.world = new GameWorld(context);

        this.getEnemyManager().injectScreenEffectsManager(screenEffectsManager);
        this.getObjectManager().lateInit();
        this.getSpellManager().lateInit();
        this.questManager.registerObservers();

        initEventHandlers();

        this.getObjectManager().loadObjects(this.getLevelManager().getCurrentLevel());
        loadFromDatabase();
    }

    private void initEventHandlers() {
        GameFlowEventHandler gameFlowManager = new GameFlowEventHandler(context);
        RoricEventHandler roricHandler = new RoricEventHandler(context);
        this.eventHandlers.add(new LancerEventHandler(context));
        this.eventHandlers.add(roricHandler);
        this.eventHandlers.add(gameFlowManager);

        dialogueManager.addSubscriber(gameFlowManager);
        roricHandler.addSubscriber(gameFlowManager);
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
        perksManager.reset();
        perksManager.loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
        getPlayer().getInventory().fillItems(Framework.getInstance().getAccount().getItems());
        Logger.getInstance().notify("Player data fully refreshed from loaded save.", Message.INFORMATION);
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
        camera.updateLevelBounds(getLevelManager().getCurrentLevel());
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
            getEffectManager().update();
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

    private void handleGameState() {
        try {
            checkLevelExit();
            updateEventHandlers();
            world.update();
            minimapManager.update();
            camera.update(getPlayer().getHitBox());
            overlayManager.update(state);
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

    // Facade
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

    // Getters and Setters
    public void setRespawning(boolean respawning) {
        isRespawning = respawning;
    }

    public void setOverlay(PlayingState newOverlay) {
        if (state == PlayingState.PAUSE) {
            Audio.getInstance().getAudioPlayer().unpauseSounds();
            Audio.getInstance().getAudioPlayer().unpauseSong();
            getEnemyManager().unpauseRoricTimer();
            eventHandlers.forEach(EventHandler::unpause);
        }
        this.state = newOverlay;
        if (state == PlayingState.PAUSE) {
            Audio.getInstance().getAudioPlayer().pauseSounds();
            Audio.getInstance().getAudioPlayer().pauseSong();
            getEnemyManager().pauseRoricTimer();
            eventHandlers.forEach(EventHandler::pause);
        }
    }
}
