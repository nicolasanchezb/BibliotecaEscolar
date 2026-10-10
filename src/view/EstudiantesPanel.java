package view;

import controller.EstudianteController;
import model.Estudiante;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EstudiantesPanel extends JPanel {

    private final EstudianteController controller;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Nombre", "RUT", "Curso", "Correo"}, 0) {

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    private final JTextField nombreField = new JTextField();
    private final JTextField rutField = new JTextField();
    private final JTextField cursoField = new JTextField();
    private final JTextField correoField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    private final JButton registrarButton = new JButton("Registrar");
    private final JButton actualizarButton = new JButton("Modificar");
    private final JButton eliminarButton = new JButton("Eliminar");
    private final JButton limpiarButton = new JButton("Limpiar");
    private final JButton recargarButton = new JButton("Actualizar lista");

    private final JLabel estadoLabel = new JLabel(" ");

    private List<Estudiante> estudiantes = new ArrayList<>();
    private Estudiante seleccionado;
    private boolean trabajando;

    public EstudiantesPanel(Usuario usuario) {
        controller = new EstudianteController(usuario);

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
        superior.add(new JLabel("Gestión de estudiantes"), BorderLayout.WEST);
        superior.add(recargarButton, BorderLayout.EAST);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));

        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombreField);
        formulario.add(new JLabel("RUT:"));
        formulario.add(rutField);
        formulario.add(new JLabel("Curso:"));
        formulario.add(cursoField);
        formulario.add(new JLabel("Correo:"));
        formulario.add(correoField);
        formulario.add(new JLabel("Contraseña:"));
        formulario.add(passwordField);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(limpiarButton);
        botones.add(registrarButton);
        botones.add(actualizarButton);
        botones.add(eliminarButton);

        JPanel inferior = new JPanel(new BorderLayout(8, 8));
        inferior.add(formulario, BorderLayout.CENTER);

        JPanel avisos = new JPanel(new GridLayout(0, 1));
        avisos.add(new JLabel(
                "Al modificar, deja la contraseña vacía para conservar la actual."));
        avisos.add(estadoLabel);

        inferior.add(avisos, BorderLayout.NORTH);
        inferior.add(botones, BorderLayout.SOUTH);

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
            Estudiante estudiante = leerFormulario(0);
            String password = leerPassword();

            ejecutar(
                    () -> controller.registrar(estudiante, password),
                    "Estudiante y usuario registrados."
            );
        });

        actualizarButton.addActionListener(e -> {
            if (seleccionado == null) {
                mostrarError(new IllegalArgumentException(
                        "Selecciona un estudiante."));
                return;
            }

            Estudiante estudiante = leerFormulario(seleccionado.getId());
            String rutOriginal = seleccionado.getRut();
            String password = leerPassword();

            ejecutar(
                    () -> controller.actualizar(
                            estudiante, rutOriginal, password),
                    "Estudiante y usuario actualizados."
            );
        });

        eliminarButton.addActionListener(e -> {
            if (seleccionado == null) {
                mostrarError(new IllegalArgumentException(
                        "Selecciona un estudiante."));
                return;
            }

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Eliminar el estudiante y su cuenta de usuario?",
                    "Eliminar estudiante",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta == JOptionPane.YES_OPTION) {
                Estudiante estudiante = seleccionado;

                ejecutar(
                        () -> controller.eliminar(estudiante),
                        "Estudiante y usuario eliminados."
                );
            }
        });
    }

    private Estudiante leerFormulario(int id) {
        return new Estudiante(
                id,
                nombreField.getText().trim(),
                rutField.getText().trim(),
                cursoField.getText().trim(),
                correoField.getText().trim()
        );
    }

    private String leerPassword() {
        char[] caracteres = passwordField.getPassword();
        String password = new String(caracteres);
        Arrays.fill(caracteres, '\0');
        return password;
    }

    public void recargar() {
        if (trabajando) {
            return;
        }

        cambiarEstado(true, "Cargando estudiantes...");

        new SwingWorker<List<Estudiante>, Void>() {

            @Override
            protected List<Estudiante> doInBackground() throws Exception {
                return controller.listar();
            }

            @Override
            protected void done() {
                try {
                    estudiantes = get();
                    modelo.setRowCount(0);

                    for (Estudiante estudiante : estudiantes) {
                        modelo.addRow(new Object[]{
                                estudiante.getId(),
                                estudiante.getNombre(),
                                estudiante.getRut(),
                                estudiante.getCurso(),
                                estudiante.getCorreo()
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
                    passwordField.setText("");

                    JOptionPane.showMessageDialog(
                            EstudiantesPanel.this, mensaje);

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
        seleccionado = estudiantes.get(filaModelo);

        nombreField.setText(seleccionado.getNombre());
        rutField.setText(seleccionado.getRut());
        cursoField.setText(seleccionado.getCurso());
        correoField.setText(seleccionado.getCorreo());
        passwordField.setText("");

        aplicarEstado();
    }

    private void limpiar() {
        tabla.clearSelection();
        seleccionado = null;

        nombreField.setText("");
        rutField.setText("");
        cursoField.setText("");
        correoField.setText("");
        passwordField.setText("");

        aplicarEstado();
    }

    private void cambiarEstado(boolean ocupado, String mensaje) {
        trabajando = ocupado;
        estadoLabel.setText(mensaje);
        aplicarEstado();
    }

    private void aplicarEstado() {
        boolean disponible = !trabajando;

        nombreField.setEnabled(disponible);
        rutField.setEnabled(disponible);
        cursoField.setEnabled(disponible);
        correoField.setEnabled(disponible);
        passwordField.setEnabled(disponible);

        registrarButton.setEnabled(disponible);
        actualizarButton.setEnabled(disponible && seleccionado != null);
        eliminarButton.setEnabled(disponible && seleccionado != null);
        limpiarButton.setEnabled(disponible);
        recargarButton.setEnabled(disponible);
        tabla.setEnabled(disponible);
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

    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws Exception;
    }
}