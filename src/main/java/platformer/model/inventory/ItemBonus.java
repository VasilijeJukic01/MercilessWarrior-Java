package platformer.model.inventory;

public enum ItemBonus {
    ARMOR_WARRIOR(2, 0, 0, 0, 0, 3, 0);

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
