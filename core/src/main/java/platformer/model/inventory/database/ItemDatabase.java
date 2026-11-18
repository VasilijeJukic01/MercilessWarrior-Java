package platformer.model.inventory.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.animation.AssetManager;
import platformer.constants.FilePaths;
import platformer.model.inventory.item.ItemData;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that manages the item database for the game.
 * It loads item data from a JSON file and provides methods to access item information.
 */
public class ItemDatabase {

    private static volatile ItemDatabase instance;
    private Map<String, ItemData> itemData;

    private ItemDatabase() {
        loadItems();
    }

    public static ItemDatabase getInstance() {
        if (instance == null) {
            synchronized (ItemDatabase.class) {
                if (instance == null) instance = new ItemDatabase();
            }
        }
        return instance;
    }

    private void loadItems() {
        // [Online] Try to load from server cache
        if (GameDataCache.getInstance().isItemDataCached()) {
            this.itemData = GameDataCache.getInstance().getItemData();
            return;
        }

        // [Offline] Fallback to local JSON file
        this.itemData = new HashMap<>();
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, ItemData>>() {}.getType();
        for (String path : FilePaths.ITEM_CATALOG_PATHS) {
            try (InputStreamReader reader = new InputStreamReader(AssetManager.getInstance().getResourceAsStream(path))) {
                Map<String, ItemData> partial = gson.fromJson(reader, type);
                if (partial != null) {
                    this.itemData.putAll(partial);
                }
            } catch (Exception ignored) {}
        }
    }

    public ItemData getItemData(String itemId) {
        return itemData.get(itemId);
    }
}
