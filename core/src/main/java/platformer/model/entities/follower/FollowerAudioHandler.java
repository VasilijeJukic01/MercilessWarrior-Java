package platformer.model.entities.follower;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.gameObjects.npc.NpcType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Manages combat, vocal, and spell audio for any Follower using a per-NpcType sound profile.
 */
public class FollowerAudioHandler {

    public record FollowerSoundProfile(
            Sound[] attackVocals,
            Sound[] hitVocals,
            Sound[] weaponSlashes,
            Sound spellSound,
            Sound knockdownSound,
            Sound reviveSound
    ) {}

    private static final Map<NpcType, FollowerSoundProfile> PROFILES = new EnumMap<>(NpcType.class);

    static {
        PROFILES.put(NpcType.ANITA, new FollowerSoundProfile(
                new Sound[]{Sound.ANITA_ATTACK_1, Sound.ANITA_ATTACK_2},
                new Sound[]{Sound.ANITA_HIT_1, Sound.ANITA_HIT_2},
                new Sound[]{Sound.ANITA_SLASH_1, Sound.ANITA_SLASH_2},
                Sound.ANITA_WIND_SPELL,
                Sound.ANITA_KNOCKDOWN,
                Sound.ANITA_REVIVE
        ));
    }

    private final FollowerSoundProfile profile;
    private final Random rand = new Random();
    private int hitVocalCooldown = 0;

    public FollowerAudioHandler(NpcType type) {
        this.profile = PROFILES.get(type);
    }

    public void update() {
        if (hitVocalCooldown > 0) hitVocalCooldown--;
    }

    public void playMeleeAttack() {
        if (profile != null && profile.weaponSlashes() != null) {
            playRandom(profile.weaponSlashes());
        }
        if (profile != null && profile.attackVocals() != null && rand.nextDouble() < 0.75) {
            playRandom(profile.attackVocals());
        }
    }

    public void playSpellAttack() {
        if (profile != null && profile.spellSound() != null) {
            Audio.getInstance().getAudioPlayer().playSound(profile.spellSound());
        }
        if (profile != null && profile.attackVocals() != null) {
            playRandom(profile.attackVocals());
        }
    }

    public void playHit() {
        Audio.getInstance().getAudioPlayer().playHitSound();
        if (profile != null && profile.hitVocals() != null && hitVocalCooldown == 0) {
            playRandom(profile.hitVocals());
            hitVocalCooldown = 35;
        }
    }

    public void playKnockdown() {
        Audio.getInstance().getAudioPlayer().playHitSound();
        if (profile != null && profile.knockdownSound() != null) {
            Audio.getInstance().getAudioPlayer().playSound(profile.knockdownSound());
        }
    }

    public void playRevive() {
        if (profile != null && profile.reviveSound() != null) {
            Audio.getInstance().getAudioPlayer().playSound(profile.reviveSound());
        }
    }

    private void playRandom(Sound[] pool) {
        if (pool == null || pool.length == 0) return;
        Audio.getInstance().getAudioPlayer().playSound(pool[rand.nextInt(pool.length)]);
    }
}