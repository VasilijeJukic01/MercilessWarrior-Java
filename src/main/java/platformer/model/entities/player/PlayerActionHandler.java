package platformer.model.entities.player;

import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.gameObjects.ObjectManager;
import platformer.utils.Utils;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.TILES_SIZE;

public class PlayerActionHandler {

    private final Player player;
    private final Rectangle2D.Double hitBox;

    private int dashCount = 0;

    public PlayerActionHandler(Player player) {
        this.player = player;
        this.hitBox = player.getHitBox();
    }

    public boolean canJump(int[][] levelData, boolean left, boolean right, boolean doubleJump) {
        if (Utils.getInstance().isOnWall(hitBox, levelData, Direction.LEFT) && left && !right) return false;
        if (Utils.getInstance().isOnWall(hitBox, levelData, Direction.RIGHT) && right && !left) return false;
        if (player.isInAir() && doubleJump && player.isOnWall()) return false;
        if (player.isInAir() && player.getCurrentJumps() != 1) return false;
        int tileX = (int)(hitBox.x / TILES_SIZE);
        int tileY = (int)((hitBox.y - 5) / TILES_SIZE);
        return !player.isOnObject() || !Utils.getInstance().isTileSolid(tileX, tileY, levelData);
    }

    public void doDash() {
        if (player.getCooldown()[Cooldown.DASH.ordinal()] != 0) return;
        if (Utils.getInstance().isTouchingWall(hitBox,Direction.LEFT) || Utils.getInstance().isTouchingWall(hitBox,Direction.RIGHT)) return;
        if (dashCount > 0) return;
        if (player.isDash() || !player.canDash()) return;
        if (player.getCurrentStamina() >= 3) {
            player.setDash(true);
            dashCount++;
            player.setCanDash(true);
            player.changeStamina(-3);
            player.getCooldown()[Cooldown.DASH.ordinal()] = 1.75 + PlayerBonus.getInstance().getDashCooldown();
            Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);
        }
    }

    public void doSpell() {
        if (player.isInAir()) return;
        if (player.getCurrentStamina() >= 5) {
            player.setSpellState(1);
            Logger.getInstance().notify("Player has used FLAME spell!", Message.INFORMATION);
        }
    }

    public void handleObjectActions(ObjectManager objectManager) {
        handleObjectInteraction(objectManager);
        checkTrapCollide(objectManager);
    }

    private void handleObjectInteraction(ObjectManager objectManager) {
        objectManager.handleObjectInteraction(hitBox, player);
    }

    private void checkTrapCollide(ObjectManager objectManager) {
        objectManager.checkPlayerIntersection(player);
    }

    public void setDashCount(int dashCount) {
        this.dashCount = dashCount;
    }
}