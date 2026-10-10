package controller;

import config.DatabaseConnection;
import dao.EstudianteDAO;
import dao.LibroDAO;
import dao.PrestamoDAO;
import dao.ReporteDAO;
import model.Estudiante;
import model.Libro;
import model.Prestamo;
import model.Usuario;
import service.PrestamoService;

import java.sql.SQLException;
import java.util.List;

public class PrestamoController {

    private final Usuario usuario;

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final PrestamoService prestamoService = new PrestamoService();

    public PrestamoController(Usuario usuario) {
        if (usuario == null
                || (!usuario.esBibliotecario() && !usuario.esEstudiante())) {
            throw new SecurityException("Se requiere una sesión válida.");
        }

        this.usuario = usuario;
    }

    public boolean esBibliotecario() {
        return usuario.esBibliotecario();
    }

    public List<Libro> listarLibros() throws SQLException {
        return libroDAO.listar();
    }

    public List<Estudiante> listarEstudiantes() throws SQLException {
        if (usuario.esBibliotecario()) {
            return estudianteDAO.listar();
        }

        return List.of(obtenerEstudianteActual());
    }

    public List<Object[]> listarPrestamos() throws SQLException {
        if (usuario.esBibliotecario()) {
            return reporteDAO.prestamosActivos();
        }

        return reporteDAO.historialPorEstudiante(
                obtenerEstudianteActual().getId());
    }

    public void registrar(int idEstudiante, int idLibro)
            throws SQLException {

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            if (usuario.esEstudiante()) {
                Estudiante estudiante = obtenerEstudianteActual();

                if (estudiante.getId() != idEstudiante) {
                    throw new SecurityException(
                            "Solo puedes registrar préstamos para ti.");
                }
            }

            prestamoService.registrarPrestamo(idEstudiante, idLibro);
        }
    }

    public boolean devolver(int idPrestamo) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Prestamo prestamo = prestamoDAO.buscarPorId(idPrestamo);

            if (prestamo == null) {
                throw new IllegalArgumentException(
                        "El préstamo no existe.");
            }

            if (usuario.esEstudiante()) {
                Estudiante estudiante = obtenerEstudianteActual();

                if (prestamo.getIdEstudiante() != estudiante.getId()) {
                    throw new SecurityException(
                            "Solo puedes devolver tus propios préstamos.");
                }
            }

            return prestamoService.devolverPrestamo(idPrestamo);
        }
    }

    private Estudiante obtenerEstudianteActual() throws SQLException {
        Estudiante estudiante =
                estudianteDAO.buscarPorRut(usuario.getRut());

        if (estudiante == null) {
            throw new IllegalArgumentException(
                    "No se encontró tu registro de estudiante.");
        }

        return estudiante;
    }
}