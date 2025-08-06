package platformer.model.projectiles;

import platformer.core.GameContext;
import platformer.model.entities.player.Player;
import platformer.model.perks.PerksBonus;
import platformer.model.projectiles.types.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.isProjectileHitLevel;

/**
 * Manages all projectiles in the game, including their creation, updates, and rendering.
 * It is the central hub for handling the lifecycle of projectiles, their collisions, and interactions with other game entities and the environment.
 */
public class ProjectileManager {

    private GameContext context;
    private final List<Projectile> projectiles = new ArrayList<>();

    public void wire(GameContext context) {
        this.context = context;
    }

    // Core
    public void update(int[][] lvlData, Player player) {
        projectiles.removeIf(projectile -> !projectile.isAlive());
        for (Projectile projectile : projectiles) {
            if (projectile.isAlive()) {
                projectile.updatePosition(player);
                projectile.updatePosition(player, context.getObjectManager(), lvlData);
                if (projectile.getShapeBounds().intersects(player.getHitBox())) {
                    player.changeHealth(-PLAYER_PROJECTILE_DMG, projectile);
                    projectile.setAlive(false);
                }
                else if (isProjectileHitLevel(lvlData, projectile)) {
                    projectile.setAlive(false);
                }
                else if (projectile instanceof Fireball)
                    context.getObjectManager().getObjectBreakHandler().checkProjectileBreak(projectiles);
            }
        }
        context.getEnemyManager().checkEnemyProjectileHit(projectiles);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        List<Projectile> projectilesSnapshot = new ArrayList<>(projectiles);
        for (Projectile p : projectilesSnapshot) {
            if (p.isAlive()) p.render(g, xLevelOffset, yLevelOffset);
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
                .filter(projectile -> projectile.isAlive() && projectile.getShapeBounds().intersects(attackBox))
                .forEach(projectile -> projectile.setAlive(false));
    }

    // Activators
    public void addProjectile(Projectile p) {
        if (p != null) this.projectiles.add(p);
    }

    public void addProjectiles(Projectile[] projectiles) {
        if (projectiles != null){
            this.projectiles.addAll(Arrays.asList(projectiles));
        }
    }

    // Reset
    public void reset() {
        projectiles.clear();
    }
}