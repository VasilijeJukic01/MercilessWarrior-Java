package platformer.model.gameObjects.objects;

import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.model.inventory.item.ShopItem;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static platformer.constants.Constants.*;

public class Shop extends GameObject implements Interactable {

    private boolean active;
    private final ArrayList<ShopItem> shopItems;

    private double animPhase = 0.0;
    private int blinkTimer = 180;
    private int lookOffsetX = 0;

    public Shop(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        this.shopItems = new ArrayList<>();
        generateHitBox();
    }

    // Init
    private void generateHitBox() {
        super.animate = false;
        initHitBox(SHOP_HB_WID, SHOP_HB_HEI);
        super.xOffset = SHOP_OFFSET_X;
        super.yOffset = SHOP_OFFSET_Y;
    }

    // Core
    @Override
    public void update() {
        animPhase += 0.045;
        blinkTimer--;
        if (blinkTimer < -14) {
            blinkTimer = 220 + (int) (Math.random() * 180);
        }
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int) hitBox.x - xOffset - xLevelOffset;
        int y = (int) hitBox.y - yOffset - yLevelOffset + (int) (5 * SCALE);
        g.drawImage(animations[0], x, y, SHOP_WID, SHOP_HEI, null);
        renderAmbientShopLife(g, x, y);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
    }

    /**
     * Brings the static shop sprite to life with flickering lantern halos, pulsing potion glows, and subtle blinking/player-tracking eyes under the merchant's hood.
     */
    private void renderAmbientShopLife(Graphics g, int shopX, int shopY) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float flicker1 = (float) (0.85 + 0.15 * Math.sin(animPhase * 0.6 + Math.cos(animPhase * 0.3)));
        float flicker2 = (float) (0.85 + 0.15 * Math.sin(animPhase * 0.5 + 1.2));
        int lanternR = (int) (20 * SCALE);

        int leftLanternX = shopX + (int) (12 * SCALE), rightLanternX = shopX + (int) (139 * SCALE);
        int lanternY = shopY + (int) (51 * SCALE);

        drawSoftGlow(g2d, leftLanternX, lanternY, (int) (lanternR * flicker1), new Color(255, 175, 55, (int) (125 * flicker1)));
        drawSoftGlow(g2d, rightLanternX, lanternY, (int) (lanternR * flicker2), new Color(255, 175, 55, (int) (125 * flicker2)));

        float potionPulse = (float) (0.75 + 0.25 * Math.sin(animPhase * 0.6));
        int cyanPotionX = shopX + (int) (43 * SCALE);
        int redPotionX = shopX + (int) (101 * SCALE);
        int potionY = shopY + (int) (69 * SCALE);

        drawSoftGlow(g2d, cyanPotionX, potionY, (int) (15 * SCALE), new Color(0, 235, 245, (int) (80 * potionPulse)));
        drawSoftGlow(g2d, redPotionX, potionY, (int) (13 * SCALE), new Color(255, 50, 70, (int) (75 * potionPulse)));

        if (blinkTimer > 0) {
            int hoodCenterX = shopX + (int) (72 * SCALE) + lookOffsetX;
            int hoodEyeY = shopY + (int) (59 * SCALE);
            int eyeGap = (int) (2.2 * SCALE);
            int eyeW = Math.max(1, (int) (1.2 * SCALE));
            int eyeH = Math.max(1, (int) (1.0 * SCALE));

            g2d.setColor(new Color(0, 225, 235, 70));
            g2d.fillOval(hoodCenterX - eyeGap - eyeW, hoodEyeY - eyeH, eyeW * 3, eyeH * 3);
            g2d.fillOval(hoodCenterX + eyeGap - eyeW, hoodEyeY - eyeH, eyeW * 3, eyeH * 3);
            g2d.setColor(new Color(200, 250, 255, 200));
            g2d.fillRect(hoodCenterX - eyeGap, hoodEyeY, eyeW, eyeH);
            g2d.fillRect(hoodCenterX + eyeGap, hoodEyeY, eyeW, eyeH);
        }

        g2d.dispose();
    }

    private void drawSoftGlow(Graphics2D g2d, int cx, int cy, int radius, Color centerColor) {
        if (radius <= 2) return;
        Color edgeColor = new Color(centerColor.getRed(), centerColor.getGreen(), centerColor.getBlue(), 0);
        RadialGradientPaint paint = new RadialGradientPaint(new Point2D.Float(cx, cy), radius, new float[]{0.0f, 1.0f}, new Color[]{centerColor, edgeColor});
        g2d.setPaint(paint);
        g2d.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
    }

    @Override
    public void renderInteractionUI(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        int centerX = (int) (hitBox.getCenterX() - xLevelOffset);
        int promptY = (int) (hitBox.y - yLevelOffset + 32 * SCALE);
        UIButtonRenderer.drawInteractionPrompt(g, "SHOP", centerX, promptY);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
    }

    @Override
    public void onEnter(Player player) {
        this.active = true;
    }

    @Override
    public void onIntersect(Player player) {
        double dx = player.getHitBox().getCenterX() - hitBox.getCenterX();
        if (dx < -10 * SCALE) lookOffsetX = (int) (-1.5 * SCALE);
        else if (dx > 10 * SCALE) lookOffsetX = (int) (1.5 * SCALE);
        else lookOffsetX = 0;
    }

    @Override
    public void onExit(Player player) {
        this.active = false;
        this.lookOffsetX = 0;
    }

    @Override
    public String getInteractionPrompt() {
        return "Shop";
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