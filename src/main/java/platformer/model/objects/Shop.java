package platformer.model.objects;

import platformer.audio.Audio;
import platformer.audio.Sounds;
import platformer.model.Tiles;
import platformer.model.entities.Player;
import platformer.ui.ItemType;
import platformer.ui.ShopItem;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Shop extends GameObject {

    private boolean active;
    private ArrayList<ShopItem> shopItems;
    private final Random rand;

    public Shop(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        this.rand = new Random();
        generateHitBox();
        getItems();
    }

    private void generateHitBox() {
        super.animate = true;
        int hbWid = (int)(154 * Tiles.SCALE.getValue());
        int hbHei = (int)(132 * Tiles.SCALE.getValue());
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(1 * Tiles.SCALE.getValue());
        super.yOffset = (int)(1 * Tiles.SCALE.getValue());
    }

    private void getItems() {
        this.shopItems = new ArrayList<>();
        int slot = 0;
        BufferedImage healthItemImg = Utils.getInstance().importImage("src/main/resources/images/shop/HealthItem.png", -1, -1);
        shopItems.add(new ShopItem(ItemType.HEALTH, healthItemImg, slot++, rand.nextInt(10)+1, 15));
        BufferedImage staminaItemImg = Utils.getInstance().importImage("src/main/resources/images/shop/StaminaItem.png", -1, -1);
        shopItems.add(new ShopItem(ItemType.STAMINA, staminaItemImg, slot, rand.nextInt(6)+1, 20));
    }

    public void buyItem(Player player, int slot) {
        for (ShopItem item : shopItems) {
            if (item.getAmount() > 0 && item.getSlot() == slot) {
                if (player.getCoins() >= item.getCost()) {
                    player.changeCoins(-item.getCost());
                    item.setAmount(item.getAmount()-1);
                    Audio.getInstance().getAudioPlayer().playSound(Sounds.SHOP_BUY.ordinal());
                    switch(item.getItemType()) {
                        case HEALTH: player.changeHealth(30); break;
                        case STAMINA: player.changeStamina(30); break;
                        default: break;
                    }
                }
            }
        }
    }

    public void update() {
        if (animate) updateAnimation();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            int infoX = (int)(hitBox.x+hitBox.width/3-xLevelOffset);
            int infoY = (int)(hitBox.y-yLevelOffset+25*Tiles.SCALE.getValue());
            g.drawString("SHOP", infoX, infoY);
        }
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.ORANGE);
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
