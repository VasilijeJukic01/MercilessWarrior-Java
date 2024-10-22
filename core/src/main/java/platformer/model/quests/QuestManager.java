package platformer.model.quests;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.inventory.ItemType;
import platformer.observer.Subscriber;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static platformer.constants.FilePaths.QUESTS_PATH;

@Getter
public class QuestManager implements Subscriber {

    private List<Quest> quests;

    public QuestManager(EnemyManager enemyManager) {
        loadQuests(QUESTS_PATH);
        enemyManager.addSubscriber(this);
    }

    public void loadQuests(String filePath) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(new TypeToken<Map<ItemType, Integer>>() {}.getType(), new ItemTypeMapDeserializer());
        Gson gson = gsonBuilder.create();

        try (FileReader reader = new FileReader(filePath)) {
            Type questListType = new TypeToken<List<Quest>>() {}.getType();
            quests = gson.fromJson(reader, questListType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Accept Events
    @Override
    public <T> void update(T... o) {
        if (o[0].equals("Kill Skeletons")) {
            Optional<Quest> q = quests.stream()
                    .filter(quest -> quest.getName().equals("Kill Skeletons"))
                    .findFirst();
            if (!q.isPresent()) return;
            q.get().progress();
            if (q.get().isCompleted()) {
                Logger.getInstance().notify("Quest Completed: " + q.get().getName(), Message.INFORMATION);
            }
        }
    }

    public void rewardPlayer(Quest quest) {
        //TODO: Implement reward system
    }

    // Custom Deserializer for ItemType Map
    private static class ItemTypeMapDeserializer implements JsonDeserializer<Map<ItemType, Integer>> {
        @Override
        public Map<ItemType, Integer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            Map<ItemType, Integer> itemMap = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                ItemType itemType = ItemType.valueOf(entry.getKey());
                Integer quantity = entry.getValue().getAsInt();
                itemMap.put(itemType, quantity);
            }

            return itemMap;
        }
    }
}