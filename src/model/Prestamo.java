package model;

import java.time.LocalDate;

public class Prestamo {
    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private boolean devuelto;

    public Prestamo(int id, int idEstudiante, int idLibro,
                    LocalDate fechaPrestamo, LocalDate fechaDevolucion, boolean devuelto) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
    }
    public int getId() {
        return id;}

    public int getIdEstudiante() {
        return idEstudiante;}

    public int getIdLibro() {
        return idLibro;}

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;}

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;}

    public boolean isDevuelto() {
        return devuelto;}

    public void setDevuelto(boolean devuelto) {
        this.devuelto = devuelto;
    }
}
