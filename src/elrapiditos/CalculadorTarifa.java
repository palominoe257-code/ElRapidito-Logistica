package elrapiditos;

public class CalculadorTarifa {

    public enum Zona {
        LIMA_METRO, NORTE, SUR_COSTA, SIERRA, SELVA
    }

    public enum Nivel {
        LOCAL, CORTO, MEDIO, LARGO
    }

    private static final double[] KG   = { 0.20, 0.40, 0.60, 0.80 };
    private static final double[] BASE = { 4.00, 8.00, 12.00, 15.00 };
    private static final double[] MIN  = { 5.00, 10.00, 15.00, 20.00 };

    public static Zona obtenerZona(String agencia) {
        if (agencia == null) return Zona.LIMA_METRO;
        String a = agencia.toLowerCase();
        if (a.contains("lima") || a.contains("callao"))          return Zona.LIMA_METRO;
        if (a.contains("trujillo") || a.contains("chiclayo")
                                   || a.contains("piura"))       return Zona.NORTE;
        if (a.contains("arequipa") || a.contains("tacna"))       return Zona.SUR_COSTA;
        if (a.contains("cusco")    || a.contains("puno"))        return Zona.SIERRA;
        if (a.contains("iquitos"))                                return Zona.SELVA;
        return Zona.LIMA_METRO;
    }

    public static Nivel obtenerNivel(String origen, String destino) {
        Zona zo = obtenerZona(origen);
        Zona zd = obtenerZona(destino);

        if (zo == zd) return Nivel.LOCAL;

        if ((zo == Zona.SUR_COSTA && zd == Zona.SIERRA)
         || (zo == Zona.SIERRA    && zd == Zona.SUR_COSTA)) return Nivel.CORTO;

        if ((zo == Zona.NORTE && zd == Zona.SELVA)
         || (zo == Zona.SELVA && zd == Zona.NORTE))         return Nivel.MEDIO;

        if (zo == Zona.LIMA_METRO || zd == Zona.LIMA_METRO) {
            Zona otra = (zo == Zona.LIMA_METRO) ? zd : zo;
            if (otra == Zona.NORTE || otra == Zona.SUR_COSTA) return Nivel.MEDIO;
        }

        return Nivel.LARGO;
    }

    public static double getTarifaKg(String origen, String destino) {
        return KG[obtenerNivel(origen, destino).ordinal()];
    }

    public static double getCostoBase(String origen, String destino) {
        return BASE[obtenerNivel(origen, destino).ordinal()];
    }

    public static double getMinimo(String origen, String destino) {
        return MIN[obtenerNivel(origen, destino).ordinal()];
    }

    public static double calcularCosto(double peso, String origen, String destino) {
        int idx = obtenerNivel(origen, destino).ordinal();
        double costo = peso * KG[idx] + BASE[idx];
        return Math.max(costo, MIN[idx]);
    }

    public static String getEtiquetaTarifa(String origen, String destino) {
        Nivel nivel = obtenerNivel(origen, destino);
        int idx = nivel.ordinal();
        String tipo;
        switch (nivel) {
            case LOCAL:  tipo = "Tarifa Local";            break;
            case CORTO:  tipo = "Tarifa Regional";         break;
            case MEDIO:  tipo = "Tarifa Interprovincial";  break;
            default:     tipo = "Tarifa Larga Distancia";  break;
        }
        return String.format("%s  |  S/. %.2f/kg + S/. %.2f base  |  Min. S/. %.2f",
            tipo, KG[idx], BASE[idx], MIN[idx]);
    }
}
