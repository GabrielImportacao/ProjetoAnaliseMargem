package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DadosItemConsulta {

    private final String codigoItem;
    private final String descricao;
    private final BigDecimal ipi;
    private final String ncm;
    private final String unidadeVenda;
    private final BigDecimal pesoBruto;
    private final BigDecimal pesoLiquido;
    private final BigDecimal precoUnitarioLiquidoAtual;

    private final BigDecimal custoAtual;
    private final String registroCustoAtual;
    private final LocalDate dataCustoAtual;
    private final FonteCusto fonteCustoAtual;

    private final BigDecimal custoPromob;
    private final String registroCustoPromob;
    private final LocalDate dataCustoPromob;
    private final FonteCusto fonteCustoPromob;

    private final BigDecimal custoAnterior;
    private final String registroCustoAnterior;
    private final LocalDate dataCustoAnterior;
    private final FonteCusto fonteCustoAnterior;
    
    private final LocalDate dataUltimaSaida;
    private final boolean itemEncalhado;

    public DadosItemConsulta(
            String codigoItem,
            String descricao,
            BigDecimal ipi,
            String ncm,
            String unidadeVenda,
            BigDecimal pesoBruto,
            BigDecimal pesoLiquido,
            BigDecimal precoUnitarioLiquidoAtual,
            BigDecimal custoAtual,
            String registroCustoAtual,
            LocalDate dataCustoAtual,
            FonteCusto fonteCustoAtual,
            BigDecimal custoPromob,
            String registroCustoPromob,
            LocalDate dataCustoPromob,
            FonteCusto fonteCustoPromob,
            BigDecimal custoAnterior,
            String registroCustoAnterior,
            LocalDate dataCustoAnterior,
            FonteCusto fonteCustoAnterior,
            LocalDate dataUltimaSaida,
            boolean itemEncalhado
    ) {
        this.codigoItem = codigoItem;
        this.descricao = descricao;
        this.ipi = ipi;
        this.ncm = ncm;
        this.unidadeVenda = unidadeVenda;
        this.pesoBruto = pesoBruto;
        this.pesoLiquido = pesoLiquido;
        this.precoUnitarioLiquidoAtual = precoUnitarioLiquidoAtual;
        this.custoAtual = custoAtual;
        this.registroCustoAtual = registroCustoAtual;
        this.dataCustoAtual = dataCustoAtual;
        this.fonteCustoAtual = fonteCustoAtual;
        this.custoPromob = custoPromob;
        this.registroCustoPromob = registroCustoPromob;
        this.dataCustoPromob = dataCustoPromob;
        this.fonteCustoPromob = fonteCustoPromob;
        this.custoAnterior = custoAnterior;
        this.registroCustoAnterior = registroCustoAnterior;
        this.dataCustoAnterior = dataCustoAnterior;
        this.fonteCustoAnterior = fonteCustoAnterior;
        this.dataUltimaSaida = dataUltimaSaida;
        this.itemEncalhado = itemEncalhado;
    }

    public LocalDate getDataUltimaSaida() {
        return dataUltimaSaida;
    }

    public boolean isItemEncalhado() {
        return itemEncalhado;
    }
    
    public String getCodigoItem() {
        return codigoItem;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getIpi() {
        return ipi;
    }

    public String getNcm() {
        return ncm;
    }

    public String getUnidadeVenda() {
        return unidadeVenda;
    }

    public BigDecimal getPesoBruto() {
        return pesoBruto;
    }

    public BigDecimal getPesoLiquido() {
        return pesoLiquido;
    }

    public BigDecimal getPrecoUnitarioLiquidoAtual() {
        return precoUnitarioLiquidoAtual;
    }

    public BigDecimal getCustoAtual() {
        return custoAtual;
    }

    public String getRegistroCustoAtual() {
        return registroCustoAtual;
    }

    public LocalDate getDataCustoAtual() {
        return dataCustoAtual;
    }

    public FonteCusto getFonteCustoAtual() {
        return fonteCustoAtual;
    }

    public BigDecimal getCustoPromob() {
        return custoPromob;
    }

    public String getRegistroCustoPromob() {
        return registroCustoPromob;
    }

    public LocalDate getDataCustoPromob() {
        return dataCustoPromob;
    }

    public FonteCusto getFonteCustoPromob() {
        return fonteCustoPromob;
    }

    public BigDecimal getCustoAnterior() {
        return custoAnterior;
    }

    public String getRegistroCustoAnterior() {
        return registroCustoAnterior;
    }

    public LocalDate getDataCustoAnterior() {
        return dataCustoAnterior;
    }

    public FonteCusto getFonteCustoAnterior() {
        return fonteCustoAnterior;
    }
    
    public BigDecimal getPrecoPadraoVenda() {
        if (precoUnitarioLiquidoAtual == null) {
            return BigDecimal.ZERO;
        }

        return precoUnitarioLiquidoAtual.multiply(new BigDecimal("2"));
    }

    @Override
    public String toString() {
        return "DadosItemConsulta{" +
                "codigoItem='" + codigoItem + '\'' +
                ", descricao='" + descricao + '\'' +
                ", ipi=" + ipi +
                ", ncm='" + ncm + '\'' +
                ", unidadeVenda='" + unidadeVenda + '\'' +
                ", pesoBruto=" + pesoBruto +
                ", pesoLiquido=" + pesoLiquido +
                ", precoUnitarioLiquidoAtual=" + precoUnitarioLiquidoAtual +
                ", custoAtual=" + custoAtual +
                ", registroCustoAtual='" + registroCustoAtual + '\'' +
                ", dataCustoAtual=" + dataCustoAtual +
                ", fonteCustoAtual=" + fonteCustoAtual +
                ", custoPromob=" + custoPromob +
                ", registroCustoPromob='" + registroCustoPromob + '\'' +
                ", dataCustoPromob=" + dataCustoPromob +
                ", fonteCustoPromob=" + fonteCustoPromob +
                ", custoAnterior=" + custoAnterior +
                ", registroCustoAnterior='" + registroCustoAnterior + '\'' +
                ", dataCustoAnterior=" + dataCustoAnterior +
                ", fonteCustoAnterior=" + fonteCustoAnterior +
                '}';
    }
}