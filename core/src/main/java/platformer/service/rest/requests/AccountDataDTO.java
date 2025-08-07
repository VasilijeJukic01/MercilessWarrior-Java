package platformer.service.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDataDTO {
    private String username;
    private long accountId;
    private long settingsId;
    private int spawn;
    private int coins;
    private int tokens;
    private int level;
    private int exp;
    private List<String> perks;
    private List<String> items;
}
