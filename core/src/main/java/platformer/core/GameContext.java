package platformer.core;

import lombok.Getter;
import lombok.Setter;
import platformer.model.dialogue.DialogueManager;
import platformer.model.effects.EffectManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.lighting.LightManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.levels.LevelManager;
import platformer.model.minimap.MinimapManager;
import platformer.model.perks.PerksManager;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.quests.QuestManager;
import platformer.model.spells.SpellManager;
import platformer.model.tutorial.TutorialManager;
import platformer.service.multiplayer.MultiplayerManager;
import platformer.state.types.GameState;
import platformer.ui.overlays.OverlayManager;
import platformer.view.Camera;

/**
 * A service provider class that holds references to all major game systems and managers.
 * This is used to manage dependencies cleanly via dependency injection, avoiding long constructor parameter lists.
 */
@Getter
@Setter
public final class GameContext {
    private GameState gameState;
    private LevelManager levelManager;
    private EffectManager effectManager;
    private RainManager rainManager;
    private EnemyManager enemyManager;
    private ObjectManager objectManager;
    private ProjectileManager projectileManager;
    private SpellManager spellManager;
    private LightManager lightManager;
    private TimeCycleManager timeCycleManager;
    private MinimapManager minimapManager;
    private PerksManager perksManager;
    private QuestManager questManager;
    private TutorialManager tutorialManager;
    private ScreenEffectsManager screenEffectsManager;
    private DialogueManager dialogueManager;
    private OverlayManager overlayManager;
    private MultiplayerManager multiplayerManager;
    private Camera camera;
    private Player player;
}