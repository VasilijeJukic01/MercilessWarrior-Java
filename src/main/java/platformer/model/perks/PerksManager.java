package platformer.model.perks;

import platformer.model.entities.player.PlayerBonus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static platformer.constants.Constants.*;

public class PerksManager {

    private interface PerkAction {
        void performAction();
    }

    private final int[][] placeHolders = {
            {1, 0, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 0, 1},
            {0, 1, 1, 1, 0, 1, 1},
            {0, 1, 0, 1, 0, 0, 1}
    };

    private final int[] startUnlocks = {0, 2, 4, 6};

    private final ArrayList<Perk> perks;
    private final ArrayList<Integer> unlocked;

    private final Map<String, PerkAction> perkActions;

    public PerksManager() {
        this.perks = new ArrayList<>();
        this.unlocked = new ArrayList<>();
        this.perkActions = initPerkActions();
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

    private Map<String, PerkAction> initPerkActions() {
        Map<String, PerkAction> actions = new HashMap<>();
        actions.put("XP Bonus", () -> PlayerBonus.getInstance().setBonusExp(XP_BONUS_AMOUNT));
        actions.put("Strong Arms", () -> PlayerBonus.getInstance().setBonusCooldown(STRONG_ARMS_BONUS_COOLDOWN));
        actions.put("Ancient Notes", () -> PlayerBonus.getInstance().setFireball(true));
        actions.put("Power Pills", () -> PlayerBonus.getInstance().setBonusPower(POWER_PILL_BONUS_POWER));
        actions.put("Lucky Drop", () -> PlayerBonus.getInstance().setBonusCoin(LUCKY_DROP_BONUS_COINS));
        actions.put("Broken Bones", () -> PlayerBonus.getInstance().setBonusCoin(BROKEN_BONES_BONUS_ATTACK));
        actions.put("Dark Magic", () -> PlayerBonus.getInstance().setTransform(true));
        actions.put("Warrior Heart", () -> PlayerBonus.getInstance().setBonusHealth(WARRIOR_HEART_BONUS_HEALTH));
        actions.put("Furious", () -> PlayerBonus.getInstance().setDashCooldown(FURIOUS_DASH_COOLDOWN));
        actions.put("Necropolis", () -> PlayerBonus.getInstance().setRestorePower(true));
        actions.put("Fast Eye", () -> PlayerBonus.getInstance().setDeflect(true));
        actions.put("Amber", () ->PlayerBonus.getInstance().setLavaWalk(true));
        actions.put("Dragon Fruit", () -> PlayerBonus.getInstance().setBonusPower(DRAGON_FRUIT_BONUS_POWER));
        actions.put("Elementary Magic", () -> PlayerBonus.getInstance().setCriticalHitChance(ELEMENTARY_MAGIC_CRITICAL_HIT_CHANCE));
        actions.put("Unbreakable Bones", () -> PlayerBonus.getInstance().setDashSlash(true));
        actions.put("God's Blood", () -> PlayerBonus.getInstance().setBonusHealth(GODS_BLOOD_BONUS_HEALTH));
        return actions;
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

    // Upgrade
    public void upgrade(int n, int m, int slot) {
        ArrayList<Integer> unlocks = new ArrayList<>();
        int I = slot / n, J = slot % n;
        for (Perk perk : perks) {
            if (perk.getSlot() == slot && !perk.isLocked()) {
                if (perk.isUpgraded()) return;
                perk.setUpgraded(true);
                unlockPerk(perk, unlocks, I, J, n, m);
                break;
            }
        }
        for (Perk perk : perks) {
            if (unlocks.contains(perk.getSlot())) perk.setLocked(false);
        }
    }

    private void unlockPerk(Perk perk, List<Integer> unlocks, int i, int j, int n, int m) {
        if (perkActions.containsKey(perk.getName())) {
            perkActions.get(perk.getName()).performAction();
            checkAdjacentSlots(perk.getSlot(), unlocks, i, j, n, m);
        }
    }

    private void checkAdjacentSlots(int slot, List<Integer> unlocks, int I, int J, int n, int m) {
        if (isSafe(I+1, J, n, m) && placeHolders[I+1][J] == 1 && !unlocked.contains(slot+n)) unlocks.add(slot+n);
        if (isSafe(I-1, J, n, m) && placeHolders[I-1][J] == 1 && !unlocked.contains(slot-n)) unlocks.add(slot-n);
        if (isSafe(I, J+1, n, m) && placeHolders[I][J+1] == 1 && !unlocked.contains(slot+1)) unlocks.add(slot+1);
        if (isSafe(I, J-1, n, m) && placeHolders[I][J-1] == 1 && !unlocked.contains(slot-1)) unlocks.add(slot-1);
    }

    public void loadUnlockedPerks(List<String> p) {
        for (Perk perk : perks) {
            if (p.contains(perk.getName())) {
                upgrade(PERK_SLOT_MAX_COL, PERK_SLOT_MAX_ROW, perk.getSlot());
            }
        }
    }

    public List<String> getUpgradedPerks() {
        return perks.stream()
                .filter(Perk::isUpgraded)
                .map(Perk::getName)
                .collect(Collectors.toList());
    }

    private boolean isSafe(int i, int j, int n, int m) {
        return i >= 0 && j >= 0 && i < m && j < n;
    }

    public List<Perk> getPerks() {
        return perks;
    }

    public int[][] getPlaceHolders() {
        return placeHolders;
    }
}
