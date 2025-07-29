package platformer.ui;

import platformer.core.Account;

import java.awt.*;

import static platformer.constants.Constants.FONT_MEDIUM;
import static platformer.constants.Constants.SCALE;
import static platformer.constants.UI.*;

/**
 * The GameSlot class represents a save slot in the game where a player's account information is stored.
 * It can be either a local save slot or a database slot.
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
        Graphics2D g2d = (Graphics2D) g;
        renderSlotRectangle(g2d);
        renderSelectionBorder(g2d);

        if (!databaseSlot) renderSlotInfo(g);
        else renderDatabaseSlotInfo(g);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(xPos, yPos, GAME_SLOT_WID, GAME_SLOT_HEI, 10, 10);
    }

    private void renderSlotRectangle(Graphics2D g2d) {
        GradientPaint gp;
        if (databaseSlot)
            gp = new GradientPaint(xPos, yPos, DATABASE_SLOT_BG_START, xPos, yPos + GAME_SLOT_HEI, DATABASE_SLOT_BG_END);
        else if (account != null)
            gp = new GradientPaint(xPos, yPos, SAVE_SLOT_BG_START, xPos, yPos + GAME_SLOT_HEI, SAVE_SLOT_BG_END);
        else
            gp = new GradientPaint(xPos, yPos, EMPTY_SLOT_BG_START, xPos, yPos + GAME_SLOT_HEI, EMPTY_SLOT_BG_END);
        g2d.setPaint(gp);
        g2d.fillRoundRect(xPos, yPos, GAME_SLOT_WID, GAME_SLOT_HEI, 10, 10);
    }

    private void renderSelectionBorder(Graphics2D g2d) {
        if (selected) {
            g2d.setColor(QUEST_SELECTED_GLOW_COLOR);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(xPos - 1, yPos - 1, GAME_SLOT_WID + 2, GAME_SLOT_HEI + 2, 12, 12);
        }
    }

    private void renderSlotInfo(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        if (account == null) {
            g.setColor(Color.GRAY);
            g.drawString("Empty", xPos + (int)(GAME_SLOT_WID / 2.3), yPos + (int)(GAME_SLOT_HEI / 1.75));
        }
        else {
            g.setColor(Color.WHITE);
            g.drawString("Lvl: "+account.getLevel(), xPos + (int)(SCALE * 130), yPos + (int)(SCALE * 15));
            g.drawString("Exp: "+account.getExp(), xPos + (int)(SCALE * 130), yPos + (int)(SCALE * 30));
            g.drawString("Playtime: "+(account.getPlaytime()/3600)+"h", xPos + (int)(SCALE * 10), yPos + (int)(SCALE * 15));
            g.drawString(account.getLastTimeSaved(), xPos + (int)(SCALE * 10), yPos + (int)(SCALE * 30));
        }
    }

    private void renderDatabaseSlotInfo(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        if (account.getName().equalsIgnoreCase("Default")) {
            g.setColor(Color.GRAY);
            g.drawString("Unregistered", xPos + (int)(GAME_SLOT_WID / 2.8), yPos + (int)(GAME_SLOT_HEI / 1.75));
        }
        else {
            g.setColor(Color.WHITE);
            g.drawString("Cloud Save", xPos + (int)(SCALE * 10), yPos + (int)(SCALE * 15));
            g.drawString("Lvl: "+account.getLevel(), xPos + (int)(SCALE * 130), yPos + (int)(SCALE * 15));
            g.drawString("Exp: "+account.getExp(), xPos + (int)(SCALE * 130), yPos + (int)(SCALE * 30));
        }
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
