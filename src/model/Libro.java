package model;

public class Libro {
    private int id;
    private int stock;
    private int idCategoria;
    private String titulo;
    private String autor;
    private String isbn;
    private String editorial;

    public Libro(int id, int stock, int idCategoria, String titulo, String autor, String isbn, String editorial) {
        this.id = id;
        this.stock = stock;
        this.idCategoria = idCategoria;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
    }

    public int getId() {
        return id;}

    public int getStock() {
        return stock;}

    public void setStock(int stock) {
        this.stock = stock;}

    public int getIdCategoria() {
          return idCategoria;}

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;}

    public String getTitulo() {
        return titulo;}

    public String getAutor() {
        return autor;}

    public String getIsbn() {
        return isbn;}

    public String getEditorial() {
        return editorial;}
}
