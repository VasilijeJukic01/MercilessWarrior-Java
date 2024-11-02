package platformer.model.quests;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.inventory.ItemType;
import platformer.observer.Subscriber;
import platformer.state.GameState;
import platformer.ui.QuestSlot;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static platformer.constants.FilePaths.QUESTS_PATH;
import static platformer.constants.UI.*;

@Getter
public class QuestManager implements Subscriber {

    private final GameState gameState;

    private List<Quest> quests;
    private List<QuestSlot> slots;

    public QuestManager(GameState gameState) {
        this.gameState = gameState;
        loadQuests(QUESTS_PATH);
        initSlots();
        gameState.getEnemyManager().addSubscriber(this);
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

    private void initSlots() {
        this.slots = new ArrayList<>();
        for (int i = 1; i <= QUEST_SLOT_CAP; i++) {
            slots.add(new QuestSlot(quests.get(i-1), QUEST_SLOT_X, QUEST_SLOT_Y + i * QUEST_SLOT_SPACING));
        }
    }

    // Accept Events
    @Override
    @SafeVarargs
    public final <T> void update(T... o) {
        if (o[0].equals("Kill Skeletons")) {
            Optional<Quest> q = quests.stream()
                    .filter(quest -> quest.getName().equals("Kill Skeletons"))
                    .findFirst();
            if (!q.isPresent()) return;
            q.get().progress();
            if (q.get().isCompleted()) {
                completeQuest(q.get());
                Logger.getInstance().notify("Quest Completed: " + q.get().getName(), Message.INFORMATION);
            }
        }
    }

    public void completeQuest(Quest quest) {
        gameState.getPlayer().getInventory().completeQuestFill(quest.getItemRewards());
        quest.reset();
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