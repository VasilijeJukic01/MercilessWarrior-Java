package platformer.model.entities.enemies.boss.lancer;

import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.audio.types.Sound;

import java.util.Random;

/**
 * Encapsulates all voice, weapon, and spell audio playback for the Lancer boss.
 */
public class LancerAudioHandler {

    private static final Sound[] HAH_SOUNDS = {
            Sound.LANCER_HAH_1, Sound.LANCER_HAH_2, Sound.LANCER_HAH_3, Sound.LANCER_HAH_4
    };
    private static final Sound[] GRUNT_SOUNDS = {
            Sound.LANCER_GRUNT_1, Sound.LANCER_GRUNT_2, Sound.LANCER_GRUNT_3
    };
    private static final Sound[] METAL_SLASH_SOUNDS = {
            Sound.LANCER_METAL_SLASH_1, Sound.LANCER_METAL_SLASH_2
    };
    private static final Sound[] SLASH_SOUNDS = {
            Sound.LANCER_SLASH_1, Sound.LANCER_SLASH_2
    };
    private static final Sound[] CUT_SOUNDS = {
            Sound.LANCER_CUT_1, Sound.LANCER_CUT_2
    };

    private final Random rand = new Random();
    private int gruntCooldown = 0;

    public void update() {
        if (gruntCooldown > 0) gruntCooldown--;
    }

    public void playBossTheme(boolean active) {
        if (active) Audio.getInstance().getAudioPlayer().playSong(Song.BOSS_1);
        else Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);
    }

    public void playHitGrunt() {
        if (gruntCooldown == 0) {
            playRandom(GRUNT_SOUNDS);
            gruntCooldown = 35;
        }
    }

    public void playBattleCry() {
        playRandom(HAH_SOUNDS);
    }

    public void playClassicSwing() {
        playRandom(METAL_SLASH_SOUNDS);
    }

    public void playFollowUpSwing() {
        playRandom(HAH_SOUNDS);
        playRandom(METAL_SLASH_SOUNDS);
    }

    public void playDashSlash() {
        playRandom(HAH_SOUNDS);
        playRandom(SLASH_SOUNDS);
    }

    public void playRapidFlurryStart() {
        playRandom(HAH_SOUNDS);
        Audio.getInstance().getAudioPlayer().playSound(Sound.LANCER_CHARGE);
    }

    public void playRapidCut(int animIndex) {
        Sound cut = CUT_SOUNDS[(animIndex / 2) % CUT_SOUNDS.length];
        Audio.getInstance().getAudioPlayer().playSound(cut);
    }

    public void playCharge() {
        Audio.getInstance().getAudioPlayer().playSound(Sound.LANCER_CHARGE);
    }

    public void playOrbCast() {
        Audio.getInstance().getAudioPlayer().playSound(Sound.LANCER_ORB_LAUNCH);
        playRandom(HAH_SOUNDS);
    }

    public void playOrbVolley() {
        Audio.getInstance().getAudioPlayer().playSound(Sound.LANCER_ORB_LAUNCH);
    }

    public void playThunderSlamImpact() {
        Audio.getInstance().getAudioPlayer().playSound(Sound.LIGHTNING_1);
    }

    public void playTeleport() {
        Audio.getInstance().getAudioPlayer().playSound(Sound.LANCER_TRANSFORMING);
    }

    public void reset() {
        gruntCooldown = 0;
    }

    private void playRandom(Sound[] pool) {
        Audio.getInstance().getAudioPlayer().playSound(pool[rand.nextInt(pool.length)]);
    }
}