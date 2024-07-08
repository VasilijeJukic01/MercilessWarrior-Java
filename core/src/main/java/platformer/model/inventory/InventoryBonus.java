package platformer.model.inventory;

/**
 * Class that represents the cumulative bonuses of all items in the inventory.
 * It provides methods to apply and remove bonuses from items.
 */
public class InventoryBonus {

    private static volatile InventoryBonus instance = null;

    private double health, defense, attack, stamina, critical, spell, cooldown;

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
     * Applies the bonuses from an item to the inventory.
     * @param itemBonus the item whose bonuses are to be applied
     */
    public void applyBonus(ItemBonus itemBonus) {
        double[] bonuses = itemBonus.getEquipmentBonus();;
        this.health += bonuses[0];
        this.defense += bonuses[1];
        this.attack += bonuses[2];
        this.stamina += bonuses[3];
        this.critical += bonuses[4];
        this.spell += bonuses[5];
        this.cooldown += bonuses[6];
    }

    /**
     * Removes the bonuses from an item from the inventory.
     * @param itemBonus the item whose bonuses are to be removed
     */
    public void removeBonus(ItemBonus itemBonus) {
        double[] bonuses = itemBonus.getEquipmentBonus();
        this.health -= bonuses[0];
        this.defense -= bonuses[1];
        this.attack -= bonuses[2];
        this.stamina -= bonuses[3];
        this.critical -= bonuses[4];
        this.spell -= bonuses[5];
        this.cooldown -= bonuses[6];
    }

    public double getHealth() {
        return health/100;
    }

    public double getDefense() {
        return defense/100;
    }

    public double getAttack() {
        return attack/100;
    }

    public double getStamina() {
        return stamina/100;
    }

    public double getCritical() {
        return critical/100;
    }

    public double getSpell() {
        return spell/100;
    }

    public double getCooldown() {
        return cooldown/100;
    }

}
