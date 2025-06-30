package platformer.model.quests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platformer.model.inventory.ItemType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Quest {

    private String name;
    private int id;
    private List<Integer> prerequisites;
    private QuestType type;
    private String description;
    private List<QuestObjective> objectives;
    private Map<ItemType, Integer> itemRewards;
    private int coinReward, expReward;
    private int levelRequirement;
    private int npcRequest;
    private int activeObjectiveIndex = 0;

    private boolean completed = false;

    public void reset() {
        this.completed = false;
        this.activeObjectiveIndex = 0;
        if (objectives != null) {
            for (QuestObjective objective : objectives) {
                objective.setCurrentAmount(0);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quest quest = (Quest) o;
        return id == quest.id && id != -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}