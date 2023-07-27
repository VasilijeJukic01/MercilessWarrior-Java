package platformer.database.bridge;

import platformer.core.Account;
import platformer.database.Settings;

import java.sql.*;

public class SQLStorage implements Storage {

    private final Settings settings;
    private Connection connection;

    public SQLStorage(Settings settings) {
        this.settings = settings;
    }

    // Connection
    private void initConnection() throws SQLException {
        String ip = (String) settings.getParameter("IP");
        String database = (String) settings.getParameter("DATABASE");
        String username = (String) settings.getParameter("USERNAME");
        String password = (String) settings.getParameter("PASSWORD");
        this.connection = DriverManager.getConnection("jdbc:mysql://"+ip+"/"+database,username,password);
    }

    private void closeConnection(){
        try{
            connection.close();
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            connection = null;
        }
    }

    @Override
    public Account loadData(String user) {
        int spawn = 1;
        int coins = 0, tokens = 0;
        int level = 1, exp = 0;

        try{
            initConnection();
            String findAccountQuery  = "SELECT * FROM Accounts WHERE Name LIKE ?";
            PreparedStatement preparedStatement = connection.prepareStatement(findAccountQuery);
            preparedStatement.setString(1, "%" + user + "%");
            ResultSet accountResultSet = preparedStatement.executeQuery();

            if (accountResultSet.next()) {
                int accountId = accountResultSet.getInt("Account_ID");

                String findSettingsQuery = "SELECT * FROM Settings WHERE account_id = ?";
                preparedStatement = connection.prepareStatement(findSettingsQuery);
                preparedStatement.setInt(1, accountId);
                ResultSet settingsResultSet = preparedStatement.executeQuery();

                if (settingsResultSet.next()) {
                    spawn = settingsResultSet.getInt("spawn");
                    coins = settingsResultSet.getInt("coins");
                    tokens = settingsResultSet.getInt("tokens");
                    level = settingsResultSet.getInt("level");
                    exp = settingsResultSet.getInt("exp");

                }

                int settingsId = settingsResultSet.getInt("Settings_ID");

                String findPerksQuery = "SELECT * FROM Perks WHERE settings_id = ?";
                preparedStatement = connection.prepareStatement(findPerksQuery);
                preparedStatement.setInt(1, settingsId);
                ResultSet perksResultSet = preparedStatement.executeQuery();

                Account account = new Account(user, accountId, settingsId, spawn, coins, tokens, level, exp);

                while (perksResultSet.next()) {
                    account.getPerks().add(perksResultSet.getString("name"));
                }

                preparedStatement.close();
                accountResultSet.close();
                settingsResultSet.close();
                perksResultSet.close();

                return account;
            }
            return new Account();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            closeConnection();
        }
        return new Account();
    }

    @Override
    public void updateData(Account account) {
        try {
            initConnection();

            // Settings update
            String updateSettingsQuery = "UPDATE Settings SET spawn = ?, coins = ?, tokens = ?, level = ?, exp = ? WHERE account_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateSettingsQuery);
            preparedStatement.setInt(1, account.getSpawn());
            preparedStatement.setInt(2, account.getCoins());
            preparedStatement.setInt(3, account.getTokens());
            preparedStatement.setInt(4, account.getLevel());
            preparedStatement.setInt(5, account.getExp());
            preparedStatement.setInt(6, account.getAccountID());
            preparedStatement.executeUpdate();

            // Perks update
            String deletePerksQuery = "DELETE FROM Perks WHERE settings_id = ?";
            preparedStatement = connection.prepareStatement(deletePerksQuery);
            preparedStatement.setInt(1, account.getSettingsID());
            preparedStatement.executeUpdate();

            String insertPerkQuery = "INSERT INTO Perks (settings_id, name) VALUES (?, ?)";
            preparedStatement = connection.prepareStatement(insertPerkQuery);
            int settingsId = account.getSettingsID();
            for (String perk : account.getPerks()) {
                preparedStatement.setInt(1, settingsId);
                preparedStatement.setString(2, perk);
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeConnection();
        }
    }
}
