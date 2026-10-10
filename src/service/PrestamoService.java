package service;

import config.DatabaseConnection;
import dao.LibroDAO;
import dao.PrestamoDAO;
import model.Prestamo;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class PrestamoService {

    private final LibroDAO libroDAO = new LibroDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    public void registrarPrestamo(int idEstudiante, int idLibro)
            throws SQLException {

        if (idEstudiante <= 0 || idLibro <= 0) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un estudiante y un libro.");
        }

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            boolean autoCommitOriginal = connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                if (!libroDAO.descontarStock(idLibro)) {
                    throw new SQLException(
                            "El libro no existe o no tiene stock disponible.");
                }

                LocalDate fechaPrestamo = LocalDate.now();
                LocalDate vencimiento = fechaPrestamo.plusDays(7);

                Prestamo prestamo = new Prestamo(
                        0,
                        idEstudiante,
                        idLibro,
                        fechaPrestamo,
                        vencimiento,
                        false
                );

                if (!prestamoDAO.registrar(prestamo)) {
                    throw new SQLException(
                            "No se pudo registrar el préstamo.");
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException errorRollback) {
                    e.addSuppressed(errorRollback);
                }

                throw e;

            } finally {
                connection.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    public boolean devolverPrestamo(int idPrestamo) throws SQLException {
        if (idPrestamo <= 0) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un préstamo.");
        }

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            boolean autoCommitOriginal = connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);

                if (prestamo == null) {
                    throw new SQLException("El préstamo no existe.");
                }

                if (!prestamoDAO.marcarDevuelto(idPrestamo)) {
                    throw new SQLException(
                            "El préstamo ya fue devuelto.");
                }

                if (!libroDAO.incrementarStock(prestamo.getIdLibro())) {
                    throw new SQLException(
                            "No se pudo actualizar el stock del libro.");
                }

                boolean atrasado = prestamo.getFechaDevolucion() != null
                        && LocalDate.now().isAfter(
                        prestamo.getFechaDevolucion());

                connection.commit();

                return atrasado;

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException errorRollback) {
                    e.addSuppressed(errorRollback);
                }

                throw e;

            } finally {
                connection.setAutoCommit(autoCommitOriginal);
            }
        }
    }
}