package dao;

import config.DatabaseConnection;
import model.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    public List<Libro> listar() throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();
            List<Libro> libros = new ArrayList<>();

            String sql = "SELECT id, stock, id_categoria, titulo, autor, "
                    + "isbn, editorial FROM libros ORDER BY id";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultado = statement.executeQuery()) {

                while (resultado.next()) {
                    Libro libro = new Libro(
                            resultado.getInt("id"),
                            resultado.getInt("stock"),
                            resultado.getInt("id_categoria"),
                            resultado.getString("titulo"),
                            resultado.getString("autor"),
                            resultado.getString("isbn"),
                            resultado.getString("editorial")
                    );

                    libros.add(libro);
                }
            }

            return libros;
        }
    }

    public boolean registrar(Libro libro) throws SQLException {
        validarLibro(libro);

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "INSERT INTO libros "
                    + "(titulo, autor, isbn, editorial, stock, id_categoria) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                asignarDatos(statement, libro);

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean actualizar(Libro libro) throws SQLException {
        validarLibro(libro);

        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, "
                    + "editorial = ?, stock = ?, id_categoria = ? WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                asignarDatos(statement, libro);
                statement.setInt(7, libro.getId());

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean eliminar(int id) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "DELETE FROM libros WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean descontarStock(int idLibro) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "UPDATE libros SET stock = stock - 1 "
                    + "WHERE id = ? AND stock > 0";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, idLibro);

                return statement.executeUpdate() == 1;
            }
        }
    }

    public boolean incrementarStock(int idLibro) throws SQLException {
        DatabaseConnection database = DatabaseConnection.getInstance();

        synchronized (database) {
            Connection connection = database.getConnection();

            String sql = "UPDATE libros SET stock = stock + 1 WHERE id = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, idLibro);

                return statement.executeUpdate() == 1;
            }
        }
    }

    private void asignarDatos(PreparedStatement statement, Libro libro)
            throws SQLException {

        statement.setString(1, libro.getTitulo());
        statement.setString(2, libro.getAutor());
        statement.setString(3, libro.getIsbn());
        statement.setString(4, libro.getEditorial());
        statement.setInt(5, libro.getStock());
        statement.setInt(6, libro.getIdCategoria());
    }

    private void validarLibro(Libro libro) {
        if (libro == null) {
            throw new IllegalArgumentException("Debes proporcionar un libro.");
        }

        if (textoVacio(libro.getTitulo())
                || textoVacio(libro.getAutor())
                || textoVacio(libro.getIsbn())
                || textoVacio(libro.getEditorial())) {

            throw new IllegalArgumentException(
                    "Título, autor, ISBN y editorial son obligatorios.");
        }

        if (libro.getStock() < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo.");
        }

        if (libro.getIdCategoria() <= 0) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una categoría.");
        }
    }

    private boolean textoVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}