package platformer.bridge.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemMasterDTO {
    private String itemId;
    private String name;
    private String description;
    private String rarity;
    private String imagePath;
    private int sellValue;
    private boolean stackable;
    private EquipmentDataDTO equip;
}