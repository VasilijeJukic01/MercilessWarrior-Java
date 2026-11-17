package platformer.model.entities.player;

import platformer.constants.Constants;
import platformer.core.config.BalanceManager;
import platformer.event.EventBus;
import platformer.event.events.effects.HitStopEvent;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.inventory.item.ItemRarity;
import platformer.model.perks.PerksBonus;

import java.util.Arrays;
import java.util.Random;

import static platformer.constants.Constants.RUN_DUST_BURST;

/**
 * Manages all visual, particle, and status-effect feedback related to the Player.
 */
public class PlayerFeedbackHandler {

    private final Player player;
    private final EffectManager effectManager;
    private final Random rand = new Random();

    private int runDustTick = 0;
    private int wallSlideDustTick = 0;
    private int mythicAuraTick = 0;
    private int burnTick = 0;

    public PlayerFeedbackHandler(Player player, EffectManager effectManager) {
        this.player = player;
        this.effectManager = effectManager;
    }

    /**
     * Called every frame from the Player's update loop to handle ongoing effects and status ticks.
     */
    public void update() {
        updateMythicAura();
        updateBurnStatus();
        updateRunningEffects();
        updateWallSlideEffects();
    }

    private void updateMythicAura() {
        boolean hasMythic = Arrays.stream(player.getInventory().getEquipped())
                .anyMatch(item -> item != null && item.getData() != null && item.getData().rarity == ItemRarity.MYTHIC);

        if (!hasMythic) return;

        mythicAuraTick++;
        if (mythicAuraTick >= 5) {
            mythicAuraTick = 0;
            double x = player.getHitBox().getCenterX();
            double y = player.getHitBox().getCenterY();
            effectManager.spawnDustParticles(x, y, 1, DustType.THUNDERBOLT_AURA, player.getFlipSign(), player);
        }
    }

    private void updateBurnStatus() {
        if (player.checkAction(PlayerAction.LAVA) && !PerksBonus.getInstance().isLavaWalk()) {
            burnTick = 90;
            player.applyStatusDamage(-BalanceManager.getInstance().getCombat().getLavaTickDamage());
        }
        else if (burnTick > 0) {
            burnTick--;
            if (burnTick % 25 == 0) {
                player.applyStatusDamage(-BalanceManager.getInstance().getCombat().getLavaTickDamage() * 2.0);
            }
        }

        if (burnTick > 0 && burnTick % 4 == 0) {
            effectManager.spawnDustParticles(
                    player.getHitBox().getCenterX(),
                    player.getHitBox().getCenterY(),
                    2,
                    DustType.PLAYER_BURN,
                    player.getFlipSign(),
                    player
            );
        }
    }

    /**
     * Spawns dust particles periodically when the player is running on the ground.
     */
    private void updateRunningEffects() {
        boolean isMovingOnGround = player.checkAction(PlayerAction.MOVE) && !player.isInAir();
        if (isMovingOnGround) {
            runDustTick++;
            if (runDustTick >= 11) {
                double dustX = (player.getFlipSign() == 1) ? player.getHitBox().x + player.getHitBox().width * 0.25 : player.getHitBox().x + player.getHitBox().width * 0.75;
                effectManager.spawnDustParticles(dustX, player.getHitBox().y + player.getHitBox().height, 2, DustType.RUNNING, player.getFlipSign(), player);
                runDustTick = 0;
            }
        }
        else {
            runDustTick = 0;
        }
    }

    /**
     * Spawns friction smoke and sparks periodically when the player is sliding on a wall.
     */
    private void updateWallSlideEffects() {
        boolean onWall = player.checkAction(PlayerAction.ON_WALL);
        boolean onObject = player.checkAction(PlayerAction.ON_OBJECT);

        if (onWall && !onObject) {
            wallSlideDustTick++;
            if (wallSlideDustTick > 3) {
                wallSlideDustTick = 0;
                double dustX = (player.getFlipSign() == 1) ? player.getHitBox().x + player.getHitBox().width : player.getHitBox().x;
                double dustY = player.getHitBox().y + (rand.nextDouble() * player.getHitBox().height);
                effectManager.spawnDustParticles(dustX, dustY, 1, DustType.WALL_SLIDE, player.getFlipSign(), player);
            }
        }
        else {
            wallSlideDustTick = 0;
        }
    }

    /**
     * Handles all visual and audio feedback when the player takes damage.
     */
    public void onPlayerDamaged(double damageValue) {
        EventBus.getInstance().publish(new HitStopEvent(8));
        effectManager.spawnDustParticles(player.getHitBox().getCenterX(), player.getHitBox().getCenterY(), 15 + rand.nextInt(6), DustType.PLAYER_HIT, player.getFlipSign(), player);
        String dmgText = String.format("%.1f", damageValue);
        effectManager.spawnDamageNumber(dmgText, player.getHitBox().getCenterX(), player.getHitBox().y, Constants.DAMAGE_COLOR);
    }

    public int getBurnTick() {
        return burnTick;
    }

    public void reset() {
        runDustTick = 0;
        wallSlideDustTick = 0;
        mythicAuraTick = 0;
        burnTick = 0;
    }
}