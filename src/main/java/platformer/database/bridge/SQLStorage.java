package platformer.database.bridge;

import org.mindrot.jbcrypt.BCrypt;
import platformer.core.Account;
import platformer.model.BoardItem;
import platformer.database.Settings;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 The SQLStorage class is a bridge between the game and the SQL database.
 It is responsible for executing SQL queries and operations on the database.
 */
public class SQLStorage implements Storage {

    private final Settings settings;
    private final DatabaseConnection databaseConnection;

    private static final String FIND_ACCOUNT_QUERY = "SELECT * FROM Users WHERE username LIKE ?";
    private static final String FIND_SETTINGS_QUERY = "SELECT * FROM Settings WHERE userId = ?";
    private static final String FIND_PERKS_QUERY = "SELECT * FROM Perks WHERE settingsId = ?";
    private static final String FIND_ITEMS_QUERY = "SELECT * FROM Items WHERE settingsId = ?";
    private static final String UPDATE_SETTINGS_QUERY = "UPDATE Settings SET spawnId = ?, coins = ?, tokens = ?, level = ?, exp = ? WHERE userId = ?";
    private static final String DELETE_PERKS_QUERY = "DELETE FROM Perks WHERE settingsId = ?";
    private static final String DELETE_ITEMS_QUERY = "DELETE FROM Items WHERE settingsId = ?";
    private static final String INSERT_SETTINGS_QUERY = "INSERT INTO Settings (id, userId, spawnId, coins, tokens, level, exp) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_PERK_QUERY = "INSERT INTO Perks (id, settingsId, name) VALUES (?, ?, ?)";
    private static final String INSERT_ITEM_QUERY = "INSERT INTO Items (id, settingsId, name, amount, equiped) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_USER_QUERY = "INSERT INTO Users (id, username, password, roleId) VALUES (?, ?, ?, ?)";

    private static final String MAX_USER_ID_QUERY = "SELECT MAX(id) FROM Users";
    private static final String MAX_PERK_ID_QUERY = "SELECT MAX(id) FROM Perks";
    private static final String MAX_ITEM_ID_QUERY = "SELECT MAX(id) FROM Items";

    public SQLStorage(Settings settings) {
        this.settings = settings;
        this.databaseConnection = new DatabaseConnection();
    }

    /**
     * Executes a given action on the database.
     * @param action the action to be executed
     */
    private void execute(Consumer<Connection> action) {
        try {
            databaseConnection.initConnection(settings);
            action.accept(databaseConnection.getConnection());
        } catch (SQLException e) {
            Logger.getInstance().notify("Database operation failed!", Message.ERROR);
        } finally {
            if (databaseConnection.getConnection() != null)
                databaseConnection.closeConnection(true);
        }
    }

    // Operations
    @Override
    public int createAccount(String username, String password) {
        AtomicInteger result = new AtomicInteger(2);
        execute(connection -> {
            try {
                if (findAccount(username) != null) {
                    result.set(2);
                    return;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_QUERY)) {
                String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

                int newUserId = findMaxID(MAX_USER_ID_QUERY) + 1;

                preparedStatement.setInt(1, newUserId);
                preparedStatement.setString(2, username);
                preparedStatement.setString(3, hashed);
                preparedStatement.setInt(4, 2);
                preparedStatement.executeUpdate();

                insertSettings(newUserId, connection);

                result.set(0);
            } catch (SQLException e) {
                Logger.getInstance().notify("Database operation failed! [REGISTRATION]", Message.ERROR);
                result.set(1);
            }
        });
        return result.get();
    }

    @Override
    public Account loadAccountData(String user, String password) {
        AtomicReference<Account> account = new AtomicReference<>(new Account());
        execute(connection -> {
            if (user.isEmpty()) return;
            try {
                Account foundAccount = findAccount(user);
                if (foundAccount != null) {
                    if (!BCrypt.checkpw(password, foundAccount.getPassword())) {
                        Logger.getInstance().notify("Invalid password!", Message.ERROR);
                        return;
                    }
                    int settingsId = foundAccount.getSettingsID();
                    List<String> perks = findPerks(settingsId);
                    List<String> items = findItems(settingsId);
                    foundAccount.setPerks(perks);
                    foundAccount.setItems(items);
                    account.set(foundAccount);
                }
            } catch (SQLException e) {
                Logger.getInstance().notify("Database operation failed! [LOAD DATA]", Message.ERROR);
            }
        });
        return account.get();
    }

