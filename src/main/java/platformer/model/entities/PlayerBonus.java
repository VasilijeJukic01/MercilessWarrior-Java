package platformer.model.entities;

public class PlayerBonus {

    public static PlayerBonus instance = null;

    private int bonusAttack, bonusHealth, bonusPower, bonusExp, bonusCoin, criticalHitChance;
    private double bonusCooldown, dashCooldown;
    private boolean fireball, transform, restorePower, deflect, lavaWalk, dashSlash;

    private PlayerBonus() {}

    public static PlayerBonus getInstance() {
        if (instance == null) {
            instance = new PlayerBonus();
        }
        return instance;
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
