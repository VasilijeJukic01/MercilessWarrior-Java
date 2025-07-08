package platformer.model.inventory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ItemType.class, (JsonDeserializer<ItemType>) (json, typeOfT, context) -> ItemType.valueOf(json.getAsString()));
        Gson gson = gsonBuilder.create();

        try (InputStream is = getClass().getResourceAsStream(RECIPES_PATH); InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Type recipeListType = new TypeToken<List<Recipe>>() {}.getType();
            recipes = gson.fromJson(reader, recipeListType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }
}
