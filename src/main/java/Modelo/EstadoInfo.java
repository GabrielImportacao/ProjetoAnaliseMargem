package Modelo;

import java.math.BigDecimal;

public class EstadoInfo {
    private final String sigla;
    private final BigDecimal baseMargem;

    public EstadoInfo(String sigla, BigDecimal baseMargem) {
        this.sigla = sigla;
        this.baseMargem = baseMargem;
    }

    public String getSigla() {
        return sigla;
    }

    public BigDecimal getBaseMargem() {
        return baseMargem;
    }

    @Override
    public String toString() {
        return sigla;
    }
}
