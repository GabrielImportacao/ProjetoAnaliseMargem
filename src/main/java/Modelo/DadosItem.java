package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DadosItem {
    private final String codigo;
    private final String descricao;
    
    private final BigDecimal
    custoReposicao;

private final String
    processoReposicao;

    private final BigDecimal custoAtual;
    private final BigDecimal custoVerdadeiro;
    private final BigDecimal custoPromob;
    private final BigDecimal custoAnterior;

    private final String registroCustoAtual;
    private final String registroCustoVerdadeiro;
    private final String registroCustoPromob;
    private final String registroCustoAnterior;

    private final LocalDate dataCustoAtual;
    private final LocalDate dataCustoVerdadeiro;
    private final LocalDate dataCustoPromob;
    private final LocalDate dataCustoAnterior;
    
    private final BigDecimal ipi;
    private final BigDecimal precoPadraoVenda;
    private final LocalDate dataUltimaSaida;
    private final boolean itemEncalhado;

    public DadosItem(
            String codigo,
            String descricao,
            BigDecimal custoReposicao,
            String processoReposicao,
            BigDecimal custoAtual,
            BigDecimal custoVerdadeiro,
            BigDecimal custoPromob,
            BigDecimal custoAnterior,
                     String registroCustoAtual,
                     String registroCustoVerdadeiro,
                     String registroCustoPromob,
                     String registroCustoAnterior,
                     LocalDate dataCustoAtual,
                     LocalDate dataCustoVerdadeiro,
                     LocalDate dataCustoPromob,
                     LocalDate dataCustoAnterior,
    				 BigDecimal precoPadraoVenda,
    				 BigDecimal ipi,
    				 LocalDate dataUltimaSaida,
    				 boolean itemEncalhado){
        this.codigo = codigo;
        this.descricao = descricao;
        this.custoReposicao =
                custoReposicao;

        this.processoReposicao =
                processoReposicao;
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
        this.custoVerdadeiro = custoVerdadeiro;
        this.registroCustoVerdadeiro = registroCustoVerdadeiro;
        this.dataCustoVerdadeiro = dataCustoVerdadeiro;
    }
    
    public BigDecimal getCustoReposicao() {
        return custoReposicao;
    }

    public String getProcessoReposicao() {
        return processoReposicao;
    }
    
    public BigDecimal getCustoVerdadeiro() {
        return custoVerdadeiro;
    }

    public String getRegistroCustoVerdadeiro() {
        return registroCustoVerdadeiro;
    }

    public LocalDate getDataCustoVerdadeiro() {
        return dataCustoVerdadeiro;
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