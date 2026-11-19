package platformer.model.entities.player;

import lombok.Getter;
import platformer.core.config.BalanceManager;
import platformer.model.inventory.InventoryBonus;
import platformer.model.perks.PerksBonus;

/**
 * Aggregates instance-scoped stat modifiers (perks and equipped gear) for a single Player.
 */
@Getter
public class PlayerStats {

    private final PerksBonus perks = new PerksBonus();
    private final InventoryBonus equipment = new InventoryBonus();

    public double getMaxHealthMultiplier() {
        return 1.0 + (perks.getBonusHealth() / 100.0) + equipment.getHealth();
    }

    public double getMaxStaminaMultiplier() {
        return 1.0 + (perks.getBonusPower() / 100.0) + equipment.getStamina();
    }

    public double getAttackMultiplier() {
        return 1.0 + (perks.getBonusAttack() / 100.0) + equipment.getAttack();
    }

    public double getDefenseReduction() {
        return Math.min(0.75, equipment.getDefense() + perks.getDefenseBonus());
    }

    public double getCritChance() {
        double baseCrit = BalanceManager.getInstance().getPlayerBalance().getBaseCritChance();
        return baseCrit + perks.getCriticalHitChance() + (equipment.getCritical() * 100.0);
    }

    public double getCritMultiplier() {
        double baseMult = BalanceManager.getInstance().getPlayerBalance().getBaseCritMultiplier();
        return baseMult + perks.getCritMultiplierBonus();
    }

    public double getSpellMultiplier() {
        return 1.0 + equipment.getSpell() + (perks.getSpellDamageBonus() / 100.0);
    }

    public double getCooldownMultiplier() {
        return Math.max(0.1, 1.0 - equipment.getCooldown());
    }

    public double getExpMultiplier() {
        return 1.0 + (perks.getBonusExp() / 100.0);
    }

    // Reset
    public void reset() {
        perks.reset();
        equipment.reset();
    }
}