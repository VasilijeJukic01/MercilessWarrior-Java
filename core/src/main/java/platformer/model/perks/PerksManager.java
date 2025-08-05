package platformer.model.perks;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.core.Framework;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.EventBus;
import platformer.event.events.PerkUnlockedEvent;
import platformer.state.types.GameState;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static platformer.constants.FilePaths.PERKS_PATH;

/**
 * Class that is responsible for managing all the perks in the game.
 * It holds references to all the perks and provides methods for upgrading and unlocking them.
 */
public class PerksManager {

    private final GameState gameState;
    private final ArrayList<Perk> perks = new ArrayList<>();

    public PerksManager(GameState gameState) {
        this.gameState = gameState;
        loadPerksFromFile();
    }

    private void loadPerksFromFile() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(PERKS_PATH)))) {
            Type listType = new TypeToken<List<Perk>>() {}.getType();
            List<Perk> loaded = new Gson().fromJson(reader, listType);
            if (loaded != null) {
                this.perks.addAll(loaded);
                for (Perk perk : this.perks) {
                    perk.loadImage();
                    perk.setLocked(!perk.isStartPerk());
                }
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load perks.json! " + e.getMessage(), Message.ERROR);
        }
    }

    /**
     * Attempts to upgrade a perk for the player.
     *
     * @param slot The grid slot of the perk to upgrade.
     */
    public void upgrade(int slot) {
        Optional<Perk> targetPerkOpt = perks.stream().filter(p -> p.getSlot() == slot).findFirst();
        if (targetPerkOpt.isEmpty()) return;

        Perk targetPerk = targetPerkOpt.get();
        if (targetPerk.isLocked() || targetPerk.isUpgraded()) return;

        if (Framework.getInstance().getAccount().getTokens() < targetPerk.getCost()) {
            Logger.getInstance().notify("Not enough tokens to upgrade perk: " + targetPerk.getName(), Message.WARNING);
            return;
        }

        gameState.getPlayer().changeUpgradeTokens(-targetPerk.getCost());

        targetPerk.setUpgraded(true);
        applyPerkBonuses(targetPerk);
        unlockAdjacentPerks(targetPerk);

        Framework.getInstance().getAccount().setPerks(getUpgradedPerks());
        EventBus.getInstance().publish(new PerkUnlockedEvent(targetPerk));
    }

    /**
     * Unlocks perks adjacent to a newly upgraded perk.
     *
     * @param upgradedPerk The perk that was just upgraded.
     */
    private void unlockAdjacentPerks(Perk upgradedPerk) {
        if (upgradedPerk.getUnlocks() == null) return;
        for (int unlockedSlot : upgradedPerk.getUnlocks()) {
            perks.stream()
                    .filter(p -> p.getSlot() == unlockedSlot)
                    .findFirst()
                    .ifPresent(perkToUnlock -> perkToUnlock.setLocked(false));
        }
    }

    /**
     * Applies the bonuses of a given perk to the global PerksBonus.
     *
     * @param perk The perk whose bonuses should be applied.
     */
    private void applyPerkBonuses(Perk perk) {
        if (perk.getBonuses() == null) return;
        PerksBonus bonusManager = PerksBonus.getInstance();

        for (Map.Entry<String, Double> bonus : perk.getBonuses().entrySet()) {
            String key = bonus.getKey().toLowerCase();
            double value = bonus.getValue();

            switch (key) {
                case "exp": bonusManager.setBonusExp(bonusManager.getBonusExp() + (int)value); break;
                case "cooldown": bonusManager.setBonusCooldown(bonusManager.getBonusCooldown() + value); break;
                case "fireball": bonusManager.setFireball(value > 0); break;
                case "power": bonusManager.setBonusPower(bonusManager.getBonusPower() + (int)value); break;
                case "coin": bonusManager.setBonusCoin(bonusManager.getBonusCoin() + (int)value); break;
                case "attack": bonusManager.setBonusAttack(bonusManager.getBonusAttack() + (int)value); break;
                case "transform": bonusManager.setTransform(value > 0); break;
                case "health": bonusManager.setBonusHealth(bonusManager.getBonusHealth() + (int)value); break;
                case "dashcooldown": bonusManager.setDashCooldown(bonusManager.getDashCooldown() + value); break;
                case "restorepower": bonusManager.setRestorePower(value > 0); break;
                case "deflect": bonusManager.setDeflect(value > 0); break;
                case "lavawalk": bonusManager.setLavaWalk(value > 0); break;
                case "criticalhitchance": bonusManager.setCriticalHitChance(bonusManager.getCriticalHitChance() + (int)value); break;
                case "dashslash": bonusManager.setDashSlash(value > 0); break;
                default: break;
            }
        }
    }

    /**
     * Loads the player's saved perk progress.
     * It iterates through the list of saved perk IDs and applies their bonuses.
     *
     * @param upgradedPerkIds A list of perk IDs that the player has unlocked.
     */
    public void loadUnlockedPerks(List<String> upgradedPerkIds) {
        if (upgradedPerkIds == null) return;

        Set<String> upgradedIdSet = new HashSet<>(upgradedPerkIds);
        for (Perk perk : perks) {
            if (upgradedIdSet.contains(perk.getId())) {
                perk.setUpgraded(true);
                perk.setLocked(false);
                applyPerkBonuses(perk);
            }
        }

        perks.stream()
                .filter(Perk::isUpgraded)
                .forEach(this::unlockAdjacentPerks);
    }

    /**
     * Gathers a list of IDs of all currently upgraded perks.
     *
     * @return A list of strings, where each string is the unique ID of an upgraded perk.
     */
    public List<String> getUpgradedPerks() {
        return perks.stream()
                .filter(Perk::isUpgraded)
                .map(Perk::getId)
                .collect(Collectors.toList());
    }

    /**
     * Resets all perks and bonuses to their initial state.
     */
    public void reset() {
        PerksBonus.getInstance().reset();
        perks.clear();
        loadPerksFromFile();
    }

    public List<Perk> getPerks() {
        return perks;
    }
}
