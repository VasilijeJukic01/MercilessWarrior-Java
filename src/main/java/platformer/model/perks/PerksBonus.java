package platformer.model.perks;

/**
 * Singleton that holds the bonuses provided by the perks.
 * It includes bonuses to attack, health, power, experience, and coins, as well as special abilities.
 */
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
        instance = new PerksBonus();
    }

    public int getBonusAttack() {
        return bonusAttack;
    }

    public void setBonusAttack(int bonusAttack) {
        this.bonusAttack = bonusAttack;
    }

    public int getBonusHealth() {
        return bonusHealth;
    }

    public void setBonusHealth(int bonusHealth) {
        this.bonusHealth = bonusHealth;
    }

    public int getBonusPower() {
        return bonusPower;
    }

    public void setBonusPower(int bonusPower) {
        this.bonusPower = bonusPower;
    }

    public int getBonusExp() {
        return bonusExp;
    }

    public void setBonusExp(int bonusExp) {
        this.bonusExp = bonusExp;
    }

    public int getBonusCoin() {
        return bonusCoin;
    }

    public void setBonusCoin(int bonusCoin) {
        this.bonusCoin = bonusCoin;
    }

    public double getBonusCooldown() {
        return bonusCooldown;
    }

    public void setBonusCooldown(double bonusCooldown) {
        this.bonusCooldown = bonusCooldown;
    }

    public double getDashCooldown() {
        return dashCooldown;
    }

    public void setDashCooldown(double dashCooldown) {
        this.dashCooldown = dashCooldown;
    }

    public int getCriticalHitChance() {
        return criticalHitChance;
    }

    public void setCriticalHitChance(int criticalHitChance) {
        this.criticalHitChance = criticalHitChance;
    }

    public boolean isFireball() {
        return fireball;
    }

    public void setFireball(boolean fireball) {
        this.fireball = fireball;
    }

    public boolean isTransform() {
        return transform;
    }

    public void setTransform(boolean transform) {
        this.transform = transform;
    }

    public boolean isRestorePower() {
        return restorePower;
    }

    public void setRestorePower(boolean restorePower) {
        this.restorePower = restorePower;
    }

    public boolean isDeflect() {
        return deflect;
    }

    public void setDeflect(boolean deflect) {
        this.deflect = deflect;
    }

    public boolean isLavaWalk() {
        return lavaWalk;
    }

    public void setLavaWalk(boolean lavaWalk) {
        this.lavaWalk = lavaWalk;
    }

    public boolean isDashSlash() {
        return dashSlash;
    }

    public void setDashSlash(boolean dashSlash) {
        this.dashSlash = dashSlash;
    }
}
