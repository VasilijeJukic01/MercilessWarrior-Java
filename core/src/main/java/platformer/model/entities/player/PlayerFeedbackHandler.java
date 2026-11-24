package platformer.model.entities.player;

import platformer.audio.Audio;
import platformer.constants.Constants;
import platformer.core.GameContext;
import platformer.core.config.BalanceManager;
import platformer.event.EventBus;
import platformer.event.events.effects.HitStopEvent;
import platformer.model.effects.EffectManager;
import platformer.model.effects.ScreenEffectsManager;
import platformer.model.effects.particles.DustType;
import platformer.model.inventory.item.ItemRarity;
import platformer.view.Camera;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.scale;

/**
 * Manages all visual, audio, particle, and status-effect feedback related to the Player.
 */
public class PlayerFeedbackHandler {

    private static final Color PARRY_GOLD = new Color(255, 220, 115);
    private static final Color PARRY_CORE_CYAN = new Color(170, 250, 255);

    private final Player player;
    private final GameContext context;
    private final EffectManager effectManager;
    private final ScreenEffectsManager screenEffectsManager;
    private final Random rand = new Random();

    private int runDustTick = 0;
    private int wallSlideDustTick = 0;
    private int mythicAuraTick = 0;
    private int burnTick = 0;
    private int hitFlashTick = 0;
    private int invulnerabilityTick = 0;
    private int parryFlashTick = 0;

    public PlayerFeedbackHandler(Player player, GameContext context) {
        this.player = player;
        this.context = context;
        this.effectManager = context.getEffectManager();
        this.screenEffectsManager = context.getScreenEffectsManager();
    }

    /**
     * Called every frame from the Player's update loop to handle ongoing effects and status ticks.
     */
    public void update() {
        if (hitFlashTick > 0) hitFlashTick--;
        if (invulnerabilityTick > 0) invulnerabilityTick--;
        if (parryFlashTick > 0) parryFlashTick--;
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
        if (player.checkAction(PlayerAction.LAVA) && !player.getStats().getPerks().isLavaWalk()) {
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
                double dustX = (player.getFlipSign() == 1)
                        ? player.getHitBox().x + player.getHitBox().width * 0.25
                        : player.getHitBox().x + player.getHitBox().width * 0.75;
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
     * Triggers hit-stop, camera punch, vignette flash, dual shockwaves, steel-clash sparks, and the blade lens-flare effect when the player successfully parries an attack.
     */
    public void onParrySuccess() {
        this.parryFlashTick = 22;
        EventBus.getInstance().publish(new HitStopEvent(12));

        double clashWorldX = player.getHitBox().getCenterX() + player.getFlipSign() * (14.0 * SCALE);
        double clashWorldY = player.getHitBox().getCenterY() - (4.0 * SCALE);

        if (screenEffectsManager != null) {
            screenEffectsManager.triggerShake(14, 8.5);
            screenEffectsManager.triggerLightningStrobe(PARRY_GOLD, 135, false);

            Camera cam = context.getCamera();
            if (cam != null) {
                double screenX = clashWorldX - cam.getXOffset();
                double screenY = clashWorldY - cam.getYOffset();
                screenEffectsManager.triggerShockwave(screenX, screenY, scale(88), 22, PARRY_GOLD);
                screenEffectsManager.triggerShockwave(screenX, screenY, scale(46), 15, PARRY_CORE_CYAN);
            }
        }

        effectManager.spawnDustParticles(clashWorldX, clashWorldY, 18, DustType.IMPACT_SPARK, player.getFlipSign(), null);
        effectManager.spawnDustParticles(clashWorldX, clashWorldY, 12, DustType.CRITICAL_HIT, -player.getFlipSign(), null);
        effectManager.spawnDamageNumber("PARRY!", clashWorldX, player.getHitBox().y - (6.0 * SCALE), PARRY_GOLD);
    }

    /**
     * Handles all visual, camera, and audio feedback when the player takes damage.
     */
    public void onPlayerDamaged(double damageValue) {
        this.hitFlashTick = 18;
        this.invulnerabilityTick = 125;
        Audio.getInstance().getAudioPlayer().playHitSound();
        EventBus.getInstance().publish(new HitStopEvent(9));

        if (screenEffectsManager != null) {
            double intensity = Math.min(12.0, 6.5 + (damageValue * 0.15));
            screenEffectsManager.triggerShake(16, intensity);
            screenEffectsManager.triggerLightningStrobe(new Color(220, 20, 35), 130, false);
        }

        effectManager.spawnDustParticles(player.getHitBox().getCenterX(), player.getHitBox().getCenterY(), 20 + rand.nextInt(6), DustType.PLAYER_HIT, player.getFlipSign(), player);
        String dmgText = String.format("-%.1f", damageValue);
        effectManager.spawnDamageNumber(dmgText, player.getHitBox().getCenterX(), player.getHitBox().y, Constants.DAMAGE_COLOR);
    }

    public int getBurnTick() {
        return burnTick;
    }

    public int getHitFlashTick() {
        return hitFlashTick;
    }

    public int getInvulnerabilityTick() {
        return invulnerabilityTick;
    }

    public int getParryFlashTick() {
        return parryFlashTick;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTick > 0;
    }

    public void reset() {
        runDustTick = 0;
        wallSlideDustTick = 0;
        mythicAuraTick = 0;
        burnTick = 0;
        hitFlashTick = 0;
        invulnerabilityTick = 0;
        parryFlashTick = 0;
    }
}