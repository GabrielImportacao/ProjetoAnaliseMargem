package Modelo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ItemAnalise {
    private final StringProperty codigo = new SimpleStringProperty("");
    private final StringProperty descricao = new SimpleStringProperty("");
    private final IntegerProperty quantidade = new SimpleIntegerProperty(0);

    private final ObjectProperty<BigDecimal> valorUnitario = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> valorTotal = new SimpleObjectProperty<>(BigDecimal.ZERO);

    private final ObjectProperty<BigDecimal> variacaoAtual = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> margemAtual = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<LocalDate> dataCustoAtual = new SimpleObjectProperty<>();
    private final StringProperty registroCustoAtual = new SimpleStringProperty("");

    private final ObjectProperty<BigDecimal> margemPromob = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty registroCustoPromob = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> dataCustoPromob = new SimpleObjectProperty<>();
    
    private final ObjectProperty<BigDecimal> variacaoAnterior = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> margemAnterior = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty registroCustoAnterior = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> dataCustoAnterior = new SimpleObjectProperty<>();

    private final ObjectProperty<BigDecimal> custoAtual = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> custoPromob = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> custoAnterior = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> precoPadraoVenda = new SimpleObjectProperty<>(BigDecimal.ZERO);
    
    public BigDecimal getPrecoPadraoVenda() {
        return precoPadraoVenda.get();
    }

    public void setPrecoPadraoVenda(BigDecimal precoPadraoVenda) {
        this.precoPadraoVenda.set(precoPadraoVenda == null ? BigDecimal.ZERO : precoPadraoVenda);
    }

    public ObjectProperty<BigDecimal> precoPadraoVendaProperty() {
        return precoPadraoVenda;
    }

    public String getCodigo() {
        return codigo.get();
    }

    public void setCodigo(String codigo) {
        this.codigo.set(codigo == null ? "" : codigo.trim().toUpperCase());
    }

    public StringProperty codigoProperty() {
        return codigo;
    }

    public String getDescricao() {
        return descricao.get();
    }

    public void setDescricao(String descricao) {
        this.descricao.set(descricao == null ? "" : descricao);
    }

    public StringProperty descricaoProperty() {
        return descricao;
    }

    public int getQuantidade() {
        return quantidade.get();
    }

    public void setQuantidade(int quantidade) {
        this.quantidade.set(Math.max(0, quantidade));
    }

    public IntegerProperty quantidadeProperty() {
        return quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario.get();
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario.set(valorUnitario == null ? BigDecimal.ZERO : valorUnitario);
    }

    public ObjectProperty<BigDecimal> valorUnitarioProperty() {
        return valorUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorTotal.get();
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal.set(valorTotal == null ? BigDecimal.ZERO : valorTotal);
    }

    public ObjectProperty<BigDecimal> valorTotalProperty() {
        return valorTotal;
    }

    public BigDecimal getVariacaoAtual() {
        return variacaoAtual.get();
    }

    public void setVariacaoAtual(BigDecimal variacaoAtual) {
        this.variacaoAtual.set(variacaoAtual == null ? BigDecimal.ZERO : variacaoAtual);
    }

    public ObjectProperty<BigDecimal> variacaoAtualProperty() {
        return variacaoAtual;
    }

    public BigDecimal getMargemAtual() {
        return margemAtual.get();
    }

    public void setMargemAtual(BigDecimal margemAtual) {
        this.margemAtual.set(margemAtual == null ? BigDecimal.ZERO : margemAtual);
    }

    public ObjectProperty<BigDecimal> margemAtualProperty() {
        return margemAtual;
    }

    public LocalDate getDataCustoAtual() {
        return dataCustoAtual.get();
    }

    public void setDataCustoAtual(LocalDate dataCustoAtual) {
        this.dataCustoAtual.set(dataCustoAtual);
    }

    public ObjectProperty<LocalDate> dataCustoAtualProperty() {
        return dataCustoAtual;
    }
    
    public LocalDate getDataCustoPromob() {
        return dataCustoPromob.get();
    }

    public void setDataCustoPromob(LocalDate dataCustoPromob) {
        this.dataCustoPromob.set(dataCustoPromob);
    }

    public ObjectProperty<LocalDate> dataCustoPromobProperty() {
        return dataCustoPromob;
    }

    public LocalDate getDataCustoAnterior() {
        return dataCustoAnterior.get();
    }

    public void setDataCustoAnterior(LocalDate dataCustoAnterior) {
        this.dataCustoAnterior.set(dataCustoAnterior);
    }

    public ObjectProperty<LocalDate> dataCustoAnteriorProperty() {
        return dataCustoAnterior;
    }

    public String getRegistroCustoAtual() {
        return registroCustoAtual.get();
    }

    public void setRegistroCustoAtual(String registroCustoAtual) {
        this.registroCustoAtual.set(registroCustoAtual == null ? "" : registroCustoAtual);
    }

    public StringProperty registroCustoAtualProperty() {
        return registroCustoAtual;
    }

    public BigDecimal getMargemPromob() {
        return margemPromob.get();
    }

    public void setMargemPromob(BigDecimal margemPromob) {
        this.margemPromob.set(margemPromob == null ? BigDecimal.ZERO : margemPromob);
    }

    public ObjectProperty<BigDecimal> margemPromobProperty() {
        return margemPromob;
    }

    public String getRegistroCustoPromob() {
        return registroCustoPromob.get();
    }

    public void setRegistroCustoPromob(String registroCustoPromob) {
        this.registroCustoPromob.set(registroCustoPromob == null ? "" : registroCustoPromob);
    }

    public StringProperty registroCustoPromobProperty() {
        return registroCustoPromob;
    }

    public BigDecimal getVariacaoAnterior() {
        return variacaoAnterior.get();
    }

    public void setVariacaoAnterior(BigDecimal variacaoAnterior) {
        this.variacaoAnterior.set(variacaoAnterior == null ? BigDecimal.ZERO : variacaoAnterior);
    }

    public ObjectProperty<BigDecimal> variacaoAnteriorProperty() {
        return variacaoAnterior;
    }

    public BigDecimal getMargemAnterior() {
        return margemAnterior.get();
    }

    public void setMargemAnterior(BigDecimal margemAnterior) {
        this.margemAnterior.set(margemAnterior == null ? BigDecimal.ZERO : margemAnterior);
    }

    public ObjectProperty<BigDecimal> margemAnteriorProperty() {
        return margemAnterior;
    }

    public String getRegistroCustoAnterior() {
        return registroCustoAnterior.get();
    }

    public void setRegistroCustoAnterior(String registroCustoAnterior) {
        this.registroCustoAnterior.set(registroCustoAnterior == null ? "" : registroCustoAnterior);
    }

    public StringProperty registroCustoAnteriorProperty() {
        return registroCustoAnterior;
    }

    public BigDecimal getCustoAtual() {
        return custoAtual.get();
    }

    public void setCustoAtual(BigDecimal custoAtual) {
        this.custoAtual.set(custoAtual == null ? BigDecimal.ZERO : custoAtual);
    }

    public BigDecimal getCustoPromob() {
        return custoPromob.get();
    }

    public void setCustoPromob(BigDecimal custoPromob) {
        this.custoPromob.set(custoPromob == null ? BigDecimal.ZERO : custoPromob);
    }

    public BigDecimal getCustoAnterior() {
        return custoAnterior.get();
    }

    public void setCustoAnterior(BigDecimal custoAnterior) {
        this.custoAnterior.set(custoAnterior == null ? BigDecimal.ZERO : custoAnterior);
    }

    public void aplicarDadosItem(DadosItem dados) {
        if (dados == null) {
            setDescricao("ITEM NÃO ENCONTRADO");
            setCustoAtual(BigDecimal.ZERO);
            setCustoPromob(BigDecimal.ZERO);
            setCustoAnterior(BigDecimal.ZERO);
            setRegistroCustoAtual("");
            setRegistroCustoPromob("");
            setRegistroCustoAnterior("");
            setPrecoPadraoVenda(dados.getPrecoPadraoVenda());
            setDataCustoAtual(dados.getDataCustoAtual());
            setDataCustoPromob(dados.getDataCustoPromob());
            setDataCustoAnterior(dados.getDataCustoAnterior());
            return;
        }

        setDescricao(dados.getDescricao());
        setCustoAtual(dados.getCustoAtual());
        setCustoPromob(dados.getCustoPromob());
        setCustoAnterior(dados.getCustoAnterior());
        setRegistroCustoAtual(dados.getRegistroCustoAtual());
        setRegistroCustoPromob(dados.getRegistroCustoPromob());
        setRegistroCustoAnterior(dados.getRegistroCustoAnterior());
        setDataCustoAtual(dados.getDataCustoAtual());
        setDataCustoPromob(dados.getDataCustoPromob());
        setDataCustoAnterior(dados.getDataCustoAnterior());
    }
}
