package elrapiditos;

import java.util.ArrayList;
import java.util.List;

public class CajaComposite implements IEnvio {

    private String nombreCaja;
    private List<IEnvio> componentes = new ArrayList<>();

    public CajaComposite(String nombreCaja) {
        this.nombreCaja = nombreCaja;
    }

    public void crearYAgregarPaquete(String nombre, double peso) {
        IEnvio nuevoPaquete = EnvioFactory.crearPaquete(nombre, peso);
        this.componentes.add(nuevoPaquete);
    }

    public void agregarEnvio(IEnvio envio) {
        componentes.add(envio);
    }

    @Override
    public double calcularCosto() {
        double costoTotal = 0;
        for (IEnvio envio : componentes) {
            costoTotal += envio.calcularCosto();
        }
        return costoTotal;
    }

    @Override
    public String getDescripcion() {
        StringBuilder desc = new StringBuilder("[" + nombreCaja + " contiene: ");
        for (int i = 0; i < componentes.size(); i++) {
            desc.append(componentes.get(i).getDescripcion());
            if (i < componentes.size() - 1) desc.append(", ");
        }
        desc.append("]");
        return desc.toString();
    }
}