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
import platformer.state.types.GameState;

/**
 * Initializes all core game systems and wires them together.
 * This class centralizes dependency injection and setup logic.
 */
public class GameInitializer {

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
        context.setScreenEffectsManager(screenEffectsManager);

        // Dependency Injection
        context.getPerksManager().wire(context);
        context.getQuestManager().wire(context);
        context.getTutorialManager().wire(context);
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

    private static void initEventListeners(GameContext context, GameState gameState) {
        EventBus eventBus = EventBus.getInstance();

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
