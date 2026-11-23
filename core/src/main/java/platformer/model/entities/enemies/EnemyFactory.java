package platformer.model.entities.enemies;

import platformer.model.entities.enemies.boss.Lancer;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.types.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Centralizes instantiation of enemy entities by {@link EnemyType}.
 */
public final class EnemyFactory {

    private static final Map<EnemyType, BiFunction<Integer, Integer, Enemy>> CREATORS = new EnumMap<>(EnemyType.class);

    static {
        CREATORS.put(EnemyType.SKELETON, Skeleton::new);
        CREATORS.put(EnemyType.GHOUL, Ghoul::new);
        CREATORS.put(EnemyType.KNIGHT, Knight::new);
        CREATORS.put(EnemyType.WRAITH, Wraith::new);
        CREATORS.put(EnemyType.CORRUPTED_KNIGHT, CorruptedKnight::new);
        CREATORS.put(EnemyType.LANCER, Lancer::new);
        CREATORS.put(EnemyType.RORIC, Roric::new);
        CREATORS.put(EnemyType.GOBLIN, (x, y) -> new Goblin(x, y, EnemyType.GOBLIN));
        CREATORS.put(EnemyType.GOBLIN_SOLDIER, (x, y) -> new Goblin(x, y, EnemyType.GOBLIN_SOLDIER));
        CREATORS.put(EnemyType.GOBLIN_ASSASSIN, (x, y) -> new Goblin(x, y, EnemyType.GOBLIN_ASSASSIN));
        CREATORS.put(EnemyType.GOBLIN_SPEARMAN, (x, y) -> new Goblin(x, y, EnemyType.GOBLIN_SPEARMAN));
        CREATORS.put(EnemyType.GOBLIN_MAGE, (x, y) -> new Goblin(x, y, EnemyType.GOBLIN_MAGE));
    }

    private EnemyFactory() {}

    /**
     * Instantiates an enemy of the given type at world pixel coordinates (xPos, yPos).
     */
    public static Enemy create(EnemyType type, int xPos, int yPos) {
        BiFunction<Integer, Integer, Enemy> creator = CREATORS.get(type);
        return (creator != null) ? creator.apply(xPos, yPos) : null;
    }
}