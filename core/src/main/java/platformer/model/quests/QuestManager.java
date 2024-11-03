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
import java.util.stream.Collectors;

import static platformer.constants.FilePaths.QUESTS_PATH;
import static platformer.constants.UI.*;

@Getter
public class QuestManager implements Subscriber {

    private final GameState gameState;

    private List<Quest> quests;
    private List<Quest> progressiveQuests, repeatableQuests;

    private List<QuestSlot> slots;

    public QuestManager(GameState gameState) {
        this.gameState = gameState;
        loadQuests(QUESTS_PATH);
        initSlots();
        gameState.getEnemyManager().addSubscriber(this);
        gameState.getObjectManager().addSubscriber(this);
    }

    public void loadQuests(String filePath) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(new TypeToken<Map<ItemType, Integer>>() {}.getType(), new ItemTypeMapDeserializer());
        gsonBuilder.registerTypeAdapter(QuestType.class, new QuestTypeDeserializer());
        Gson gson = gsonBuilder.create();

        try (FileReader reader = new FileReader(filePath)) {
            Type questListType = new TypeToken<List<Quest>>() {}.getType();
            quests = gson.fromJson(reader, questListType);
            progressiveQuests = quests.stream()
                    .filter(quest -> quest.getType() == QuestType.PROGRESSIVE)
                    .sorted(Comparator.comparingInt(Quest::getId))
                    .collect(Collectors.toList());
            repeatableQuests = quests.stream()
                    .filter(quest -> quest.getType() == QuestType.REPEATABLE)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSlots() {
        this.slots = new ArrayList<>();

        repeatableQuests.forEach(q -> System.out.println(q.getName()));

        if (!repeatableQuests.isEmpty()) {
            Quest randomRepeatableQuest = repeatableQuests.get(new Random().nextInt(repeatableQuests.size()));
            slots.add(new QuestSlot(randomRepeatableQuest, QUEST_SLOT_X, QUEST_SLOT_Y + QUEST_SLOT_SPACING));
        }

        for (int i = 0; i < QUEST_SLOT_CAP-1 && i < progressiveQuests.size(); i++) {
            slots.add(new QuestSlot(progressiveQuests.get(i), QUEST_SLOT_X, QUEST_SLOT_Y + (i + 2) * QUEST_SLOT_SPACING));
        }
    }

    @Override
    @SafeVarargs
    public final <T> void update(T... o) {
        System.out.println("QuestManager update");
        if (o[0] instanceof String) {
            String event = (String) o[0];
            switch (event) {
                case "Kill Skeletons":
                    updateQuestProgress("Kill Skeletons");
                    break;
                case "Kill Ghouls":
                    updateQuestProgress("Kill Ghouls");
                    break;
                case "Break Crates":
                    updateQuestProgress("Break Crates");
                    break;
                default: break;
            }
        }
    }

    private void updateQuestProgress(String questName) {
        Optional<Quest> quest = quests.stream()
                .filter(q -> q.getName().equals(questName))
                .findFirst();
        if (!quest.isPresent()) return;
        quest.get().progress();
        Logger.getInstance().notify("Quest Progress: " + quest.get().getName(), Message.INFORMATION);
        if (quest.get().isCompleted()) {
            completeQuest(quest.get());
            Logger.getInstance().notify("Quest Completed: " + quest.get().getName(), Message.INFORMATION);
        }
    }

    public void completeQuest(Quest quest) {
        gameState.getPlayer().getInventory().completeQuestFill(quest.getItemRewards());
        switchQuest(quest);
    }

    private void switchQuest(Quest quest) {
        Quest newQuest = findNextQuest(quest);

        if (newQuest != null) {
            slots.stream()
                    .filter(slot -> slot.getQuest().equals(quest))
                    .findFirst()
                    .ifPresent(slot -> slot.setQuest(newQuest));
        }
    }

    private Quest findNextQuest(Quest quest) {
        if (quest.getType() == QuestType.REPEATABLE) {
            quest.reset();
            quest.setProgress(0);
            return repeatableQuests.stream()
                    .filter(q -> !q.equals(quest))
                    .findAny()
                    .orElse(null);
        }
        else if (quest.getType() == QuestType.PROGRESSIVE) {
            return progressiveQuests.stream()
                    .filter(q -> q.getId() == quest.getId() + 1)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    // Deserializers
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

    private static class QuestTypeDeserializer implements JsonDeserializer<QuestType> {
        @Override
        public QuestType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return QuestType.valueOf(json.getAsString().toUpperCase());
        }
    }
}