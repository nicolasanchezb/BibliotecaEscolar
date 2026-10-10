package dao;

import config.DatabaseConnection;
import model.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public List<Estudiante> listar() throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            List<Estudiante> estudiantes = new ArrayList<>();

            String sql = "SELECT id, nombre, rut, curso, correo "
                    + "FROM estudiantes ORDER BY id";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {
                    estudiantes.add(leerEstudiante(resultado));
                }
            }

            return estudiantes;
        }
    }

    public Estudiante buscarPorRut(String rut) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "SELECT id, nombre, rut, curso, correo "
                    + "FROM estudiantes WHERE rut = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, rut);

                try (ResultSet resultado = statement.executeQuery()) {
                    if (resultado.next()) {
                        return leerEstudiante(resultado);
                    }
                }
            }

            return null;
        }
    }

    public boolean registrar(Estudiante estudiante) throws SQLException {
        validar(estudiante);

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "INSERT INTO estudiantes "
                    + "(nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                asignarDatos(statement, estudiante);

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean actualizar(Estudiante estudiante) throws SQLException {
        validar(estudiante);

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, "
                    + "curso = ?, correo = ? WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                asignarDatos(statement, estudiante);
                statement.setInt(5, estudiante.getId());

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean eliminar(Estudiante estudiante) throws SQLException {
        if (estudiante == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un estudiante.");
        }

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "DELETE FROM estudiantes WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, estudiante.getId());

                return statement.executeUpdate() == 1;
            }
        }
    }

    private Estudiante leerEstudiante(ResultSet resultado)
            throws SQLException {

        return new Estudiante(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getString("rut"),
                resultado.getString("curso"),
                resultado.getString("correo")
        );
    }

    private void asignarDatos(
            PreparedStatement statement, Estudiante estudiante)
            throws SQLException {

        statement.setString(1, estudiante.getNombre());
        statement.setString(2, estudiante.getRut());
        statement.setString(3, estudiante.getCurso());
        statement.setString(4, estudiante.getCorreo());
    }

    private void validar(Estudiante estudiante) {
        if (estudiante == null) {
            throw new IllegalArgumentException(
                    "Debes proporcionar un estudiante.");
        }

        if (textoVacio(estudiante.getNombre())
                || textoVacio(estudiante.getRut())
                || textoVacio(estudiante.getCurso())
                || textoVacio(estudiante.getCorreo())) {

            throw new IllegalArgumentException(
                    "Nombre, RUT, curso y correo son obligatorios.");
        }
    }

    private boolean textoVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}