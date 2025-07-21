package platformer.model.entities.player;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.perks.PerksBonus;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

/**
 * Handles player actions and object interaction.
 */
public class PlayerActionHandler {

    private final Player player;
    private final EffectManager effectManager;
    private final Rectangle2D.Double hitBox;

    private int dashCount = 0;

    public PlayerActionHandler(Player player, EffectManager effectManager) {
        this.player = player;
        this.effectManager = effectManager;
        this.hitBox = player.getHitBox();
    }

    public boolean canJump(int[][] levelData, boolean left, boolean right, boolean doubleJump) {
        if (isOnWall(hitBox, levelData, Direction.LEFT) && left && !right) return false;
        if (isOnWall(hitBox, levelData, Direction.RIGHT) && right && !left) return false;
        boolean onWall = player.checkAction(PlayerAction.ON_WALL);
        if (player.isInAir() && doubleJump && onWall) return false;
        if (player.isInAir() && player.getCurrentJumps() != 1) return false;
        int tileX = (int)(hitBox.x / TILES_SIZE);
        int tileY = (int)((hitBox.y - 5) / TILES_SIZE);
        boolean onObject = player.checkAction(PlayerAction.ON_OBJECT);
        return !onObject || !isTileSolid(tileX, tileY, levelData);
    }

    public void doDash() {
        if (player.getCooldown()[Cooldown.DASH.ordinal()] != 0) return;
        if (isTouchingWall(hitBox,Direction.LEFT) || isTouchingWall(hitBox,Direction.RIGHT)) return;
        if (dashCount > 0) return;
        boolean dash = player.checkAction(PlayerAction.DASH);
        boolean canDash = player.checkAction(PlayerAction.CAN_DASH);
        if (dash || !canDash) return;

        if (player.getCurrentStamina() >= 3) activateDash();
    }

    private void activateDash() {
        player.addAction(PlayerAction.DASH);
        dashCount++;
        player.addAction(PlayerAction.CAN_DASH);
        player.changeStamina(-3);
        player.getCooldown()[Cooldown.DASH.ordinal()] = PLAYER_DASH_CD + PerksBonus.getInstance().getDashCooldown();
        Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);

        effectManager.spawnDustParticles(player.getHitBox().getCenterX(), player.getHitBox().getCenterY(), DASH_BURST, DustType.DASH, player.getFlipSign(), player);
    }

    public void doSpell() {
        if (player.isInAir()) return;
        if (player.getCurrentStamina() >= 5) {
            player.setSpellState(1);
            Logger.getInstance().notify("Player has used FLAME spell!", Message.INFORMATION);
        }
    }

    public void doFireBall() {
        if (!PerksBonus.getInstance().isFireball()) return;
        if (player.getCooldown()[Cooldown.SPELL.ordinal()] != 0 || player.getCurrentStamina() < 15) return;
        player.addAction(PlayerAction.FIREBALL);
        player.changeStamina(-15);
        Audio.getInstance().getAudioPlayer().playSound(Sound.FIREBALL);
        player.getCooldown()[Cooldown.SPELL.ordinal()] = PLAYER_SPELL_CD;
    }

    public void handleObjectActions(ObjectManager objectManager) {
        handleObjectInteraction(objectManager);
        checkIntersection(objectManager);
    }

    private void handleObjectInteraction(ObjectManager objectManager) {
        objectManager.handleObjectInteraction(hitBox, player);
    }

    private void checkIntersection(ObjectManager objectManager) {
        objectManager.checkPlayerIntersection(player);
    }

    public void setDashCount(int dashCount) {
        this.dashCount = dashCount;
    }
}
