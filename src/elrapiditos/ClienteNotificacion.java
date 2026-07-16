package elrapiditos;

import javax.swing.JOptionPane;

public class ClienteNotificacion implements IObserver {

    private String nombreCliente;

    public ClienteNotificacion(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    @Override
    public void actualizarEstado(String nuevoEstado) {
        JOptionPane.showMessageDialog(null,
            "SMS para " + nombreCliente + ":\nEstado de su paquete: '" + nuevoEstado + "'",
            "Notificacion - El Rapidito",
            JOptionPane.INFORMATION_MESSAGE);
    }
}