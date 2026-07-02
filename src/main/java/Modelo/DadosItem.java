package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DadosItem {
    private final String codigo;
    private final String descricao;

    private final BigDecimal custoAtual;
    private final BigDecimal custoPromob;
    private final BigDecimal custoAnterior;

    private final String registroCustoAtual;
    private final String registroCustoPromob;
    private final String registroCustoAnterior;

    private final LocalDate dataCustoAtual;
    private final LocalDate dataCustoPromob;
    private final LocalDate dataCustoAnterior;
    
    private final BigDecimal ipi;
    private final BigDecimal precoPadraoVenda;
    private final LocalDate dataUltimaSaida;
    private final boolean itemEncalhado;

    public DadosItem(String codigo,
                     String descricao,
                     BigDecimal custoAtual,
                     BigDecimal custoPromob,
                     BigDecimal custoAnterior,
                     String registroCustoAtual,
                     String registroCustoPromob,
                     String registroCustoAnterior,
                     LocalDate dataCustoAtual,
                     LocalDate dataCustoPromob,
                     LocalDate dataCustoAnterior,
    				 BigDecimal precoPadraoVenda,
    				 BigDecimal ipi,
    				 LocalDate dataUltimaSaida,
    				 boolean itemEncalhado){
        this.codigo = codigo;
        this.descricao = descricao;
        this.custoAtual = custoAtual;
        this.custoPromob = custoPromob;
        this.custoAnterior = custoAnterior;
        this.registroCustoAtual = registroCustoAtual;
        this.registroCustoPromob = registroCustoPromob;
        this.registroCustoAnterior = registroCustoAnterior;
        this.dataCustoAtual = dataCustoAtual;
        this.dataCustoPromob = dataCustoPromob;
        this.dataCustoAnterior = dataCustoAnterior;
        this.precoPadraoVenda = precoPadraoVenda;
        this.ipi = ipi;
        this.dataUltimaSaida = dataUltimaSaida;
        this.itemEncalhado = itemEncalhado;
    }

    public LocalDate getDataUltimaSaida() {
        return dataUltimaSaida;
    }

    public boolean isItemEncalhado() {
        return itemEncalhado;
    }
    
    public BigDecimal getIpi() {
        return ipi;
    }
    
    public BigDecimal getPrecoPadraoVenda() {
        return precoPadraoVenda;
    }
    
    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getCustoAtual() {
        return custoAtual;
    }

    public BigDecimal getCustoPromob() {
        return custoPromob;
    }

    public BigDecimal getCustoAnterior() {
        return custoAnterior;
    }

    public String getRegistroCustoAtual() {
        return registroCustoAtual;
    }

    public String getRegistroCustoPromob() {
        return registroCustoPromob;
    }

    public String getRegistroCustoAnterior() {
        return registroCustoAnterior;
    }

    public LocalDate getDataCustoAtual() {
        return dataCustoAtual;
    }

    public LocalDate getDataCustoPromob() {
        return dataCustoPromob;
    }

    public LocalDate getDataCustoAnterior() {
        return dataCustoAnterior;
    }
}