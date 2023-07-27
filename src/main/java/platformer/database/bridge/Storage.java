package platformer.database.bridge;

import platformer.core.Account;

public interface Storage {

    Account loadData(String name);

    void updateData(Account account);

}
