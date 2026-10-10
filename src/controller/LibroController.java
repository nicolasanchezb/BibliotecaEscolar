package controller;

import dao.CategoriaDAO;
import dao.LibroDAO;
import model.Categoria;
import model.Libro;
import model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class LibroController {

    private final Usuario usuario;
    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public LibroController(Usuario usuario) {
        if (usuario == null
                || (!usuario.esBibliotecario() && !usuario.esEstudiante())) {
            throw new IllegalArgumentException(
                    "Se requiere una sesión válida.");
        }

        this.usuario = usuario;
    }

    public List<Libro> listarLibros() throws SQLException {
        return libroDAO.listar();
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listar();
    }

    public void registrar(Libro libro) throws SQLException {
        exigirBibliotecario();

        if (!libroDAO.registrar(libro)) {
            throw new SQLException("No se pudo registrar el libro.");
        }
    }

    public void actualizar(Libro libro) throws SQLException {
        exigirBibliotecario();

        if (libro == null || libro.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un libro para modificar.");
        }

        if (!libroDAO.actualizar(libro)) {
            throw new SQLException(
                    "El libro ya no existe o no pudo actualizarse.");
        }
    }

    public void eliminar(int idLibro) throws SQLException {
        exigirBibliotecario();

        if (idLibro <= 0) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un libro para eliminar.");
        }

        try {
            if (!libroDAO.eliminar(idLibro)) {
                throw new SQLException("El libro ya no existe.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException(
                        "No puedes eliminar un libro con historial de préstamos.",
                        e
                );
            }

            throw e;
        }
    }

    public boolean puedeGestionar() {
        return usuario.esBibliotecario();
    }

    private void exigirBibliotecario() {
        if (!usuario.esBibliotecario()) {
            throw new SecurityException(
                    "Solo el bibliotecario puede gestionar libros.");
        }
    }
}