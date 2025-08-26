package platformer.model.gameObjects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.effects.EffectManager;
import platformer.model.effects.particles.DustType;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.objects.*;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.model.inventory.loot.LootItem;
import platformer.model.inventory.loot.LootTable;
import platformer.model.perks.PerksBonus;

import java.awt.geom.Rectangle2D;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.LOOT_TABLE_PATH;

/**
 * Handles all logic related to loot generation and collection within the game world.
 * <p>
 * It collaborates with the {@link ObjectManager} to spawn new game objects and the {@link EffectManager} for visual feedback.
 *
 * @see ObjectManager
 * @see LootTable
 * @see InventoryItem
 */
public class LootHandler {

    private final ObjectManager objectManager;
    private final EffectManager effectManager;
    private final Random rand = new Random();
    private final Map<String, LootTable> lootTables;

    public LootHandler(ObjectManager objectManager, EffectManager effectManager) {
        this.objectManager = objectManager;
        this.effectManager = effectManager;
        this.lootTables = loadLootTables();
    }

    /**
     * Populates a given {@link Container} with loot based on its predefined loot table.
     * This method is called when a container is first loaded into the level.
     *
     * @param container The container to be filled with loot.
     */
    public void generateCrateLoot(Container container) {
        LootTable crateTable = lootTables.get("CRATE");
        List<InventoryItem> generatedItems = generateItemsFromTable(crateTable);
        if (!generatedItems.isEmpty()) {
            container.getItems().addAll(generatedItems);
        }
    }

    /**
     * Generates loot from a defeated enemy.
     */
    public void generateEnemyLoot(Rectangle2D.Double location, EnemyType enemyType) {
        generateCoins(location);
        generateLoot(location, enemyType);
    }

    /**
     * Generates coins at the specified location based on the total value to drop.
     * The distribution of coin types is determined by random chance.
     *
     * @param location The location where coins should be spawned.
     */
    private void generateCoins(Rectangle2D.Double location) {
        int totalValue = rand.nextInt(11) + 5 + PerksBonus.getInstance().getBonusCoin();

        while (totalValue > 0) {
            CoinType dropType;
            int roll = rand.nextInt(100);

            if (roll < 5) dropType = CoinType.GOLD;
            else if (roll < 30) dropType = CoinType.SILVER;
            else dropType = CoinType.BRONZE;

            if (totalValue >= dropType.getValue()) {
                totalValue -= dropType.getValue();
                spawnCoin(location, dropType);
            }
            else if (totalValue >= CoinType.SILVER.getValue()) {
                totalValue -= CoinType.SILVER.getValue();
                spawnCoin(location, CoinType.SILVER);
            }
            else {
                for (int i = 0; i < totalValue; i++)
                    spawnCoin(location, CoinType.BRONZE);
                totalValue = 0;
            }
        }
    }

    /**
     * Instantiates and adds a single Coin object to the game world with randomized initial velocity.
     *
     * @param location The spawn location.
     * @param coinType The type of coin to spawn.
     */
    private void spawnCoin(Rectangle2D.Double location, CoinType coinType) {
        int x = (int) location.getCenterX();
        int y = (int) location.y;
        double initialYSpeed = -2.2 * SCALE - (rand.nextDouble() * 1.5 * SCALE);
        double initialXSpeed = (rand.nextDouble() - 0.5) * (2.5 * SCALE);
        Coin coin = new Coin(coinType, x, y, initialXSpeed, initialYSpeed);
        objectManager.addGameObject(coin);
    }

    /**
     * Instantiates and adds a single {@link Loot} bag object to the game world.
     *
     * @param location  The spawn location.
     * @param enemyType The type of enemy that dropped the loot.
     */
    private void generateLoot(Rectangle2D.Double location, EnemyType enemyType) {
        LootTable table = lootTables.get(enemyType.name());
        List<InventoryItem> collectedItems = generateItemsFromTable(table);

        if (!collectedItems.isEmpty()) {
            int x = (int) (location.width / 4) + (int) location.x;
            int y = (int) (location.height / 2.3) + (int) location.y;
            Loot loot = new Loot(ObjType.LOOT, x, y);
            loot.getItems().addAll(collectedItems);
            objectManager.addGameObject(loot);
        }
    }

