package cl.ipvg.nutrivi;
public class Receta {
    private String nombrePlato;
    private String ingredientes;
    private int calorias;
    private String autor;


    public Receta() {}

    public Receta(String nombrePlato, String ingredientes, int calorias, String autor) {
        this.nombrePlato = nombrePlato;
        this.ingredientes = ingredientes;
        this.calorias = calorias;
        this.autor = autor;
    }


    public String getNombrePlato() { return nombrePlato; }
    public String getIngredientes() { return ingredientes; }
    public int getCalorias() { return calorias; }
    public String getAutor() { return autor; }
}
