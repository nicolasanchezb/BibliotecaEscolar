package app;

import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PruebaConexion {

    public static void main(String[] args) {
        DatabaseConnection database = null;

        try {
            database = DatabaseConnection.getInstance();
            Connection connection = database.getConnection();

            try (Statement statement = connection.createStatement();
                 ResultSet resultado = statement.executeQuery(
                         "SELECT DATABASE() AS nombre_bd"
                 )) {

                if (resultado.next()) {
                    System.out.println(
                            "Conexión exitosa a: "
                                    + resultado.getString("nombre_bd")
                    );
                }
            }

            System.out.println(
                    "Singleton: ambas referencias son la misma instancia: "
                            + (database == DatabaseConnection.getInstance())
            );

        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());

        } finally {
            if (database != null) {
                try {
                    database.cerrarConexion();
                } catch (SQLException e) {
                    System.err.println(
                            "Error al cerrar la conexión: " + e.getMessage()
                    );
                }
            }
        }
    }
}