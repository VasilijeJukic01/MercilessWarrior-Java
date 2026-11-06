package platformer.model.quests;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.event.EventBus;
import platformer.event.events.PreSaveEvent;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.npc.NpcType;
import platformer.ui.components.slots.QuestSlot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static platformer.constants.FilePaths.QUESTS_PATH;
import static platformer.constants.UI.*;

/**
 * This class manages quests in the game.
 * It handles quest loading, initializing quest slots, updating quest progress and managing quest events.
 */
@Getter
public class QuestManager {

    private GameContext context;
    private List<Quest> allQuests;
    private List<Quest> progressiveQuests, repeatableQuests;
    private List<QuestSlot> slots;

    public QuestManager() {
        loadQuests(QUESTS_PATH);
        initSlots();
        EventBus.getInstance().register(PreSaveEvent.class, e -> syncToAccount());
    }

    public void wire(GameContext context) {
        this.context = context;
    }

    public void loadQuests(String filePath) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(QuestType.class, new QuestTypeDeserializer());
        gsonBuilder.registerTypeAdapter(QuestObjectiveType.class, new QuestObjectiveTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ObjectiveTarget.class, new ObjectiveTargetDeserializer());
        Gson gson = gsonBuilder.create();

        try (InputStream is = getClass().getResourceAsStream(filePath);
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Type questListType = new TypeToken<List<Quest>>() {}.getType();
            allQuests = gson.fromJson(reader, questListType);
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
        return allQuests.stream()
                .filter(quest -> quest.getType() == type)
                .sorted(Comparator.comparingInt(Quest::getId))
                .collect(Collectors.toList());
    }

    private void initSlots() {
        this.slots = new ArrayList<>();
        List<Quest> availableProgressiveQuests = progressiveQuests.stream()
                .filter(quest -> (quest.getPrerequisites() == null || quest.getPrerequisites().isEmpty()) && quest.getNpcRequest() == 0)
                .toList();

        if (!repeatableQuests.isEmpty()) {
            Quest randomRepeatableQuest = repeatableQuests.get(new Random().nextInt(repeatableQuests.size()));
            slots.add(new QuestSlot(randomRepeatableQuest, 0, 0));
        }

        for (Quest quest : availableProgressiveQuests) {
            slots.add(new QuestSlot(quest, 0, 0));
        }
        sortAndRepositionSlots();
    }

    /**
     * Updates the progress of active quests based on a game event.
     * This method is called by dedicated listeners in response to events from the EventBus.
     *
     * @param eventType   The type of objective to update (e.g., KILL, COLLECT).
     * @param eventTarget The specific target of the objective (e.g., SKELETON, CRATE).
     */
    public void updateQuestProgress(QuestObjectiveType eventType, ObjectiveTarget eventTarget) {
        String currentLevelId = context.getLevelManager().getCurrentLevelId();
        for (QuestSlot slot : new ArrayList<>(slots)) {
            Quest quest = slot.getQuest();
            if (quest.isCompleted() || quest.getObjectives() == null || quest.getObjectives().isEmpty()) continue;

            if (quest.getActiveObjectiveIndex() < quest.getObjectives().size()) {
                QuestObjective activeObjective = quest.getObjectives().get(quest.getActiveObjectiveIndex());
                if (activeObjective.getLevelId() != null && !activeObjective.getLevelId().equalsIgnoreCase(currentLevelId)) {
                    continue;
                }
                if (activeObjective.getType() == eventType && activeObjective.getTarget() == eventTarget) {
                    activeObjective.setCurrentAmount(activeObjective.getCurrentAmount() + 1);
                    checkObjectiveCompletion(quest);
                }
            }
        }
    }

