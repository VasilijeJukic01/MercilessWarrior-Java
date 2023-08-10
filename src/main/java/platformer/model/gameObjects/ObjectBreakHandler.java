package platformer.model.gameObjects;

import platformer.audio.Audio;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.player.PlayerBonus;
import platformer.model.gameObjects.objects.Coin;
import platformer.model.gameObjects.objects.Potion;
import platformer.model.spells.Flame;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

public class ObjectBreakHandler {

    private final ObjectManager objectManager;

    public ObjectBreakHandler(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public void checkObjectBreak(Rectangle2D.Double attackBox, Flame flame) {
        for (platformer.model.gameObjects.objects.Container container : getObjects(platformer.model.gameObjects.objects.Container.class)) {
            boolean isFlame = flame.getHitBox().intersects(container.getHitBox()) && flame.isActive();
            if (container.isAlive() && !container.animate && (attackBox.intersects(container.getHitBox()) || isFlame)) {
                container.setAnimate(true);
                Audio.getInstance().getAudioPlayer().playCrateSound();
                Logger.getInstance().notify("Player breaks container.", Message.NOTIFICATION);
                generateLoot(container);
            }
        }
    }

    private void generateLoot(platformer.model.gameObjects.objects.Container container) {
        Random rand = new Random();
        int value = rand.nextInt(4)-1;
        ObjType obj = null;
        if (value == 0) obj = ObjType.STAMINA_POTION;
        else if (value == 1) obj = ObjType.HEAL_POTION;
        if (obj != null) {
            int xPos = (int)(container.getHitBox().x + container.getHitBox().width / 2);
            int yPos = (int)(container.getHitBox().y - container.getHitBox().height / 4);
            objectManager.addGameObject(new Potion(obj, xPos, yPos));
        }
    }

    public void generateCoins(Rectangle2D.Double location) {
        Random rand = new Random();
        int n = rand.nextInt(7+ PlayerBonus.getInstance().getBonusCoin());
        for (int i = 0; i < n; i++) {
            int x = rand.nextInt((int)location.width)+(int)location.x;
            int y = rand.nextInt((int)(location.height/3)) + (int)location.y + 2*(int)location.height/3;
            Coin coin = new Coin(ObjType.COIN, x, y);
            objectManager.addGameObject(coin);
        }
    }

    private <T> List<T> getObjects(Class<T> objectType) {
        return objectManager.getObjects(objectType);
    }

}
