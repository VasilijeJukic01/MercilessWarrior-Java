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
            this.initConnection();
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

                    settingsResultSet.close();
                    preparedStatement.close();
                    accountResultSet.close();
                }
                return new Account(user, spawn, coins, tokens, level, exp);
            }
            return new Account();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            this.closeConnection();
        }
        return new Account();
    }
}
