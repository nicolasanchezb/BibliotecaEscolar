package view;

import controller.ReporteController;
import model.Estudiante;
import model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ReportesPanel extends JPanel {

    private static final String HISTORIAL = "Historial por estudiante";
    private static final String MAS_PRESTADOS = "Libros más prestados";
    private static final String ACTIVOS = "Préstamos activos";

    private final ReporteController controller;

    private final JComboBox<String> reporteCombo = new JComboBox<>();
    private final JComboBox<EstudianteOpcion> estudianteCombo = new JComboBox<>();

    private final JButton consultarButton = new JButton("Consultar");
    private final JButton recargarButton = new JButton("Actualizar estudiantes");
    private final JLabel estadoLabel = new JLabel(" ");

    private final DefaultTableModel modelo = new DefaultTableModel() {

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);
    private boolean trabajando;

    public ReportesPanel(Usuario usuario) {
        controller = new ReporteController(usuario);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        construirInterfaz();
        conectarEventos();
        aplicarEstado();
        recargarEstudiantes();
    }

    private void construirInterfaz() {
        reporteCombo.addItem(HISTORIAL);

        if (controller.esBibliotecario()) {
            reporteCombo.addItem(MAS_PRESTADOS);
            reporteCombo.addItem(ACTIVOS);
        }

        JPanel filtros = new JPanel(new GridLayout(0, 2, 8, 8));
        filtros.add(new JLabel("Reporte:"));
        filtros.add(reporteCombo);
        filtros.add(new JLabel("Estudiante:"));
        filtros.add(estudianteCombo);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(recargarButton);
        botones.add(consultarButton);

        JPanel superior = new JPanel(new BorderLayout(8, 8));
        superior.add(filtros, BorderLayout.CENTER);
        superior.add(botones, BorderLayout.SOUTH);

        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);

        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(estadoLabel, BorderLayout.SOUTH);
    }

    private void conectarEventos() {
        reporteCombo.addActionListener(e -> {
            if (!trabajando) {
                modelo.setRowCount(0);
                aplicarEstado();
            }
        });

        consultarButton.addActionListener(e -> consultar());
        recargarButton.addActionListener(e -> recargarEstudiantes());
    }

    public void recargarEstudiantes() {
        if (trabajando) {
            return;
        }

        cambiarEstado(true, "Cargando estudiantes...");

        new SwingWorker<List<Estudiante>, Void>() {

            @Override
            protected List<Estudiante> doInBackground() throws Exception {
                return controller.listarEstudiantes();
            }

            @Override
            protected void done() {
                boolean correcto = false;

                try {
                    List<Estudiante> estudiantes = get();
                    estudianteCombo.removeAllItems();

                    for (Estudiante estudiante : estudiantes) {
                        estudianteCombo.addItem(new EstudianteOpcion(
                                estudiante.getId(),
                                estudiante.getNombre(),
                                estudiante.getRut()
                        ));
                    }

                    correcto = true;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError(e);

                } catch (ExecutionException e) {
                    mostrarError(e.getCause());

                } finally {
                    cambiarEstado(false, " ");
                }

                if (correcto) {
                    consultar();
                }
            }
        }.execute();
    }

    public void consultar() {
        if (trabajando) {
            return;
        }

        String reporte = (String) reporteCombo.getSelectedItem();
        EstudianteOpcion estudiante =
                (EstudianteOpcion) estudianteCombo.getSelectedItem();

        if (reporte == null) {
            return;
        }

        if (HISTORIAL.equals(reporte) && estudiante == null) {
            modelo.setRowCount(0);
            estadoLabel.setText("No hay estudiantes disponibles.");
            return;
        }

        int idEstudiante = estudiante == null ? 0 : estudiante.id;
        cambiarEstado(true, "Generando reporte...");

        new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return switch (reporte) {
                    case MAS_PRESTADOS -> controller.librosMasPrestados();
                    case ACTIVOS -> controller.prestamosActivos();
                    case HISTORIAL -> controller.historial(idEstudiante);
                    default -> throw new IllegalArgumentException(
                            "Selecciona un reporte válido.");
                };
            }

            @Override
            protected void done() {
                String estadoFinal = " ";

                try {
                    List<Object[]> filas = get();

                    if (MAS_PRESTADOS.equals(reporte)) {
                        modelo.setColumnIdentifiers(new String[]{
                                "ID", "Título", "Autor", "Total de préstamos"
                        });
                    } else {
                        modelo.setColumnIdentifiers(new String[]{
                                "ID", "Estudiante", "Libro", "Fecha préstamo",
                                "Vencimiento", "Devolución real",
                                "Estado", "Días de atraso"
                        });
                    }

                    modelo.setRowCount(0);

                    for (Object[] fila : filas) {
                        modelo.addRow(fila);
                    }

                    estadoFinal = "Registros encontrados: " + filas.size();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError(e);

                } catch (ExecutionException e) {
                    mostrarError(e.getCause());

                } finally {
                    cambiarEstado(false, estadoFinal);
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
        boolean historial = HISTORIAL.equals(reporteCombo.getSelectedItem());

        reporteCombo.setEnabled(disponible);
        estudianteCombo.setEnabled(
                disponible && historial && controller.esBibliotecario());

        consultarButton.setEnabled(disponible);
        recargarButton.setEnabled(disponible);
        tabla.setEnabled(disponible);
    }

    private void mostrarError(Throwable error) {
        String mensaje = error.getMessage();

        JOptionPane.showMessageDialog(
                this,
                mensaje == null ? "No se pudo generar el reporte." : mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private record EstudianteOpcion(int id, String nombre, String rut) {

        @Override
        public String toString() {
            return nombre + " — " + rut;
        }
    }
}