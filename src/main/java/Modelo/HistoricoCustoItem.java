package Modelo;

import java.util.Optional;

public class HistoricoCustoItem {

    private final CustoItem custoAtual;
    private final CustoItem custoAnterior;

    public HistoricoCustoItem(CustoItem custoAtual, CustoItem custoAnterior) {
        this.custoAtual = custoAtual;
        this.custoAnterior = custoAnterior;
    }

    public Optional<CustoItem> getCustoAtual() {
        return Optional.ofNullable(custoAtual);
    }

    public Optional<CustoItem> getCustoAnterior() {
        return Optional.ofNullable(custoAnterior);
    }

    public boolean possuiCustoAtual() {
        return custoAtual != null;
    }

    public boolean possuiCustoAnterior() {
        return custoAnterior != null;
    }

    @Override
    public String toString() {
        return "HistoricoCustoItem{" +
                "custoAtual=" + custoAtual +
                ", custoAnterior=" + custoAnterior +
                '}';
    }
}