    /**
     * Core loot generation logic. Determines the number of items to drop and then rolls for each item.
     *
     * @param table The LootTable to generate items from.
     * @return A list of generated InventoryItems.
     */
    private List<InventoryItem> generateItemsFromTable(LootTable table) {
        if (table == null || table.getItems() == null || table.getItems().isEmpty()) return Collections.emptyList();

        int numberOfRolls = 1;
        if (table.getNumRolls() != null && !table.getNumRolls().isEmpty()) {
            int totalRollsWeight = table.getTotalRollsWeight();
            if (totalRollsWeight > 0) {
                int roll = rand.nextInt(totalRollsWeight);
                int cumulativeWeight = 0;
                List<Map.Entry<String, Integer>> sortedRolls = new ArrayList<>(table.getNumRolls().entrySet());
                sortedRolls.sort(Comparator.comparingInt(e -> Integer.parseInt(e.getKey())));

                for (Map.Entry<String, Integer> entry : sortedRolls) {
                    cumulativeWeight += entry.getValue();
                    if (roll < cumulativeWeight) {
                        numberOfRolls = Integer.parseInt(entry.getKey());
                        break;
                    }
                }
            }
        }

        List<InventoryItem> collectedItems = new ArrayList<>();
        int totalItemWeight = table.getTotalItemWeight();
        if (totalItemWeight <= 0) return Collections.emptyList();

        for (int i = 0; i < numberOfRolls; i++) {
            int roll = rand.nextInt(totalItemWeight);
            int cumulativeWeight = 0;
            for (LootItem lootItem : table.getItems()) {
                cumulativeWeight += lootItem.getWeight();
                if (roll < cumulativeWeight) {
                    if (!lootItem.getItemId().equals("NOTHING")) {
                        InventoryItem newItem = new InventoryItem(lootItem.getItemId(), lootItem.getQuantity());
                        ItemData data = newItem.getData();
                        boolean itemStacked = false;
                        if (data != null && data.stackable) {
                            for (InventoryItem existingItem : collectedItems) {
                                if (existingItem.getItemId().equals(newItem.getItemId())) {
                                    existingItem.addAmount(newItem.getAmount());
                                    itemStacked = true;
                                    break;
                                }
                            }
                        }
                        if (!itemStacked) collectedItems.add(newItem);
                    }
                    break;
                }
            }
        }
        return collectedItems;
    }

    /**
     * Handles the collection of an item by the player.
     */
    public void collectItem(GameObject object, Player player) {
        if (object instanceof Potion) collectPotion((Potion) object, player);
        else if (object instanceof Coin) collectCoin((Coin) object, player);
    }

    private void collectPotion(Potion potion, Player player) {
        if (!potion.isAlive()) return;

        String itemId = "";
        if (potion.getObjType() == ObjType.HEAL_POTION) itemId = "HEALTH_POTION";
        else if (potion.getObjType() == ObjType.STAMINA_POTION) itemId = "STAMINA_POTION";

        if (!itemId.isEmpty()) {
            player.getInventory().addItemToBackpack(new InventoryItem(itemId, 1));
            potion.setAlive(false);
        }
    }

    private void collectCoin(Coin coin, Player player) {
        if (!coin.isAlive()) return;
        Audio.getInstance().getAudioPlayer().playSound(Sound.COIN_PICK);
        player.changeCoins(coin.getValue());
        coin.setAlive(false);
    }

    /**
     * Handles the logic for harvesting a {@link Herb} object.
     * This involves playing a visual effect, determining the looted item from the loot table, adding it to the player's inventory, and deactivating the herb object.
     *
     * @param herb   The herb object being harvested.
     * @param player The player performing the harvest.
     */
    public void harvestHerb(GameObject herb, Player player) {
        effectManager.spawnDustParticles(herb.getHitBox().getCenterX(), herb.getHitBox().getCenterY() - (10 * SCALE), 15, DustType.HERB_CUT, 0, null);

        LootTable herbTable = lootTables.get("HERB");
        if (herbTable == null) return;

        int roll = rand.nextInt(herbTable.getTotalItemWeight());
        int cumulativeWeight = 0;
        for (LootItem lootItem : herbTable.getItems()) {
            cumulativeWeight += lootItem.getWeight();
            if (roll < cumulativeWeight) {
                InventoryItem item = new InventoryItem(lootItem.getItemId(), lootItem.getQuantity());
                player.getInventory().addItemToBackpack(item);
                ItemData data = item.getData();
                if (data != null) effectManager.spawnItemPickupText("+" + lootItem.getQuantity() + " " + data.name, player, ITEM_TEXT_COLOR);
                break;
            }
        }
        herb.setAlive(false);
    }

    // Helper
    /**
     * Loads and parses all loot tables from the resource file.
     *
     * @return A map where the key is the loot table ID and the value is the {@link LootTable} object.
     */
    private Map<String, LootTable> loadLootTables() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(LOOT_TABLE_PATH)))) {
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, JsonElement>>() {}.getType();
            Map<String, JsonElement> rawJsonMap = gson.fromJson(reader, mapType);

            Map<String, LootTable> processedTables = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : rawJsonMap.entrySet()) {
                JsonElement element = entry.getValue();
                LootTable table;
                if (element.isJsonObject()) {
                    table = gson.fromJson(element, LootTable.class);
                    if (table.getNumRolls() == null) {
                        table.setNumRolls(Map.of("1", 100));
                    }
                }
                else if (element.isJsonArray()) {
                    table = new LootTable();
                    Type listType = new TypeToken<List<LootItem>>() {}.getType();
                    table.setItems(gson.fromJson(element, listType));
                    table.setNumRolls(Map.of("1", 100));
                }
                else continue;
                processedTables.put(entry.getKey(), table);
            }
            return processedTables;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

}
