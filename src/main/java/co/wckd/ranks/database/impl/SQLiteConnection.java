package co.wckd.ranks.database.impl;

import co.wckd.ranks.database.DatabaseConnection;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@RequiredArgsConstructor
public class SQLiteConnection implements DatabaseConnection {

    private final File file;
    private Connection connection;

    @Override
    public void openConnection() {
        try {

            if (!file.exists())
                file.createNewFile();

            Class.forName("org.sqlite.JDBC");
            connect();
            createTables();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Connection getConnection(boolean autoCommit) {
        try {

            if (connection == null || connection.isClosed())
                connect();

            connection.setAutoCommit(autoCommit);

            return connection;

        } catch (Exception exception) {
            return null;
        }
    }

    private void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS vips (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "vip_type VARCHAR(50) NOT NULL, " +
                    "time BIGINT(19) NOT NULL, " +
                    "is_active BOOLEAN NOT NULL, " +
                    "primary key (uuid, vip_type) " +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS keys (" +
                    "key CHAR(19) NOT NULL, " +
                    "vip_type VARCHAR(50) NOT NULL, " +
                    "time BIGINT(19) NOT NULL, " +
                    "primary key (key)" +
                    ")");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
