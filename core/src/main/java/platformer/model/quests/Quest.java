package platformer.model.quests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platformer.model.inventory.ItemType;

import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Quest {

    private String name;
    private int id;
    private int parentId;
    private QuestType type;
    private String description;
    private int goal;
    private int progress;
    private Map<ItemType, Integer> itemRewards;
    private int coinReward, expReward;
    private int levelRequirement;
    private int npcRequest;

    private boolean completed = false;

    /**
     * Progresses the quest by one step.
     */
    public void progress() {
        if (completed) return;
        progress++;
        if (progress == goal) completed = true;
    }

    public void reset() {
        progress = 0;
        completed = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quest quest = (Quest) o;
        return id == quest.id &&
                parentId == quest.parentId &&
                goal == quest.goal &&
                progress == quest.progress &&
                coinReward == quest.coinReward &&
                expReward == quest.expReward &&
                levelRequirement == quest.levelRequirement &&
                completed == quest.completed &&
                Objects.equals(name, quest.name) &&
                type == quest.type &&
                Objects.equals(description, quest.description) &&
                Objects.equals(itemRewards, quest.itemRewards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, parentId, type, description, goal, progress, itemRewards, coinReward, expReward, levelRequirement, completed);
    }
}