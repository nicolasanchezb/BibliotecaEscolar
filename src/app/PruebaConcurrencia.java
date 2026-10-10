package app;

import service.PrestamoService;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

public class PruebaConcurrencia {

    public static void main(String[] args) throws InterruptedException {
        int idLibro = 15;
        int idEstudiante = 1;

        CountDownLatch inicio = new CountDownLatch(1);

        Runnable tarea = () -> {
            PrestamoService servicio = new PrestamoService();

            try {
                inicio.await();

                servicio.registrarPrestamo(idEstudiante, idLibro);

                System.out.println(
                        Thread.currentThread().getName() + ": préstamo registrado");

            } catch (SQLException e) {
                System.out.println(
                        Thread.currentThread().getName() + ": " + e.getMessage());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        Thread hilo1 = new Thread(tarea, "Hilo 1");
        Thread hilo2 = new Thread(tarea, "Hilo 2");

        hilo1.start();
        hilo2.start();

        inicio.countDown();

        hilo1.join();
        hilo2.join();

        System.out.println("Prueba finalizada.");
    }
}