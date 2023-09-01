package platformer.database.bridge;

import platformer.core.Account;
import platformer.database.BoardDatum;

import java.util.List;

public interface Storage {

    Account loadData(String name);

    List<BoardDatum> loadLeaderboardData();

    void updateData(Account account);

}
