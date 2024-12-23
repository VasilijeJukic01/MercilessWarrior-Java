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
        Color slotColor = (!databaseSlot) ? SAVE_SLOT_COLOR : DATABASE_SLOT_COLOR;
        g.setColor(slotColor);
        g.fillRect(xPos, yPos, GAME_SLOT_WID, GAME_SLOT_HEI);

        if (!databaseSlot) renderSlotInfo(g);
        else renderDatabaseSlotInfo(g);

        if (selected) g.drawRect(xPos, yPos, GAME_SLOT_WID, GAME_SLOT_HEI);
    }

    private void renderSlotInfo(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        if (account == null) {
            g.drawString("Empty", xPos + (int)(GAME_SLOT_WID / 2.3), yPos + (int)(GAME_SLOT_HEI / 2.1));
        }
        else {
            g.drawString("Lvl: "+account.getLevel(), xPos + (int)(SCALE * 130), yPos + (int)(SCALE * 15));
            g.drawString("Exp: "+account.getExp(), xPos + (int)(SCALE * 130), yPos + (int)(SCALE * 30));
            g.drawString("Playtime: "+(account.getPlaytime()/3600)+"h", xPos + (int)(SCALE * 10), yPos + (int)(SCALE * 15));
            g.drawString(account.getLastTimeSaved(), xPos + (int)(SCALE * 10), yPos + (int)(SCALE * 30));
        }
    }

    private void renderDatabaseSlotInfo(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        if (account.getName().equalsIgnoreCase("Default")) {
            g.drawString("Unregistered", xPos + (int)(GAME_SLOT_WID / 2.8), yPos + (int)(GAME_SLOT_HEI / 2.1));
        }
        else {
            g.drawString("Database", xPos + (int)(SCALE * 10), yPos + (int)(SCALE * 15));
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
