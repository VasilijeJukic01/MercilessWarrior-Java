package platformer.model.perks;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import platformer.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Perk {

    private String id;
    private String name;
    private String branch;
    private String description;
    private int cost;
    private String imageName;
    private int slot;
    private double treeX;
    private double treeY;
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
            this.image = ImageUtils.importImage("/images/objs/perks/" + imageName + ".png", -1, -1);
        }
    }
}
