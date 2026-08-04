package Modelo;

import java.math.BigDecimal;

public class ItemCadastro {

    private final String codigoItem;
    private final String descricao;
    private final BigDecimal ipi;
    private final String ncm;
    private final String unidadeVenda;
    private final BigDecimal pesoBruto;
    private final BigDecimal pesoLiquido;
    private final BigDecimal custoUnitarioPlanilha;
    private final String registroCustoPlanilha;
    private final BigDecimal precoUnitarioLiquidoAtual;
    private final BigDecimal fatorImportacaoFallback;

    public ItemCadastro(
            String codigoItem,
            String descricao,
            BigDecimal ipi,
            String ncm,
            String unidadeVenda,
            BigDecimal pesoBruto,
            BigDecimal pesoLiquido,
            BigDecimal custoUnitarioPlanilha,
            String registroCustoPlanilha,
            BigDecimal precoUnitarioLiquidoAtual,
            BigDecimal fatorImportacaoFallback
    ) {
        this.codigoItem = codigoItem;
        this.descricao = descricao;
        this.ipi = ipi;
        this.ncm = ncm;
        this.unidadeVenda = unidadeVenda;
        this.pesoBruto = pesoBruto;
        this.pesoLiquido = pesoLiquido;
        this.custoUnitarioPlanilha = custoUnitarioPlanilha;
        this.registroCustoPlanilha = registroCustoPlanilha;
        this.precoUnitarioLiquidoAtual = precoUnitarioLiquidoAtual;
        this.fatorImportacaoFallback = fatorImportacaoFallback;
    }
    
    public BigDecimal getFatorImportacaoFallback() {
        return fatorImportacaoFallback;
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

    public BigDecimal getCustoUnitarioPlanilha() {
        return custoUnitarioPlanilha;
    }

    public String getRegistroCustoPlanilha() {
        return registroCustoPlanilha;
    }

    public BigDecimal getPrecoUnitarioLiquidoAtual() {
        return precoUnitarioLiquidoAtual;
    }

    @Override
    public String toString() {
        return "ItemCadastro{" +
                "codigoItem='" + codigoItem + '\'' +
                ", descricao='" + descricao + '\'' +
                ", ipi=" + ipi +
                ", ncm='" + ncm + '\'' +
                ", unidadeVenda='" + unidadeVenda + '\'' +
                ", pesoBruto=" + pesoBruto +
                ", pesoLiquido=" + pesoLiquido +
                ", custoUnitarioPlanilha=" + custoUnitarioPlanilha +
                ", registroCustoPlanilha='" + registroCustoPlanilha + '\'' +
                ", precoUnitarioLiquidoAtual=" + precoUnitarioLiquidoAtual +
                '}';
    }
}