    @Override
    public List<BoardItem> loadLeaderboardData() {
        List<BoardItem> boardData = new ArrayList<>();
        execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Users")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String username = resultSet.getString("username");
                        String password = resultSet.getString("password");
                        Account account = findSettings(id, username, password);

                        if (account == null) return;
                        boardData.add(new BoardItem(username, account.getLevel(), account.getExp()));
                    }
                }
            } catch (SQLException e) {
                Logger.getInstance().notify("Database operation failed! [LEADERBOARD]", Message.ERROR);
            }
            boardData.sort(Comparator.comparing(BoardItem::getLevel).reversed().thenComparing(Comparator.comparing(BoardItem::getExp).reversed()));
        });
        return boardData;
    }

    @Override
    public void updateAccountData(Account account) {
        execute(connection -> {
            try {
                updateSettings(account);
                deletePerks(account.getSettingsID());
                deleteItems(account.getSettingsID());
                insertPerks(account);
                insertItems(account);
            } catch (SQLException e) {
                Logger.getInstance().notify("Database operation failed! [UPDATE DATA]", Message.ERROR);
            }
        });
    }

    // Queries
    private Account findAccount(String user) throws SQLException {
        try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement(FIND_ACCOUNT_QUERY)) {
            preparedStatement.setString(1, "%" + user + "%");
            try (ResultSet accountResultSet = preparedStatement.executeQuery()) {
                if (accountResultSet.next()) {
                    int accountId = accountResultSet.getInt("id");
                    String password = accountResultSet.getString("password");
                    return findSettings(accountId, user, password);
                }
            }
        }
        return null;
    }

    private Account findSettings(int accountId, String user, String password) throws SQLException {
        try (PreparedStatement settingsPreparedStatement = databaseConnection.getConnection().prepareStatement(FIND_SETTINGS_QUERY)) {
            settingsPreparedStatement.setInt(1, accountId);
            try (ResultSet settingsResultSet = settingsPreparedStatement.executeQuery()) {
                if (settingsResultSet.next()) {
                    int settingsId = settingsResultSet.getInt("id");
                    int spawn = settingsResultSet.getInt("spawnId");
                    int coins = settingsResultSet.getInt("coins");
                    int tokens = settingsResultSet.getInt("tokens");
                    int level = settingsResultSet.getInt("level");
                    int exp = settingsResultSet.getInt("exp");
                    return new Account(user, password, accountId, settingsId, spawn, coins, tokens, level, exp);
                }
            }
        }
        return null;
    }

    private List<String> findPerks(int settingsId) throws SQLException {
        return findData(settingsId, FIND_PERKS_QUERY, "name");
    }

    private List<String> findItems(int settingsId) throws SQLException {
        return findData(settingsId, FIND_ITEMS_QUERY, "name", "amount", "equiped");
    }

    private void updateSettings(Account account) throws SQLException {
        try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement(UPDATE_SETTINGS_QUERY)) {
            int[] params = {account.getSpawn(), account.getCoins(), account.getTokens(), account.getLevel(), account.getExp(), account.getAccountID()};
            IntStream.range(0, params.length).forEach(i -> {
                try {
                    preparedStatement.setInt(i + 1, params[i]);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            preparedStatement.executeUpdate();
        }
    }

    private void deleteData(int settingsId, String query) throws SQLException {
        try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement(query)) {
            preparedStatement.setInt(1, settingsId);
            preparedStatement.executeUpdate();
        }
    }

    private void deletePerks(int settingsId) throws SQLException {
        deleteData(settingsId, DELETE_PERKS_QUERY);
    }

    private void deleteItems(int settingsId) throws SQLException {
        deleteData(settingsId, DELETE_ITEMS_QUERY);
    }

    private void insertData(Account account, String query, String maxIdQuery, List<String> itemData, String[] itemDataColumns) throws SQLException {
        try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement(query)) {
            int settingsId = account.getSettingsID();
            int maxId = findMaxID(maxIdQuery) + 1;
            for (String item : itemData) {
                String[] itemValues = item.split(",");
                preparedStatement.setInt(1, maxId++);
                preparedStatement.setInt(2, settingsId);
                for (int i = 0; i < itemDataColumns.length; i++) {
                    preparedStatement.setObject(i + 3, itemValues[i]);
                }
                preparedStatement.executeUpdate();
            }
        }
    }

    private void insertSettings(int userId, Connection connection) throws SQLException {
        try (PreparedStatement settingsStatement = connection.prepareStatement(INSERT_SETTINGS_QUERY)) {
            settingsStatement.setInt(1, findMaxID("SELECT MAX(id) FROM Settings") + 1);
            settingsStatement.setInt(2, userId);
            settingsStatement.setInt(3, 0);
            settingsStatement.setInt(4, 0);
            settingsStatement.setInt(5, 0);
            settingsStatement.setInt(6, 1);
            settingsStatement.setInt(7, 0);
            settingsStatement.executeUpdate();
        }
    }

    private void insertPerks(Account account) throws SQLException {
        insertData(account, INSERT_PERK_QUERY, MAX_PERK_ID_QUERY, account.getPerks(), new String[]{"name"});
    }

    private void insertItems(Account account) throws SQLException {
        insertData(account, INSERT_ITEM_QUERY, MAX_ITEM_ID_QUERY, account.getItems(), new String[]{"name", "amount", "equiped"});
    }

    // Other
    private List<String> findData(int settingsId, String query, String... columnNames) throws SQLException {
        List<String> data = new ArrayList<>();
        try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement(query)) {
            preparedStatement.setInt(1, settingsId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    StringBuilder rowData = new StringBuilder();
                    for (String columnName : columnNames) {
                        rowData.append(resultSet.getString(columnName)).append(",");
                    }
                    data.add(rowData.substring(0, rowData.length() - 1));
                }
            }
        }
        return data;
    }

    private int findMaxID(String query) {
        int maxId = 0;
        try (Statement statement = databaseConnection.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                maxId = resultSet.getInt(1);
            }
        }
        catch (Exception ignored) {}
        return maxId;
    }

}
