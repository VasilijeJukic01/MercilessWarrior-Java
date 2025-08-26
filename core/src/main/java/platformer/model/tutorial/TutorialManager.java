package platformer.model.tutorial;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * This class manages tutorials in the game.
 * It handles tutorial activation and deactivation.
 */
public class TutorialManager {

    private List<TutorialTip> tips = new ArrayList<>();

    public TutorialManager() {
        loadTips();
    }

    private void loadTips() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/tutorials.json")))) {
            Type listType = new TypeToken<List<TutorialTip>>() {}.getType();
            this.tips = new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load tutorials.json! " + e.getMessage(), Message.ERROR);
        }
    }

    public List<TutorialTip> getTips() {
        return tips;
    }
}
