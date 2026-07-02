package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HistoricoSaidaItem {

    private final String codigoItem;
    private final LocalDate dataSaida;
    private final BigDecimal quantidadeFaturada;
    private final String numeroNotaFiscal;
    private final String serie;
    private final String sequencia;
    private final String numeroPedido;

    public HistoricoSaidaItem(
            String codigoItem,
            LocalDate dataSaida,
            BigDecimal quantidadeFaturada,
            String numeroNotaFiscal,
            String serie,
            String sequencia,
            String numeroPedido
    ) {
        this.codigoItem = codigoItem;
        this.dataSaida = dataSaida;
        this.quantidadeFaturada = quantidadeFaturada;
        this.numeroNotaFiscal = numeroNotaFiscal;
        this.serie = serie;
        this.sequencia = sequencia;
        this.numeroPedido = numeroPedido;
    }

    public String getCodigoItem() {
        return codigoItem;
    }

    public LocalDate getDataSaida() {
        return dataSaida;
    }

    public BigDecimal getQuantidadeFaturada() {
        return quantidadeFaturada;
    }

    public String getNumeroNotaFiscal() {
        return numeroNotaFiscal;
    }

    public String getSerie() {
        return serie;
    }

    public String getSequencia() {
        return sequencia;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }
}