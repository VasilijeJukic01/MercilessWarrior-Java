package platformer.event.listeners;

import platformer.event.events.CrateDestroyedEvent;
import platformer.event.events.EnemyDefeatedEvent;
import platformer.event.events.ItemPurchasedEvent;
import platformer.event.events.PerkUnlockedEvent;
import platformer.model.entities.enemies.Enemy;
import platformer.model.quests.QuestManager;
import platformer.model.quests.QuestObjectiveType;

/**
 * Pure data-driven listener that forwards game events to {@link QuestManager}.
 */
public class QuestSystemListener {

    private final QuestManager questManager;

    public QuestSystemListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles the EnemyDefeatedEvent to update relevant quests.
     *
     * @param event The event object containing the defeated enemy.
     */
    public void onEnemyDefeated(EnemyDefeatedEvent event) {
        Enemy enemy = event.defeatedEnemy();
        String exactType = enemy.getEnemyType().name();
        String familyTarget = enemy.getDefinition().getQuestTargetOrDefault(exactType);

        questManager.updateQuestProgress(QuestObjectiveType.KILL, familyTarget);
        if (!familyTarget.equalsIgnoreCase(exactType)) {
            questManager.updateQuestProgress(QuestObjectiveType.KILL, exactType);
        }
    }

    /**
     * Handles the CrateDestroyedEvent to update relevant quests.
     *
     * @param event The event object.
     */
    public void onCrateDestroyed(CrateDestroyedEvent event) {
        questManager.updateQuestProgress(QuestObjectiveType.COLLECT, "CRATE");
    }

    /**
     * Handles the PerkUnlockedEvent to update relevant quests.
     *
     * @param event The event object containing the unlocked perk.
     */
    public void onPerkUnlocked(PerkUnlockedEvent event) {
        questManager.updateQuestProgress(QuestObjectiveType.UPGRADE, event.unlockedPerk().getId());
    }

    /**
     * Handles the ItemPurchasedEvent to update relevant quests.
     *
     * @param event The event object containing the purchased item.
     */
    public void onItemPurchased(ItemPurchasedEvent event) {
        questManager.updateQuestProgress(QuestObjectiveType.COLLECT, event.purchasedItem().getItemId(), Math.max(1, event.quantity()));
    }
}