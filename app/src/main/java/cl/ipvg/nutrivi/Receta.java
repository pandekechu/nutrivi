package cl.ipvg.nutrivi;
public class Receta {
    private String id;
    private String nombrePlato;
    private String ingredientes;
    private int calorias;
    private String autor;
    private String userId;

    public Receta() {}

    public Receta(String id, String nombrePlato, String ingredientes, int calorias, String autor, String userId) {
        this.id = id;
        this.nombrePlato = nombrePlato;
        this.ingredientes = ingredientes;
        this.calorias = calorias;
        this.autor = autor;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombrePlato() { return nombrePlato; }
    public void setNombrePlato(String nombrePlato) { this.nombrePlato = nombrePlato; }
    public String getIngredientes() { return ingredientes; }
    public void setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }
    public int getCalorias() { return calorias; }
    public void setCalorias(int calorias) { this.calorias = calorias; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
