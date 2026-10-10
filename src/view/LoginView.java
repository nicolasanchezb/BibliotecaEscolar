package view;

import controller.AuthController;
import model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class LoginView extends JFrame {

    private final AuthController controller = new AuthController();
    private final Consumer<Usuario> alIngresar;

    private final JTextField correoField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);
    private final JButton ingresarButton = new JButton("Ingresar");
    private final JLabel estadoLabel = new JLabel(" ");

    public LoginView(Consumer<Usuario> alIngresar) {
        this.alIngresar = alIngresar;

        setTitle("Biblioteca Escolar — Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        construirInterfaz();

        ingresarButton.addActionListener(event -> iniciarSesion());
        getRootPane().setDefaultButton(ingresarButton);

        pack();
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 0, 6, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridx = 0;

        JLabel titulo = new JLabel("Biblioteca Escolar");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 24f));

        constraints.gridy = 0;
        panel.add(titulo, constraints);

        constraints.gridy = 1;
        panel.add(new JLabel("Ingresa con tu cuenta registrada."), constraints);

        constraints.gridy = 2;
        panel.add(new JLabel("Correo:"), constraints);

        constraints.gridy = 3;
        panel.add(correoField, constraints);

        constraints.gridy = 4;
        panel.add(new JLabel("Contraseña:"), constraints);

        constraints.gridy = 5;
        panel.add(passwordField, constraints);

        constraints.gridy = 6;
        panel.add(ingresarButton, constraints);

        constraints.gridy = 7;
        panel.add(estadoLabel, constraints);

        setContentPane(panel);
    }

    private void iniciarSesion() {
        String correo = correoField.getText().trim();
        char[] caracteres = passwordField.getPassword();

        if (correo.isBlank() || caracteres.length == 0) {
            Arrays.fill(caracteres, '\0');

            JOptionPane.showMessageDialog(
                    this,
                    "Ingresa tu correo y contraseña.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String password = new String(caracteres);
        Arrays.fill(caracteres, '\0');

        cambiarEstado(true);

        SwingWorker<Usuario, Void> worker = new SwingWorker<>() {

            @Override
            protected Usuario doInBackground() throws Exception {
                return controller.iniciarSesion(correo, password);
            }

            @Override
            protected void done() {
                try {
                    Usuario usuario = get();
                    passwordField.setText("");

                    alIngresar.accept(usuario);
                    dispose();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mostrarError("La operación fue interrumpida.");

                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();

                    if (causa instanceof IllegalArgumentException) {
                        mostrarError(causa.getMessage());
                    } else {
                        mostrarError(
                                "No se pudo conectar con la base de datos. "
                                        + "Revisa la configuración de conexión.");
                    }

                } catch (RuntimeException e) {
                    mostrarError("No se pudo abrir la ventana principal.");

                } finally {
                    cambiarEstado(false);
                }
            }
        };

        worker.execute();
    }

    private void cambiarEstado(boolean trabajando) {
        correoField.setEnabled(!trabajando);
        passwordField.setEnabled(!trabajando);
        ingresarButton.setEnabled(!trabajando);
        estadoLabel.setText(trabajando ? "Validando ingreso..." : " ");
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "No se pudo ingresar",
                JOptionPane.ERROR_MESSAGE
        );
    }
}