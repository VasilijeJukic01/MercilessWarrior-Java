package platformer.database.bridge;

import platformer.core.Account;
import platformer.model.BoardItem;

import java.util.List;

public interface Storage {

    Account loadData(String name);

    List<BoardItem> loadLeaderboardData();

    void updateData(Account account);

}
