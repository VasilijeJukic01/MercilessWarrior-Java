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

    public static void createLightningBall(Lancer lancer, int shotIndex) {
        Direction dir = lancer.getDirection();
        double dirSign = (dir == Direction.LEFT) ? -1.0 : 1.0;
        int handX = (int) (lancer.getHitBox().getCenterX() + dirSign * (20 * SCALE));
        int handY = (int) (lancer.getHitBox().getCenterY() - 2 * SCALE);
        double baseAngle = (dir == Direction.LEFT) ? Math.PI : 0.0;
        double speed = 1.18 * SCALE;

        if (shotIndex == 0) {
            addProjectiles(new Projectile[]{
                    new LightningBall(handX, handY, dir, LightningBall.Mode.SPEAR_HURL_LOW),
                    new LightningBall(handX, handY, baseAngle - dirSign * Math.toRadians(22), speed, 1, false)
            });
        }
        else if (shotIndex == 1) {
            addProjectiles(new Projectile[]{
                    new LightningBall(handX, handY, baseAngle - dirSign * Math.toRadians(10), speed, 1, false),
                    new LightningBall(handX, handY, baseAngle - dirSign * Math.toRadians(26), speed, 1, false)
            });
        }
        else {
            addProjectiles(new Projectile[]{
                    new LightningBall(handX, handY, dir, LightningBall.Mode.SPEAR_HURL_LOW),
                    new LightningBall(handX, handY, baseAngle - dirSign * Math.toRadians(16), speed * 1.05, 1, false),
                    new LightningBall(handX, handY, baseAngle - dirSign * Math.toRadians(30), speed * 1.05, 1, false)
            });
        }
    }

    public static void createTrackingLightningBall(Lancer lancer) {
        int cx = (int) lancer.getHitBox().getCenterX();
        int cy = (int) lancer.getHitBox().getCenterY();
        double speed = 1.35 * SCALE;
        addProjectiles(new Projectile[]{
                new LightningBall(cx, cy, Math.toRadians(55), speed, 2, true),
                new LightningBall(cx, cy, Math.toRadians(90), speed, 2, true),
                new LightningBall(cx, cy, Math.toRadians(125), speed, 2, true)
        });
    }

    public static void createMultiLightningBallVariation1(Lancer lancer) {
        int cx = (int) lancer.getHitBox().getCenterX();
        int cy = (int) lancer.getHitBox().getCenterY();
        double speed = 1.35 * SCALE;
        double[] anglesDeg = {30, 60, 90, 120, 150};
        Projectile[] volley = new Projectile[anglesDeg.length];
        for (int i = 0; i < anglesDeg.length; i++) {
            volley[i] = new LightningBall(cx, cy, Math.toRadians(anglesDeg[i]), speed, 2, true);
        }
        addProjectiles(volley);
    }

    public static void createMultiLightningBallVariation2(Lancer lancer) {
        int cx = (int) lancer.getHitBox().getCenterX();
        int cy = (int) lancer.getHitBox().getCenterY();
        double speed = 1.35 * SCALE;
        double[] anglesDeg = {42, 74, 106, 138};
        Projectile[] volley = new Projectile[anglesDeg.length];
        for (int i = 0; i < anglesDeg.length; i++) {
            volley[i] = new LightningBall(cx, cy, Math.toRadians(anglesDeg[i]), speed, 2, true);
        }
        addProjectiles(volley);
    }

}