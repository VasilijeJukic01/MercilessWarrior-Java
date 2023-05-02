package platformer.model;

import platformer.model.entities.enemies.EnemyType;
import platformer.model.objects.ObjType;
import platformer.model.spells.SpellType;

public class ModelUtils {

    public static ModelUtils instance = null;

    private ModelUtils() {}

    public int getHealth(EnemyType enemyType) {
        switch (enemyType) {
            case SKELETON: return 25;
            case GHOUL: return 40;
            default: return 5;
        }
    }

    public int getDamage(EnemyType enemyType) {
        switch (enemyType) {
            case SKELETON: return 15;
            case GHOUL: return 20;
            default: return 0;
        }
    }

    public int getObjectSprite(ObjType object) {
        switch (object) {
            case HEAL_POTION:
            case STAMINA_POTION:
                return 7;
            case BARREL:
            case BOX:
                return 8;
            case ARROW_LAUNCHER_RIGHT:
            case ARROW_LAUNCHER_LEFT:
                return 16;
            case COIN:
                return 4;
        }
        return 1;
    }

    public int getSpellSprite(SpellType spell) {
        if (spell == SpellType.FLAME_1) return 14;
        return 1;
    }

    public static ModelUtils getInstance() {
        if (instance == null) {
            instance = new ModelUtils();
        }
        return instance;
    }

}
