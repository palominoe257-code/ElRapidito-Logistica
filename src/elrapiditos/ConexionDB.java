package elrapiditos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String HOST     = "localhost";
    private static final String PUERTO   = "3306";
    private static final String BD       = "elrapiditos_db";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "";

    private static final String URL =
        "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BD +
        "?useSSL=false&serverTimezone=America/Lima&allowPublicKeyRetrieval=true";

    private static Connection conexion = null;

    private ConexionDB() {}

    public static Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                System.out.println("[DB] Conexion establecida con: " + BD);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver no encontrado. Agrega el .jar al proyecto.");
            conexion = null;
        } catch (SQLException e) {
            System.err.println("[DB] Error al conectar: " + e.getMessage());
            conexion = null;
        }
        return conexion;
    }

    public static boolean estaConectado() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                conexion = null;
                System.out.println("[DB] Conexion cerrada.");
            } catch (SQLException e) {
                System.err.println("[DB] Error al cerrar: " + e.getMessage());
            }
        }
    }
}
