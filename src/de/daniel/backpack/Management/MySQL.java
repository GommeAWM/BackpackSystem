package de.daniel.backpack.Management;

import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.MainLogger;
import de.daniel.backpack.BackpackSystemMain;

import java.sql.*;

public class MySQL {

    private static final Config cfg = BackpackSystemMain.cfg;

    private static final String host = cfg.getSection("mysql").getString("host");
    private static final String port = cfg.getSection("mysql").getString("port");
    private static final String database = cfg.getSection("mysql").getString("database");
    private static final String username = cfg.getSection("mysql").getString("username");
    private static final String password = cfg.getSection("mysql").getString("password");

    private static Connection connection;
    private static final String prefix = "[" + BackpackSystemMain.getInstance().getName() + " - MySQL] ";
    private static final MainLogger logger = Server.getInstance().getLogger();

    public static void connect() {
        if (isConnected()) {
            logger.warning(prefix + "The database is already connected to the server!");
            return;
        }
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error(prefix + "Connection failed!\n" + "Error Message:" + e.getMessage());
            return;
        }
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

            logger.info(prefix + "successfully connected");
        } catch (SQLException e) {
            logger.error(prefix + "Connection failed!\n" + "Error Message:" + e.getMessage());
            return;
        }
    }

    public static void disconnect() {
        if (!isConnected()) {
            logger.warning(prefix + "The database is not connected to the server!");
            return;
        }
        try {
            connection.close();
            logger.info(prefix + "successfully disconnected");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        if (connection == null) {
            return false;
        }
        return true;
    }

    public static void update(String qry) {
        if (!isConnected()) {
            logger.warning(prefix + "The database is not connected to the server!");
            return;
        }
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(qry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet getResult(String qry) {
        if (!isConnected()) {
            logger.warning(prefix + "The database is not connected to the server!");
            return null;
        }
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(qry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
