package platformer.model.gameObjects.objects;

import platformer.core.Framework;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.state.types.GameState;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Represents an ancient Save Totem that anchors the player's soul, allowing resting, saving, and fast travel.
 */
public class SaveTotem extends GameObject implements Interactable {

    private boolean active;
    private boolean discovered;
    private double runePhase = 0.0;

    public SaveTotem(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(SAVE_TOTEM_HB_WID, SAVE_TOTEM_HB_HEI);
        super.xOffset = SAVE_TOTEM_OFFSET_X;
        super.yOffset = SAVE_TOTEM_OFFSET_Y;
    }

    @Override
    public void update() {
        runePhase += 0.012;
        if (runePhase > Math.PI * 20) {
            runePhase -= Math.PI * 20;
        }
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int) hitBox.x - xOffset - xLevelOffset;
        int y = (int) hitBox.y - yOffset - yLevelOffset;
        renderRunicAura(g, (int) (hitBox.getCenterX() - xLevelOffset), (int) (hitBox.getCenterY() - yLevelOffset));
        g.drawImage(animations[animIndex], x, y, SAVE_TOTEM_WID, SAVE_TOTEM_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    private void renderRunicAura(Graphics g, int cx, int cy) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float pulse = (float) (0.80 + 0.20 * Math.sin(runePhase));
        int radius = (int) ((active ? 30 : 22) * SCALE * pulse);
        int alpha = (int) ((active ? 95 : 55) * pulse);

        if (radius > 2) {
            Color centerCol = new Color(55, 235, 225, alpha);
            Color edgeCol = new Color(20, 120, 130, 0);
            RadialGradientPaint aura = new RadialGradientPaint(new Point2D.Float(cx, cy - scale(4)), radius, new float[]{0.0f, 1.0f}, new Color[]{centerCol, edgeCol});
            g2d.setPaint(aura);
            g2d.fillOval(cx - radius, cy - scale(4) - radius, radius * 2, radius * 2);
        }

        for (int i = 0; i < 3; i++) {
            double p = (runePhase * 0.32 + i * 1.0) % 3.0;
            double norm = p / 3.0;
            int moteX = cx + (int) (Math.sin(runePhase * 0.8 + i * 2.1) * (9 * SCALE));
            int moteY = cy + scale(10) - (int) (norm * (30 * SCALE));
            int moteAlpha = (int) (Math.sin(norm * Math.PI) * (active ? 185 : 110));
            int moteS = Math.max(2, scale(2.0));
            g2d.setColor(new Color(140, 255, 245, Math.max(0, Math.min(255, moteAlpha))));
            g2d.fillOval(moteX - moteS / 2, moteY - moteS / 2, moteS, moteS);
        }

        g2d.dispose();
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
        if (!discovered && Framework.getInstance().getGame() != null && Framework.getInstance().getGame().getCurrentState() instanceof GameState gameState) {
            gameState.getContext().getWorldMemoryManager().unlockCurrentRoomTotem();
            discovered = true;
        }
    }

    @Override
    public void onIntersect(Player player) {

    }

    @Override
    public void onExit(Player player) {
        this.active = false;
    }

    @Override
    public String getInteractionPrompt() {
        return "SaveTotem";
    }

    @Override
    public void renderInteractionUI(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        UIButtonRenderer.drawInteractionPrompt(g, "TOTEM", (int) (hitBox.getCenterX() - xLevelOffset), (int) (hitBox.y - yLevelOffset - 10 * SCALE));
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
