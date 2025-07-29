package platformer.bridge.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDataDTO {
    private boolean canEquip;
    private String slot;
    private Map<String, Double> bonuses;
}