package platformer.model.entities.follower;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.TILES_SIZE;

public record JumpSnapshot(
        int x,
        int y,
        double speedX,
        int direction,
        boolean usedDash,
        boolean isHighJump
) {

    public boolean isRelevant(Rectangle2D.Double npcBox) {
        return Math.abs(npcBox.x - x) < (TILES_SIZE * 1.5) && Math.abs(npcBox.y - y) < (TILES_SIZE * 1.5);
    }

}
