package view;

import controller.LibroController;
import model.Categoria;
import model.Libro;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LibrosPanel extends JPanel {

    private final LibroController controller;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{
                    "ID", "Título", "Autor", "ISBN",
                    "Editorial", "Stock", "Categoría"
            }, 0) {

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    private final JTextField tituloField = new JTextField();
    private final JTextField autorField = new JTextField();
    private final JTextField isbnField = new JTextField();
    private final JTextField editorialField = new JTextField();

    private final JSpinner stockSpinner =
            new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

    private final JComboBox<CategoriaOpcion> categoriaCombo = new JComboBox<>();

    private final JButton registrarButton = new JButton("Registrar");
    private final JButton actualizarButton = new JButton("Modificar");
    private final JButton eliminarButton = new JButton("Eliminar");
    private final JButton limpiarButton = new JButton("Limpiar");
    private final JButton recargarButton = new JButton("Actualizar lista");

    private final JLabel estadoLabel = new JLabel(" ");

    private List<Libro> libros = new ArrayList<>();
    private int idSeleccionado;
    private boolean trabajando;

    public LibrosPanel(Usuario usuario) {
        controller = new LibroController(usuario);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        construirInterfaz();
        conectarEventos();
        aplicarEstado();
        recargar();
    }

    private void construirInterfaz() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(new JLabel("Catálogo de libros"), BorderLayout.WEST);
        superior.add(recargarButton, BorderLayout.EAST);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));

        formulario.add(new JLabel("Título:"));
        formulario.add(tituloField);
        formulario.add(new JLabel("Autor:"));
        formulario.add(autorField);
        formulario.add(new JLabel("ISBN:"));
        formulario.add(isbnField);
        formulario.add(new JLabel("Editorial:"));
        formulario.add(editorialField);
        formulario.add(new JLabel("Stock disponible:"));
        formulario.add(stockSpinner);
        formulario.add(new JLabel("Categoría:"));
        formulario.add(categoriaCombo);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(limpiarButton);
        botones.add(registrarButton);
        botones.add(actualizarButton);
        botones.add(eliminarButton);

        JPanel inferior = new JPanel(new BorderLayout(8, 8));

        if (controller.puedeGestionar()) {
            inferior.add(formulario, BorderLayout.CENTER);
            inferior.add(botones, BorderLayout.SOUTH);
        }

        inferior.add(estadoLabel, BorderLayout.NORTH);
        add(inferior, BorderLayout.SOUTH);
    }

    private void conectarEventos() {
        recargarButton.addActionListener(e -> recargar());
        limpiarButton.addActionListener(e -> limpiar());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !trabajando) {
                cargarSeleccion();
            }
        });

        registrarButton.addActionListener(e -> {
            try {
                Libro libro = leerFormulario(0);
                ejecutar(() -> controller.registrar(libro), "Libro registrado.");
            } catch (Exception error) {
                mostrarError(error);
            }
        });

        actualizarButton.addActionListener(e -> {
            try {
                if (idSeleccionado <= 0) {
                    throw new IllegalArgumentException(
                            "Selecciona un libro.");
                }

                Libro libro = leerFormulario(idSeleccionado);
                ejecutar(() -> controller.actualizar(libro), "Libro actualizado.");
            } catch (Exception error) {
                mostrarError(error);
            }
        });

        eliminarButton.addActionListener(e -> {
            if (idSeleccionado <= 0) {
                mostrarError(new IllegalArgumentException(
                        "Selecciona un libro."));
                return;
            }

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Eliminar el libro seleccionado?",
                    "Eliminar libro",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                int id = idSeleccionado;
                ejecutar(() -> controller.eliminar(id), "Libro eliminado.");
            }
        });
    }

    private Libro leerFormulario(int id) throws java.text.ParseException {
        CategoriaOpcion categoria =
                (CategoriaOpcion) categoriaCombo.getSelectedItem();

        if (categoria == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una categoría.");
        }

        stockSpinner.commitEdit();

        return new Libro(
                id,
                ((Number) stockSpinner.getValue()).intValue(),
                categoria.id,
                tituloField.getText().trim(),
                autorField.getText().trim(),
                isbnField.getText().trim(),
                editorialField.getText().trim()
        );
    }

    public void recargar() {
        if (trabajando) {
            return;
        }

        cambiarEstado(true, "Cargando libros...");

        new SwingWorker<DatosCatalogo, Void>() {

            @Override
            protected DatosCatalogo doInBackground() throws Exception {
                return new DatosCatalogo(
                        controller.listarLibros(),
                        controller.listarCategorias()
                );
            }

            @Override
            protected void done() {
                try {
                    DatosCatalogo datos = get();
                    libros = datos.libros;

                    categoriaCombo.removeAllItems();

                    for (Categoria categoria : datos.categorias) {
                        categoriaCombo.addItem(new CategoriaOpcion(
                                categoria.getId(), categoria.getNombre()));
                    }

                    modelo.setRowCount(0);

                    for (Libro libro : libros) {
                        modelo.addRow(new Object[]{
                                libro.getId(),
                                libro.getTitulo(),
                                libro.getAutor(),
                                libro.getIsbn(),
                                libro.getEditorial(),
                                libro.getStock(),
                                nombreCategoria(libro.getIdCategoria())
                        });
                    }

                    limpiar();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError(e);
                } catch (ExecutionException e) {
                    mostrarError(e.getCause());
                } finally {
                    cambiarEstado(false, " ");
                }
            }
        }.execute();
    }

    private void ejecutar(Operacion operacion, String mensaje) {
        if (trabajando) {
            return;
        }

        cambiarEstado(true, "Guardando cambios...");

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                operacion.ejecutar();
                return null;
            }

            @Override
            protected void done() {
                boolean correcto = false;

                try {
                    get();
                    correcto = true;
                    JOptionPane.showMessageDialog(LibrosPanel.this, mensaje);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError(e);
                } catch (ExecutionException e) {
                    mostrarError(e.getCause());
                } finally {
                    cambiarEstado(false, " ");
                }

                if (correcto) {
                    recargar();
                }
            }
        }.execute();
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);
        Libro libro = libros.get(filaModelo);

        idSeleccionado = libro.getId();
        tituloField.setText(libro.getTitulo());
        autorField.setText(libro.getAutor());
        isbnField.setText(libro.getIsbn());
        editorialField.setText(libro.getEditorial());
        stockSpinner.setValue(libro.getStock());

        for (int i = 0; i < categoriaCombo.getItemCount(); i++) {
            if (categoriaCombo.getItemAt(i).id == libro.getIdCategoria()) {
                categoriaCombo.setSelectedIndex(i);
                break;
            }
        }

        aplicarEstado();
    }

    private String nombreCategoria(int id) {
        for (int i = 0; i < categoriaCombo.getItemCount(); i++) {
            CategoriaOpcion opcion = categoriaCombo.getItemAt(i);

            if (opcion.id == id) {
                return opcion.nombre;
            }
        }

        return "Sin categoría";
    }

    private void limpiar() {
        tabla.clearSelection();
        idSeleccionado = 0;

        tituloField.setText("");
        autorField.setText("");
        isbnField.setText("");
        editorialField.setText("");
        stockSpinner.setValue(0);

        if (categoriaCombo.getItemCount() > 0) {
            categoriaCombo.setSelectedIndex(0);
        }

        aplicarEstado();
    }

    private void cambiarEstado(boolean ocupado, String mensaje) {
        trabajando = ocupado;
        estadoLabel.setText(mensaje);
        aplicarEstado();
    }

    private void aplicarEstado() {
        boolean gestionar = controller.puedeGestionar() && !trabajando;

        tituloField.setEnabled(gestionar);
        autorField.setEnabled(gestionar);
        isbnField.setEnabled(gestionar);
        editorialField.setEnabled(gestionar);
        stockSpinner.setEnabled(gestionar);
        categoriaCombo.setEnabled(gestionar);

        registrarButton.setEnabled(gestionar);
        actualizarButton.setEnabled(gestionar && idSeleccionado > 0);
        eliminarButton.setEnabled(gestionar && idSeleccionado > 0);
        limpiarButton.setEnabled(gestionar);

        tabla.setEnabled(!trabajando);
        recargarButton.setEnabled(!trabajando);
    }

    private void mostrarError(Throwable error) {
        String mensaje = error.getMessage();

        JOptionPane.showMessageDialog(
                this,
                mensaje == null ? "No se pudo completar la operación." : mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private record DatosCatalogo(
            List<Libro> libros, List<Categoria> categorias) {
    }

    private record CategoriaOpcion(int id, String nombre) {

        @Override
        public String toString() {
            return nombre;
        }
    }

    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws Exception;
    }
}