    private void checkObjectiveCompletion(Quest quest) {
        QuestObjective activeObjective = quest.getObjectives().get(quest.getActiveObjectiveIndex());
        if (activeObjective.getCurrentAmount() >= activeObjective.getRequiredAmount()) {
            if (quest.getActiveObjectiveIndex() >= quest.getObjectives().size() - 1) {
                quest.setCompleted(true);
                completeQuest(quest);
                Logger.getInstance().notify("Quest Completed: " + quest.getName(), Message.INFORMATION);
            }
            else {
                quest.setActiveObjectiveIndex(quest.getActiveObjectiveIndex() + 1);
                Logger.getInstance().notify("Quest Updated: " + quest.getName(), Message.INFORMATION);
            }
        }
    }

    public void completeQuest(Quest quest) {
        Player player = context.getGameState().getPlayer();
        if(quest.getItemRewards() != null) player.getInventory().completeQuestFill(quest.getItemRewards());
        if(quest.getCoinReward() > 0) player.changeCoins(quest.getCoinReward());
        if(quest.getExpReward() > 0) player.changeExp(quest.getExpReward());

        switchQuest(quest);
        sortAndRepositionSlots();
    }

    private void switchQuest(Quest quest) {
        if (quest.getType() == QuestType.REPEATABLE) {
            quest.reset();
            slots.stream()
                    .filter(s -> s.getQuest().equals(quest))
                    .findFirst()
                    .ifPresent(slot -> {
                        Quest newRepeatable = repeatableQuests.stream()
                                .filter(q -> !q.equals(quest) && slots.stream().noneMatch(s2 -> s2.getQuest().equals(q)))
                                .findAny()
                                .orElse(quest);
                        slot.setQuest(newRepeatable);
                    });
        }

        List<Quest> nextQuests = findNextQuests(quest);
        for (Quest newQuest : nextQuests) {
            if (slots.stream().noneMatch(slot -> slot.getQuest().equals(newQuest))) {
                slots.add(new QuestSlot(newQuest, 0, 0));
            }
        }
    }

    private List<Quest> findNextQuests(Quest completedQuest) {
        if (completedQuest.getId() == -1) return new ArrayList<>();
        return progressiveQuests.stream()
                .filter(q -> q.getNpcRequest() == 0)
                .filter(q -> q.getPrerequisites() != null && q.getPrerequisites().contains(completedQuest.getId()))
                .filter(this::areAllPrerequisitesMet)
                .collect(Collectors.toList());
    }

    private boolean areAllPrerequisitesMet(Quest quest) {
        if (quest.getPrerequisites() == null || quest.getPrerequisites().isEmpty()) return true;
        return quest.getPrerequisites().stream()
                .allMatch(prereqId ->
                        allQuests.stream()
                                .filter(q -> q.getId() == prereqId)
                                .findFirst()
                                .map(Quest::isCompleted)
                                .orElse(false)
                );
    }

