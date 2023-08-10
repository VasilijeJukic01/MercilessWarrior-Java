package platformer.model.gameObjects.objects;

import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.ui.ItemType;
import platformer.ui.ShopItem;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.HEALTH_ITEM;
import static platformer.constants.FilePaths.STAMINA_ITEM;

public class Shop extends GameObject {

    private boolean active;
    private final ArrayList<ShopItem> shopItems;

    public Shop(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        this.shopItems = new ArrayList<>();
        generateHitBox();
        getItems();
    }

    // Init
    private void generateHitBox() {
        super.animate = true;
        initHitBox(SHOP_HB_WID, SHOP_HB_HEI);
        super.xOffset = SHOP_OFFSET_X;
        super.yOffset = SHOP_OFFSET_Y;
    }

    private void getItems() {
        int slot = 0;
        Random rand = new Random();
        BufferedImage healthItemImg = Utils.getInstance().importImage(HEALTH_ITEM, -1, -1);
        shopItems.add(new ShopItem(ItemType.HEALTH, healthItemImg, slot++, rand.nextInt(10)+1, HEALTH_COST));
        BufferedImage staminaItemImg = Utils.getInstance().importImage(STAMINA_ITEM, -1, -1);
        shopItems.add(new ShopItem(ItemType.STAMINA, staminaItemImg, slot, rand.nextInt(6)+1, STAMINA_COST));
    }

    public void buyItem(Player player, int slot) {
        for (ShopItem item : shopItems) {
            if (item.getAmount() > 0 && item.getSlot() == slot) {
                if (player.getCoins() >= item.getCost()) {
                    player.changeCoins(-item.getCost());
                    item.setAmount(item.getAmount()-1);
                    Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
                    switch(item.getItemType()) {
                        case HEALTH: player.changeHealth(HEALTH_VAL); break;
                        case STAMINA: player.changeStamina(STAMINA_VAL); break;
                        default: break;
                    }
                }
            }
        }
    }

    // Core
    @Override
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset + (int)(1*SCALE);
        g.drawImage(animations[animIndex], x, y, SHOP_WID, SHOP_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
        render(g, xLevelOffset, yLevelOffset);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, FONT_BIG));
            int infoX = (int)(hitBox.x + hitBox.width / 3 - xLevelOffset);
            int infoY = (int)(hitBox.y - yLevelOffset + 25 * SCALE);
            g.drawString("SHOP", infoX, infoY);
        }
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ArrayList<ShopItem> getShopItems() {
        return shopItems;
    }

}
