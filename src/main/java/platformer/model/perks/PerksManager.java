package platformer.model.perks;

import platformer.audio.Audio;
import platformer.audio.Sounds;
import platformer.model.entities.PlayerBonus;

import java.util.ArrayList;
import java.util.List;

public class PerksManager {

    private final int SLOT_MAX_COL = 7, SLOT_MAX_ROW = 4;
    private final int[][] placeHolders = {
            {1, 0, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 0, 1},
            {0, 1, 1, 1, 0, 1, 1},
            {0, 1, 0, 1, 0, 0, 1}
    };

    private final ArrayList<Perk> perks;
    private final ArrayList<Integer> unlocked;
    private final int[] startUnlocks = {0, 2, 4, 6};

    public PerksManager() {
        this.perks = new ArrayList<>();
        this.unlocked = new ArrayList<>();
        initPerks();
        initLock();
    }

    // Init
    private void initPerks() {
        perks.add(new Perk(0, "Perk0", "Increase gained XP by 15%", 1, "XP Bonus"));
        perks.add(new Perk(2, "Perk1", "Reduce attack cooldown by 30%", 1, "Strong Arms"));
        perks.add(new Perk(4, "Perk2", "Unlock fire ball spell", 1, "Ancient Notes"));
        perks.add(new Perk(6, "Perk3", "Increase stamina capacity by 20%", 1, "Power Pills"));
        perks.add(new Perk(7, "Perk4", "Get more coins from loot", 1, "Lucky Drop"));
        perks.add(new Perk(9, "Perk5", "Increase sword attack power by 20%", 1, "Broken Bones"));
        perks.add(new Perk(11, "Perk6", "Unlock fire transformation", 2, "Dark Magic"));
        perks.add(new Perk(13, "Perk7", "Increase max health by 5%", 1, "Warrior Heart"));
        perks.add(new Perk(15, "Perk8", "Reduce dash cooldown", 2, "Furious"));
        perks.add(new Perk(16, "Perk9", "Blocking an attack restores stamina", 1, "Necropolis"));
        perks.add(new Perk(17, "Perk10", "Ability to deflect projectiles", 1, "Fast Eye"));
        perks.add(new Perk(19, "Perk11", "Ability to walk in lava", 2, "Amber"));
        perks.add(new Perk(20, "Perk12", "Increase stamina by 5%", 1, "Dragon Fruit"));
        perks.add(new Perk(22, "Perk13", "Increase critical hit chance by 5%", 2, "Elementary Magic"));
        perks.add(new Perk(24, "Perk14", "Dashing damage enemies", 3, "Unbreakable Bones"));
        perks.add(new Perk(27, "Perk15", "Increase max health by 10%", 3, "God's Blood"));
    }

    private void initLock() {
        for (int lock : startUnlocks) {
            for (Perk perk : perks) {
                if (perk.getSlot() == lock) {
                    perk.setLocked(false);
                    unlocked.add(lock);
                }
            }
        }
    }

    private boolean isSafe(int i, int j, int n, int m) {
        return i >= 0 && j >= 0 && i < m && j < n;
    }

    // Upgrade
    public void upgrade(int[][] slots, int n, int m, int slot) {
        ArrayList<Integer> unlocks = new ArrayList<>();
        int I = slot/n, J = slot%n;
        for (Perk perk : perks) {
            if (perk.getSlot() == slot && !perk.isLocked()) {
                if (perk.isUpgraded()) return;
                perk.setUpgraded(true);
                unlock(perk);
                Audio.getInstance().getAudioPlayer().playSound(Sounds.SHOP_BUY.ordinal());
                if (isSafe(I+1, J, n, m) && slots[I+1][J] == 1 && !unlocked.contains(slot+n)) unlocks.add(slot+n);
                if (isSafe(I-1, J, n, m) && slots[I-1][J] == 1 && !unlocked.contains(slot-n)) unlocks.add(slot-n);
                if (isSafe(I, J+1, n, m) && slots[I][J+1] == 1 && !unlocked.contains(slot+1)) unlocks.add(slot+1);
                if (isSafe(I, J-1, n, m) && slots[I][J-1] == 1 && !unlocked.contains(slot-1)) unlocks.add(slot-1);
                break;
            }
        }
        for (Perk perk : perks) {
            if (unlocks.contains(perk.getSlot())) perk.setLocked(false);
        }
    }

    private void unlock(Perk perk) {
        switch (perk.getName()) {
            case "XP Bonus": PlayerBonus.getInstance().setBonusExp(15); break;
            case "Strong Arms": PlayerBonus.getInstance().setBonusCooldown(-0.225); break;
            case "Ancient Notes": PlayerBonus.getInstance().setFireball(true); break;
            case "Power Pills": PlayerBonus.getInstance().setBonusPower(20); break;
            case "Lucky Drop": PlayerBonus.getInstance().setBonusCoin(5); break;
            case "Broken Bones": PlayerBonus.getInstance().setBonusAttack(1); break;
            case "Dark Magic": PlayerBonus.getInstance().setTransform(true); break;
            case "Warrior Heart": PlayerBonus.getInstance().setBonusHealth(5); break;
            case "Furious": PlayerBonus.getInstance().setDashCooldown(-0.75); break;
            case "Necropolis": PlayerBonus.getInstance().setRestorePower(true); break;
            case "Fast Eye": PlayerBonus.getInstance().setDeflect(true); break;
            case "Amber": PlayerBonus.getInstance().setLavaWalk(true); break;
            case "Dragon Fruit": PlayerBonus.getInstance().setBonusPower(26); break;
            case "Elementary Magic": PlayerBonus.getInstance().setCriticalHitChance(5); break;
            case "Unbreakable Bones": PlayerBonus.getInstance().setDashSlash(true); break;
            case "God's Blood": PlayerBonus.getInstance().setBonusHealth(32); break;
            default: break;
        }
    }

    public void loadUnlockedPerks(List<String> p) {
        for (Perk perk : perks) {
            if (p.contains(perk.getName())) {
                upgrade(placeHolders, SLOT_MAX_COL, SLOT_MAX_ROW, perk.getSlot());
            }
        }
    }

    public ArrayList<Perk> getPerks() {
        return perks;
    }

    public int[][] getPlaceHolders() {
        return placeHolders;
    }

    public int getSlotMaxCol() {
        return SLOT_MAX_COL;
    }

    public int getSlotMaxRow() {
        return SLOT_MAX_ROW;
    }
}
