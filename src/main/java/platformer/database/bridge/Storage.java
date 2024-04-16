package platformer.database.bridge;

import platformer.core.Account;
import platformer.model.BoardItem;

import java.util.List;

public interface Storage {

    int createAccount(String username, String password);

    Account loadAccountData(String name, String password);

    List<BoardItem> loadLeaderboardData();

    void updateAccountData(Account account);

}
