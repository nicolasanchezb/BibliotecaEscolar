package view;

import model.Usuario;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {

    private final LibrosPanel librosPanel;
    private final EstudiantesPanel estudiantesPanel;
    private final PrestamosPanel prestamosPanel;
    private final ReportesPanel reportesPanel;

    public MainView(Usuario usuario) {
        setTitle("Biblioteca Escolar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setSize(1150, 800);
        setLocationRelativeTo(null);

        librosPanel = new LibrosPanel(usuario);
        prestamosPanel = new PrestamosPanel(usuario);
        reportesPanel = new ReportesPanel(usuario);

        estudiantesPanel = usuario.esBibliotecario()
                ? new EstudiantesPanel(usuario)
                : null;

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel sesionLabel = new JLabel(
                usuario.getNombre() + " | Rol: " + usuario.getRol());

        sesionLabel.setFont(
                sesionLabel.getFont().deriveFont(Font.BOLD, 16f));

        encabezado.add(sesionLabel, BorderLayout.WEST);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Libros", librosPanel);

        if (estudiantesPanel != null) {
            pestanas.addTab("Estudiantes", estudiantesPanel);
        }

        pestanas.addTab("Préstamos y devoluciones", prestamosPanel);
        pestanas.addTab("Reportes", reportesPanel);

        pestanas.addChangeListener(e -> {
            Component seleccionado = pestanas.getSelectedComponent();

            if (seleccionado == librosPanel) {
                librosPanel.recargar();
            } else if (seleccionado == estudiantesPanel
                    && estudiantesPanel != null) {
                estudiantesPanel.recargar();
            } else if (seleccionado == prestamosPanel) {
                prestamosPanel.recargar();
            } else if (seleccionado == reportesPanel) {
                reportesPanel.recargarEstudiantes();
            }
        });

        prestamosPanel.addPropertyChangeListener(
                "datosActualizados",
                e -> {
                    librosPanel.recargar();
                    reportesPanel.consultar();
                }
        );

        setLayout(new BorderLayout());
        add(encabezado, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
    }
}