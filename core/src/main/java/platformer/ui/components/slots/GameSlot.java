package platformer.ui.components.slots;

import platformer.core.Account;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Represents a styled runic save slot card for either a cloud save or a local save slot.
 */
public class GameSlot {

    private final boolean databaseSlot;

    private Account account;
    private final int xPos, yPos;
    private boolean selected;
    private final int index;

    public GameSlot(Account account, int xPos, int yPos, boolean databaseSlot, int index) {
        this.account = account;
        this.xPos = xPos;
        this.yPos = yPos;
        this.databaseSlot = databaseSlot;
        this.index = index;
    }

    public void render(Graphics g) {
        renderAt(g, xPos, yPos, GAME_SLOT_WID, GAME_SLOT_HEI);
    }

    /**
     * Renders this save slot at custom coordinates and dimensions so it can adapt to different overlays.
     */
    public void renderAt(Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int arc = scale(6);
        GradientPaint gp;
        if (databaseSlot) {
            gp = new GradientPaint(x, y, new Color(16, 36, 48, 235), x, y + height, new Color(22, 52, 68, 240));
        }
        else if (account != null) {
            gp = new GradientPaint(x, y, new Color(16, 28, 34, 235), x, y + height, new Color(24, 42, 48, 240));
        }
        else {
            gp = new GradientPaint(x, y, new Color(12, 18, 22, 215), x, y + height, new Color(18, 26, 30, 220));
        }
        g2d.setPaint(gp);
        g2d.fillRoundRect(x, y, width, height, arc, arc);

        // Border
        if (selected) {
            float pulse = (float) (0.7 + 0.3 * Math.sin(System.currentTimeMillis() / 180.0));
            g2d.setColor(new Color(45, 240, 230, (int) (90 * pulse)));
            g2d.setStroke(new BasicStroke(scale(2.6)));
            g2d.drawRoundRect(x - 1, y - 1, width + 2, height + 2, arc + 2, arc + 2);
            g2d.setColor(new Color(160, 255, 250, 245));
            g2d.setStroke(new BasicStroke(scale(1.3)));
            g2d.drawRoundRect(x, y, width, height, arc, arc);
        }
        else {
            g2d.setColor(account != null ? new Color(85, 118, 124, 200) : new Color(55, 72, 76, 160));
            g2d.setStroke(new BasicStroke(1.2f));
            g2d.drawRoundRect(x, y, width, height, arc, arc);
        }

        if (databaseSlot) renderDatabaseInfo(g2d, x, y, width, height);
        else renderLocalSlotInfo(g2d, x, y, width, height);
        g2d.dispose();
    }

    private void renderLocalSlotInfo(Graphics2D g2d, int x, int y, int width, int height) {
        int badgeSize = height - scale(12);
        int badgeX = x + scale(6);
        int badgeY = y + scale(6);

        Color fillCol = account != null ? new Color(35, 110, 120) : null;
        Color borderCol = account != null ? new Color(80, 215, 210) : new Color(65, 85, 90);
        UIButtonRenderer.drawRunicSlot(g2d, badgeX, badgeY, badgeSize, fillCol, borderCol, false);

        g2d.setFont(UIButtonRenderer.getFont(11f * SCALE));
        String slotNum = String.valueOf(index - 1);
        FontMetrics fmNum = g2d.getFontMetrics();
        g2d.setColor(account != null ? new Color(215, 250, 248) : new Color(110, 125, 130));
        g2d.drawString(slotNum, badgeX + (badgeSize - fmNum.stringWidth(slotNum)) / 2, badgeY + (badgeSize + (int) (fmNum.getAscent() * 0.72f)) / 2);

        int textLeft = badgeX + badgeSize + scale(10);
        int rightX = x + width - scale(10);

        if (account == null) {
            g2d.setFont(new Font("Arial", Font.ITALIC, FONT_MEDIUM));
            g2d.setColor(new Color(125, 145, 150));
            g2d.drawString("Empty Save Slot", textLeft, y + height / 2 + scale(4));
            return;
        }

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Slot " + (index - 1) + "  •  " + formatPlaytime(account.getPlaytime()), textLeft, y + scale(16));

        String lvlBadge = "LVL " + account.getLevel();
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        int badgeW = g2d.getFontMetrics().stringWidth(lvlBadge) + scale(10);
        Color lvlCol = new Color(90, 245, 235);
        UIButtonRenderer.drawBadge(g2d, lvlBadge, rightX - badgeW, y + scale(15), lvlCol, lvlCol);

        g2d.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
        g2d.setColor(new Color(170, 195, 200));
        String dateStr = account.getLastTimeSaved() != null ? account.getLastTimeSaved() : "Saved";
        g2d.drawString(dateStr, textLeft, y + height - scale(8));

        String coinStr = account.getCoins() + " Gold";
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        g2d.setColor(SHOP_TEXT_GOLD);
        g2d.drawString(coinStr, rightX - g2d.getFontMetrics().stringWidth(coinStr), y + height - scale(8));
    }

