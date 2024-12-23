package platformer.model.quests;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.gameObjects.objects.Shop;
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

/**
 * This class manages quests in the game.
 * It handles quest loading, initializing quest slots, updating quest progress and managing quest events.
 */
@Getter
public class QuestManager implements Subscriber {

    private final GameState gameState;
    private List<Quest> quests;
    private List<Quest> progressiveQuests, repeatableQuests;
    private List<QuestSlot> slots;

    private final Map<String, Runnable> eventActions = new HashMap<>();

    public QuestManager(GameState gameState) {
        this.gameState = gameState;
        loadQuests(QUESTS_PATH);
        initSlots();
        initObservers();
        initEventActions();
    }

    private void initObservers() {
        gameState.getEnemyManager().addSubscriber(this);
        gameState.getObjectManager().addSubscriber(this);
        gameState.getPerksManager().addSubscriber(this);
    }

    public void loadQuests(String filePath) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(new TypeToken<Map<ItemType, Integer>>() {}.getType(), new ItemTypeMapDeserializer());
        gsonBuilder.registerTypeAdapter(QuestType.class, new QuestTypeDeserializer());
        Gson gson = gsonBuilder.create();

        try (FileReader reader = new FileReader(filePath)) {
            Type questListType = new TypeToken<List<Quest>>() {}.getType();
            quests = gson.fromJson(reader, questListType);
            progressiveQuests = filterAndSortQuests(QuestType.PROGRESSIVE);
            repeatableQuests = filterAndSortQuests(QuestType.REPEATABLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filters and sorts quests by the specified type.
     *
     * @param type the quest type
     * @return a list of filtered and sorted quests
     */
    private List<Quest> filterAndSortQuests(QuestType type) {
        return quests.stream()
                .filter(quest -> quest.getType() == type)
                .sorted(Comparator.comparingInt(Quest::getId))
                .collect(Collectors.toList());
    }

    private void initSlots() {
        this.slots = new ArrayList<>();
        List<Quest> availableProgressiveQuests = progressiveQuests.stream()
                .filter(quest -> quest.getParentId() == 0 && quest.getNpcRequest() == 0)
                .collect(Collectors.toList());

        if (!repeatableQuests.isEmpty()) {
            Quest randomRepeatableQuest = repeatableQuests.get(new Random().nextInt(repeatableQuests.size()));
            slots.add(new QuestSlot(randomRepeatableQuest, QUEST_SLOT_X, QUEST_SLOT_Y + QUEST_SLOT_SPACING));
        }

        int k = QUEST_SLOT_CAP - 1;
        for (int i = 0; i < availableProgressiveQuests.size(); i++, k++) {
            slots.add(new QuestSlot(availableProgressiveQuests.get(i), QUEST_SLOT_X, QUEST_SLOT_Y + k * QUEST_SLOT_SPACING));
            if (k == QUEST_SLOT_CAP) k = 0;
        }
    }

    private void initEventActions() {
        eventActions.put("Kill Skeletons", () -> updateQuestProgress("Kill Skeletons"));
        eventActions.put("Kill Ghouls", () -> updateQuestProgress("Kill Ghouls"));
        eventActions.put("Break Crates", () -> updateQuestProgress("Break Crates"));
        eventActions.put("Upgrade Sword", () -> updateQuestProgress("Upgrade Sword"));
        eventActions.put("Buy Armor", () -> updateQuestProgress("Buy Armor"));
    }

    /**
     * Updates the quest progress based on the event.
     *
     * @param o the event parameters
     * @param <T> the type of the event parameters
     */
    @Override
    @SafeVarargs
    public final <T> void update(T... o) {
        if (o[0] instanceof String) {
            String event = (String) o[0];
            Optional.ofNullable(eventActions.get(event)).ifPresent(Runnable::run);
        }
    }

    /**
     * Updates the progress of the specified quest.
     *
     * @param questName the name of the quest
     */
    private void updateQuestProgress(String questName) {
        findQuestByName(questName).ifPresent(quest -> {
            quest.progress();
            Logger.getInstance().notify("Quest Progress: " + quest.getName(), Message.INFORMATION);
            if (quest.isCompleted()) {
                completeQuest(quest);
                Logger.getInstance().notify("Quest Completed: " + quest.getName(), Message.INFORMATION);
            }
        });
    }

    /**
     * Completes the specified quest and updates the quest slots.
     *
     * @param quest the quest to complete
     */
    public void completeQuest(Quest quest) {
        gameState.getPlayer().getInventory().completeQuestFill(quest.getItemRewards());
        switchQuest(quest);
        sortAndRepositionSlots();
    }

    /**
     * Switches the completed quest with the next quest in the sequence.
     *
     * @param quest the completed quest
     */
    private void switchQuest(Quest quest) {
        Quest newQuest = findNextQuest(quest);
        if (newQuest != null && slots.stream().noneMatch(slot -> slot.getQuest().equals(newQuest))) {
            if (quest.getType() == QuestType.PROGRESSIVE) {
                int position = slots.size() % QUEST_SLOT_CAP - 1;
                slots.stream()
                        .filter(slot -> slot.getQuest().equals(quest))
                        .findFirst()
                        .ifPresent(slot -> slot.setQuest(newQuest));
                QuestSlot newSlot = new QuestSlot(quest, QUEST_SLOT_X, QUEST_SLOT_Y + position * QUEST_SLOT_SPACING);
                if (slots.stream().noneMatch(s -> s.getQuest().equals(quest))) slots.add(newSlot);
            }
        }
    }

    /**
     * Finds the next quest in the sequence for the specified quest.
     *
     * @param quest the current quest
     * @return the next quest, or null if no next quest is found
     */
    private Quest findNextQuest(Quest quest) {
        if (quest.getType() == QuestType.REPEATABLE) {
            quest.reset();
            return repeatableQuests.stream()
                    .filter(q -> !q.equals(quest))
                    .findAny()
                    .orElse(null);
        } else if (quest.getType() == QuestType.PROGRESSIVE) {
            return progressiveQuests.stream()
                    .filter(q -> q.getParentId() == quest.getId())
                    .findAny()
                    .orElse(null);
        }
        return null;
    }

    private void sortAndRepositionSlots() {
        slots.sort(Comparator.comparing((QuestSlot slot) -> slot.getQuest().getType() == QuestType.REPEATABLE)
                .reversed()
                .thenComparing(slot -> slot.getQuest().isCompleted()));

        int k = 1;
        for (int i = 0; i < slots.size(); i++, k++) {
            slots.get(i).setXPos(QUEST_SLOT_X);
            slots.get(i).setYPos(QUEST_SLOT_Y + k * QUEST_SLOT_SPACING);
            if (k == QUEST_SLOT_CAP) k = 0;
        }
    }

    public void startNPCQuest(String questName) {
        findQuestByName(questName).ifPresent(quest -> {
            if (slots.stream().noneMatch(slot -> slot.getQuest().equals(quest))) {
                int position = slots.size() % QUEST_SLOT_CAP - 1;
                QuestSlot newSlot = new QuestSlot(quest, QUEST_SLOT_X, QUEST_SLOT_Y + position * QUEST_SLOT_SPACING);
                slots.add(newSlot);
                sortAndRepositionSlots();
                Logger.getInstance().notify("Quest Started: " + questName, Message.INFORMATION);
            } else {
                Logger.getInstance().notify("Quest already started: " + questName, Message.WARNING);
            }
        });
    }

    private Optional<Quest> findQuestByName(String questName) {
        return quests.stream()
                .filter(q -> q.getName().equals(questName))
                .findFirst();
    }

    public void reset() {
        gameState.getObjectManager().getObjects(Shop.class)
                .forEach(shop -> shop.addSubscriber(this));
    }

    /**
     * Custom deserializer for ItemType map.
     */
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

    /**
     * Custom deserializer for QuestType.
     */
    private static class QuestTypeDeserializer implements JsonDeserializer<QuestType> {
        @Override
        public QuestType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return QuestType.valueOf(json.getAsString().toUpperCase());
        }
    }
}