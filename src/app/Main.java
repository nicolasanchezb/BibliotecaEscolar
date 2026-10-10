package app;

import view.LoginView;
import view.MainView;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println(
                        "Se utilizará la apariencia predeterminada.");
            }

            LoginView login = new LoginView(usuario -> {
                MainView ventana = new MainView(usuario);
                ventana.setVisible(true);
            });

            login.setVisible(true);
        });
    }
}