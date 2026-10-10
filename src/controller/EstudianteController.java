package controller;

import dao.EstudianteDAO;
import model.Estudiante;
import model.Usuario;
import service.EstudianteService;

import java.sql.SQLException;
import java.util.List;

public class EstudianteController {

    private final Usuario usuario;
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final EstudianteService estudianteService = new EstudianteService();

    public EstudianteController(Usuario usuario) {
        if (usuario == null || !usuario.esBibliotecario()) {
            throw new SecurityException(
                    "Solo el bibliotecario puede gestionar estudiantes.");
        }

        this.usuario = usuario;
    }

    public List<Estudiante> listar() throws SQLException {
        exigirBibliotecario();
        return estudianteDAO.listar();
    }

    public void registrar(Estudiante estudiante, String password)
            throws SQLException {

        exigirBibliotecario();
        estudianteService.registrarConUsuario(estudiante, password);
    }

    public void actualizar(
            Estudiante estudiante, String rutOriginal, String nuevaPassword)
            throws SQLException {

        exigirBibliotecario();

        if (estudiante == null || estudiante.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un estudiante para modificar.");
        }

        estudianteService.actualizarConUsuario(
                estudiante, rutOriginal, nuevaPassword);
    }

    public void eliminar(Estudiante estudiante) throws SQLException {
        exigirBibliotecario();

        try {
            estudianteService.eliminarConUsuario(estudiante);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException(
                        "No puedes eliminar un estudiante "
                                + "con historial de préstamos.",
                        e
                );
            }

            throw e;
        }
    }

    private void exigirBibliotecario() {
        if (!usuario.esBibliotecario()) {
            throw new SecurityException(
                    "Solo el bibliotecario puede gestionar estudiantes.");
        }
    }
}