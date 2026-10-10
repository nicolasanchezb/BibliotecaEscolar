package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private final String url;
    private final String usuario;
    private final String password;

    private DatabaseConnection() throws SQLException {
        url = System.getenv().getOrDefault(
                "BIBLIOTECA_DB_URL",
                "jdbc:mysql://localhost:3306/biblioteca"
        );

        usuario = System.getenv().getOrDefault(
                "BIBLIOTECA_DB_USER",
                "root"
        );

        password = System.getenv("BIBLIOTECA_DB_PASSWORD");

        if (password == null) {
            throw new SQLException(
                    "Debes configurar BIBLIOTECA_DB_PASSWORD en IntelliJ."
            );
        }

        connection = DriverManager.getConnection(url, usuario, password);
    }

    public static synchronized DatabaseConnection getInstance()
            throws SQLException {

        if (instance == null) {
            instance = new DatabaseConnection();
        }

        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, usuario, password);
        }

        return connection;
    }

    public synchronized void cerrarConexion() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}