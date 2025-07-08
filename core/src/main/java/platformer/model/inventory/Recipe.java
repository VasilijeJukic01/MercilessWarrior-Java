package platformer.model.inventory;

import java.util.Map;

public class Recipe {

    private ItemType output;
    private int amount;
    private Map<ItemType, Integer> ingredients;

    public ItemType getOutput() {
        return output;
    }

    public int getAmount() {
        return amount;
    }

    public Map<ItemType, Integer> getIngredients() {
        return ingredients;
    }
}