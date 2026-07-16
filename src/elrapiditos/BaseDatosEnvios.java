package elrapiditos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BaseDatosEnvios {

    private static BaseDatosEnvios instanciaUnica;

    private BaseDatosEnvios() {
    }

    public static BaseDatosEnvios getInstancia() {
        if (instanciaUnica == null) {
            instanciaUnica = new BaseDatosEnvios();
        }
        return instanciaUnica;
    }

    // -----------------------------------------------------------------------
    // Helper: Mapear fila de ResultSet a Objeto RegistroEnvio
    // -----------------------------------------------------------------------
    private RegistroEnvio mapearEnvio(ResultSet rs) throws SQLException {
        int idInterno = rs.getInt("id_interno");
        String codigoSeguridad = rs.getString("codigo_seguridad");
        
        // Formatear la fecha a dd/MM/yyyy HH:mm
        java.sql.Timestamp ts = rs.getTimestamp("fecha_registro");
        String fechaRegistro = ts.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        String descripcion = rs.getString("descripcion");
        double peso = rs.getDouble("peso_kg");
        boolean seguro = rs.getBoolean("tiene_seguro");
        boolean express = rs.getBoolean("tiene_express");
        String origen = rs.getString("agencia_origen");
        String destino = rs.getString("agencia_destino");

        // Limpiar la descripción de las etiquetas [ ] que le pusimos para que no se dupliquen
        String descLimpia = descripcion.replace(" [+Seguro]", "").replace(" [+Express]", "");
        
        // Reconstruir el paquete y decoradores para calcularCosto y descripciones
        IEnvio envioFinal = EnvioFactory.crearPaquete(descLimpia, peso, origen, destino);
        
        if (seguro) envioFinal = new SeguroDecorator(envioFinal);
        if (express) envioFinal = new ExpressDecorator(envioFinal);

        // Convertir string de estado a Enum
        RegistroEnvio.EstadoEnvio estado = RegistroEnvio.EstadoEnvio.PENDIENTE;
        String estadoDb = rs.getString("estado");
        for (RegistroEnvio.EstadoEnvio e : RegistroEnvio.EstadoEnvio.values()) {
            if (e.toString().equals(estadoDb)) {
                estado = e;
                break;
            }
        }

        return new RegistroEnvio(
            idInterno, codigoSeguridad, fechaRegistro,
            envioFinal,
            rs.getString("remitente"), rs.getString("dni_remitente"), rs.getString("cel_remitente"),
            rs.getString("destinatario"), rs.getString("dni_destinatario"), rs.getString("cel_destinatario"),
            origen, destino,
            estado, rs.getString("metodo_pago")
        );
    }

    // -----------------------------------------------------------------------
    // CREATE
    // -----------------------------------------------------------------------
    public RegistroEnvio registrarEnvio(IEnvio envio,
                                         String remitente,    String dniRem,    String celRem,
                                         String destinatario, String dniDest,   String celDest,
                                         String agenciaOrigen, String agenciaDestino) {
        
        // Objeto temporal para usar su constructor normal y que autogenere fecha y código
        RegistroEnvio regTmp = new RegistroEnvio(0, envio, remitente, dniRem, celRem, destinatario, dniDest, celDest, agenciaOrigen, agenciaDestino);
        
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return regTmp; // Si no hay BD, devuelve el temporal (aunque no estara guardado)

        String sql = "INSERT INTO envios " +
                     "(codigo_seguridad, descripcion, peso_kg, tiene_seguro, tiene_express, costo_total, " +
                     "agencia_origen, agencia_destino, zona_nivel, " +
                     "remitente, dni_remitente, cel_remitente, " +
                     "destinatario, dni_destinatario, cel_destinatario, " +
                     "estado, metodo_pago, atendido_por) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        boolean seguro = envio.getDescripcion().contains("Seguro");
        boolean express = envio.getDescripcion().contains("Express");
        
        // Intentar extraer el peso numerico desde la descripcion limpia "Nombre (X kg)"
        double peso = 1.0;
        try {
             String desc = envio.getDescripcion();
             int idx1 = desc.indexOf("(");
             int idx2 = desc.indexOf(" kg)");
             if(idx1 != -1 && idx2 != -1) {
                 peso = Double.parseDouble(desc.substring(idx1 + 1, idx2));
             }
        } catch(Exception e) {}

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, regTmp.getCodigoSeguridad());
            stmt.setString(2, envio.getDescripcion());
            stmt.setDouble(3, peso);
            stmt.setBoolean(4, seguro);
            stmt.setBoolean(5, express);
            stmt.setDouble(6, envio.calcularCosto());
            stmt.setString(7, agenciaOrigen);
            stmt.setString(8, agenciaDestino);
            stmt.setString(9, CalculadorTarifa.obtenerNivel(agenciaOrigen, agenciaDestino).name());
            
            stmt.setString(10, remitente);
            stmt.setString(11, dniRem);
            stmt.setString(12, celRem);
            
            stmt.setString(13, destinatario);
            stmt.setString(14, dniDest);
            stmt.setString(15, celDest);
            
            stmt.setString(16, regTmp.getEstado().toString());
            stmt.setString(17, regTmp.getMetodoPago());
            
            String atendido = SesionActiva.getInstancia().getNombreUsuario();
            stmt.setString(18, atendido != null ? atendido : "sistema");

            stmt.executeUpdate();
            
            return buscarPorCodigo(regTmp.getCodigoSeguridad());
            
        } catch (SQLException e) {
            System.err.println("[DB] Error al insertar envio: " + e.getMessage());
        }
        return regTmp;
    }

    // -----------------------------------------------------------------------
    // READ
    // -----------------------------------------------------------------------
    public List<RegistroEnvio> obtenerTodos() {
        List<RegistroEnvio> lista = new ArrayList<>();
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return lista;

        String sql = "SELECT * FROM envios ORDER BY fecha_registro DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearEnvio(rs));
            }
        } catch (SQLException e) {
             System.err.println("[DB] Error al listar envios: " + e.getMessage());
        }
        return lista;
    }

    public List<RegistroEnvio> filtrarPorEstado(RegistroEnvio.EstadoEnvio estado) {
        List<RegistroEnvio> lista = new ArrayList<>();
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return lista;

        String sql = "SELECT * FROM envios WHERE estado = ? ORDER BY fecha_registro DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEnvio(rs));
                }
            }
        } catch (SQLException e) {
             System.err.println("[DB] Error al filtrar envios: " + e.getMessage());
        }
        return lista;
    }

    public RegistroEnvio buscarPorCodigo(String codigo) {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return null;

        String sql = "SELECT * FROM envios WHERE codigo_seguridad = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEnvio(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error al buscar envio: " + e.getMessage());
        }
        return null;
    }

    public List<RegistroEnvio> buscarPorDni(String dni) {
        List<RegistroEnvio> lista = new ArrayList<>();
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return lista;

        String sql = "SELECT * FROM envios WHERE dni_remitente LIKE ? OR dni_destinatario LIKE ? ORDER BY fecha_registro DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + dni + "%");
            stmt.setString(2, "%" + dni + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEnvio(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error al buscar por DNI: " + e.getMessage());
        }
        return lista;
    }

    public List<RegistroEnvio> buscarPorNombre(String nombre) {
        List<RegistroEnvio> lista = new ArrayList<>();
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return lista;

        String sql = "SELECT * FROM envios WHERE remitente LIKE ? OR destinatario LIKE ? ORDER BY fecha_registro DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + nombre + "%");
            stmt.setString(2, "%" + nombre + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEnvio(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error al buscar por Nombre: " + e.getMessage());
        }
        return lista;
    }

    // -----------------------------------------------------------------------
    // UPDATE
    // -----------------------------------------------------------------------
    public boolean actualizarEstado(String codigoSeguridad, RegistroEnvio.EstadoEnvio nuevoEstado) {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return false;

        String sql = "UPDATE envios SET estado = ? WHERE codigo_seguridad = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado.toString());
            stmt.setString(2, codigoSeguridad);
            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error al actualizar estado directo: " + e.getMessage());
            return false;
        }
    }
    public boolean avanzarEstado(String codigoSeguridad) {
        RegistroEnvio reg = buscarPorCodigo(codigoSeguridad);
        if (reg == null) return false;

        RegistroEnvio.EstadoEnvio nuevoEstado = siguienteEstado(reg.getEstado());
        if (nuevoEstado == null) return false;

        Connection conn = ConexionDB.getConexion();
        if (conn == null) return false;

        String sql = "UPDATE envios SET estado = ? WHERE codigo_seguridad = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado.toString());
            stmt.setString(2, codigoSeguridad);
            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("[DB] Error al actualizar estado: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelarEnvio(String codigoSeguridad) {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return false;

        String sql = "UPDATE envios SET estado = 'Cancelado' WHERE codigo_seguridad = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigoSeguridad);
            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
             System.err.println("[DB] Error al cancelar envio: " + e.getMessage());
             return false;
        }
    }

    // -----------------------------------------------------------------------
    // ESTADISTICAS Y AGREGACIONES
    // -----------------------------------------------------------------------
    public int totalEnvios() {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return 0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM envios");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {}
        return 0;
    }

    public int totalActivos() {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return 0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM envios WHERE estado NOT IN ('Cancelado', 'Entregado')");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {}
        return 0;
    }

    public int contarPorEstado(RegistroEnvio.EstadoEnvio estado) {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return 0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM envios WHERE estado = ?")) {
            stmt.setString(1, estado.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {}
        return 0;
    }

    public double calcularIngresosTotal() {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return 0.0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT SUM(costo_total) FROM envios");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {}
        return 0.0;
    }

    public double calcularIngresosNetos() {
        Connection conn = ConexionDB.getConexion();
        if (conn == null) return 0.0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT SUM(costo_total) FROM envios WHERE estado != 'Cancelado'");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {}
        return 0.0;
    }

    // -----------------------------------------------------------------------
    // DATOS DE EJEMPLO
    // -----------------------------------------------------------------------
    public void cargarDatosEjemplo() {
        System.out.println("[DB] La funcion de datos de ejemplo fue desactivada. Los datos provienen directamente de MySQL (elrapiditos_db).");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private RegistroEnvio.EstadoEnvio siguienteEstado(RegistroEnvio.EstadoEnvio actual) {
        switch (actual) {
            case PENDIENTE:   return RegistroEnvio.EstadoEnvio.EN_ALMACEN;
            case EN_ALMACEN:  return RegistroEnvio.EstadoEnvio.EN_TRANSITO;
            case EN_TRANSITO: return RegistroEnvio.EstadoEnvio.ENTREGADO;
            default:          return null;
        }
    }
}
