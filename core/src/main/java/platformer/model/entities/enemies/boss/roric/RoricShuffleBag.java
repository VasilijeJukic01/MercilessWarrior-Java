package platformer.model.entities.enemies.boss.roric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A utility class that implements the "Shuffle Bag" or "Deck of Cards" algorithm for selecting Roric's attacks in a fair and varied manner.
 * <p>
 * The Shuffle Bag pattern ensures that every attack in a defined set is used exactly once before any attack can be repeated.
 * This prevents frustrating streaks of the same move while maintaining an element of unpredictability for the player.
 *
 * <h4>Usage:</h4>
 * <ol>
 *     <li>An instance is created with a "master list" of all possible attacks for a given phase.</li>
 *     <li>The {@link #draw()} method is called to pull a random attack from the bag.</li>
 *     <li>The drawn attack is removed from the current pool.</li>
 *     <li>Once the bag is empty, it is automatically refilled (reshuffled) from the master list.</li>
 * </ol>
 *
 * This class is used exclusively by the {@link RoricPhaseManager} to choreograph the boss fight.
 *
 * @see RoricPhaseManager
 */
public class RoricShuffleBag {

    private final List<RoricState> masterList;
    private final List<RoricState> currentBag;
    private final Random random;

    private RoricState lastDrawnItem = null;

    RoricShuffleBag(List<RoricState> attacks) {
        this.masterList = new ArrayList<>(attacks);
        this.currentBag = new ArrayList<>();
        this.random = new Random();
        reshuffle();
    }

    /**
     * Draws a single random attack from the bag and removes it.
     * <p>
     * If the bag is empty before the draw, it automatically triggers a {@link #reshuffle()}
     * to refill it from the master list, guaranteeing a continuous and fair selection of attacks.
     *
     * @return A {@link RoricState} representing the chosen attack.
     */
    public RoricState draw() {
        if (currentBag.isEmpty()) reshuffle();

        if (currentBag.size() > 1) {
            List<RoricState> possibleDraws = new ArrayList<>();
            for (RoricState item : currentBag) {
                if (item != lastDrawnItem) possibleDraws.add(item);
            }
            RoricState drawnItem;
            if (!possibleDraws.isEmpty()) drawnItem = possibleDraws.get(random.nextInt(possibleDraws.size()));
            else drawnItem = currentBag.get(0);
            currentBag.remove(drawnItem);
            this.lastDrawnItem = drawnItem;
            return drawnItem;
        }
        RoricState drawnItem = currentBag.remove(0);
        this.lastDrawnItem = drawnItem;
        return drawnItem;
    }

    /**
     * Resets the bag by clearing its current contents and refilling it with all the attacks from the master list.
     * It guarantees the first item in the new bag is different from the last item drawn, if possible.
     * This is called automatically when the bag becomes empty.
     */
    private void reshuffle() {
        currentBag.clear();
        currentBag.addAll(masterList);
        Collections.shuffle(currentBag);

        if (lastDrawnItem != null && !currentBag.isEmpty() && currentBag.get(0) == lastDrawnItem) {
            if (currentBag.size() > 1) {
                int swapIndex = -1;
                for (int i = 1; i < currentBag.size(); i++) {
                    if (currentBag.get(i) != lastDrawnItem) {
                        swapIndex = i;
                        break;
                    }
                }
                if (swapIndex != -1) Collections.swap(currentBag, 0, swapIndex);
            }
        }
    }

    public List<RoricState> getMasterList() {
        return masterList;
    }
}
