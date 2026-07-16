package elrapiditos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class RegistroEnvio {

    public enum EstadoEnvio {
        PENDIENTE("Pendiente de Recojo"),
        EN_ALMACEN("En Almacen"),
        EN_TRANSITO("En Transito"),
        ENTREGADO("Entregado"),
        CANCELADO("Cancelado");

        private final String etiqueta;
        EstadoEnvio(String etiqueta) { this.etiqueta = etiqueta; }

        @Override
        public String toString() { return etiqueta; }
    }

    private final int    idInterno;
    private final String codigoSeguridad;
    private final String fechaRegistro;
    private final IEnvio envio;

    private final String remitente;
    private final String dniRemitente;
    private final String celRemitente;
    private final String destinatario;
    private final String dniDestinatario;
    private final String celDestinatario;

    private final String agenciaOrigen;
    private final String agenciaDestino;
    private       String metodoPago;

    private EstadoEnvio estado;

    public RegistroEnvio(int idInterno, IEnvio envio,
                         String remitente,    String dniRemitente,    String celRemitente,
                         String destinatario, String dniDestinatario, String celDestinatario,
                         String agenciaOrigen, String agenciaDestino) {
        this.idInterno       = idInterno;
        this.codigoSeguridad = UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase();
        this.fechaRegistro   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        this.envio           = envio;
        this.remitente       = remitente;
        this.dniRemitente    = dniRemitente;
        this.celRemitente    = celRemitente;
        this.destinatario    = destinatario;
        this.dniDestinatario = dniDestinatario;
        this.celDestinatario = celDestinatario;
        this.agenciaOrigen   = agenciaOrigen;
        this.agenciaDestino  = agenciaDestino;
        this.estado          = EstadoEnvio.PENDIENTE;
        this.metodoPago      = "Efectivo";
    }

    // Constructor para cargar desde base de datos con todos los campos
    public RegistroEnvio(int idInterno, String codigoSeguridad, String fechaRegistro,
                         IEnvio envio, String remitente, String dniRemitente, String celRemitente,
                         String destinatario, String dniDestinatario, String celDestinatario,
                         String agenciaOrigen, String agenciaDestino,
                         EstadoEnvio estado, String metodoPago) {
        this.idInterno       = idInterno;
        this.codigoSeguridad = codigoSeguridad;
        this.fechaRegistro   = fechaRegistro;
        this.envio           = envio;
        this.remitente       = remitente;
        this.dniRemitente    = dniRemitente;
        this.celRemitente    = celRemitente;
        this.destinatario    = destinatario;
        this.dniDestinatario = dniDestinatario;
        this.celDestinatario = celDestinatario;
        this.agenciaOrigen   = agenciaOrigen;
        this.agenciaDestino  = agenciaDestino;
        this.estado          = estado;
        this.metodoPago      = metodoPago;
    }

    public boolean setEstado(EstadoEnvio nuevoEstado) {
        if (this.estado == EstadoEnvio.CANCELADO || this.estado == EstadoEnvio.ENTREGADO) return false;
        this.estado = nuevoEstado;
        return true;
    }

    public void cancelar() { this.estado = EstadoEnvio.CANCELADO; }

    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public int         getIdInterno()        { return idInterno;        }
    public String      getCodigoSeguridad()  { return codigoSeguridad;  }
    public String      getFechaRegistro()    { return fechaRegistro;    }
    public IEnvio      getEnvio()            { return envio;            }
    public String      getRemitente()        { return remitente;        }
    public String      getDniRemitente()     { return dniRemitente;     }
    public String      getCelRemitente()     { return celRemitente;     }
    public String      getDestinatario()     { return destinatario;     }
    public String      getDniDestinatario()  { return dniDestinatario;  }
    public String      getCelDestinatario()  { return celDestinatario;  }
    public String      getAgenciaOrigen()    { return agenciaOrigen;    }
    public String      getAgenciaDestino()   { return agenciaDestino;   }
    public EstadoEnvio getEstado()           { return estado;           }
    public String      getMetodoPago()       { return metodoPago;       }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s | %s | S/. %.2f | %s",
            codigoSeguridad, agenciaOrigen, agenciaDestino,
            remitente, envio.calcularCosto(), estado);
    }
}
