package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustoPromobItem {

    private final String codigoItem;
    private final BigDecimal custo;
    private final LocalDate dataMovimento;

    public CustoPromobItem(String codigoItem, BigDecimal custo, LocalDate dataMovimento) {
        this.codigoItem = codigoItem;
        this.custo = custo;
        this.dataMovimento = dataMovimento;
    }

    public String getCodigoItem() {
        return codigoItem;
    }

    public BigDecimal getCusto() {
        return custo;
    }

    public LocalDate getDataMovimento() {
        return dataMovimento;
    }

    @Override
    public String toString() {
        return "CustoPromobItem{" +
                "codigoItem='" + codigoItem + '\'' +
                ", custo=" + custo +
                ", dataMovimento=" + dataMovimento +
                '}';
    }
}