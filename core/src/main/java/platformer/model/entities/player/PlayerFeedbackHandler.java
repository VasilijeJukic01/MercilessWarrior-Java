package platformer.model.entities.player;

import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;

import java.util.Random;

import static platformer.constants.Constants.RUN_DUST_BURST;

/**
 * Manages all visual and audio feedback related to the Player's actions.
 * This class decouples presentational logic from the core state management within the Player class.
 */
public class PlayerFeedbackHandler {

    private final Player player;
    private final EffectManager effectManager;

    private int runDustTick = 0;
    private int wallSlideDustTick = 0;

    public PlayerFeedbackHandler(Player player, EffectManager effectManager) {
        this.player = player;
        this.effectManager = effectManager;
    }

    /**
     * Called every frame from the Player's update loop to handle ongoing effects.
     */
    public void update() {
        updateRunningEffects();
        updateWallSlideEffects();
    }

    /**
     * Spawns dust particles periodically when the player is running on the ground.
     */
    private void updateRunningEffects() {
        boolean isMovingOnGround = player.checkAction(PlayerAction.MOVE) && !player.isInAir();
        if (isMovingOnGround) {
            runDustTick++;
            if (runDustTick >= 15) {
                double dustX = (player.getFlipSign() == 1) ? player.getHitBox().x : player.getHitBox().x + player.getHitBox().width;
                effectManager.spawnDustParticles(dustX, player.getHitBox().y + player.getHitBox().height, RUN_DUST_BURST, DustType.RUNNING, player.getFlipSign(), player);
                runDustTick = 0;
            }
        }
        else runDustTick = 0;
    }
    
    /**
     * Spawns dust particles periodically when the player is sliding on a wall.
     */
    private void updateWallSlideEffects() {
        boolean onWall = player.checkAction(PlayerAction.ON_WALL);
        boolean onObject = player.checkAction(PlayerAction.ON_OBJECT);

        if (onWall && !onObject) {
            wallSlideDustTick++;
            if (wallSlideDustTick > 3) {
                wallSlideDustTick = 0;
                double dustX = (player.getFlipSign() == 1) ? player.getHitBox().x + player.getHitBox().width : player.getHitBox().x;
                double dustY = player.getHitBox().y + (new Random().nextDouble() * player.getHitBox().height);
                effectManager.spawnDustParticles(dustX, dustY, 1, DustType.WALL_SLIDE, player.getFlipSign(), player);
            }
        }
        else wallSlideDustTick = 0;
    }
}