package dao;

import config.DatabaseConnection;
import model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listar() throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            List<Categoria> categorias = new ArrayList<>();

            String sql = "SELECT id, nombre FROM categorias ORDER BY id";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {
                    categorias.add(new Categoria(
                            resultado.getInt("id"),
                            resultado.getString("nombre")
                    ));
                }
            }

            return categorias;
        }
    }

    public boolean registrar(Categoria categoria) throws SQLException {
        validar(categoria);

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "INSERT INTO categorias (nombre) VALUES (?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, categoria.getNombre());

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean actualizar(Categoria categoria) throws SQLException {
        validar(categoria);

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, categoria.getNombre());
                statement.setInt(2, categoria.getId());

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean eliminar(int id) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "DELETE FROM categorias WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);

                return statement.executeUpdate() == 1;
            }
        }
    }

    private void validar(Categoria categoria) {
        if (categoria == null
                || categoria.getNombre() == null
                || categoria.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre de la categoría es obligatorio.");
        }
    }
}