package platformer.model.entities.follower;

import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the Breadcrumb system for the follower.
 * Listens to the player's jumps and stores them so the follower can mimic movements when trajectory calculation fails.
 */
public class JumpHistoryTracker {

    private final List<JumpSnapshot> jumpHistory = new ArrayList<>();
    private JumpSnapshot tempJump = null;

    /**
     * Records the player's jumping actions to build a history of successful movements.
     * Records take-off location, speed, dash usage, and high-jump status.
     */
    public void updateTracking(Player player) {
        boolean playerInAir = player.isInAir();

        // A. Just Jumped
        if (playerInAir && tempJump == null && player.getAirSpeed() < 0) {
            double pSpeed = player.getHorizontalSpeed();
            if (Math.abs(pSpeed) > 0.1) {
                tempJump = new JumpSnapshot((int)player.getHitBox().x, (int)player.getHitBox().y, Math.abs(pSpeed), player.getFlipSign(), false, false);
            }
        }

        // B. In Air (update the current recording based on player input)
        if (playerInAir && tempJump != null) {
            if (player.checkAction(PlayerAction.DASH)) {
                tempJump = new JumpSnapshot(tempJump.x(), tempJump.y(), tempJump.speedX(), tempJump.direction(), true, tempJump.isHighJump());
            }

            // Detect High Jump (upward velocity persists longer than a tap jump)
            if (player.getAirSpeed() < -2.0) {
                tempJump = new JumpSnapshot(tempJump.x(), tempJump.y(), tempJump.speedX(), tempJump.direction(), tempJump.usedDash(), true);
            }
        }

        // C. Land (commit to history)
        if (!playerInAir && tempJump != null) {
            jumpHistory.add(tempJump);
            if (jumpHistory.size() > 5) jumpHistory.remove(0);
            tempJump = null;
        }
    }

    /**
     * Finds a recorded player jump that occurred near the NPC's current location and moving in the desired direction.
     */
    public JumpSnapshot getNearestJumpSnapshot(Player player, Rectangle2D.Double hitBox) {
        for (int i = jumpHistory.size() - 1; i >= 0; i--) {
            JumpSnapshot s = jumpHistory.get(i);
            if (s.isRelevant(hitBox)) {
                // Ensure the jump goes in the direction we want to go
                int directionToPlayer = (player.getHitBox().x > hitBox.x) ? 1 : -1;
                if (s.direction() == directionToPlayer) return s;
            }
        }
        return null;
    }
}