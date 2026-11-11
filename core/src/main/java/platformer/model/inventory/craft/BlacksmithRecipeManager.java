package platformer.model.inventory.craft;

import com.google.gson.Gson;
import lombok.Getter;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.world.WorldMemoryManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static platformer.constants.FilePaths.BLACKSMITH_RECIPES_PATH;

/**
 * Loads and manages Blacksmith sword tempering tiers and equipment smithing recipes from blacksmith_recipes.json.
 */
public class BlacksmithRecipeManager {

    private static volatile BlacksmithRecipeManager instance;
    private BlacksmithData data;

    @Getter
    public static class BlacksmithData {
        private List<SwordUpgradeTier> swordUpgrades = Collections.emptyList();
        private List<SmithingRecipe> smithingRecipes = Collections.emptyList();
    }

    @Getter
    public static class SwordUpgradeTier {
        private int tier;
        private String name;
        private String description;
        private int bonusDamage;
        private int coinCost;
        private Map<String, Integer> ingredients = Collections.emptyMap();
    }

    @Getter
    public static class SmithingRecipe {
        private String output;
        private int amount;
        private int coinCost;
        private Map<String, Integer> ingredients = Collections.emptyMap();
    }

    private BlacksmithRecipeManager() {
        loadData();
    }

    public static BlacksmithRecipeManager getInstance() {
        if (instance == null) {
            synchronized (BlacksmithRecipeManager.class) {
                if (instance == null) {
                    instance = new BlacksmithRecipeManager();
                }
            }
        }
        return instance;
    }

    private void loadData() {
        try (InputStream is = getClass().getResourceAsStream(BLACKSMITH_RECIPES_PATH)) {
            if (is != null) {
                try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    this.data = new Gson().fromJson(reader, BlacksmithData.class);
                }
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load blacksmith_recipes.json: " + e.getMessage(), Message.ERROR);
        }
        if (this.data == null) {
            this.data = new BlacksmithData();
        }
    }

    public List<SwordUpgradeTier> getSwordUpgrades() {
        return data.getSwordUpgrades();
    }

    public List<SmithingRecipe> getSmithingRecipes() {
        return data.getSmithingRecipes();
    }

    /**
     * Returns the current unlocked sword upgrade tier (0 = base sword, up to max tier in JSON).
     */
    public int getCurrentSwordTier(WorldMemoryManager memory) {
        if (memory == null) return 0;
        int tier = 0;
        for (SwordUpgradeTier upgrade : getSwordUpgrades()) {
            if (memory.hasFlag("SWORD_TIER_" + upgrade.getTier())) {
                tier = Math.max(tier, upgrade.getTier());
            }
        }
        return tier;
    }

    /**
     * Returns the cumulative flat bonus damage from all unlocked sword tiers.
     */
    public int getTotalSwordBonusDamage(WorldMemoryManager memory) {
        if (memory == null) return 0;
        int bonus = 0;
        for (SwordUpgradeTier upgrade : getSwordUpgrades()) {
            if (memory.hasFlag("SWORD_TIER_" + upgrade.getTier())) {
                bonus += upgrade.getBonusDamage();
            }
        }
        return bonus;
    }
}