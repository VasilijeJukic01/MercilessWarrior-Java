package platformer.model.inventory.craft;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static platformer.constants.FilePaths.RECIPES_PATH;

/**
 * RecipeManager is responsible for managing recipes in the game.
 * It loads recipes from a JSON file and provides access to them.
 */
public class RecipeManager {

    private static volatile RecipeManager instance;
    private List<Recipe> recipes = new ArrayList<>();

    private RecipeManager() {
        loadRecipes();
    }

    public static RecipeManager getInstance() {
        if (instance == null) {
            synchronized (RecipeManager.class) {
                if (instance == null) instance = new RecipeManager();
            }
        }
        return instance;
    }

    private void loadRecipes() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(RECIPES_PATH)))) {
            Type recipeListType = new TypeToken<List<Recipe>>() {}.getType();
            recipes = new Gson().fromJson(reader, recipeListType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }
}
