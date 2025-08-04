package platformer.model.projectiles;

import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.boss.Lancer;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.types.*;

import java.awt.*;

import static platformer.constants.Constants.SCALE;

/**
 * A factory for creating Projectile instances.
 * This class centralizes the creation logic for all projectiles in the game, abstracting the instantiation details away from the callers.
 */
public final class ProjectileFactory {

    private static ProjectileManager manager;

    private ProjectileFactory() {}

    /**
     * Initializes the factory with the active ProjectileManager instance.
     * This MUST be called once when the game state is created.
     *
     * @param projectileManager The running ProjectileManager.
     */
    public static void init(ProjectileManager projectileManager) {
        manager = projectileManager;
    }

    private static void addProjectile(Projectile p) {
        if (manager != null && p != null) {
            manager.addProjectile(p);
        }
    }

    private static void addProjectiles(Projectile[] projectiles) {
        if (manager != null && projectiles != null) {
            manager.addProjectiles(projectiles);
        }
    }

    public static void createFireball(Player player) {
        Direction direction = (player.getFlipSign() == 1) ? Direction.LEFT : Direction.RIGHT;
        addProjectile(new Fireball((int)player.getHitBox().x, (int)player.getHitBox().y, direction));
    }

    public static void createArrow(Point position, Direction direction) {
        addProjectile(new Arrow(position.x, position.y, direction));
    }

    public static void createRoricArrow(Enemy enemy, double speedMultiplier) {
        addProjectile(new RoricArrow((int)enemy.getHitBox().x, (int)enemy.getHitBox().y, enemy.getDirection(), speedMultiplier));
    }

    public static void  createRoricAngledArrow(Enemy enemy, Player player, boolean isTrap) {
        double spawnX = enemy.getHitBox().getCenterX();
        double spawnY = enemy.getHitBox().getCenterY();
        double horizontalOffset = 15 * SCALE, verticalOffset = 13 * SCALE;
        if (enemy.getDirection() == Direction.LEFT) {
            spawnX -= horizontalOffset;
            spawnY -= verticalOffset;
        }
        double angle = Math.atan2(player.getHitBox().getCenterY() - spawnY, player.getHitBox().getCenterX() - spawnX);
        addProjectile(new RoricAngledArrow((int)spawnX, (int)spawnY, angle, isTrap, enemy.getDirection()));
    }

    public static void createCelestialOrb(Roric roric, double angle) {
        int spawnX = (int) roric.getHitBox().getCenterX();
        int spawnY = (int) roric.getHitBox().getCenterY();
        addProjectile(new CelestialOrb(spawnX, spawnY, angle));
    }

    public static void createLightningBall(Lancer lancer) {
        Direction direction = (lancer.getFlipSign() == 1) ? Direction.LEFT : Direction.RIGHT;
        addProjectile(new LightningBall((int) lancer.getHitBox().x, (int) lancer.getHitBox().y, direction));
    }

    public static void createTrackingLightningBall(Lancer lancer) {
        addProjectile(new LightningBall((int)(lancer.getHitBox().x/1.1), (int)(lancer.getHitBox().y*1.3), Direction.TRACK));
    }

    public static void createMultiLightningBallVariation1(Lancer lancer) {
        double x = lancer.getHitBox().x, y = lancer.getHitBox().y;
        addProjectiles(new Projectile[]{
                new LightningBall((int)(x / 1.1), (int)(y * 1.3), Direction.DOWN),
                new LightningBall((int)x, (int)(y * 1.3), Direction.DEGREE_45),
                new LightningBall((int)(x * 1.1), (int)(y * 1.2), Direction.DEGREE_30),
                new LightningBall((int)(x / 1.23), (int)(y * 1.3), Direction.N_DEGREE_45),
                new LightningBall((int)(x / 1.38), (int)(y * 1.2), Direction.N_DEGREE_30)
        });
    }

    public static void createMultiLightningBallVariation2(Lancer lancer) {
        double x = lancer.getHitBox().x, y = lancer.getHitBox().y;
        addProjectiles(new Projectile[]{
                new LightningBall((int)x, (int)(y * 1.3), Direction.DEGREE_60),
                new LightningBall((int)(x / 1.15), (int)(y * 1.3), Direction.N_DEGREE_60)
        });
    }

}