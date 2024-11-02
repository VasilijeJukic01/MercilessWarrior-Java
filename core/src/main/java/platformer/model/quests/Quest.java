package platformer.model.quests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platformer.model.inventory.ItemType;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Quest {

    private String name;
    private String description;
    private int goal;
    private int progress;
    private Map<ItemType, Integer> itemRewards;
    private int coinReward;
    private int expReward;
    private int levelRequirement;

    private boolean completed = false;

    public void progress() {
        progress++;
        if (progress == goal) completed = true;
    }

    public void reset() {
        progress = 0;
        completed = false;
    }

}