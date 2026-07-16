package elrapiditos;

public class ExpressDecorator extends EnvioDecorator {

    public ExpressDecorator(IEnvio envioEnvuelto) {
        super(envioEnvuelto);
    }

    @Override
    public double calcularCosto() {
        return super.calcularCosto() + 10.0;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + "\n   + [Entrega Express 24h]";
    }
}