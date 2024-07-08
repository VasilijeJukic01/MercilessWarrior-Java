package platformer.bridge;

import platformer.core.Account;
import platformer.model.BoardItem;

import java.util.List;

/**
 * Interface for the connector between the game and services.
 */
public interface Bridge {

    /**
     * Creates a new account with the given username and password.
     *
     * @param username the username of the account
     * @param password the password of the account
     * @return the account id
     */
    int createAccount(String username, String password);

    /**
     * Loads the account data for the given username and password.
     *
     * @param name the username of the account
     * @param password the password of the account
     * @return the account data
     */
    Account loadAccountData(String name, String password);

    /**
     * Loads the account data for the given account id.
     *
     * @return the account data
     */
    List<BoardItem> loadLeaderboardData();

    /**
     * Updates the account data for the given account.
     *
     * @param account the account data
     */
    void updateAccountData(Account account);

}
