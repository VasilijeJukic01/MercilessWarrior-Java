package platformer.model.perks;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platformer.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Perk model that can be unlocked and upgraded in the game.
 * Each perk has a slot, image, description, cost, and name.
 * Perks can be locked or upgraded.
 */
@Getter
@Setter
@NoArgsConstructor
public class Perk {

    private String id;
    private String name;
    private String description;
    private int cost;
    private String imageName;
    private int slot;
    private boolean isStartPerk;
    private List<Integer> unlocks;
    private Map<String, Double> bonuses;

    @Expose(serialize = false, deserialize = false)
    private transient BufferedImage image;
    @Expose(serialize = false, deserialize = false)
    private boolean locked = true;
    @Expose(serialize = false, deserialize = false)
    private boolean upgraded;

    public void loadImage() {
        if (imageName != null) {
            this.image = ImageUtils.importImage("/images/objs/perks/"+imageName+".png", -1, -1);
        }
    }
}
