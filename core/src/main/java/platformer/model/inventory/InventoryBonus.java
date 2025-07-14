package platformer.model.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton that holds the cumulative bonuses from all equipped items.
 * Uses a flexible map to handle any type of bonus defined in items.json.
 */
public class InventoryBonus {

    private static volatile InventoryBonus instance = null;
    private final Map<String, Double> bonuses = new HashMap<>();

    private InventoryBonus() {}

    public static InventoryBonus getInstance() {
        if (instance == null) {
            synchronized (InventoryBonus.class) {
                if (instance == null) {
                    instance = new InventoryBonus();
                }
            }
        }
        return instance;
    }

    /**
     * Adds a bonus value to the total.
     * @param bonusName The name of the bonus (e.g., "health", "attack").
     * @param value The value to add (e.g., 5 for 5%).
     */
    public void addBonus(String bonusName, double value) {
        bonuses.put(bonusName, bonuses.getOrDefault(bonusName, 0.0) + value);
    }

    /**
     * Removes a bonus value from the total.
     * @param bonusName The name of the bonus.
     * @param value The value to remove.
     */
    public void removeBonus(String bonusName, double value) {
        bonuses.put(bonusName, bonuses.getOrDefault(bonusName, 0.0) - value);
    }

    /**
     * Gets the total value for a specific bonus.
     * @param bonusName The name of the bonus.
     * @return The total bonus value as a decimal (e.g., 5% is returned as 0.05).
     */
    public double getBonus(String bonusName) {
        return bonuses.getOrDefault(bonusName.toLowerCase(), 0.0) / 100.0;
    }

    public double getHealth() {
        return getBonus("health");
    }

    public double getDefense() {
        return getBonus("defense");
    }

    public double getAttack() {
        return getBonus("attack");
    }

    public double getStamina() {
        return getBonus("stamina");
    }

    public double getCritical() {
        return getBonus("critical");
    }

    public double getSpell() {
        return getBonus("spell");
    }

    public double getCooldown() {
        return getBonus("cooldown");
    }

    public void reset() {
        bonuses.clear();
    }

}