    private void renderDatabaseInfo(Graphics2D g2d, int x, int y, int width, int height) {
        int badgeSize = height - scale(12);
        int badgeX = x + scale(6);
        int badgeY = y + scale(6);

        boolean registered = account != null && !account.getName().equalsIgnoreCase("Default");
        Color fillCol = registered ? new Color(30, 95, 145) : null;
        Color borderCol = registered ? new Color(95, 195, 255) : new Color(65, 85, 90);
        UIButtonRenderer.drawRunicSlot(g2d, badgeX, badgeY, badgeSize, fillCol, borderCol, false);

        g2d.setFont(UIButtonRenderer.getFont(10f * SCALE));
        String cloudSymbol = "C";
        FontMetrics fmSym = g2d.getFontMetrics();
        g2d.setColor(registered ? new Color(180, 235, 255) : new Color(110, 125, 130));
        g2d.drawString(cloudSymbol, badgeX + (badgeSize - fmSym.stringWidth(cloudSymbol)) / 2, badgeY + (badgeSize + (int) (fmSym.getAscent() * 0.72f)) / 2);

        int textLeft = badgeX + badgeSize + scale(10);
        int rightX = x + width - scale(10);

        if (!registered) {
            g2d.setFont(new Font("Arial", Font.ITALIC, FONT_MEDIUM));
            g2d.setColor(new Color(130, 150, 160));
            g2d.drawString("Cloud Slot (Offline)", textLeft, y + height / 2 + scale(4));
            return;
        }

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g2d.setColor(new Color(185, 235, 255));
        g2d.drawString("Cloud Save (" + account.getName() + ")", textLeft, y + scale(16));

        String lvlBadge = "LVL " + account.getLevel();
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_SMALL));
        int badgeW = g2d.getFontMetrics().stringWidth(lvlBadge) + scale(10);
        Color lvlCol = new Color(110, 215, 255);
        UIButtonRenderer.drawBadge(g2d, lvlBadge, rightX - badgeW, y + scale(15), lvlCol, lvlCol);

        g2d.setFont(new Font("Arial", Font.PLAIN, FONT_LIGHT));
        g2d.setColor(new Color(165, 195, 210));
        g2d.drawString("XP: " + account.getExp(), textLeft, y + height - scale(8));

        String coinStr = account.getCoins() + " Gold";
        g2d.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        g2d.setColor(SHOP_TEXT_GOLD);
        g2d.drawString(coinStr, rightX - g2d.getFontMetrics().stringWidth(coinStr), y + height - scale(8));
    }

    private String formatPlaytime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }

    public boolean isPointInSlot(int x, int y) {
        return (x >= xPos && x <= xPos + GAME_SLOT_WID && y >= yPos && y <= yPos + GAME_SLOT_HEI);
    }

    // Getters and Setters
    public void setSelected(boolean value) {
        this.selected = value;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isDatabaseSlot() {
        return databaseSlot;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public int getIndex() {
        return index;
    }
}