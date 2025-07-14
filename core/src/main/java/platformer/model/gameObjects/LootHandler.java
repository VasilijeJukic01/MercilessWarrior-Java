package platformer.model.gameObjects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.effects.EffectManager;
import platformer.model.entities.effects.particles.DustType;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.objects.*;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemData;
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
 * Handles all logic related to loot generation and collection.
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
     * Generates loot and places it inside a container.
     */
    public void generateCrateLoot(Container container) {
        LootTable crateTable = lootTables.get("CRATE");
        if (crateTable == null) return;

        int roll = rand.nextInt(crateTable.getTotalWeight());
        int cumulativeWeight = 0;
        for (LootItem lootItem : crateTable.getItems()) {
            cumulativeWeight += lootItem.getWeight();
            if (roll < cumulativeWeight) {
                InventoryItem item = new InventoryItem(lootItem.getItemId(), lootItem.getQuantity());
                container.getItems().add(item);
                return;
            }
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

    private void spawnCoin(Rectangle2D.Double location, CoinType coinType) {
        int x = (int) location.getCenterX();
        int y = (int) location.y;
        double initialYSpeed = -2.2 * SCALE - (rand.nextDouble() * 1.5 * SCALE);
        double initialXSpeed = (rand.nextDouble() - 0.5) * (2.5 * SCALE);
        Coin coin = new Coin(coinType, x, y, initialXSpeed, initialYSpeed);
        objectManager.addGameObject(coin);
    }

    private void generateLoot(Rectangle2D.Double location, EnemyType enemyType) {
        int x = (int) (location.width / 4) + (int) location.x;
        int y = (int) (location.height / 2.3) + (int) location.y;
        Loot loot = new Loot(ObjType.LOOT, x, y, enemyType);
        objectManager.addGameObject(loot);
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
     * Handles the harvesting of a herb, giving the player loot.
     */
    public void harvestHerb(GameObject herb, Player player) {
        effectManager.spawnDustParticles(herb.getHitBox().getCenterX(), herb.getHitBox().getCenterY() - (10 * SCALE), 15, DustType.HERB_CUT, 0, null);

        LootTable herbTable = lootTables.get("HERB");
        if (herbTable == null) return;

        int roll = rand.nextInt(herbTable.getTotalWeight());
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
    private Map<String, LootTable> loadLootTables() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(LOOT_TABLE_PATH)))) {
            Type type = new TypeToken<Map<String, List<LootItem>>>() {}.getType();
            Map<String, List<LootItem>> rawTables = new Gson().fromJson(reader, type);

            Map<String, LootTable> processedTables = new HashMap<>();
            for (Map.Entry<String, List<LootItem>> entry : rawTables.entrySet()) {
                LootTable table = new LootTable();
                table.setItems(entry.getValue());
                processedTables.put(entry.getKey(), table);
            }
            return processedTables;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

}
