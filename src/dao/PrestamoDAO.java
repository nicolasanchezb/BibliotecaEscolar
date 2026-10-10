package dao;

import config.DatabaseConnection;
import model.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    public List<Prestamo> listar() throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            List<Prestamo> prestamos = new ArrayList<>();

            String sql = "SELECT id, id_estudiante, id_libro, "
                    + "fecha_prestamo, fecha_devolucion, devuelto "
                    + "FROM prestamos ORDER BY id";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {
                    prestamos.add(leerPrestamo(resultado));
                }
            }

            return prestamos;
        }
    }

    public Prestamo buscarPorId(int id) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "SELECT id, id_estudiante, id_libro, "
                    + "fecha_prestamo, fecha_devolucion, devuelto "
                    + "FROM prestamos WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);

                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        return leerPrestamo(resultado);
                    }
                }
            }

            return null;
        }
    }

    public boolean registrar(Prestamo prestamo) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "INSERT INTO prestamos "
                    + "(id_estudiante, id_libro, fecha_prestamo, "
                    + "fecha_devolucion, devuelto) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, prestamo.getIdEstudiante());
                statement.setInt(2, prestamo.getIdLibro());
                statement.setDate(
                        3, Date.valueOf(prestamo.getFechaPrestamo()));
                statement.setDate(
                        4, Date.valueOf(prestamo.getFechaDevolucion()));
                statement.setBoolean(5, prestamo.isDevuelto());

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean marcarDevuelto(int idPrestamo) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "UPDATE prestamos "
                    + "SET devuelto = TRUE, fecha_devolucion_real = CURRENT_DATE "
                    + "WHERE id = ? AND devuelto = FALSE";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, idPrestamo);

                return statement.executeUpdate() == 1;
            }
        }
    }

    private Prestamo leerPrestamo(ResultSet resultado) throws SQLException {
        Date fechaPrestamo = resultado.getDate("fecha_prestamo");
        Date fechaDevolucion = resultado.getDate("fecha_devolucion");

        return new Prestamo(
                resultado.getInt("id"),
                resultado.getInt("id_estudiante"),
                resultado.getInt("id_libro"),
                fechaPrestamo == null ? null : fechaPrestamo.toLocalDate(),
                fechaDevolucion == null ? null : fechaDevolucion.toLocalDate(),
                resultado.getBoolean("devuelto")
        );
    }
}