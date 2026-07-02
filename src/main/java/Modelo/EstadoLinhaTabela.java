package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EstadoLinhaTabela {

    private final int ordem;
    private final String codigo;
    private final String descricao;
    private final int quantidade;

    private final BigDecimal valorUnitario;
    private final BigDecimal valorTotal;

    private final String condicaoVenda;
    private final String corEspecialFundo;
    private final String corEspecialTexto;

    private final BigDecimal variacaoAtual;
    private final BigDecimal margemAtual;
    private final BigDecimal custoAtual;
    private final String registroCustoAtual;
    private final LocalDate dataCustoAtual;

    private final BigDecimal margemPromob;
    private final BigDecimal custoPromob;
    private final String registroCustoPromob;
    private final LocalDate dataCustoPromob;

    private final BigDecimal variacaoAnterior;
    private final BigDecimal margemAnterior;
    private final BigDecimal custoAnterior;
    private final String registroCustoAnterior;
    private final LocalDate dataCustoAnterior;

    private final BigDecimal precoPadraoVenda;
    private final BigDecimal percentualIpi;
    
    private final LocalDate dataUltimaSaida;
    private final boolean itemEncalhado;
    
    private final boolean itemEncalhadoConfirmado;

    public EstadoLinhaTabela(
            int ordem,
            String codigo,
            String descricao,
            int quantidade,
            BigDecimal valorUnitario,
            BigDecimal valorTotal,
            String condicaoVenda,
            String corEspecialFundo,
            String corEspecialTexto,
            BigDecimal variacaoAtual,
            BigDecimal margemAtual,
            BigDecimal custoAtual,
            String registroCustoAtual,
            LocalDate dataCustoAtual,
            BigDecimal margemPromob,
            BigDecimal custoPromob,
            String registroCustoPromob,
            LocalDate dataCustoPromob,
            BigDecimal variacaoAnterior,
            BigDecimal margemAnterior,
            BigDecimal custoAnterior,
            String registroCustoAnterior,
            LocalDate dataCustoAnterior,
            BigDecimal precoPadraoVenda,
            BigDecimal percentualIpi,
            LocalDate dataUltimaSaida,
            boolean itemEncalhado,
            boolean itemEncalhadoConfirmado
    ) {
        this.ordem = ordem;
        this.codigo = codigo;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
        this.condicaoVenda = condicaoVenda;
        this.corEspecialFundo = corEspecialFundo;
        this.corEspecialTexto = corEspecialTexto;
        this.variacaoAtual = variacaoAtual;
        this.margemAtual = margemAtual;
        this.custoAtual = custoAtual;
        this.registroCustoAtual = registroCustoAtual;
        this.dataCustoAtual = dataCustoAtual;
        this.margemPromob = margemPromob;
        this.custoPromob = custoPromob;
        this.registroCustoPromob = registroCustoPromob;
        this.dataCustoPromob = dataCustoPromob;
        this.variacaoAnterior = variacaoAnterior;
        this.margemAnterior = margemAnterior;
        this.custoAnterior = custoAnterior;
        this.registroCustoAnterior = registroCustoAnterior;
        this.dataCustoAnterior = dataCustoAnterior;
        this.precoPadraoVenda = precoPadraoVenda;
        this.percentualIpi = percentualIpi;
        this.dataUltimaSaida = dataUltimaSaida;
        this.itemEncalhado = itemEncalhado;
        this.itemEncalhadoConfirmado = itemEncalhadoConfirmado;
    }
    
    public boolean isItemEncalhadoConfirmado() {
        return itemEncalhadoConfirmado;
    }

    public LocalDate getDataUltimaSaida() {
        return dataUltimaSaida;
    }

    public boolean isItemEncalhado() {
        return itemEncalhado;
    }
    
    public int getOrdem() {
        return ordem;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getCondicaoVenda() {
        return condicaoVenda;
    }

    public String getCorEspecialFundo() {
        return corEspecialFundo;
    }

    public String getCorEspecialTexto() {
        return corEspecialTexto;
    }

    public BigDecimal getVariacaoAtual() {
        return variacaoAtual;
    }

    public BigDecimal getMargemAtual() {
        return margemAtual;
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

    public BigDecimal getMargemPromob() {
        return margemPromob;
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

    public BigDecimal getVariacaoAnterior() {
        return variacaoAnterior;
    }

    public BigDecimal getMargemAnterior() {
        return margemAnterior;
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

    public BigDecimal getPrecoPadraoVenda() {
        return precoPadraoVenda;
    }

    public BigDecimal getPercentualIpi() {
        return percentualIpi;
    }
}