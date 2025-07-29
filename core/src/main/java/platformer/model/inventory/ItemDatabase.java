package platformer.model.inventory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

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
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/items/items.json")))) {
            Type type = new TypeToken<Map<String, ItemData>>() {}.getType();
            itemData = new Gson().fromJson(reader, type);
        } catch (Exception e) {
            itemData = Collections.emptyMap();
        }
    }

    public ItemData getItemData(String itemId) {
        return itemData.get(itemId);
    }
}
