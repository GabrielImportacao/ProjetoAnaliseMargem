package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustoVerdadeiroItem {

    private final String codigoItem;
    private final BigDecimal custo;
    private final BigDecimal estoqueAtual;
    private final BigDecimal quantidadeLotes;
    private final LocalDate dataReferencia;
    private final int quantidadeLotesUsados;

    public CustoVerdadeiroItem(
            String codigoItem,
            BigDecimal custo,
            BigDecimal estoqueAtual,
            BigDecimal quantidadeLotes,
            LocalDate dataReferencia,
            int quantidadeLotesUsados
    ) {
        this.codigoItem = codigoItem;
        this.custo = custo;
        this.estoqueAtual = estoqueAtual;
        this.quantidadeLotes = quantidadeLotes;
        this.dataReferencia = dataReferencia;
        this.quantidadeLotesUsados = quantidadeLotesUsados;
    }

    public String getCodigoItem() {
        return codigoItem;
    }

    public BigDecimal getCusto() {
        return custo;
    }

    public BigDecimal getEstoqueAtual() {
        return estoqueAtual;
    }

    public BigDecimal getQuantidadeLotes() {
        return quantidadeLotes;
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public int getQuantidadeLotesUsados() {
        return quantidadeLotesUsados;
    }
}