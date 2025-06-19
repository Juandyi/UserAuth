import java.io.Serializable;
import java.time.LocalDateTime;

public class Publicacion implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String usuario;
    private String contenido;
    private LocalDateTime fecha;
    
    public Publicacion(String usuario, String contenido) {
        this.usuario = usuario;
        this.contenido = contenido;
        this.fecha = LocalDateTime.now();
    }
    
    public String getUsuario() {
        return usuario;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
}