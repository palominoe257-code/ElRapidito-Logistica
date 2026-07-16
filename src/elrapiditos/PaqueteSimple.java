package elrapiditos;

public class PaqueteSimple implements IEnvio {

    private final String nombre;
    private final double peso;
    private final double tarifaPorKg;
    private final double costoBase;
    private final double minimo;

    public PaqueteSimple(String nombre, double peso) {
        this(nombre, peso, 0.80, 5.00, 7.00);
    }

    public PaqueteSimple(String nombre, double peso,
                         double tarifaPorKg, double costoBase, double minimo) {
        this.nombre      = nombre;
        this.peso        = peso;
        this.tarifaPorKg = tarifaPorKg;
        this.costoBase   = costoBase;
        this.minimo      = minimo;
    }

    @Override
    public double calcularCosto() {
        double costo = peso * tarifaPorKg + costoBase;
        return Math.max(costo, minimo);
    }

    @Override
    public String getDescripcion() {
        return nombre + " (" + peso + " kg)";
    }
}