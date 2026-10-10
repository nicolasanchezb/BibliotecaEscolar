package service;

import config.DatabaseConnection;
import dao.EstudianteDAO;
import dao.UsuarioDAO;
import model.Estudiante;
import model.Usuario;

import java.sql.Connection;
import java.sql.SQLException;

public class EstudianteService {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public void registrarConUsuario(Estudiante estudiante, String password)
            throws SQLException {

        validarPassword(password);

        ejecutarTransaccion(() -> {
            if (!estudianteDAO.registrar(estudiante)) {
                throw new SQLException(
                        "No se pudo registrar el estudiante.");
            }

            Usuario usuario = new Usuario(
                    0,
                    estudiante.getNombre(),
                    estudiante.getRut(),
                    estudiante.getCorreo(),
                    password,
                    "estudiante"
            );

            if (!usuarioDAO.registrar(usuario)) {
                throw new SQLException(
                        "No se pudo registrar el usuario.");
            }
        });
    }

    public void actualizarConUsuario(
            Estudiante estudiante, String rutOriginal, String nuevaPassword)
            throws SQLException {

        ejecutarTransaccion(() -> {
            Estudiante original = estudianteDAO.buscarPorRut(rutOriginal);

            if (original == null || original.getId() != estudiante.getId()) {
                throw new SQLException(
                        "No se encontró el estudiante original.");
            }

            Usuario usuarioOriginal = usuarioDAO.buscarPorRut(rutOriginal);

            if (usuarioOriginal != null && !usuarioOriginal.esEstudiante()) {
                throw new SQLException(
                        "El RUT está asociado a un usuario bibliotecario.");
            }

            String password;

            if (nuevaPassword != null && !nuevaPassword.isBlank()) {
                password = nuevaPassword;
            } else if (usuarioOriginal != null) {
                password = usuarioOriginal.getPassword();
            } else {
                throw new IllegalArgumentException(
                        "Este estudiante no tiene usuario. "
                                + "Ingresa una contraseña para crearlo.");
            }

            validarPassword(password);

            if (!estudianteDAO.actualizar(estudiante)) {
                throw new SQLException(
                        "No se pudo actualizar el estudiante.");
            }

            Usuario usuarioActualizado = new Usuario(
                    usuarioOriginal == null ? 0 : usuarioOriginal.getId(),
                    estudiante.getNombre(),
                    estudiante.getRut(),
                    estudiante.getCorreo(),
                    password,
                    "estudiante"
            );

            boolean guardado;

            if (usuarioOriginal == null) {
                guardado = usuarioDAO.registrar(usuarioActualizado);
            } else {
                guardado = usuarioDAO.actualizar(usuarioActualizado);
            }

            if (!guardado) {
                throw new SQLException(
                        "No se pudo guardar el usuario del estudiante.");
            }
        });
    }

    public void eliminarConUsuario(Estudiante estudiante)
            throws SQLException {

        if (estudiante == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un estudiante.");
        }

        ejecutarTransaccion(() -> {
            Estudiante original =
                    estudianteDAO.buscarPorRut(estudiante.getRut());

            if (original == null || original.getId() != estudiante.getId()) {
                throw new SQLException(
                        "El estudiante ya no existe.");
            }

            Usuario usuario = usuarioDAO.buscarPorRut(original.getRut());

            if (usuario != null && !usuario.esEstudiante()) {
                throw new SQLException(
                        "El RUT está asociado a un usuario bibliotecario.");
            }

            // MySQL impedirá borrar estudiantes con historial de préstamos.
            if (!estudianteDAO.eliminar(original)) {
                throw new SQLException(
                        "No se pudo eliminar el estudiante.");
            }

            if (usuario != null && !usuarioDAO.eliminar(usuario.getId())) {
                throw new SQLException(
                        "No se pudo eliminar el usuario del estudiante.");
            }
        });
    }

    private void validarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria.");
        }
    }

    private void ejecutarTransaccion(OperacionSQL operacion)
            throws SQLException {

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            boolean autoCommitOriginal = connection.getAutoCommit();

            connection.setAutoCommit(false);

            try {
                operacion.ejecutar();
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

    @FunctionalInterface
    private interface OperacionSQL {
        void ejecutar() throws SQLException;
    }
}