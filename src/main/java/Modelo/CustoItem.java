package Modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustoItem {

    private final int id;
    private final String codigoItem;
    private final String registroImportacao;
    private final int anoImportacao;
    private final int numeroImportacao;
    private final BigDecimal custo;

    private final BigDecimal valorUnitPiUsd;
    private final BigDecimal valorUnitCiUsd;
    private final BigDecimal fatorLiquidoBrl;

    private final LocalDate dataCusto;
    private final String arquivoOrigem;
    private final LocalDateTime ultimaModificacaoArquivo;

    public CustoItem(
            int id,
            String codigoItem,
            String registroImportacao,
            int anoImportacao,
            int numeroImportacao,
            BigDecimal custo,
            BigDecimal valorUnitPiUsd,
            BigDecimal valorUnitCiUsd,
            BigDecimal fatorLiquidoBrl,
            LocalDate dataCusto,
            String arquivoOrigem,
            LocalDateTime ultimaModificacaoArquivo
    ) {
        this.id = id;
        this.codigoItem = codigoItem;
        this.registroImportacao = registroImportacao;
        this.anoImportacao = anoImportacao;
        this.numeroImportacao = numeroImportacao;
        this.custo = custo;
        this.valorUnitPiUsd = valorUnitPiUsd;
        this.valorUnitCiUsd = valorUnitCiUsd;
        this.fatorLiquidoBrl = fatorLiquidoBrl;
        this.dataCusto = dataCusto;
        this.arquivoOrigem = arquivoOrigem;
        this.ultimaModificacaoArquivo =
                ultimaModificacaoArquivo;
    }
    
    public BigDecimal getFatorLiquidoBrl() {
        return fatorLiquidoBrl;
    }

    public int getId() {
        return id;
    }

    public String getCodigoItem() {
        return codigoItem;
    }

    public String getRegistroImportacao() {
        return registroImportacao;
    }

    public int getAnoImportacao() {
        return anoImportacao;
    }

    public int getNumeroImportacao() {
        return numeroImportacao;
    }

    public BigDecimal getCusto() {
        return custo;
    }

    public BigDecimal getValorUnitPiUsd() {
        return valorUnitPiUsd;
    }

    public BigDecimal getValorUnitCiUsd() {
        return valorUnitCiUsd;
    }

    /*
     * Prioridade:
     *
     * 1. Valor da CI;
     * 2. Valor da PI;
     * 3. Sem valor disponível.
     */
    public BigDecimal getValorReposicaoUsd() {
        /*
         * A CI sempre possui prioridade.
         *
         * A PI é utilizada somente quando a CI
         * estiver nula, vazia ou zerada.
         */
        if (valorUnitCiUsd != null
                && valorUnitCiUsd.compareTo(
                        BigDecimal.ZERO
                ) > 0) {

            return valorUnitCiUsd;
        }

        if (valorUnitPiUsd != null
                && valorUnitPiUsd.compareTo(
                        BigDecimal.ZERO
                ) > 0) {

            return valorUnitPiUsd;
        }

        return null;
    }

    public LocalDate getDataCusto() {
        return dataCusto;
    }

    public String getArquivoOrigem() {
        return arquivoOrigem;
    }

    public LocalDateTime
    getUltimaModificacaoArquivo() {
        return ultimaModificacaoArquivo;
    }

    @Override
    public String toString() {
        return "CustoItem{" +
                "id=" + id +
                ", codigoItem='" + codigoItem + '\'' +
                ", registroImportacao='" +
                registroImportacao + '\'' +
                ", anoImportacao=" + anoImportacao +
                ", numeroImportacao=" +
                numeroImportacao +
                ", custo=" + custo +
                ", valorUnitPiUsd=" +
                valorUnitPiUsd +
                ", valorUnitCiUsd=" +
                valorUnitCiUsd +", fatorLiquidoBrl=" +
                		fatorLiquidoBrl +
                ", dataCusto=" + dataCusto +
                ", ultimaModificacaoArquivo=" +
                ultimaModificacaoArquivo +
                '}';
    }
}