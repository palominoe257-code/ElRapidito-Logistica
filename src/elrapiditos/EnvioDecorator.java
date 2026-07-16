package elrapiditos;

public abstract class EnvioDecorator implements IEnvio {
    protected IEnvio envioEnvuelto;

    public EnvioDecorator(IEnvio envioEnvuelto) {
        this.envioEnvuelto = envioEnvuelto;
    }

    @Override
    public double calcularCosto() {
        return envioEnvuelto.calcularCosto();
    }

    @Override
    public String getDescripcion() {
        return envioEnvuelto.getDescripcion();
    }
}