package platformer.core.initializer;

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
import platformer.event.events.ui.GamePausedEvent;
import platformer.event.events.ui.GameResumedEvent;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.event.handlers.GameFlowEventHandler;
import platformer.event.handlers.LancerEventHandler;
import platformer.event.handlers.RoricEventHandler;
import platformer.event.listeners.QuestSystemListener;
import platformer.model.dialogue.DialogueManager;
import platformer.model.effects.EffectManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.TimeCycleManager;
import platformer.model.effects.lighting.LightManager;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.levels.LevelManager;
import platformer.model.minimap.MinimapManager;
import platformer.model.perks.PerksManager;
import platformer.model.projectiles.ProjectileFactory;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.quests.QuestManager;
import platformer.model.spells.SpellManager;
import platformer.model.tutorial.TutorialManager;
import platformer.service.multiplayer.MultiplayerManager;
import platformer.state.types.GameState;

/**
 * Handles the creation and wiring of all core game systems.
 * <p>
 * Its primary responsibility is to instantiate all game managers, inject their dependencies, and register all necessary event listeners with {@link EventBus}.
 * The final result is a fully configured {@link GameContext} object that contains the entire operational object graph for the game world.
 *
 * @see GameState
 * @see GameContext
 */
public class GameInitializer {

    /**
     * Creates and configures all major game managers, wires their dependencies, and registers global event listeners.
     * This is the main entry point for building the game's runtime environment.
     *
     * @param gameState The main game state instance, providing a link back for context-aware systems.
     * @param screenEffectsManager The screen effects manager, which is created early and needs to be passed in.
     * @return A fully initialized {@link GameContext} object containing references to all created game systems.
     */
    public static GameContext initialize(GameState gameState, ScreenEffectsManager screenEffectsManager) {
        Logger.getInstance().notify("Initializing game systems...", Message.INFORMATION);
        GameContext context = new GameContext();
        context.setGameState(gameState);

        // Context Setup
        context.setPerksManager(new PerksManager());
        context.setQuestManager(new QuestManager());
        context.setTutorialManager(new TutorialManager());
        context.setDialogueManager(new DialogueManager());
        context.setLevelManager(new LevelManager());
        context.setEffectManager(new EffectManager());
        context.setRainManager(new RainManager());
        context.setEnemyManager(new EnemyManager());
        context.setObjectManager(new ObjectManager());
        context.setProjectileManager(new ProjectileManager());
        context.setSpellManager(new SpellManager());
        context.setLightManager(new LightManager());
        context.setTimeCycleManager(new TimeCycleManager());
        context.setMinimapManager(new MinimapManager());
        context.setOverlayManager(gameState.getOverlayManager());
        context.setMultiplayerManager(new MultiplayerManager());
        context.setCamera(gameState.getCamera());
        context.setScreenEffectsManager(screenEffectsManager);

        // Dependency Injection
        context.getPerksManager().wire(context);
        context.getQuestManager().wire(context);
        context.getDialogueManager().wire(context);
        context.getLevelManager().wire(context);
        context.getMinimapManager().wire(context);
        context.getObjectManager().wire(context);
        context.getEnemyManager().wire(context);
        context.getProjectileManager().wire(context);
        context.getSpellManager().wire(context);
        context.getLightManager().wire(context);

        ProjectileFactory.init(context.getProjectileManager());
        context.getEnemyManager().injectScreenEffectsManager(screenEffectsManager);
        initEventListeners(context, gameState);

        Logger.getInstance().notify("Game systems initialized successfully.", Message.INFORMATION);
        return context;
    }

    /**
     * Sets up the global event handling by registering various listeners and handlers with the central {@link EventBus}.
     * This decouples game systems, allowing them to communicate without direct references.
     *
     * @param context The {@link GameContext} containing the manager instances that need to listen to events.
     * @param gameState The {@link GameState} which holds the list of continuous update handlers.
     */
    private static void initEventListeners(GameContext context, GameState gameState) {
        EventBus eventBus = EventBus.getInstance();

        // UI & Overlay Listeners
        eventBus.register(OverlayChangeEvent.class, context.getOverlayManager()::onOverlayChange);

        // Pause & Resume Listeners
        eventBus.register(GamePausedEvent.class, context.getEnemyManager()::onGamePaused);
        eventBus.register(GameResumedEvent.class, context.getEnemyManager()::onGameResumed);
        eventBus.register(GamePausedEvent.class, gameState::onGamePaused);
        eventBus.register(GameResumedEvent.class, gameState::onGameResumed);

        // Quest Listeners
        QuestSystemListener questListener = new QuestSystemListener(context.getQuestManager());
        eventBus.register(EnemyDefeatedEvent.class, questListener::onEnemyDefeated);
        eventBus.register(CrateDestroyedEvent.class, questListener::onCrateDestroyed);
        eventBus.register(PerkUnlockedEvent.class, questListener::onPerkUnlocked);
        eventBus.register(ItemPurchasedEvent.class, questListener::onItemPurchased);

        // Lancer Event Handler
        LancerEventHandler lancerHandler = new LancerEventHandler(context);
        eventBus.register(LancerTeleportEvent.class, lancerHandler::onLancerTeleport);
        eventBus.register(LancerAuraEvent.class, lancerHandler::onLancerAura);
        eventBus.register(LancerDashSlashEvent.class, lancerHandler::onLancerDashSlash);
        eventBus.register(ScreenShakeEvent.class, lancerHandler::onScreenShake);

        // Roric Event Handler
        RoricEventHandler roricHandler = new RoricEventHandler(context);
        eventBus.register(RoricPhaseChangeEvent.class, roricHandler::onPhaseChange);
        eventBus.register(RoricTeleportEvent.class, roricHandler::onRoricTeleport);
        eventBus.register(RoricCloneEvent.class, roricHandler::onRoricClone);
        eventBus.register(RoricEffectEvent.class, roricHandler::onRoricEffect);

        // Game Flow Event Handler
        GameFlowEventHandler gameFlowManager = new GameFlowEventHandler(context);
        eventBus.register(FightInitiatedEvent.class, gameFlowManager::onFightInitiated);
        eventBus.register(BossDefeatedEvent.class, gameFlowManager::onBossDefeated);

        // Register handlers for continuous updates
        gameState.getEventHandlers().add(roricHandler);
        gameState.getEventHandlers().add(gameFlowManager);
    }
}
