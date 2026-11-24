package platformer.model.combat;

import platformer.animation.Anim;
import platformer.audio.types.Sound;

import java.awt.*;

/**
 * Immutable configuration describing an interactive aerial clash minigame for any boss or elite enemy.
 * Anchor coordinates are normalized [0.0..1.0] within the room's detected open arena bounds.
 */
public record ClashConfig(
        double[][] normalizedAnchors,
        int[] windowDurations,
        double chipDamage,
        double perfectChipDamage,
        double finisherBaseDamage,
        double finisherPerPerfectBonus,
        double missDamage,
        int stunRewardTicks,
        int cooldownTicks,
        Color primaryColor,
        Color accentColor,
        Anim bossZipAnim,
        int bossZipFrame,
        Anim bossClashAnim,
        int bossClashFrame,
        Anim bossRecoilAnim,
        int bossRecoilFrame,
        Sound clashSound,
        Sound finishSound
) {
}