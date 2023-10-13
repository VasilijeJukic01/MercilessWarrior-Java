package platformer.database.bridge;

import platformer.core.Account;
import platformer.model.BoardItem;
import platformer.database.Settings;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SQLStorage implements Storage {

    private final Settings settings;
    private final DatabaseConnection databaseConnection;

    private static final String FIND_ACCOUNT_QUERY = "SELECT * FROM Accounts WHERE Name LIKE ?";
    private static final String FIND_SETTINGS_QUERY = "SELECT * FROM Settings WHERE account_id = ?";
    private static final String FIND_PERKS_QUERY = "SELECT * FROM Perks WHERE settings_id = ?";
    private static final String FIND_ITEMS_QUERY = "SELECT * FROM Items WHERE settings_id = ?";
    private static final String UPDATE_SETTINGS_QUERY = "UPDATE Settings SET spawn = ?, coins = ?, tokens = ?, level = ?, exp = ? WHERE account_id = ?";
    private static final String DELETE_PERKS_QUERY = "DELETE FROM Perks WHERE settings_id = ?";
    private static final String INSERT_PERK_QUERY = "INSERT INTO Perks (perk_id, settings_id, name) VALUES (?, ?, ?)";
    private static final String DELETE_ITEMS_QUERY = "DELETE FROM Items WHERE settings_id = ?";
    private static final String INSERT_ITEM_QUERY = "INSERT INTO Items (item_id, settings_id, name, amount, equiped) VALUES (?, ?, ?, ?, ?)";

    private static final String MAX_PERK_ID_QUERY = "SELECT MAX(perk_id) FROM Perks";
    private static final String MAX_ITEM_ID_QUERY = "SELECT MAX(item_id) FROM Items";

    public SQLStorage(Settings settings) {
        this.settings = settings;
        this.databaseConnection = new DatabaseConnection();
    }

    // Core
    @Override
    public Account loadData(String user) {
        if (user.isEmpty()) return new Account();

        try {
            databaseConnection.initConnection(settings);
            Logger.getInstance().notify("Database connection established!", Message.INFORMATION);

            Account account = findAccount(user);
            if (account != null) {
                int settingsId = account.getSettingsID();
                List<String> perks = findPerks(settingsId);
                List<String> items = findItems(settingsId);
                account.setPerks(perks);
                account.setItems(items);
            }
            return account;
        }
        catch (SQLException e) {
            Logger.getInstance().notify("Databased connection failed!", Message.ERROR);
        }
        finally {
            if (databaseConnection.getConnection() != null)
                databaseConnection.closeConnection(true);
        }

        return new Account();
    }

    @Override
    public List<BoardItem> loadLeaderboardData() {
        List<BoardItem> boardData = new ArrayList<>();

        try {
            databaseConnection.initConnection(settings);

            try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement("SELECT * FROM Accounts")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int id = resultSet.getInt("Account_ID");
                        String name = resultSet.getString("Name");
                        Account account = findSettings(id, name);

                        if (account == null) return  Collections.emptyList();
                        boardData.add(new BoardItem(name, account.getLevel(), account.getExp()));
                    }
                }
            }
            boardData.sort(Comparator.comparing(BoardItem::getLevel).reversed().thenComparing(Comparator.comparing(BoardItem::getExp).reversed()));

            return boardData;

        }
        catch (Exception ignored) {}
        finally {
            if (databaseConnection.getConnection() != null)
                databaseConnection.closeConnection(false);
        }

        return Collections.emptyList();
    }

    @Override
    public void updateData(Account account) {
        try {
            databaseConnection.initConnection(settings);
            Logger.getInstance().notify("Database connection established!", Message.INFORMATION);

            updateSettings(account);
            deletePerks(account.getSettingsID());
            deleteItems(account.getSettingsID());
            insertPerks(account);
            insertItems(account);
        }
        catch (SQLException e) {
            Logger.getInstance().notify("Databased connection failed!", Message.ERROR);
        }
        finally {
            if (databaseConnection.getConnection() != null)
                databaseConnection.closeConnection(true);
        }
    }

    // Operations
    private Account findAccount(String user) throws SQLException {
        try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement(FIND_ACCOUNT_QUERY)) {
            preparedStatement.setString(1, "%" + user + "%");
            try (ResultSet accountResultSet = preparedStatement.executeQuery()) {
                if (accountResultSet.next()) {
                    int accountId = accountResultSet.getInt("Account_ID");
                    return findSettings(accountId, user);
                }
            }
        }
        return null;
    }

    private Account findSettings(int accountId, String user) throws SQLException {
        try (PreparedStatement settingsPreparedStatement = databaseConnection.getConnection().prepareStatement(FIND_SETTINGS_QUERY)) {
            settingsPreparedStatement.setInt(1, accountId);
            try (ResultSet settingsResultSet = settingsPreparedStatement.executeQuery()) {
                if (settingsResultSet.next()) {
                    int settingsId = settingsResultSet.getInt("Settings_ID");
                    int spawn = settingsResultSet.getInt("spawn");
                    int coins = settingsResultSet.getInt("coins");
                    int tokens = settingsResultSet.getInt("tokens");
                    int level = settingsResultSet.getInt("level");
                    int exp = settingsResultSet.getInt("exp");
                    return new Account(user, accountId, settingsId, spawn, coins, tokens, level, exp);
                }
            }
        }
        return null;
    }

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

    private List<String> findPerks(int settingsId) throws SQLException {
        return findData(settingsId, FIND_PERKS_QUERY, "name");
    }

    private List<String> findItems(int settingsId) throws SQLException {
        return findData(settingsId, FIND_ITEMS_QUERY, "name", "amount", "equiped");
    }

    private void updateSettings(Account account) throws SQLException {
        try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement(UPDATE_SETTINGS_QUERY)) {
            preparedStatement.setInt(1, account.getSpawn());
            preparedStatement.setInt(2, account.getCoins());
            preparedStatement.setInt(3, account.getTokens());
            preparedStatement.setInt(4, account.getLevel());
            preparedStatement.setInt(5, account.getExp());
            preparedStatement.setInt(6, account.getAccountID());
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

    private void insertPerks(Account account) throws SQLException {
        insertData(account, INSERT_PERK_QUERY, MAX_PERK_ID_QUERY, account.getPerks(), new String[]{"name"});
    }

    private void insertItems(Account account) throws SQLException {
        insertData(account, INSERT_ITEM_QUERY, MAX_ITEM_ID_QUERY, account.getItems(), new String[]{"name", "amount", "equiped"});
    }

    // Other
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
