package elrapiditos;

import javax.swing.JOptionPane;

public class SesionActiva {

    private static SesionActiva instanciaUnica;

    private String usuarioActual;
    private String rolActual;
    private boolean logueado;

    private SesionActiva() {
        this.logueado = false;
    }

    public static SesionActiva getInstancia() {
        if (instanciaUnica == null) {
            instanciaUnica = new SesionActiva();
        }
        return instanciaUnica;
    }

    public boolean hacerLogin(String usuario, String password) {
        if (usuario.equals("jleonel") && password.equals("admin123")) {
            this.usuarioActual = "Jose Leonel";
            this.rolActual = "Administrador Logistico";
            this.logueado = true;
            return true;
        }
        return false;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
        this.rolActual = null;
        this.logueado = false;
        JOptionPane.showMessageDialog(null, "Sesion cerrada correctamente.");
    }

    public boolean isLogueado()       { return logueado;       }
    public String  getNombreUsuario() { return usuarioActual;  }
    public String  getRol()           { return rolActual;      }
}
