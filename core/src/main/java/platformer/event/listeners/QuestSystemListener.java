package platformer.event.listeners;

import platformer.event.events.CrateDestroyedEvent;
import platformer.event.events.EnemyDefeatedEvent;
import platformer.event.events.ItemPurchasedEvent;
import platformer.event.events.PerkUnlockedEvent;
import platformer.model.quests.ObjectiveTarget;
import platformer.model.quests.QuestManager;
import platformer.model.quests.QuestObjectiveType;

/**
 * Listens for game events and updates the QuestManager accordingly.
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
        ObjectiveTarget target = null;
        switch (event.defeatedEnemy().getEnemyType()) {
            case SKELETON -> target = ObjectiveTarget.SKELETON;
            case GHOUL -> target = ObjectiveTarget.GHOUL;
            case LANCER -> target = ObjectiveTarget.LANCER;
        }
        if (target != null) questManager.updateQuestProgress(QuestObjectiveType.KILL, target);
    }

    /**
     * Handles the CrateDestroyedEvent to update relevant quests.
     *
     * @param event The event object.
     */
    public void onCrateDestroyed(CrateDestroyedEvent event) {
        questManager.updateQuestProgress(QuestObjectiveType.COLLECT, ObjectiveTarget.CRATE);
    }

    /**
     * Handles the PerkUnlockedEvent to update relevant quests.
     *
     * @param event The event object containing the unlocked perk.
     */
    public void onPerkUnlocked(PerkUnlockedEvent event) {
        if (event.unlockedPerk().getName().equals("Strong Arms")) {
            questManager.updateQuestProgress(QuestObjectiveType.UPGRADE, ObjectiveTarget.SWORD_UPGRADE);
        }
    }

    /**
     * Handles the ItemPurchasedEvent to update relevant quests.
     *
     * @param event The event object containing the purchased item.
     */
    public void onItemPurchased(ItemPurchasedEvent event) {
        if ("ARMOR_WARRIOR".equals(event.purchasedItem().getItemId())) {
            questManager.updateQuestProgress(QuestObjectiveType.COLLECT, ObjectiveTarget.BUY_ARMOR);
        }
    }

}