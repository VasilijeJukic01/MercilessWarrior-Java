package platformer.model;

import platformer.model.entities.enemies.EnemyType;
import platformer.model.objects.ObjType;
import platformer.model.objects.projectiles.PRType;
import platformer.model.spells.SpellType;

public class ModelUtils {

    private static volatile ModelUtils instance = null;

    public static ModelUtils getInstance() {
        if (instance == null) {
            synchronized (ModelUtils.class) {
                if (instance == null) {
                    instance = new ModelUtils();
                }
            }
        }
        return instance;
    }

    private ModelUtils() {}

    public int getHealth(EnemyType enemyType) {
        return enemyType.getHealth();
    }

    public int getDamage(EnemyType enemyType) {
        return enemyType.getDamage();
    }

    public int getObjectSprite(ObjType object) {
        return object.getSprites();
    }

    public int getProjectileSprite(PRType prType) {
        return prType.getSprites();
    }

    public int getSpellSprite(SpellType spell) {
        return spell.getSprites();
    }

}
