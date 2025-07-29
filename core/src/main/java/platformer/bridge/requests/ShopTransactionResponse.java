package platformer.bridge.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ShopTransactionResponse {
    private String message;
    private List<ShopItemDTO> updatedShopInventory;
}