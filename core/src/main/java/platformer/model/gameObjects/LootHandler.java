package platformer.model.gameObjects;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.effects.EffectManager;
import platformer.model.entities.effects.particles.DustType;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.objects.Coin;
import platformer.model.gameObjects.objects.Container;
import platformer.model.gameObjects.objects.Loot;
import platformer.model.gameObjects.objects.Potion;
import platformer.model.inventory.InventoryItem;
import platformer.model.perks.PerksBonus;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * Handles all logic related to loot generation and collection.
 */
public class LootHandler {

    private final ObjectManager objectManager;
    private final EffectManager effectManager;

    private static final List<String> HERB_LOOT_TABLE = List.of(
            "WILD_GRASS", "CAVERN_BEANS", "GOBLINS_IVY",
            "FAE_LEAF", "GHOST_LEAF", "WITCHS_WORT", "FROST_BLOOM_PETALS"
    );

    public LootHandler(ObjectManager objectManager, EffectManager effectManager) {
        this.objectManager = objectManager;
        this.effectManager = effectManager;
    }

    /**
     * Generates loot from a broken container.
     */
    public void generateCrateLoot(Container container) {
        Random rand = new Random();
        int value = rand.nextInt(4) - 1;
        ObjType obj = null;
        if (value == 0) obj = ObjType.STAMINA_POTION;
        else if (value == 1) obj = ObjType.HEAL_POTION;
        if (obj != null) {
            int xPos = (int) (container.getHitBox().x + container.getHitBox().width / 2);
            int yPos = (int) (container.getHitBox().y - container.getHitBox().height / 4);
            objectManager.addGameObject(new Potion(obj, xPos, yPos));
        }
    }

    /**
     * Generates loot from a defeated enemy.
     */
    public void generateEnemyLoot(Rectangle2D.Double location, EnemyType enemyType) {
        generateCoins(location);
        generateLoot(location, enemyType);
    }

    private void generateCoins(Rectangle2D.Double location) {
        Random rand = new Random();
        int n = rand.nextInt(7 + PerksBonus.getInstance().getBonusCoin());
        for (int i = 0; i < n; i++) {
            int x = (int) location.getCenterX();
            int y = (int) location.y;
            double initialYSpeed = -2.2 * SCALE - (rand.nextDouble() * 1.5 * SCALE);
            double initialXSpeed = (rand.nextDouble() - 0.5) * (2.5 * SCALE);
            Coin coin = new Coin(ObjType.COIN, x, y, initialXSpeed, initialYSpeed);
            objectManager.addGameObject(coin);
        }
    }

    private void generateLoot(Rectangle2D.Double location, EnemyType enemyType) {
        int x = (int) (location.width / 4) + (int) location.x;
        int y = (int) (location.height / 2.3) + (int) location.y;
        Loot loot = new Loot(ObjType.LOOT, x, y, enemyType);
        objectManager.addGameObject(loot);
    }

    /**
     * Handles the collection of an item by the player.
     */
    public void collectItem(GameObject object, Player player) {
        if (object instanceof Potion) {
            applyPotionEffect((Potion) object, player);
            object.setAlive(false);
        } else if (object instanceof Coin) {
            Audio.getInstance().getAudioPlayer().playSound(Sound.COIN_PICK);
            player.changeCoins(1);
            object.setAlive(false);
        }
    }

    /**
     * Applies the effect of a potion to the player.
     */
    private void applyPotionEffect(Potion potion, Player player) {
        if (potion == null) return;
        switch (potion.getObjType()) {
            case HEAL_POTION:
                player.changeHealth(HEAL_POTION_VAL);
                break;
            case STAMINA_POTION:
                player.changeStamina(STAMINA_POTION_VAL);
                break;
        }
    }

    /**
     * Handles the harvesting of a herb, giving the player loot.
     */
    public void harvestHerb(GameObject herb, Player player) {
        Audio.getInstance().getAudioPlayer().playSound(Sound.CRATE_BREAK_1);
        effectManager.spawnDustParticles(herb.getHitBox().getCenterX(), herb.getHitBox().getCenterY() - (10 * SCALE), 15, DustType.HERB_CUT, 0, null);

        Random rand = new Random();
        String randomHerbId = HERB_LOOT_TABLE.get(rand.nextInt(HERB_LOOT_TABLE.size()));
        InventoryItem item = new InventoryItem(randomHerbId, 1);
        player.getInventory().addItemToBackpack(item);

        herb.setAlive(false);
    }

}
