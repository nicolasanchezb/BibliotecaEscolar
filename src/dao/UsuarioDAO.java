package dao;

import config.DatabaseConnection;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario autenticar(String correo, String password)
            throws SQLException {

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol "
                    + "FROM usuarios WHERE correo = ? AND `contraseña` = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, correo);
                statement.setString(2, password);

                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        Usuario usuario = leerUsuario(resultado);

                        // Verifica también la coincidencia exacta en Java.
                        if (password != null
                                && password.equals(usuario.getPassword())) {
                            return usuario;
                        }
                    }
                }
            }

            return null;
        }
    }

    public List<Usuario> listar() throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            List<Usuario> usuarios = new ArrayList<>();

            String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol "
                    + "FROM usuarios ORDER BY id";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {
                    usuarios.add(leerUsuario(resultado));
                }
            }

            return usuarios;
        }
    }

    public Usuario buscarPorRut(String rut) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol "
                    + "FROM usuarios WHERE rut = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, rut);

                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        return leerUsuario(resultado);
                    }
                }
            }

            return null;
        }
    }

    public boolean registrar(Usuario usuario) throws SQLException {
        validar(usuario);

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "INSERT INTO usuarios "
                    + "(nombre, rut, correo, `contraseña`, rol) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                asignarDatos(statement, usuario);

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean actualizar(Usuario usuario) throws SQLException {
        validar(usuario);

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, "
                    + "`contraseña` = ?, rol = ? WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                asignarDatos(statement, usuario);
                statement.setInt(6, usuario.getId());

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean eliminar(int id) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "DELETE FROM usuarios WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);

                return statement.executeUpdate() == 1;
            }
        }
    }

    private Usuario leerUsuario(ResultSet resultado) throws SQLException {
        return new Usuario(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("rut"),
                resultado.getString("correo"),
                resultado.getString("contraseña"),
                resultado.getString("rol")
        );
    }

    private void asignarDatos(PreparedStatement statement, Usuario usuario)
            throws SQLException {

        statement.setString(1, usuario.getNombre());
        statement.setString(2, usuario.getRut());
        statement.setString(3, usuario.getCorreo());
        statement.setString(4, usuario.getPassword());
        statement.setString(5, usuario.getRol());
    }

    private void validar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Debes proporcionar un usuario.");
        }

        if (textoVacio(usuario.getNombre())
                || textoVacio(usuario.getRut())
                || textoVacio(usuario.getCorreo())
                || textoVacio(usuario.getPassword())) {

            throw new IllegalArgumentException(
                    "Nombre, RUT, correo y contraseña son obligatorios.");
        }

        if (!usuario.esBibliotecario() && !usuario.esEstudiante()) {
            throw new IllegalArgumentException(
                    "El rol debe ser bibliotecario o estudiante.");
        }
    }

    private boolean textoVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}