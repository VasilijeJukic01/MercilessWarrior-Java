package platformer.model.gameObjects.projectiles;

import platformer.animation.Animation;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.Lancer;
import platformer.model.entities.player.Player;
import platformer.model.perks.PerksBonus;
import platformer.state.GameState;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.physics.CollisionDetector.isProjectileHitLevel;

/**
 * Manages all projectiles in the game, including their creation, updates, and rendering.
 * It is the central hub for handling the lifecycle of projectiles, their collisions, and interactions with other game entities and the environment.
 */
public class ProjectileManager {

    private final GameState gameState;
    private final List<Projectile> projectiles = new ArrayList<>();

    private BufferedImage arrow, roricArrow, roricAngledArrow;
    private BufferedImage[] fireball, lightningBall, energyBall, celestialOrb;

    public ProjectileManager(GameState gameState) {
        this.gameState = gameState;
        loadImages();
    }

    private void loadImages() {
        this.arrow = Utils.getInstance().importImage(ARROW_IMG, ARROW_WID, ARROW_HEI);
        this.roricArrow = Utils.getInstance().importImage(RORIC_ARROW_IMG, ARROW_WID, ARROW_HEI);
        this.roricAngledArrow = Utils.getInstance().importImage(RORIC_ARROW_IMG, ARROW_WID, ARROW_HEI);
        this.lightningBall = Animation.getInstance().loadLightningBall(LIGHTNING_BALL_1_SHEET);
        this.energyBall = Animation.getInstance().loadLightningBall(LIGHTNING_BALL_2_SHEET);
        this.celestialOrb = Animation.getInstance().loadRoricProjectiles()[2];
        this.fireball = Animation.getInstance().loadFireBall();
    }

