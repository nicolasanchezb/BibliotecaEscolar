package controller;

import dao.EstudianteDAO;
import dao.UsuarioDAO;
import model.Estudiante;
import model.Usuario;

import java.sql.SQLException;

public class AuthController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public Usuario iniciarSesion(String correo, String password)
            throws SQLException {

        if (correo == null || correo.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalArgumentException(
                    "Ingresa tu correo y contraseña.");
        }

        Usuario usuario = usuarioDAO.autenticar(
                correo.trim(), password);

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Correo o contraseña incorrectos.");
        }

        if (usuario.esEstudiante()) {
            Estudiante estudiante =
                    estudianteDAO.buscarPorRut(usuario.getRut());

            if (estudiante == null) {
                throw new IllegalArgumentException(
                        "El usuario no tiene un estudiante asociado. "
                                + "Solicita al bibliotecario revisar tu registro.");
            }
        } else if (!usuario.esBibliotecario()) {
            throw new IllegalArgumentException(
                    "El usuario tiene un rol inválido.");
        }

        return usuario;
    }

    public Estudiante obtenerEstudiante(Usuario usuario)
            throws SQLException {

        if (usuario == null || !usuario.esEstudiante()) {
            throw new IllegalArgumentException(
                    "La sesión no corresponde a un estudiante.");
        }

        Estudiante estudiante =
                estudianteDAO.buscarPorRut(usuario.getRut());

        if (estudiante == null) {
            throw new IllegalArgumentException(
                    "No se encontró el estudiante asociado.");
        }

        return estudiante;
    }
}