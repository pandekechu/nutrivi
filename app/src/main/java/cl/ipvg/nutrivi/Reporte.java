package cl.ipvg.nutrivi;

public class Reporte {
    private String id;
    private String receta;
    private String autorReceta;
    private String motivo;
    private String reportadoPor;
    private long fecha;

    public Reporte() {}

    public Reporte(String id, String receta, String autorReceta, String motivo, String reportadoPor, long fecha) {
        this.id = id;
        this.receta = receta;
        this.autorReceta = autorReceta;
        this.motivo = motivo;
        this.reportadoPor = reportadoPor;
        this.fecha = fecha;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReceta() { return receta; }
    public String getAutorReceta() { return autorReceta; }
    public String getMotivo() { return motivo; }
    public String getReportadoPor() { return reportadoPor; }
    public long getFecha() { return fecha; }
}
