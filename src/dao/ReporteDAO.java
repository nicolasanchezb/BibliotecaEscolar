package dao;

import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    public List<Object[]> librosMasPrestados() throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            List<Object[]> filas = new ArrayList<>();

            String sql = "SELECT l.id, l.titulo, l.autor, "
                    + "COUNT(p.id) AS total_prestamos "
                    + "FROM libros l "
                    + "LEFT JOIN prestamos p ON p.id_libro = l.id "
                    + "GROUP BY l.id, l.titulo, l.autor "
                    + "ORDER BY total_prestamos DESC, l.titulo";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {
                    filas.add(new Object[]{
                            resultado.getInt("id"),
                            resultado.getString("titulo"),
                            resultado.getString("autor"),
                            resultado.getLong("total_prestamos")
                    });
                }
            }

            return filas;
        }
    }

    public List<Object[]> historialPorEstudiante(int idEstudiante)
            throws SQLException {

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            List<Object[]> filas = new ArrayList<>();

            String sql = consultaPrestamos()
                    + " WHERE p.id_estudiante = ? ORDER BY p.id DESC";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, idEstudiante);

                try (ResultSet resultado = statement.executeQuery()) {
                    while (resultado.next()) {
                        filas.add(leerFilaPrestamo(resultado));
                    }
                }
            }

            return filas;
        }
    }

    public List<Object[]> prestamosActivos() throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            List<Object[]> filas = new ArrayList<>();

            String sql = consultaPrestamos()
                    + " WHERE p.devuelto = FALSE "
                    + "ORDER BY p.fecha_devolucion, p.id";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {
                    filas.add(leerFilaPrestamo(resultado));
                }
            }

            return filas;
        }
    }

    private String consultaPrestamos() {
        return "SELECT p.id, e.nombre AS estudiante, l.titulo, "
                + "p.fecha_prestamo, p.fecha_devolucion, "
                + "p.fecha_devolucion_real, "
                + "CASE "
                + "WHEN p.devuelto = TRUE THEN 'Devuelto' "
                + "WHEN p.fecha_devolucion < CURRENT_DATE THEN 'Vencido' "
                + "ELSE 'En préstamo' END AS estado, "
                + "CASE "
                + "WHEN p.devuelto = TRUE "
                + "AND p.fecha_devolucion_real IS NULL THEN NULL "
                + "ELSE GREATEST(0, DATEDIFF("
                + "CASE WHEN p.devuelto = TRUE "
                + "THEN p.fecha_devolucion_real ELSE CURRENT_DATE END, "
                + "p.fecha_devolucion)) END AS dias_atraso "
                + "FROM prestamos p "
                + "JOIN estudiantes e ON e.id = p.id_estudiante "
                + "JOIN libros l ON l.id = p.id_libro";
    }

    private Object[] leerFilaPrestamo(ResultSet resultado)
            throws SQLException {

        return new Object[]{
                resultado.getInt("id"),
                resultado.getString("estudiante"),
                resultado.getString("titulo"),
                resultado.getDate("fecha_prestamo"),
                resultado.getDate("fecha_devolucion"),
                resultado.getDate("fecha_devolucion_real"),
                resultado.getString("estado"),
                resultado.getObject("dias_atraso")
        };
    }
}