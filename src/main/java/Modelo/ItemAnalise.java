package Modelo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ItemAnalise {
    private final StringProperty codigo = new SimpleStringProperty("");
    private final StringProperty descricao = new SimpleStringProperty("");
    private final IntegerProperty quantidade = new SimpleIntegerProperty(0);
    
    private final ObjectProperty<CondicaoVenda> condicaoVenda = new SimpleObjectProperty<>(CondicaoVenda.NORMAL);

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
    
    private final ObjectProperty<BigDecimal> percentualIpi = new SimpleObjectProperty<>(BigDecimal.ZERO);

    private final ObjectProperty<BigDecimal> ipiProposta = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> ipiAtual = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> ipiAnterior = new SimpleObjectProperty<>(BigDecimal.ZERO);
    
    private final StringProperty corEspecialFundo = new SimpleStringProperty("#92D050");
    private final StringProperty corEspecialTexto = new SimpleStringProperty("#000000");
    
    private final ObjectProperty<LocalDate> dataUltimaSaida = new SimpleObjectProperty<>();
    private final BooleanProperty itemEncalhado = new SimpleBooleanProperty(false);
    
    private final BooleanProperty itemEncalhadoConfirmado = new SimpleBooleanProperty(false);
    
    private final ObjectProperty<BigDecimal> margemVerdadeira = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty registroCustoVerdadeiro = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> dataCustoVerdadeiro = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> custoVerdadeiro = new SimpleObjectProperty<>(BigDecimal.ZERO);
    
    public BigDecimal getMargemVerdadeira() {
        return margemVerdadeira.get();
    }

    public void setMargemVerdadeira(BigDecimal margemVerdadeira) {
        this.margemVerdadeira.set(margemVerdadeira == null ? BigDecimal.ZERO : margemVerdadeira);
    }

    public ObjectProperty<BigDecimal> margemVerdadeiraProperty() {
        return margemVerdadeira;
    }

    public String getRegistroCustoVerdadeiro() {
        return registroCustoVerdadeiro.get();
    }

    public void setRegistroCustoVerdadeiro(String registroCustoVerdadeiro) {
        this.registroCustoVerdadeiro.set(registroCustoVerdadeiro == null ? "" : registroCustoVerdadeiro);
    }

    public StringProperty registroCustoVerdadeiroProperty() {
        return registroCustoVerdadeiro;
    }

    public LocalDate getDataCustoVerdadeiro() {
        return dataCustoVerdadeiro.get();
    }

    public void setDataCustoVerdadeiro(LocalDate dataCustoVerdadeiro) {
        this.dataCustoVerdadeiro.set(dataCustoVerdadeiro);
    }

    public ObjectProperty<LocalDate> dataCustoVerdadeiroProperty() {
        return dataCustoVerdadeiro;
    }

    public BigDecimal getCustoVerdadeiro() {
        return custoVerdadeiro.get();
    }

    public void setCustoVerdadeiro(BigDecimal custoVerdadeiro) {
        this.custoVerdadeiro.set(custoVerdadeiro == null ? BigDecimal.ZERO : custoVerdadeiro);
    }

    public ObjectProperty<BigDecimal> custoVerdadeiroProperty() {
        return custoVerdadeiro;
    }    
    
    public boolean isItemEncalhadoConfirmado() {
        return itemEncalhadoConfirmado.get();
    }

    public void setItemEncalhadoConfirmado(boolean itemEncalhadoConfirmado) {
        this.itemEncalhadoConfirmado.set(itemEncalhadoConfirmado);
    }

    public BooleanProperty itemEncalhadoConfirmadoProperty() {
        return itemEncalhadoConfirmado;
    }
    
    public LocalDate getDataUltimaSaida() {
        return dataUltimaSaida.get();
    }

    public void setDataUltimaSaida(LocalDate dataUltimaSaida) {
        this.dataUltimaSaida.set(dataUltimaSaida);
    }

    public ObjectProperty<LocalDate> dataUltimaSaidaProperty() {
        return dataUltimaSaida;
    }

    public boolean isItemEncalhado() {
        return itemEncalhado.get();
    }

    public void setItemEncalhado(boolean itemEncalhado) {
        this.itemEncalhado.set(itemEncalhado);
    }

    public BooleanProperty itemEncalhadoProperty() {
        return itemEncalhado;
    }
    
    public String getCorEspecialFundo() {
        return corEspecialFundo.get();
    }

    public void setCorEspecialFundo(String corEspecialFundo) {
        this.corEspecialFundo.set(
                corEspecialFundo == null || corEspecialFundo.isBlank()
                        ? "#92D050"
                        : corEspecialFundo
        );
    }

    public StringProperty corEspecialFundoProperty() {
        return corEspecialFundo;
    }

    public String getCorEspecialTexto() {
        return corEspecialTexto.get();
    }

    public void setCorEspecialTexto(String corEspecialTexto) {
        this.corEspecialTexto.set(
                corEspecialTexto == null || corEspecialTexto.isBlank()
                        ? "#000000"
                        : corEspecialTexto
        );
    }

    public StringProperty corEspecialTextoProperty() {
        return corEspecialTexto;
    }
    
    public CondicaoVenda getCondicaoVenda() {
        return condicaoVenda.get();
    }

    public void setCondicaoVenda(CondicaoVenda condicaoVenda) {
        this.condicaoVenda.set(condicaoVenda == null ? CondicaoVenda.NORMAL : condicaoVenda);
    }

    public ObjectProperty<CondicaoVenda> condicaoVendaProperty() {
        return condicaoVenda;
    }
    
    public BigDecimal getPercentualIpi() {
        return percentualIpi.get();
    }

    public void setPercentualIpi(BigDecimal percentualIpi) {
        this.percentualIpi.set(percentualIpi == null ? BigDecimal.ZERO : percentualIpi);
    }

    public ObjectProperty<BigDecimal> percentualIpiProperty() {
        return percentualIpi;
    }

    public BigDecimal getIpiProposta() {
        return ipiProposta.get();
    }

    public void setIpiProposta(BigDecimal ipiProposta) {
        this.ipiProposta.set(ipiProposta == null ? BigDecimal.ZERO : ipiProposta);
    }

    public ObjectProperty<BigDecimal> ipiPropostaProperty() {
        return ipiProposta;
    }

    public BigDecimal getIpiAtual() {
        return ipiAtual.get();
    }

    public void setIpiAtual(BigDecimal ipiAtual) {
        this.ipiAtual.set(ipiAtual == null ? BigDecimal.ZERO : ipiAtual);
    }

    public ObjectProperty<BigDecimal> ipiAtualProperty() {
        return ipiAtual;
    }

    public BigDecimal getIpiAnterior() {
        return ipiAnterior.get();
    }

    public void setIpiAnterior(BigDecimal ipiAnterior) {
        this.ipiAnterior.set(ipiAnterior == null ? BigDecimal.ZERO : ipiAnterior);
    }

    public ObjectProperty<BigDecimal> ipiAnteriorProperty() {
        return ipiAnterior;
    }
    
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
        String novoCodigo = codigo == null ? "" : codigo.trim().toUpperCase();

        if (!novoCodigo.equals(this.codigo.get())) {
            setItemEncalhadoConfirmado(false);
        }

        this.codigo.set(novoCodigo);
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
            setCustoVerdadeiro(BigDecimal.ZERO);
            setCustoPromob(BigDecimal.ZERO);
            setCustoAnterior(BigDecimal.ZERO);

            setRegistroCustoAtual("");
            setRegistroCustoVerdadeiro("");
            setRegistroCustoPromob("");
            setRegistroCustoAnterior("");

            setDataCustoAtual(null);
            setDataCustoVerdadeiro(null);
            setDataCustoPromob(null);
            setDataCustoAnterior(null);

            setPrecoPadraoVenda(BigDecimal.ZERO);

            setPercentualIpi(BigDecimal.ZERO);
            setIpiProposta(BigDecimal.ZERO);
            setIpiAtual(BigDecimal.ZERO);
            setIpiAnterior(BigDecimal.ZERO);
            
            setDataUltimaSaida(null);
            setItemEncalhado(false);
            
            setDataUltimaSaida(null);
            setItemEncalhado(false);
            setItemEncalhadoConfirmado(false);
            
            setMargemVerdadeira(BigDecimal.ZERO);

            return;
        }

        setDescricao(dados.getDescricao());

        setCustoAtual(dados.getCustoAtual());
        setCustoVerdadeiro(dados.getCustoVerdadeiro());
        setCustoPromob(dados.getCustoPromob());
        setCustoAnterior(dados.getCustoAnterior());

        setRegistroCustoAtual(dados.getRegistroCustoAtual());
        setRegistroCustoVerdadeiro(dados.getRegistroCustoVerdadeiro());
        setRegistroCustoPromob(dados.getRegistroCustoPromob());
        setRegistroCustoAnterior(dados.getRegistroCustoAnterior());

        setDataCustoAtual(dados.getDataCustoAtual());
        setDataCustoVerdadeiro(dados.getDataCustoVerdadeiro());
        setDataCustoPromob(dados.getDataCustoPromob());
        setDataCustoAnterior(dados.getDataCustoAnterior());

        setPrecoPadraoVenda(dados.getPrecoPadraoVenda());

        setPercentualIpi(dados.getIpi());

        setIpiProposta(BigDecimal.ZERO);
        setIpiAtual(BigDecimal.ZERO);
        setIpiAnterior(BigDecimal.ZERO);
        
        setDataUltimaSaida(dados.getDataUltimaSaida());
        setItemEncalhado(dados.isItemEncalhado());

        if (!dados.isItemEncalhado()) {
            setItemEncalhadoConfirmado(false);
        }
    }
}
