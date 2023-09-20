package platformer.model.inventory;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private final List<InventoryItem> backpack = new ArrayList<>();

    public Inventory() {
    }

    public List<InventoryItem> getBackpack() {
        return backpack;
    }
}
