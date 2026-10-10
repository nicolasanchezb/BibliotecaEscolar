package view;

import controller.PrestamoController;
import model.Estudiante;
import model.Libro;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PrestamosPanel extends JPanel {

    private final PrestamoController controller;

    private final JComboBox<EstudianteOpcion> estudianteCombo =
            new JComboBox<>();

    private final JComboBox<LibroOpcion> libroCombo =
            new JComboBox<>();

    private final JButton prestarButton = new JButton("Registrar préstamo");
    private final JButton devolverButton = new JButton("Devolver seleccionado");
    private final JButton recargarButton = new JButton("Actualizar listas");

    private final JLabel estadoLabel = new JLabel(" ");

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{
                    "ID", "Estudiante", "Libro", "Fecha préstamo",
                    "Vencimiento", "Devolución real", "Estado", "Días de atraso"
            }, 0) {

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);
    private boolean trabajando;

    public PrestamosPanel(Usuario usuario) {
        controller = new PrestamoController(usuario);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        construirInterfaz();
        conectarEventos();
        aplicarEstado();
        recargar();
    }

    private void construirInterfaz() {
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Estudiante:"));
        formulario.add(estudianteCombo);
        formulario.add(new JLabel("Libro:"));
        formulario.add(libroCombo);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(recargarButton);
        botones.add(prestarButton);
        botones.add(devolverButton);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.add(formulario, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(estadoLabel, BorderLayout.SOUTH);
    }

    private void conectarEventos() {
        recargarButton.addActionListener(e -> recargar());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                aplicarEstado();
            }
        });

        prestarButton.addActionListener(e -> registrarPrestamo());
        devolverButton.addActionListener(e -> devolverPrestamo());
    }

    private void registrarPrestamo() {
        EstudianteOpcion estudiante =
                (EstudianteOpcion) estudianteCombo.getSelectedItem();

        LibroOpcion libro =
                (LibroOpcion) libroCombo.getSelectedItem();

        if (estudiante == null || libro == null) {
            mostrarError(new IllegalArgumentException(
                    "Selecciona un estudiante y un libro."));
            return;
        }

        if (libro.stock <= 0) {
            mostrarError(new IllegalArgumentException(
                    "El libro seleccionado no tiene stock disponible."));
            return;
        }

        ejecutar(() -> {
            controller.registrar(estudiante.id, libro.id);
            return "Préstamo registrado. Vence en siete días.";
        });
    }

    private void devolverPrestamo() {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            mostrarError(new IllegalArgumentException(
                    "Selecciona un préstamo."));
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);

        if ("Devuelto".equals(modelo.getValueAt(filaModelo, 6))) {
            mostrarError(new IllegalArgumentException(
                    "El préstamo ya fue devuelto."));
            return;
        }

        int idPrestamo =
                ((Number) modelo.getValueAt(filaModelo, 0)).intValue();

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Registrar la devolución del préstamo seleccionado?",
                "Devolver libro",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        ejecutar(() -> {
            boolean atrasado = controller.devolver(idPrestamo);

            return atrasado
                    ? "Devolución registrada con atraso."
                    : "Devolución registrada a tiempo.";
        });
    }

    public void recargar() {
        if (trabajando) {
            return;
        }

        cambiarEstado(true, "Cargando préstamos y disponibilidad...");

        new SwingWorker<Datos, Void>() {

            @Override
            protected Datos doInBackground() throws Exception {
                return new Datos(
                        controller.listarEstudiantes(),
                        controller.listarLibros(),
                        controller.listarPrestamos()
                );
            }

            @Override
            protected void done() {
                try {
                    Datos datos = get();

                    estudianteCombo.removeAllItems();

                    for (Estudiante estudiante : datos.estudiantes) {
                        estudianteCombo.addItem(new EstudianteOpcion(
                                estudiante.getId(),
                                estudiante.getNombre(),
                                estudiante.getRut()
                        ));
                    }

                    libroCombo.removeAllItems();

                    for (Libro libro : datos.libros) {
                        libroCombo.addItem(new LibroOpcion(
                                libro.getId(),
                                libro.getTitulo(),
                                libro.getStock()
                        ));
                    }

                    tabla.clearSelection();
                    modelo.setRowCount(0);

                    for (Object[] fila : datos.prestamos) {
                        modelo.addRow(fila);
                    }

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

    private void ejecutar(Operacion operacion) {
        if (trabajando) {
            return;
        }

        cambiarEstado(true, "Procesando operación...");

        new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                return operacion.ejecutar();
            }

            @Override
            protected void done() {
                boolean correcto = false;

                try {
                    String mensaje = get();
                    correcto = true;

                    JOptionPane.showMessageDialog(
                            PrestamosPanel.this, mensaje);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError(e);

                } catch (ExecutionException e) {
                    mostrarError(e.getCause());

                } finally {
                    cambiarEstado(false, " ");
                }

                if (correcto) {
                    firePropertyChange("datosActualizados", false, true);
                    recargar();
                }
            }
        }.execute();
    }

    private void cambiarEstado(boolean ocupado, String mensaje) {
        trabajando = ocupado;
        estadoLabel.setText(mensaje);
        aplicarEstado();
    }

    private void aplicarEstado() {
        boolean disponible = !trabajando;

        estudianteCombo.setEnabled(
                disponible && controller.esBibliotecario());

        libroCombo.setEnabled(disponible);
        recargarButton.setEnabled(disponible);
        tabla.setEnabled(disponible);

        prestarButton.setEnabled(
                disponible
                        && estudianteCombo.getItemCount() > 0
                        && libroCombo.getItemCount() > 0);

        int fila = tabla.getSelectedRow();
        boolean puedeDevolver = false;

        if (fila >= 0) {
            int filaModelo = tabla.convertRowIndexToModel(fila);

            puedeDevolver =
                    !"Devuelto".equals(modelo.getValueAt(filaModelo, 6));
        }

        devolverButton.setEnabled(disponible && puedeDevolver);
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

    private record Datos(
            List<Estudiante> estudiantes,
            List<Libro> libros,
            List<Object[]> prestamos) {
    }

    private record EstudianteOpcion(int id, String nombre, String rut) {

        @Override
        public String toString() {
            return nombre + " — " + rut;
        }
    }

    private record LibroOpcion(int id, String titulo, int stock) {

        @Override
        public String toString() {
            return titulo + " — Stock: " + stock;
        }
    }

    @FunctionalInterface
    private interface Operacion {
        String ejecutar() throws Exception;
    }
}