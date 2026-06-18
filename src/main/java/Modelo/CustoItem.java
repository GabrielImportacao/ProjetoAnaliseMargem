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
        this.dataCusto = dataCusto;
        this.arquivoOrigem = arquivoOrigem;
        this.ultimaModificacaoArquivo = ultimaModificacaoArquivo;
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

    public LocalDate getDataCusto() {
        return dataCusto;
    }

    public String getArquivoOrigem() {
        return arquivoOrigem;
    }

    public LocalDateTime getUltimaModificacaoArquivo() {
        return ultimaModificacaoArquivo;
    }

    @Override
    public String toString() {
        return "CustoItem{" +
                "id=" + id +
                ", codigoItem='" + codigoItem + '\'' +
                ", registroImportacao='" + registroImportacao + '\'' +
                ", anoImportacao=" + anoImportacao +
                ", numeroImportacao=" + numeroImportacao +
                ", custo=" + custo +
                ", dataCusto=" + dataCusto +
                ", ultimaModificacaoArquivo=" + ultimaModificacaoArquivo +
                '}';
    }
}