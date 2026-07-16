package elrapiditos;

public class EnvioFactory {

    public static IEnvio crearPaquete(String nombre, double peso) {
        return new PaqueteSimple(nombre, peso);
    }

    public static IEnvio crearPaquete(String nombre, double peso,
                                      String origen, String destino) {
        double kg   = CalculadorTarifa.getTarifaKg(origen, destino);
        double base = CalculadorTarifa.getCostoBase(origen, destino);
        double min  = CalculadorTarifa.getMinimo(origen, destino);
        return new PaqueteSimple(nombre, peso, kg, base, min);
    }
}