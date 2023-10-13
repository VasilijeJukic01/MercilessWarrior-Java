package platformer.model.inventory;

public enum ItemBonus {
    HELMET_WARRIOR(     3, 5, 0, 0, 0, 0, 0),
    ARMOR_WARRIOR(      5, 0, 0, 0, 0, 5, 0),
    BRACELETS_WARRIOR(  0, 0, 5, 0, 3, 0, 0),
    TROUSERS_WARRIOR(   0, 3, 0, 5, 0, 0, 0),
    BOOTS_WARRIOR(      0, 0, 0, 3, 0, 0, 3),
    ARMOR_GUARDIAN(     10,0, 5, 0, 0, 0, 0),
    RING_AMETHYST(      0, 0, 0, 8, 0, 8, 5);

    private final double health, defense, attack, stamina, critical, spell, cooldown;

    ItemBonus(double health, double defense, double attack, double stamina, double critical, double spell, double cooldown) {
        this.health = health;
        this.defense = defense;
        this.attack = attack;
        this.stamina = stamina;
        this.critical = critical;
        this.spell = spell;
        this.cooldown = cooldown;
    }

    public double[] getEquipmentBonus() {
        double[] bonuses = new double[7];
        bonuses[0] = health;
        bonuses[1] = defense;
        bonuses[2] = attack;
        bonuses[3] = stamina;
        bonuses[4] = critical;
        bonuses[5] = spell;
        bonuses[6] = cooldown;
        return bonuses;
    }

}
