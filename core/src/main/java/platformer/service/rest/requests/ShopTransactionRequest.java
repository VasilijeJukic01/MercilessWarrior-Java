package platformer.service.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShopTransactionRequest {
    private long userId;
    private String username;
    private String itemId;
    private int quantity;
}