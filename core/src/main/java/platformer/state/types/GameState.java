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
import platformer.core.initializer.GameInitializer;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.events.ui.GamePausedEvent;
import platformer.event.events.ui.GameResumedEvent;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.levels.LevelManager;
import platformer.model.perks.PerksBonus;
import platformer.model.perks.PerksManager;
import platformer.model.world.GameFlowManager;
import platformer.model.world.GameWorld;
import platformer.event.EventHandler;
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

/**
 * The central state for active gameplay, serving as the main hub for all in-game systems and interactions.
 * <p>
 * This class orchestrates the entire gameplay loop.
 * It uses the {@link GameInitializer} to construct and wire the complete object graph of the game world, all held within the {@link GameContext}.
 *
 * @see State
 * @see GameWorld
 * @see GameContext
 * @see GameInitializer
 * @see PlayingState
 * @see OverlayManager
 */
@Getter
public class GameState extends AbstractState implements State {

    private final GameContext context;
    private final GameWorld world;
    private final GameFlowManager flowManager;
    private final Camera camera;
    private final GameStateController stateController;
    private final ScreenEffectsManager screenEffectsManager;

    private final List<EventHandler> eventHandlers = new ArrayList<>();
    private final OverlayManager overlayManager;

    private PlayingState state;
    private final BossInterface bossInterface;

    @Setter private boolean isRespawning;
    @Setter private boolean isDarkPhase;

    public GameState(Game game) {
        super(game);
        this.screenEffectsManager = new ScreenEffectsManager(game);
        this.overlayManager = new OverlayManager(this);
        this.bossInterface = new BossInterface();

        this.context = GameInitializer.initialize(this, screenEffectsManager);

        this.world = new GameWorld(context);
        this.flowManager = new GameFlowManager(context);
        this.stateController = new GameStateController(context);
        this.camera = new Camera(getPlayer().getHitBox().x, getPlayer().getHitBox().y);
        context.setPlayer(world.getPlayer());
        context.setCamera(camera);

        loadFromDatabase();
        camera.updateLevelBounds(getLevelManager().getCurrentLevel());
    }

    private void loadFromDatabase() {
        context.getPerksManager().loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
    }

    @Override
    public void update() {
        screenEffectsManager.update();
        flowManager.update();

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

    private void handleGameState() {
        try {
            eventHandlers.forEach(EventHandler::continuousUpdate);
            world.update();
            context.getMinimapManager().update();
            camera.update(getPlayer().getHitBox());
            overlayManager.update(state);
        } catch (Exception ignored) {}
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

    public void refreshPlayerDataFromAccount() {
        Account currentAccount = Framework.getInstance().getAccount();
        getPlayer().getPlayerDataManager().refreshFromAccount(currentAccount);
    }

    public void refreshAllFromAccount() {
        if (world == null || world.getPlayer() == null) return;
        getPlayer().getPlayerDataManager().loadPlayerData();
        context.getPerksManager().reset();
        context.getPerksManager().loadUnlockedPerks(Framework.getInstance().getAccount().getPerks());
        getPlayer().getInventory().fillItems(Framework.getInstance().getAccount().getItems());
        Logger.getInstance().notify("Player data fully refreshed from loaded save.", Message.INFORMATION);
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
    public void mouseClicked(MouseEvent e) {
        stateController.mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        stateController.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        stateController.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        stateController.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        stateController.mouseDragged(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        stateController.keyPressed(e, state);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        stateController.keyReleased(e, state);
    }

    @Override
    public void windowFocusLost(WindowEvent e) {
        getPlayer().resetDirections();
    }

    // Event
    public void onGamePaused(GamePausedEvent event) {
        eventHandlers.forEach(EventHandler::pause);
    }

    public void onGameResumed(GameResumedEvent event) {
        eventHandlers.forEach(EventHandler::unpause);
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
    public PlayingState getActiveState() {
        if (state == PlayingState.DYING) return null;
        return state;
    }

    public void setOverlay(PlayingState newOverlay) {
        this.state = newOverlay;
    }
}