    // Core
    public void update(int[][] lvlData, Player player) {
        projectiles.removeIf(projectile -> !projectile.isAlive());;
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive()) {
                projectile.updatePosition(player);
                projectile.updatePosition(player, gameState.getObjectManager(), lvlData);
                if (projectile.getHitBox().intersects(player.getHitBox())) {
                    player.changeHealth(-PLAYER_PROJECTILE_DMG, projectile);
                    projectile.setAlive(false);
                }
                else if (isProjectileHitLevel(lvlData, projectile)) {
                    projectile.setAlive(false);
                }
                else if (projectile instanceof Fireball)
                    gameState.getObjectManager().getObjectBreakHandler().checkProjectileBreak(projectiles);
            }
        }
        gameState.getEnemyManager().checkEnemyProjectileHit(projectiles);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        for (Projectile p : projectiles) {
            switch (p.getPrType()) {
                case ARROW:
                    p.render(g, xLevelOffset, yLevelOffset, arrow);
                    break;
                case RORIC_ARROW:
                    p.render(g, xLevelOffset, yLevelOffset, roricArrow);
                    break;
                case RORIC_ANGLED_ARROW:
                    p.render(g, xLevelOffset, yLevelOffset, roricAngledArrow);
                    break;
                case CELESTIAL_ORB:
                    p.render(g, xLevelOffset, yLevelOffset, celestialOrb);
                    break;
                case FIREBALL:
                    p.render(g, xLevelOffset, yLevelOffset, fireball);
                    break;
                case LIGHTNING_BALL:
                    if (p.getDirection() == Direction.LEFT || p.getDirection() == Direction.RIGHT) {
                        p.render(g, xLevelOffset, yLevelOffset, lightningBall);
                    }
                    else p.render(g, xLevelOffset, yLevelOffset, energyBall);
                    break;
            }
        }
    }

    // Checkers
    /**
     * Checks if a player's attack box intersects with any projectiles.
     * If the player has the deflect perk, the intersecting projectile is destroyed.
     *
     * @param attackBox The player's attack hitbox.
     */
    public void checkProjectileDeflect(Rectangle2D.Double attackBox) {
        if (!PerksBonus.getInstance().isDeflect()) return;
        projectiles.stream()
                .filter(projectile -> projectile.isAlive() && projectile.getHitBox().intersects(attackBox))
                .forEach(projectile -> projectile.setAlive(false));
    }

    // Activators
    /**
     * Spawns a fireball projectile from the player's position.
     *
     * @param player The player who is casting the spell.
     */
    public void activateFireball(Player player) {
        Direction direction = (player.getFlipSign() == 1) ? Direction.LEFT : Direction.RIGHT;
        projectiles.add(new Fireball((int)player.getHitBox().x, (int)player.getHitBox().y, direction));
    }

    /**
     * Spawns a standard arrow from a given position and direction.
     *
     * @param position  The starting position of the arrow.
     * @param direction The direction the arrow will travel.
     */
    public void activateArrow(Point position, Direction direction) {
        projectiles.add(new Arrow(position.x, position.y, direction));
    }

    /**
     * Spawns a straight-firing arrow from an enemy.
     *
     * @param enemy The enemy shooting the arrow.
     */
    public void activateRoricArrow(Enemy enemy) {
        projectiles.add(new RoricArrow((int)enemy.getHitBox().x, (int)enemy.getHitBox().y, enemy.getDirection()));
    }

    /**
     * Internal helper method to spawn angled arrows.
     *
     * @param enemy The enemy shooting the arrow.
     * @param player The player to target.
     * @param isTrap A boolean indicating if the arrow should spawn a trap on impact.
     */
    private void activateAngledArrowInternal(Enemy enemy, Player player, boolean isTrap) {
        double spawnX = enemy.getHitBox().getCenterX();
        double spawnY = enemy.getHitBox().getCenterY();
        double horizontalOffset = 15 * SCALE, verticalOffset = 13 * SCALE;
        if (enemy.getDirection() == Direction.LEFT) {
            spawnX -= horizontalOffset;
            spawnY -= verticalOffset;
        }
        double angle = Math.atan2(player.getHitBox().getCenterY() - spawnY, player.getHitBox().getCenterX() - spawnX);
        projectiles.add(new RoricAngledArrow((int)spawnX, (int)spawnY, angle, isTrap, enemy.getDirection()));
    }

    /**
     * Spawns an angled arrow from Roric that targets the player.
     *
     * @param enemy The enemy shooting the arrow.
     * @param player The player to target.
     */
    public void activateRoricAngledArrow(Enemy enemy, Player player) {
        activateAngledArrowInternal(enemy, player, false);
    }

    /**
     * Spawns an angled arrow from Roric that leaves a trap on impact.
     *
     * @param enemy The enemy shooting the arrow.
     * @param player The player to target.
     */
    public void activateTrapArrow(Enemy enemy, Player player) {
        activateAngledArrowInternal(enemy, player, true);
    }

    /**
     * Spawns a celestial orb for Roric's special attack.
     *
     * @param roric The Roric instance creating the orb.
     * @param angle The angle at which the orb will travel.
     */
    public void activateCelestialOrb(Roric roric, double angle) {
        int spawnX = (int) roric.getHitBox().getCenterX();
        int spawnY = (int) roric.getHitBox().getCenterY();
        projectiles.add(new CelestialOrb(spawnX, spawnY, angle));
    }

    /**
     * Spawns a single lightning ball from Lancer.
     *
     * @param lancer The Lancer instance casting the spell.
     */
    public void activateLightningBall(Lancer lancer) {
        Direction direction = (lancer.getFlipSign() == 1) ? Direction.LEFT : Direction.RIGHT;
        projectiles.add(new LightningBall((int) lancer.getHitBox().x, (int) lancer.getHitBox().y, direction));
    }

    /**
     * Spawns a volley of oscillating lightning balls (Variation 1).
     *
     * @param lancer The Lancer instance casting the spell.
     */
    public void activateMultiLightningBallVariation1(Lancer lancer) {
        double x = lancer.getHitBox().x, y = lancer.getHitBox().y;
        projectiles.add(new LightningBall((int)(x / 1.1), (int)(y * 1.3), Direction.DOWN));
        projectiles.add(new LightningBall((int)x, (int)(y * 1.3), Direction.DEGREE_45));
        projectiles.add(new LightningBall((int)(x * 1.1), (int)(y * 1.2), Direction.DEGREE_30));
        projectiles.add(new LightningBall((int)(x / 1.23), (int)(y * 1.3), Direction.N_DEGREE_45));
        projectiles.add(new LightningBall((int)(x / 1.38), (int)(y * 1.2), Direction.N_DEGREE_30));
    }

    /**
     * Spawns a volley of oscillating lightning balls (Variation 2).
     *
     * @param lancer The Lancer instance casting the spell.
     */
    public void activateMultiLightningBallVariation2(Lancer lancer) {
        double x = lancer.getHitBox().x, y = lancer.getHitBox().y;
        projectiles.add(new LightningBall((int)x, (int)(y * 1.3), Direction.DEGREE_60));
        projectiles.add(new LightningBall((int)(x / 1.15), (int)(y * 1.3), Direction.N_DEGREE_60));
    }

    /**
     * Spawns a lightning ball that tracks the player.
     *
     * @param lancer The Lancer instance casting the spell.
     */
    public void activateTrackingLightningBall(Lancer lancer) {
        projectiles.add(new LightningBall((int)(lancer.getHitBox().x/1.1), (int)(lancer.getHitBox().y*1.3), Direction.TRACK));
    }

    // Reset
    public void reset() {
        projectiles.clear();
    }
}