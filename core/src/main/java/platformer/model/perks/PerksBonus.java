package platformer.model.perks;

import lombok.Getter;
import lombok.Setter;

/**
 * Singleton that holds the bonuses provided by the perks.
 * It includes bonuses to attack, health, power, experience, and coins, as well as special abilities.
 */
@Getter
@Setter
public class PerksBonus {

    private static volatile PerksBonus instance = null;

    private int bonusAttack, bonusHealth, bonusPower, bonusExp, bonusCoin, criticalHitChance;
    private double bonusCooldown, dashCooldown;
    private boolean fireball, transform, restorePower, deflect, lavaWalk, dashSlash;

    private PerksBonus() {}

    public static PerksBonus getInstance() {
        if (instance == null) {
            synchronized (PerksBonus.class) {
                if (instance == null) {
                    instance = new PerksBonus();
                }
            }
        }
        return instance;
    }

    public void reset() {
        bonusAttack = 0;
        bonusHealth = 0;
        bonusPower = 0;
        bonusExp = 0;
        bonusCoin = 0;
        criticalHitChance = 0;
        bonusCooldown = 0;
        dashCooldown = 0;
        fireball = false;
        transform = false;
        restorePower = false;
        deflect = false;
        lavaWalk = false;
        dashSlash = false;
    }
}
