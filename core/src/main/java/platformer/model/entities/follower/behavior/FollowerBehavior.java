package platformer.model.entities.follower.behavior;

import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.follower.Follower;
import platformer.model.entities.player.Player;
import java.util.List;

/**
 * Defines the Brain of a follower.
 * This separates decision-making (Targeting, Strategy) from the physical execution.
 */
public interface FollowerBehavior {
    /**
     * Called every frame to determine the follower's intent.
     * The behavior should call {@code host.setMoveTarget(x)} and {@code host.requestAttack()} to control the body.
     *
     * @param host The physical entity (Body)
     * @param player The player to protect
     * @param levelData Collision map
     * @param enemies List of active enemies
     */
    void update(Follower host, Player player, int[][] levelData, List<Enemy> enemies);

    /**
     * Called by the Follower's animation loop when the attack frame is reached.
     * The behavior is responsible for checking hitboxes and applying damage.
     *
     * @param host The physical entity
     * @param enemies List of potential targets
     */
    void onAttackFrame(Follower host, List<Enemy> enemies);
}