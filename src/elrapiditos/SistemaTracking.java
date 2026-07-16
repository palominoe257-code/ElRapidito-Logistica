package elrapiditos;

import java.util.ArrayList;
import java.util.List;

public class SistemaTracking {

    private List<IObserver> observadores = new ArrayList<>();
    private String estadoActual;
    private IEnvio envio;

    public SistemaTracking(IEnvio envio) {
        this.envio = envio;
        this.estadoActual = "Recibido en Almacen";
    }

    public void suscribir(IObserver observer) {
        observadores.add(observer);
    }

    public void cambiarEstado(String nuevoEstado) {
        this.estadoActual = nuevoEstado;
        notificarObservadores();
    }

    private void notificarObservadores() {
        for (IObserver obs : observadores) {
            obs.actualizarEstado(estadoActual);
        }
    }
}