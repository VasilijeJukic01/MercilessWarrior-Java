package platformer.model.perks;

import platformer.utils.Utils;

import java.awt.image.BufferedImage;

public class Perk {

    private final int slot;
    private final BufferedImage image;
    private final String description;
    private final int cost;
    private final String name;
    // Flags
    private boolean locked = true, upgraded;

    public Perk(int slot, String imageName, String description, int cost, String name) {
        this.slot = slot;
        this.description = description;
        this.cost = cost;
        this.name = name;
        this.image = Utils.getInstance().importImage("src/main/resources/images/objs/perks/"+imageName+".png", -1, -1);
    }

    public int getSlot() {
        return slot;
    }

    public BufferedImage getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public String getName() {
        return name;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isUpgraded() {
        return upgraded;
    }

    public void setUpgraded(boolean upgraded) {
        this.upgraded = upgraded;
    }
}
