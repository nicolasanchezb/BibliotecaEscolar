package controller;

import dao.EstudianteDAO;
import dao.ReporteDAO;
import model.Estudiante;
import model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class ReporteController {

    private final Usuario usuario;
    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public ReporteController(Usuario usuario) {
        if (usuario == null
                || (!usuario.esBibliotecario() && !usuario.esEstudiante())) {
            throw new SecurityException(
                    "Se requiere una sesión válida.");
        }

        this.usuario = usuario;
    }

    public boolean esBibliotecario() {
        return usuario.esBibliotecario();
    }

    public List<Estudiante> listarEstudiantes() throws SQLException {
        if (usuario.esBibliotecario()) {
            return estudianteDAO.listar();
        }

        return List.of(obtenerEstudianteActual());
    }

    public List<Object[]> librosMasPrestados() throws SQLException {
        exigirBibliotecario();
        return reporteDAO.librosMasPrestados();
    }

    public List<Object[]> prestamosActivos() throws SQLException {
        exigirBibliotecario();
        return reporteDAO.prestamosActivos();
    }

    public List<Object[]> historial(int idEstudiante) throws SQLException {
        if (idEstudiante <= 0) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un estudiante.");
        }

        if (usuario.esEstudiante()
                && obtenerEstudianteActual().getId() != idEstudiante) {

            throw new SecurityException(
                    "Solo puedes consultar tu propio historial.");
        }

        return reporteDAO.historialPorEstudiante(idEstudiante);
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

    private void exigirBibliotecario() {
        if (!usuario.esBibliotecario()) {
            throw new SecurityException(
                    "Este reporte está disponible solo para bibliotecarios.");
        }
    }
}