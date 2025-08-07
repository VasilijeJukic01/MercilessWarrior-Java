package platformer.model.inventory.craft;

import java.util.Map;

public class Recipe {

    private String  output;
    private int amount;
    private Map<String, Integer> ingredients;

    public String getOutput() {
        return output;
    }

    public int getAmount() {
        return amount;
    }

    public Map<String, Integer> getIngredients() {
        return ingredients;
    }
}