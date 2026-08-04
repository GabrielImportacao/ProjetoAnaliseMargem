package Configuracao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfiguracaoUsuario {

    private boolean margemEmPorcentagem;
    private boolean custoFuturo;
    private boolean custoEstimado;
    private boolean flagItensEncalhados;
    private boolean custoVerdadeiroAtivo;
    private int zoomInterface;
    private BigDecimal cotacaoDolar;
    private BigDecimal fatorImportacaoReposicao;

    /*
     * O nome interno "flagItensEncalhados" permanece temporariamente
     * para manter compatibilidade com a configuração já persistida.
     * Visualmente, usamos "Flag de Itens Obsoletos".
     */
    private final Map<String, BigDecimal> basesEstados =
            criarBasesPadraoEstados();

    public ConfiguracaoUsuario() {
    	this.margemEmPorcentagem = false;
        this.custoFuturo = false;
        this.custoEstimado = false;
        this.flagItensEncalhados = true;
        this.custoVerdadeiroAtivo = false;
        this.zoomInterface = 100;
        this.cotacaoDolar = new BigDecimal("5.30");
        this.fatorImportacaoReposicao = null;
    }

    public ConfiguracaoUsuario(ConfiguracaoUsuario outra) {
        this();

        if (outra == null) {
            return;
        }

        this.margemEmPorcentagem =
                outra.isMargemEmPorcentagem();

        this.custoFuturo =
                outra.isCustoFuturo();

        this.custoEstimado =
                outra.isCustoEstimado();

        this.flagItensEncalhados =
                outra.isFlagItensEncalhados();

        this.custoVerdadeiroAtivo =
                outra.isCustoVerdadeiroAtivo();

        this.zoomInterface =
                outra.getZoomInterface();

        this.basesEstados.clear();
        this.basesEstados.putAll(
                outra.getBasesEstados()
        );
        this.cotacaoDolar =
                outra.getCotacaoDolar();
        this.fatorImportacaoReposicao =
                outra.getFatorImportacaoReposicao();
    }
    
    public BigDecimal getFatorImportacaoReposicao() {
        return fatorImportacaoReposicao;
    }

    public void setFatorImportacaoReposicao(
            BigDecimal fatorImportacaoReposicao
    ) {
        /*
         * Null é permitido porque o campo
         * não possui valor padrão.
         */
        if (fatorImportacaoReposicao == null) {
            this.fatorImportacaoReposicao = null;
            return;
        }

        /*
         * Fatores negativos não são aceitos.
         * Zero é um valor válido.
         */
        if (fatorImportacaoReposicao.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            return;
        }

        this.fatorImportacaoReposicao =
                fatorImportacaoReposicao.setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }
    
    public BigDecimal getCotacaoDolar() {
        return cotacaoDolar;
    }

    public void setCotacaoDolar(
            BigDecimal cotacaoDolar
    ) {
        if (cotacaoDolar == null
                || cotacaoDolar.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            return;
        }

        this.cotacaoDolar =
                cotacaoDolar.setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    public BigDecimal getBaseEstado(String sigla) {
        if (sigla == null || sigla.isBlank()) {
            return new BigDecimal("50.00");
        }

        return basesEstados.getOrDefault(
                sigla.trim().toUpperCase(),
                new BigDecimal("50.00")
        );
    }

    public void setBaseEstado(
            String sigla,
            BigDecimal valor
    ) {
        if (sigla == null
                || sigla.isBlank()
                || valor == null) {
            return;
        }

        basesEstados.put(
                sigla.trim().toUpperCase(),
                valor.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );
    }

    public Map<String, BigDecimal> getBasesEstados() {
        return new LinkedHashMap<>(basesEstados);
    }

    private Map<String, BigDecimal> criarBasesPadraoEstados() {
        Map<String, BigDecimal> bases =
                new LinkedHashMap<>();

        bases.put("AC", new BigDecimal("50.00"));
        bases.put("AL", new BigDecimal("50.00"));
        bases.put("AP", new BigDecimal("50.00"));
        bases.put("AM", new BigDecimal("50.00"));
        bases.put("BA", new BigDecimal("50.00"));
        bases.put("CE", new BigDecimal("50.00"));
        bases.put("DF", new BigDecimal("50.00"));
        bases.put("ES", new BigDecimal("50.00"));
        bases.put("GO", new BigDecimal("50.00"));
        bases.put("MA", new BigDecimal("50.00"));
        bases.put("MT", new BigDecimal("50.00"));
        bases.put("MS", new BigDecimal("50.00"));
        bases.put("MG", new BigDecimal("50.00"));
        bases.put("PA", new BigDecimal("50.00"));
        bases.put("PB", new BigDecimal("50.00"));
        bases.put("PR", new BigDecimal("50.00"));
        bases.put("PE", new BigDecimal("50.00"));
        bases.put("PI", new BigDecimal("50.00"));
        bases.put("RJ", new BigDecimal("50.00"));
        bases.put("RN", new BigDecimal("50.00"));
        bases.put("RS", new BigDecimal("50.00"));
        bases.put("RO", new BigDecimal("50.00"));
        bases.put("RR", new BigDecimal("50.00"));
        bases.put("SC", new BigDecimal("40.22"));
        bases.put("SP", new BigDecimal("50.00"));
        bases.put("SE", new BigDecimal("50.00"));
        bases.put("TO", new BigDecimal("50.00"));

        return bases;
    }

    public boolean isCustoVerdadeiroAtivo() {
        return custoVerdadeiroAtivo;
    }

    public void setCustoVerdadeiroAtivo(
            boolean custoVerdadeiroAtivo
    ) {
        this.custoVerdadeiroAtivo =
                custoVerdadeiroAtivo;
    }

    public boolean isMargemEmPorcentagem() {
        return margemEmPorcentagem;
    }

    public void setMargemEmPorcentagem(
            boolean margemEmPorcentagem
    ) {
        this.margemEmPorcentagem =
                margemEmPorcentagem;
    }

    public boolean isCustoFuturo() {
        return custoFuturo;
    }

    public void setCustoFuturo(boolean custoFuturo) {
        this.custoFuturo = custoFuturo;
    }

    public boolean isCustoEstimado() {
        return custoEstimado;
    }

    public void setCustoEstimado(
            boolean custoEstimado
    ) {
        this.custoEstimado = custoEstimado;
    }

    public boolean isFlagItensEncalhados() {
        return flagItensEncalhados;
    }

    public void setFlagItensEncalhados(
            boolean flagItensEncalhados
    ) {
        this.flagItensEncalhados =
                flagItensEncalhados;
    }

    public int getZoomInterface() {
        return zoomInterface;
    }

    public void setZoomInterface(int zoomInterface) {
        this.zoomInterface = zoomInterface;
    }
}