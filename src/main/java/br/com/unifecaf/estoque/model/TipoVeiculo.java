package br.com.unifecaf.estoque.model;

/**
 * Tipos de veiculo aceitos pelo sistema.
 */
public enum TipoVeiculo {
    HATCH("Hatch"),
    SEDAN("Sedan"),
    SUV("SUV"),
    PICAPE("Picape"),
    MOTO("Moto"),
    UTILITARIO("Utilitario");

    private final String rotulo;

    TipoVeiculo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
