package platformer.model.perks;

import platformer.core.Framework;
import platformer.model.quests.QuestManager;
import platformer.observer.Publisher;
import platformer.observer.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static platformer.constants.Constants.*;

/**
 * Class that is responsible for managing all the perks in the game.
 * It holds references to all the perks and provides methods for upgrading and unlocking them.
 */
@SuppressWarnings("unchecked")
public class PerksManager implements Publisher {

    private static final List<Subscriber> subscribers = new ArrayList<>();

    @FunctionalInterface
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

    /**
     * Initializes the actions associated with each perk.
     *
     * @return A map of perk names to their associated actions.
     */
    private Map<String, PerkAction> initPerkActions() {
        Map<String, PerkAction> actions = new HashMap<>();
        actions.put("XP Bonus", () -> PerksBonus.getInstance().setBonusExp(XP_BONUS_AMOUNT));
        actions.put("Strong Arms", () -> PerksBonus.getInstance().setBonusCooldown(STRONG_ARMS_BONUS_COOLDOWN));
        actions.put("Ancient Notes", () -> PerksBonus.getInstance().setFireball(true));
        actions.put("Power Pills", () -> PerksBonus.getInstance().setBonusPower(POWER_PILL_BONUS_POWER));
        actions.put("Lucky Drop", () -> PerksBonus.getInstance().setBonusCoin(LUCKY_DROP_BONUS_COINS));
        actions.put("Broken Bones", () -> PerksBonus.getInstance().setBonusCoin(BROKEN_BONES_BONUS_ATTACK));
        actions.put("Dark Magic", () -> PerksBonus.getInstance().setTransform(true));
        actions.put("Warrior Heart", () -> PerksBonus.getInstance().setBonusHealth(WARRIOR_HEART_BONUS_HEALTH));
        actions.put("Furious", () -> PerksBonus.getInstance().setDashCooldown(FURIOUS_DASH_COOLDOWN));
        actions.put("Necropolis", () -> PerksBonus.getInstance().setRestorePower(true));
        actions.put("Fast Eye", () -> PerksBonus.getInstance().setDeflect(true));
        actions.put("Amber", () -> PerksBonus.getInstance().setLavaWalk(true));
        actions.put("Dragon Fruit", () -> PerksBonus.getInstance().setBonusPower(DRAGON_FRUIT_BONUS_POWER));
        actions.put("Elementary Magic", () -> PerksBonus.getInstance().setCriticalHitChance(ELEMENTARY_MAGIC_CRITICAL_HIT_CHANCE));
        actions.put("Unbreakable Bones", () -> PerksBonus.getInstance().setDashSlash(true));
        actions.put("God's Blood", () -> PerksBonus.getInstance().setBonusHealth(GODS_BLOOD_BONUS_HEALTH));
        return actions;
    }

    /**
     * Initializes the starting unlocked perks.
     */
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
    /**
     * Upgrades a perk and unlocks adjacent perks.
     *
     * @param n The number of columns in the perk grid.
     * @param m The number of rows in the perk grid.
     * @param slot The slot of the perk to upgrade.
     */
    public void upgrade(int n, int m, int slot) {
        ArrayList<Integer> unlocks = new ArrayList<>();
        int I = slot / n, J = slot % n;
        for (Perk perk : perks) {
            if (perk.getSlot() == slot && !perk.isLocked() && !perk.isUpgraded()) {
                perk.setUpgraded(true);
                Framework.getInstance().getAccount().setPerks(getUpgradedPerks());
                unlockPerk(perk, unlocks, I, J, n, m);
                notifyQuestManager(perk);
                break;
            }
        }
        perks.stream()
                .filter(perk -> unlocks.contains(perk.getSlot()))
                .forEach(perk -> perk.setLocked(false));
    }

    /**
     * Unlocks a perk and checks for adjacent perks to unlock.
     *
     * @param perk The perk to unlock.
     * @param unlocks A list of perks to unlock.
     * @param i The row of the perk in the grid.
     * @param j The column of the perk in the grid.
     * @param n The number of columns in the perk grid.
     * @param m The number of rows in the perk grid.
     */
    private void unlockPerk(Perk perk, List<Integer> unlocks, int i, int j, int n, int m) {
        if (perkActions.containsKey(perk.getName())) {
            perkActions.get(perk.getName()).performAction();
            checkAdjacentSlots(perk.getSlot(), unlocks, i, j, n, m);
        }
    }

    /**
     * Checks for adjacent perks to unlock.
     *
     * @param slot The slot of the perk.
     * @param unlocks A list of perks to unlock.
     * @param I The row of the perk in the grid.
     * @param J The column of the perk in the grid.
     * @param n The number of columns in the perk grid.
     * @param m The number of rows in the perk grid.
     */
    private void checkAdjacentSlots(int slot, List<Integer> unlocks, int I, int J, int n, int m) {
        if (isSafe(I+1, J, n, m) && placeHolders[I+1][J] == 1 && !unlocked.contains(slot+n)) unlocks.add(slot+n);
        if (isSafe(I-1, J, n, m) && placeHolders[I-1][J] == 1 && !unlocked.contains(slot-n)) unlocks.add(slot-n);
        if (isSafe(I, J+1, n, m) && placeHolders[I][J+1] == 1 && !unlocked.contains(slot+1)) unlocks.add(slot+1);
        if (isSafe(I, J-1, n, m) && placeHolders[I][J-1] == 1 && !unlocked.contains(slot-1)) unlocks.add(slot-1);
    }

    /**
     * Loads the unlocked perks from a list of perk names.
     *
     * @param p A list of perk names to unlock.
     */
    public void loadUnlockedPerks(List<String> p) {
        perks.stream()
                .filter(perk -> p.contains(perk.getName()))
                .forEach(perk -> upgrade(PERK_SLOT_MAX_COL, PERK_SLOT_MAX_ROW, perk.getSlot()));
    }

    public List<String> getUpgradedPerks() {
        return perks.stream()
                .filter(Perk::isUpgraded)
                .map(Perk::getName)
                .collect(Collectors.toList());
    }

    private void notifyQuestManager(Perk perk) {
        if (perk.getName().equals("Strong Arms")) notify("Upgrade Sword");
    }

    // Observer
    @Override
    public void addSubscriber(Subscriber s) {
        subscribers.add(s);
    }

    @Override
    public void removeSubscriber(Subscriber s) {
        subscribers.remove(s);
    }

    @Override
    public <T> void notify(T... o) {
        subscribers.stream()
                .filter(s -> s instanceof QuestManager)
                .findFirst()
                .ifPresent(s -> s.update(o[0]));
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
