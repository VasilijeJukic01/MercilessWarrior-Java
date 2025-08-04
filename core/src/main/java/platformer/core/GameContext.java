package platformer.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import platformer.model.effects.EffectManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.lighting.LightManager;
import platformer.model.effects.RainManager;
import platformer.model.effects.TimeCycleManager;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.levels.LevelManager;
import platformer.model.minimap.MinimapManager;
import platformer.model.perks.PerksManager;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.quests.QuestManager;
import platformer.model.spells.SpellManager;
import platformer.model.tutorial.TutorialManager;
import platformer.state.types.GameState;

/**
 * A service provider class that holds references to all major game systems and managers.
 * This is used to manage dependencies cleanly via dependency injection, avoiding long constructor parameter lists.
 */
@AllArgsConstructor
@Getter
public final class GameContext {
    private final GameState gameState;
    private final LevelManager levelManager;
    private final EffectManager effectManager;
    private final RainManager rainManager;
    private final EnemyManager enemyManager;
    private final ObjectManager objectManager;
    private final ProjectileManager projectileManager;
    private final SpellManager spellManager;
    private final LightManager lightManager;
    private final TimeCycleManager timeCycleManager;
    private final MinimapManager minimapManager;
    private final PerksManager perksManager;
    private final QuestManager questManager;
    private final TutorialManager tutorialManager;
    private final ScreenEffectsManager screenEffectsManager;
}