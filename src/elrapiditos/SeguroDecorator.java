package elrapiditos;

public class SeguroDecorator extends EnvioDecorator {

    public SeguroDecorator(IEnvio envioEnvuelto) {
        super(envioEnvuelto);
    }

    @Override
    public double calcularCosto() {
        return super.calcularCosto() * 1.15;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + "\n   + [Seguro contra Danos]";
    }
}