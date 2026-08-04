package Repositorio;

import Infraestrutura.ConexaoSqlite;
import Modelo.CustoItem;
import Modelo.HistoricoCustoItem;
import java.util.Comparator;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CustoRepositorySqlite implements CustoRepository {

    private final ConexaoSqlite conexaoSqlite;
    private Map<String, List<CustoItem>> cacheCustosPorItem;

    public CustoRepositorySqlite() {
        this.conexaoSqlite = new ConexaoSqlite();
    }

    public void limparCache() {
        cacheCustosPorItem = null;
    }

    public void preCarregarCache() {
        carregarCacheSeNecessario();
    }

    private void carregarCacheSeNecessario() {
        if (cacheCustosPorItem != null) {
            return;
        }

        cacheCustosPorItem = new HashMap<>();

        String sql = """
                SELECT
                    c.id,
                    c.id_item,
                    c.id_importacao,
                    c.ano_importacao,
                    c.numero_importacao,
        			c.custo,
        			c.valor_unit_pi_usd,
        			c.valor_unit_ci_usd,
        			c.fator_liquido_brl,	
        			c.data_custo,
                    c.arquivo,
                    a.ultima_modificacao AS ultima_modificacao_arquivo
                FROM custos c
                LEFT JOIN arquivos_lidos a
                    ON a.arquivo = c.arquivo
                WHERE c.id_item IS NOT NULL
                ORDER BY
                    UPPER(TRIM(c.id_item)) ASC,
                    c.ano_importacao DESC,
                    a.ultima_modificacao DESC,
                    c.numero_importacao DESC,
                    c.id DESC
                """;

        try (
                Connection conexao = conexaoSqlite.abrir();
                PreparedStatement statement = conexao.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                CustoItem custo = converterResultSetParaCustoItem(resultSet);

                String chave = normalizarCodigo(custo.getCodigoItem());

                cacheCustosPorItem
                        .computeIfAbsent(chave, k -> new ArrayList<>())
                        .add(custo);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao pré-carregar cache de custos gerenciais.", e);
        }
    }
    
    @Override
public HistoricoCustoItem buscarHistoricoPrincipalPorItem(
        String codigoItem
) {
    List<CustoItem> custos =
            listarCustosPorItem(codigoItem);

    CustoItem custoAtual = null;
    CustoItem custoAnterior = null;

    /*
     * A lista permanece contendo todos os processos,
     * inclusive aqueles cujo custo ainda não foi fechado.
     *
     * Para exibição, selecionamos apenas custos positivos.
     */
    for (CustoItem custo : custos) {
        if (!possuiCustoFechado(custo)) {
            continue;
        }

        if (custoAtual == null) {
            custoAtual = custo;
            continue;
        }

        custoAnterior = custo;
        break;
    }

    return new HistoricoCustoItem(
            custoAtual,
            custoAnterior
    );
}
    
    private String normalizarCodigo(String codigoItem) {
        return codigoItem == null ? "" : codigoItem.trim().toUpperCase();
    }
    
    @Override
    public Optional<CustoItem> buscarCustoMaisRecentePorItem(
            String codigoItem
    ) {
        List<CustoItem> custos =
                listarCustosPorItem(codigoItem);

        for (CustoItem custo : custos) {
            if (possuiCustoFechado(custo)) {
                return Optional.of(custo);
            }
        }

        return Optional.empty();
    }
    
    @Override
    public Optional<CustoItem>
    buscarProcessoReposicaoMaisRecentePorItem(
            String codigoItem
    ) {
        if (codigoItem == null
                || codigoItem.isBlank()) {

            return Optional.empty();
        }

        List<CustoItem> registros =
                listarCustosPorItem(
                        codigoItem
                );

        /*
         * Guarda todos os processos que já possuem
         * pelo menos um custo fechado.
         *
         * Isso evita considerar como aberto um processo
         * duplicado que possua uma linha com custo zero
         * e outra linha com custo fechado.
         */
        Set<String> processosComCustoFechado =
                new HashSet<>();

        for (CustoItem registro : registros) {
            if (registro == null
                    || !possuiCustoFechado(registro)) {

                continue;
            }

            String processo =
                    normalizarCodigo(
                            registro
                                    .getRegistroImportacao()
                    );

            if (!processo.isBlank()) {
                processosComCustoFechado.add(
                        processo
                );
            }
        }

        return registros
                .stream()

                .filter(
                        registro ->
                                registro != null
                )

                /*
                 * Mantém somente processos realmente abertos.
                 */
                .filter(
                        registro -> {
                            String processo =
                                    normalizarCodigo(
                                            registro
                                                    .getRegistroImportacao()
                                    );

                            return !processo.isBlank()
                                    && !processosComCustoFechado
                                            .contains(processo);
                        }
                )

                /*
                 * Mantém somente linhas que possuam
                 * CI ou PI válida.
                 */
                .filter(
                        registro -> {
                            BigDecimal valorReposicao =
                                    registro
                                            .getValorReposicaoUsd();

                            return valorReposicao != null
                                    && valorReposicao
                                            .compareTo(
                                                    BigDecimal.ZERO
                                            ) > 0;
                        }
                )

                /*
                 * Critério principal:
                 * maior valor entre CI e PI.
                 *
                 * Em caso de empate:
                 * 1. maior ano;
                 * 2. maior número de processo;
                 * 3. maior ID.
                 */
                .max(
                        Comparator
                                .comparing(
                                        CustoItem::
                                                getValorReposicaoUsd
                                )
                                .thenComparingInt(
                                        CustoItem::
                                                getAnoImportacao
                                )
                                .thenComparingInt(
                                        CustoItem::
                                                getNumeroImportacao
                                )
                                .thenComparingInt(
                                        CustoItem::getId
                                )
                );
    }
    
    @Override
    public Optional<CustoItem>
    buscarCustoFechadoMaisRecenteComFatorPorItem(
            String codigoItem
    ) {
        if (codigoItem == null
                || codigoItem.isBlank()) {

            return Optional.empty();
        }

        /*
         * A lista já está ordenada pela mesma regra usada
         * para determinar o custo fechado mais recente:
         *
         * 1. maior ano de importação;
         * 2. arquivo com modificação mais recente;
         * 3. maior número de importação;
         * 4. maior ID.
         *
         * Percorremos nessa ordem e retornamos o primeiro
         * custo fechado que possua Fator Líquido BRL válido.
         */
        for (
                CustoItem custo
                : listarCustosPorItem(codigoItem)
        ) {
            if (!possuiCustoFechado(custo)) {
                continue;
            }

            BigDecimal fatorLiquidoBrl =
                    custo.getFatorLiquidoBrl();

            if (fatorLiquidoBrl != null
                    && fatorLiquidoBrl.compareTo(
                            BigDecimal.ZERO
                    ) > 0) {

                return Optional.of(custo);
            }
        }

        return Optional.empty();
    }
    
    private boolean possuiCustoFechado(
            CustoItem custo
    ) {
        return custo != null
                && custo.getCusto() != null
                && custo.getCusto()
                        .compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public List<CustoItem> listarCustosPorItem(String codigoItem) {
        if (codigoItem == null || codigoItem.isBlank()) {
            return Collections.emptyList();
        }

        carregarCacheSeNecessario();

        String chave = normalizarCodigo(codigoItem);

        return cacheCustosPorItem.getOrDefault(chave, Collections.emptyList());
    }
    
    private BigDecimal
    converterBigDecimalOpcional(
            String texto
    ) {
        if (texto == null
                || texto.isBlank()) {

            return null;
        }

        return new BigDecimal(
                texto.trim()
        );
    }

    private CustoItem
    converterResultSetParaCustoItem(
            ResultSet resultSet
    ) throws SQLException {

        String custoTexto =
                resultSet.getString(
                        "custo"
                );

        String valorUnitPiUsdTexto =
                resultSet.getString(
                        "valor_unit_pi_usd"
                );

        String valorUnitCiUsdTexto =
                resultSet.getString(
                        "valor_unit_ci_usd"
                );

        String fatorLiquidoBrlTexto =
                resultSet.getString(
                        "fator_liquido_brl"
                );

        String dataTexto =
                resultSet.getString(
                        "data_custo"
                );

        BigDecimal custo =
                custoTexto == null
                        || custoTexto.isBlank()
                        ? BigDecimal.ZERO
                        : new BigDecimal(
                                custoTexto
                        );

        BigDecimal valorUnitPiUsd =
                converterBigDecimalOpcional(
                        valorUnitPiUsdTexto
                );

        BigDecimal valorUnitCiUsd =
                converterBigDecimalOpcional(
                        valorUnitCiUsdTexto
                );
        
        BigDecimal fatorLiquidoBrl =
                converterBigDecimalOpcional(
                        fatorLiquidoBrlTexto
                );

        LocalDate dataCusto =
                dataTexto == null
                        || dataTexto.isBlank()
                        ? null
                        : LocalDate.parse(
                                dataTexto
                        );

        LocalDateTime ultimaModificacaoArquivo =
                converterUltimaModificacaoArquivo(
                        resultSet
                );

        return new CustoItem(
                resultSet.getInt("id"),
                resultSet.getString("id_item"),
                resultSet.getString(
                        "id_importacao"
                ),
                resultSet.getInt(
                        "ano_importacao"
                ),
                resultSet.getInt(
                        "numero_importacao"
                ),
                custo,
                valorUnitPiUsd,
                valorUnitCiUsd,
                fatorLiquidoBrl,
                dataCusto,
                resultSet.getString("arquivo"),
                ultimaModificacaoArquivo
        );
    }

    private LocalDateTime converterUltimaModificacaoArquivo(ResultSet resultSet) throws SQLException {
        double timestamp = resultSet.getDouble("ultima_modificacao_arquivo");

        if (resultSet.wasNull() || timestamp <= 0) {
            return null;
        }

        long segundos = (long) timestamp;
        long nanos = (long) ((timestamp - segundos) * 1_000_000_000L);

        return Instant.ofEpochSecond(segundos, nanos)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}