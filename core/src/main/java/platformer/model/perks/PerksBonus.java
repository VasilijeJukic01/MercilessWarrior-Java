package platformer.model.perks;

import lombok.Getter;
import lombok.Setter;

/**
 * Singleton that holds the cumulative bonuses and active mechanics unlocked via the Mastery Perk Tree.
 */
@Getter
@Setter
public class PerksBonus {

    private static volatile PerksBonus instance = null;

    private int bonusAttack, bonusHealth, bonusPower, bonusExp, bonusCoin, criticalHitChance;
    private double bonusCooldown, dashCooldown;
    private double critMultiplierBonus, spellDamageBonus, defenseBonus, vampiricHealPct;
    private double flameCostReduction, transformCostReduction;
    private boolean fireball, transform, restorePower, deflect, lavaWalk, dashSlash, piercingFireball;

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
        critMultiplierBonus = 0;
        spellDamageBonus = 0;
        defenseBonus = 0;
        vampiricHealPct = 0;
        flameCostReduction = 0;
        transformCostReduction = 0;
        fireball = false;
        transform = false;
        restorePower = false;
        deflect = false;
        lavaWalk = false;
        dashSlash = false;
        piercingFireball = false;
    }
}