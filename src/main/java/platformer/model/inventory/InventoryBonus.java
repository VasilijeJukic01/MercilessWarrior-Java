package platformer.model.inventory;

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

    public void applyBonus(ItemBonus itemBonus) {
        double[] bonuses = itemBonus.getEquipmentBonus();;
        this.health += bonuses[0];
        this.attack += bonuses[1];
        this.defense += bonuses[2];
        this.stamina += bonuses[3];
        this.critical += bonuses[4];
        this.spell += bonuses[5];
        this.cooldown += bonuses[6];
    }

    public void removeBonus(ItemBonus itemBonus) {
        double[] bonuses = itemBonus.getEquipmentBonus();;
        this.health -= bonuses[0];
        this.attack -= bonuses[1];
        this.defense -= bonuses[2];
        this.stamina -= bonuses[3];
        this.critical -= bonuses[4];
        this.spell -= bonuses[5];
        this.cooldown -= bonuses[6];
    }

    public double getHealth() {
        return health;
    }

    public double getDefense() {
        return defense;
    }

    public double getAttack() {
        return attack;
    }

    public double getStamina() {
        return stamina;
    }

    public double getCritical() {
        return critical;
    }

    public double getSpell() {
        return spell;
    }

    public double getCooldown() {
        return cooldown;
    }
}
