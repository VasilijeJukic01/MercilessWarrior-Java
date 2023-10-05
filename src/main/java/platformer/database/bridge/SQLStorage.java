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
    private static final String UPDATE_SETTINGS_QUERY = "UPDATE Settings SET spawn = ?, coins = ?, tokens = ?, level = ?, exp = ? WHERE account_id = ?";
    private static final String DELETE_PERKS_QUERY = "DELETE FROM Perks WHERE settings_id = ?";
    private static final String INSERT_PERK_QUERY = "INSERT INTO Perks (settings_id, name) VALUES (?, ?)";

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
                account.setPerks(perks);
            }
            return account;
        }
        catch (SQLException e) {
            Logger.getInstance().notify("Databased connection failed!", Message.ERROR);
        }
        finally {
            if (databaseConnection.getConnection() != null)
                databaseConnection.closeConnection();
        }

        return new Account();
    }

    @Override
    public List<BoardItem> loadLeaderboardData() {
        List<BoardItem> boardData = new ArrayList<>();

        try {
            databaseConnection.initConnection(settings);
            Logger.getInstance().notify("Database connection established!", Message.INFORMATION);

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
        catch (SQLException e) {
            Logger.getInstance().notify("Database connection failed!", Message.ERROR);
        }
        finally {
            if (databaseConnection.getConnection() != null)
                databaseConnection.closeConnection();
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
            insertPerks(account);
        }
        catch (SQLException e) {
            Logger.getInstance().notify("Databased connection failed!", Message.ERROR);
        }
        finally {
            if (databaseConnection.getConnection() != null)
                databaseConnection.closeConnection();
        }
    }

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

    private List<String> findPerks(int settingsId) throws SQLException {
        List<String> perks = new ArrayList<>();
        try (PreparedStatement perksPreparedStatement = databaseConnection.getConnection().prepareStatement(FIND_PERKS_QUERY)) {
            perksPreparedStatement.setInt(1, settingsId);
            try (ResultSet perksResultSet = perksPreparedStatement.executeQuery()) {
                while (perksResultSet.next()) {
                    perks.add(perksResultSet.getString("name"));
                }
            }
        }
        return perks;
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

    private void deletePerks(int settingsId) throws SQLException {
        try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement(DELETE_PERKS_QUERY)) {
            preparedStatement.setInt(1, settingsId);
            preparedStatement.executeUpdate();
        }
    }

    private void insertPerks(Account account) throws SQLException {
        try (PreparedStatement preparedStatement = databaseConnection.getConnection().prepareStatement(INSERT_PERK_QUERY)) {
            int settingsId = account.getSettingsID();
            for (String perk : account.getPerks()) {
                preparedStatement.setInt(1, settingsId);
                preparedStatement.setString(2, perk);
                preparedStatement.executeUpdate();
            }
        }
    }
}
