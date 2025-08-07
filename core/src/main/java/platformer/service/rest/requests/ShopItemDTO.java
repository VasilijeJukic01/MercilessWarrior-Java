package platformer.service.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemDTO {
    private String itemId;
    private int stock;
    private int cost;
}