    private void sortAndRepositionSlots() {
        slots.sort(Comparator.comparing((QuestSlot slot) -> slot.getQuest().getType() == QuestType.REPEATABLE).reversed()
                .thenComparing(QuestSlot::isCompleted)
                .thenComparingInt(slot -> slot.getQuest().getId()));

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
                QuestSlot newSlot = new QuestSlot(quest, 0, 0);
                slots.add(newSlot);
                sortAndRepositionSlots();
                Logger.getInstance().notify("Quest Started: " + questName, Message.INFORMATION);
                launchStartSideEffect(questName);
            }
            else Logger.getInstance().notify("Quest already started: " + questName, Message.WARNING);
        });
    }

    public void completeObjectiveStep(String questName, int stepIndex) {
        for (QuestSlot slot : new ArrayList<>(slots)) {
            Quest quest = slot.getQuest();
            if (quest.getName().equals(questName) && !quest.isCompleted() && quest.getActiveObjectiveIndex() == stepIndex) {
                QuestObjective obj = quest.getObjectives().get(stepIndex);
                obj.setCurrentAmount(obj.getRequiredAmount());
                checkObjectiveCompletion(quest);
                break;
            }
        }
    }

    private void launchStartSideEffect(String questName) {
        if (questName.equals("Helping Anita")) {
            Npc anita = context.getObjectManager().getNpcByType(NpcType.ANITA);
            anita.setAlive(false);
            context.getObjectManager().spawnFollower(NpcType.ANITA, (int)anita.getHitBox().x, (int)anita.getHitBox().y);
        }
    }

    private Optional<Quest> findQuestByName(String questName) {
        return allQuests.stream()
                .filter(q -> q.getName().equals(questName))
                .findFirst();
    }

    public boolean isQuestActive(String questName) {
        for (QuestSlot slot : slots) {
            if (slot.getQuest().getName().equals(questName)) {
                return !slot.getQuest().isCompleted();
            }
        }
        return false;
    }

    public boolean isQuestCompleted(String questName) {
        return allQuests.stream().anyMatch(q -> q.getName().equals(questName) && q.isCompleted());
    }

    public void syncToAccount() {
        List<String> serialized = new ArrayList<>();
        Set<Quest> savedSet = new LinkedHashSet<>();
        for (QuestSlot slot : slots) {
            if (slot.getQuest() != null) savedSet.add(slot.getQuest());
        }
        for (Quest q : allQuests) {
            if (q.isCompleted()) savedSet.add(q);
        }

        for (Quest q : savedSet) {
            boolean inSlots = slots.stream().anyMatch(s -> s.getQuest().equals(q));
            String counts = q.getObjectives() == null ? "" :
                    q.getObjectives().stream()
                            .map(o -> String.valueOf(o.getCurrentAmount()))
                            .collect(Collectors.joining(","));
            serialized.add(q.getName() + "|" + (q.isCompleted() ? 1 : 0) + "|" + (inSlots ? 1 : 0) + "|" + q.getActiveObjectiveIndex() + "|" + counts);
        }
        Framework.getInstance().getAccount().setQuests(serialized);
    }

    public void loadFromAccount(List<String> savedQuests) {
        reset();
        if (savedQuests == null || savedQuests.isEmpty()) return;

        slots.clear();
        for (String entry : savedQuests) {
            String[] parts = entry.split("\\|", -1);
            if (parts.length < 5) continue;

            String name = parts[0];
            boolean completed = "1".equals(parts[1]);
            boolean inSlots = "1".equals(parts[2]);
            int activeIdx = Integer.parseInt(parts[3]);
            String[] counts = parts[4].isEmpty() ? new String[0] : parts[4].split(",");
            findQuestByName(name).ifPresent(quest -> {
                quest.setCompleted(completed);
                quest.setActiveObjectiveIndex(activeIdx);
                if (quest.getObjectives() != null) {
                    for (int i = 0; i < Math.min(counts.length, quest.getObjectives().size()); i++) {
                        quest.getObjectives().get(i).setCurrentAmount(Integer.parseInt(counts[i]));
                    }
                }
                if (inSlots) slots.add(new QuestSlot(quest, 0, 0));
            });
        }
        sortAndRepositionSlots();
    }

    public void reset() {
        if (allQuests != null) allQuests.forEach(Quest::reset);
        initSlots();
    }

    // Custom Deserializers
    private static class QuestTypeDeserializer implements JsonDeserializer<QuestType> {
        @Override
        public QuestType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return QuestType.valueOf(json.getAsString().toUpperCase());
        }
    }

    private static class QuestObjectiveTypeDeserializer implements JsonDeserializer<QuestObjectiveType> {
        @Override
        public QuestObjectiveType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return QuestObjectiveType.valueOf(json.getAsString().toUpperCase());
        }
    }

    private static class ObjectiveTargetDeserializer implements JsonDeserializer<ObjectiveTarget> {
        @Override
        public ObjectiveTarget deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return ObjectiveTarget.valueOf(json.getAsString().toUpperCase());
        }
    }
}