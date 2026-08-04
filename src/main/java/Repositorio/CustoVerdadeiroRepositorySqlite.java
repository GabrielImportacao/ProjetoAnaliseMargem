package Repositorio;

import Infraestrutura.ConexaoItemDepoSqlite;
import Infraestrutura.ConexaoItemLtoeSqlite;
import Infraestrutura.ConexaoSqlite;
import Modelo.CustoVerdadeiroItem;
import Modelo.LoteEstoqueItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class CustoVerdadeiroRepositorySqlite implements CustoVerdadeiroRepository {

	private static final List<String> DEPOSITOS_CUSTO_VERDADEIRO =
	        List.of(
	                "140",
	                "191"
	        );
    private static final BigDecimal DIVISOR_QTDE_LOTE = new BigDecimal("10000");

    private static final Pattern PADRAO_LOTE_EXTERNO =
            Pattern.compile("^[A-Za-z]+(\\d{3})(\\d{2}).*$");

    private final ConexaoItemDepoSqlite conexaoItemDepoSqlite;
    private final ConexaoItemLtoeSqlite conexaoItemLtoeSqlite;
    private final ConexaoSqlite conexaoCustosSqlite;

    private Map<String, BigDecimal> cacheEstoquePorItem;

    private Map<String, Map<String, BigDecimal>>
            cacheEstoquePorItemEDeposito;

    private Map<String, List<LoteEstoqueItem>>
            cacheLotesPorItem;
    private Map<String, CustoLote> cacheCustoPorItemEProcesso;
    private Map<String, List<ImportacaoCustoLog>> cacheImportacoesPorItem;

    public CustoVerdadeiroRepositorySqlite() {
        this.conexaoItemDepoSqlite = new ConexaoItemDepoSqlite();
        this.conexaoItemLtoeSqlite = new ConexaoItemLtoeSqlite();
        this.conexaoCustosSqlite = new ConexaoSqlite();
    }

    public void limparCache() {
        cacheEstoquePorItem = null;
        cacheEstoquePorItemEDeposito = null;
        cacheLotesPorItem = null;
        cacheCustoPorItemEProcesso = null;
        cacheImportacoesPorItem = null;
    }

    public void preCarregarCache() {
        carregarCachesSeNecessario();
    }
    
    private void registrarEstruturaCustosDbNoLog(StringBuilder diagnostico) {
        diagnostico.append("ESTRUTURA DA TABELA custos:\n");

        String sql = "PRAGMA table_info(custos)";

        try (
                Connection conexao = conexaoCustosSqlite.abrir();
                Statement statement = conexao.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            while (resultSet.next()) {
                diagnostico.append("- ")
                        .append(resultSet.getString("name"))
                        .append(" | tipo: ")
                        .append(resultSet.getString("type"))
                        .append("\n");
            }

        } catch (Exception e) {
            diagnostico.append("ERRO AO LER ESTRUTURA DA TABELA custos: ")
                    .append(e.getMessage())
                    .append("\n");
        }

        diagnostico.append("\n");
    }
    
    private void registrarLinhasBrutasCustosItemNoLog(StringBuilder diagnostico, String chaveItem) {
        diagnostico.append("LINHAS BRUTAS DO custos.db PARA O ITEM:\n");

        String sql = """
                SELECT *
                FROM custos
                WHERE UPPER(TRIM(id_item)) = ?
                ORDER BY
                    ano_importacao DESC,
                    numero_importacao DESC,
                    id DESC
                LIMIT 30
                """;

        try (
                Connection conexao = conexaoCustosSqlite.abrir();
                PreparedStatement statement = conexao.prepareStatement(sql)
        ) {
            statement.setString(1, chaveItem);

            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int totalColunas = metaData.getColumnCount();

                int contadorLinha = 0;

                while (resultSet.next()) {
                    contadorLinha++;

                    diagnostico.append("---- LINHA ")
                            .append(contadorLinha)
                            .append(" ----\n");

                    for (int i = 1; i <= totalColunas; i++) {
                        String nomeColuna = metaData.getColumnName(i);
                        String valor = resultSet.getString(i);

                        diagnostico.append(nomeColuna)
                                .append(": ")
                                .append(valor)
                                .append("\n");
                    }

                    diagnostico.append("\n");
                }

                if (contadorLinha == 0) {
                    diagnostico.append("Nenhuma linha bruta encontrada para o item ")
                            .append(chaveItem)
                            .append(".\n\n");
                }
            }

        } catch (Exception e) {
            diagnostico.append("ERRO AO LER LINHAS BRUTAS DO custos.db: ")
                    .append(e.getMessage())
                    .append("\n\n");
        }
    }

    @Override
    public Optional<CustoVerdadeiroItem> buscarCustoVerdadeiroPorItem(String codigoItem) {
        if (!ehItemElegivelParaCustoVerdadeiro(codigoItem)) {
            return Optional.empty();
        }

        carregarCachesSeNecessario();

        String chaveItem = normalizarCodigo(codigoItem);

        StringBuilder diagnostico = new StringBuilder();

        diagnostico.append("\n=================================================\n");
        diagnostico.append("Data/Hora: ").append(LocalDateTime.now()).append("\n");
        diagnostico.append("ITEM: ").append(chaveItem).append("\n");
        diagnostico.append("DEPÓSITOS CONSIDERADOS: ")
        .append(
                String.join(
                        ", ",
                        DEPOSITOS_CUSTO_VERDADEIRO
                )
        )
        .append("\n\n");

        BigDecimal estoqueAtual = valorSeguro(cacheEstoquePorItem.get(chaveItem));

        diagnostico.append("ESTOQUE ATUAL TOTAL itemdepo: ")
        .append(
                formatarDecimalLog(
                        estoqueAtual
                )
        )
        .append("\n");

Map<String, BigDecimal> estoquePorDeposito =
        cacheEstoquePorItemEDeposito
                .getOrDefault(
                        chaveItem,
                        Map.of()
                );

for (
        String deposito
        : DEPOSITOS_CUSTO_VERDADEIRO
) {
    diagnostico.append("ESTOQUE DEPÓSITO ")
            .append(deposito)
            .append(": ")
            .append(
                    formatarDecimalLog(
                            valorSeguro(
                                    estoquePorDeposito
                                            .get(deposito)
                            )
                    )
            )
            .append("\n");
}

diagnostico.append("\n");
        
        registrarImportacoesDoItemNoLog(diagnostico, chaveItem);
        registrarEstruturaCustosDbNoLog(diagnostico);
        registrarLinhasBrutasCustosItemNoLog(diagnostico, chaveItem);

        if (estoqueAtual.compareTo(BigDecimal.ZERO) <= 0) {
            diagnostico.append("RESULTADO: S./REG.\n");
            diagnostico.append("MOTIVO: Estoque atual zerado ou não encontrado no itemdepo.db.\n");

            registrarDiagnostico(diagnostico.toString());
            return Optional.empty();
        }

        Optional<ImportacaoCustoLog> ultimaImportacaoValida =
                buscarUltimaImportacaoValidaDoItem(chaveItem);

        if (ultimaImportacaoValida.isPresent()) {
            ImportacaoCustoLog ultimaImportacao = ultimaImportacaoValida.get();

            BigDecimal qtdUltimaEntrada = valorSeguro(ultimaImportacao.qtdImp());
            BigDecimal custoUltimaEntrada = valorSeguro(ultimaImportacao.custo());

            diagnostico.append("ÚLTIMA IMPORTAÇÃO VÁLIDA:\n");
            diagnostico.append("PROCESSO: ").append(ultimaImportacao.processo()).append("\n");
            diagnostico.append("QTD ENTRADA: ").append(formatarDecimalLog(qtdUltimaEntrada)).append("\n");
            diagnostico.append("CUSTO ENTRADA: ").append(formatarDecimalLog(custoUltimaEntrada)).append("\n");
            diagnostico.append("DATA CUSTO: ").append(ultimaImportacao.dataCusto()).append("\n\n");

            if (estoqueAtual.compareTo(qtdUltimaEntrada) <= 0) {
                diagnostico.append("REGRA APLICADA: ESTOQUE ATUAL <= QTD ÚLTIMA ENTRADA.\n");
                diagnostico.append("AÇÃO: Usar custo da última entrada diretamente.\n\n");

                diagnostico.append("CUSTO VERDADEIRO: ")
                        .append(formatarDecimalLog(custoUltimaEntrada))
                        .append("\n");

                diagnostico.append("DATA REFERÊNCIA: ")
                        .append(ultimaImportacao.dataCusto())
                        .append("\n");

                diagnostico.append("LOTES USADOS: 1\n");
                diagnostico.append("RESULTADO: OK\n");

                registrarDiagnostico(diagnostico.toString());

                return Optional.of(new CustoVerdadeiroItem(
                        codigoItem,
                        custoUltimaEntrada,
                        estoqueAtual,
                        estoqueAtual,
                        ultimaImportacao.dataCusto(),
                        1
                ));
            }

            diagnostico.append("REGRA APLICADA: ESTOQUE ATUAL > QTD ÚLTIMA ENTRADA.\n");
            diagnostico.append("AÇÃO: Calcular média ponderada pelos lotes atuais.\n\n");

        } else {
            diagnostico.append("ÚLTIMA IMPORTAÇÃO VÁLIDA: NÃO ENCONTRADA.\n");
            diagnostico.append("AÇÃO: Tentar calcular pela média ponderada dos lotes atuais.\n\n");
        }

        List<LoteEstoqueItem> lotes = cacheLotesPorItem.getOrDefault(chaveItem, List.of());

        diagnostico.append("LOTES ENCONTRADOS item_ltoe: ")
                .append(lotes.size())
                .append("\n\n");

        if (lotes.isEmpty()) {
            diagnostico.append("RESULTADO: S./REG.\n");
            diagnostico.append("MOTIVO: Nenhum lote atual encontrado no item_ltoe.db.\n");

            registrarDiagnostico(diagnostico.toString());
            return Optional.empty();
        }

        BigDecimal somaQuantidade = BigDecimal.ZERO;
        BigDecimal somaCustoPonderado = BigDecimal.ZERO;
        LocalDate dataReferencia = null;
        int quantidadeLotesUsados = 0;

        for (LoteEstoqueItem lote : lotes) {
            if (lote == null || lote.getQuantidade() == null || lote.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            List<String> processosPossiveis = extrairProcessosPossiveis(lote.getLoteExterno());

            diagnostico.append("DEPÓSITO DO LOTE: ")
            .append(lote.getDeposito())
            .append("\n");
            
            diagnostico.append("LOTE: ").append(lote.getLoteExterno()).append("\n");
            diagnostico.append("QTD LOTE: ").append(formatarDecimalLog(lote.getQuantidade())).append("\n");
            diagnostico.append("PROCESSOS TENTADOS: ")
                    .append(formatarProcessosLog(processosPossiveis))
                    .append("\n");

            Optional<CustoLote> custoLoteEncontrado = buscarCustoDoLote(chaveItem, processosPossiveis);

            if (custoLoteEncontrado.isEmpty()) {
                diagnostico.append("CUSTO ENCONTRADO: NÃO\n\n");

                diagnostico.append("RESULTADO: S./REG.\n");
                diagnostico.append("MOTIVO: Existe lote atual, mas o custo do processo não foi encontrado no custos.db.\n");

                registrarDiagnostico(diagnostico.toString());

                System.err.println(
                        "Custo verdadeiro não encontrado. Item: " + codigoItem
                                + " | Lote externo: " + lote.getLoteExterno()
                                + " | Processos tentados: " + formatarProcessosLog(processosPossiveis)
                );

                return Optional.empty();
            }

            CustoLote custoLote = custoLoteEncontrado.get();

            diagnostico.append("PROCESSO ENCONTRADO: ").append(custoLote.processo()).append("\n");
            diagnostico.append("CUSTO ENCONTRADO: ").append(formatarDecimalLog(custoLote.custo())).append("\n");
            diagnostico.append("DATA CUSTO: ").append(custoLote.dataCusto()).append("\n");
            diagnostico.append("CÁLCULO PARCIAL: ")
                    .append(formatarDecimalLog(lote.getQuantidade()))
                    .append(" * ")
                    .append(formatarDecimalLog(custoLote.custo()))
                    .append(" = ")
                    .append(formatarDecimalLog(lote.getQuantidade().multiply(custoLote.custo())))
                    .append("\n\n");

            somaQuantidade = somaQuantidade.add(lote.getQuantidade());
            somaCustoPonderado = somaCustoPonderado.add(
                    lote.getQuantidade().multiply(custoLote.custo())
            );

            if (custoLote.dataCusto() != null
                    && (dataReferencia == null || custoLote.dataCusto().isAfter(dataReferencia))) {
                dataReferencia = custoLote.dataCusto();
            }

            quantidadeLotesUsados++;
        }

        diagnostico.append("SOMA QTD LOTES: ").append(formatarDecimalLog(somaQuantidade)).append("\n");
        diagnostico.append("SOMA CUSTO PONDERADO: ").append(formatarDecimalLog(somaCustoPonderado)).append("\n");

        if (somaQuantidade.compareTo(estoqueAtual) != 0) {
            diagnostico.append("ALERTA: Soma dos lotes diferente do estoque atual.\n");
            diagnostico.append("DIFERENÇA: ")
                    .append(formatarDecimalLog(somaQuantidade.subtract(estoqueAtual)))
                    .append("\n");
        }

        if (somaQuantidade.compareTo(BigDecimal.ZERO) <= 0) {
            diagnostico.append("\nRESULTADO: S./REG.\n");
            diagnostico.append("MOTIVO: Soma das quantidades dos lotes ficou zerada.\n");

            registrarDiagnostico(diagnostico.toString());
            return Optional.empty();
        }

        BigDecimal custoVerdadeiro = somaCustoPonderado.divide(
                somaQuantidade,
                6,
                RoundingMode.HALF_UP
        );

        diagnostico.append("\nCUSTO VERDADEIRO: ")
                .append(formatarDecimalLog(custoVerdadeiro))
                .append("\n");

        diagnostico.append("DATA REFERÊNCIA: ").append(dataReferencia).append("\n");
        diagnostico.append("LOTES USADOS: ").append(quantidadeLotesUsados).append("\n");
        diagnostico.append("RESULTADO: OK\n");

        registrarDiagnostico(diagnostico.toString());

        return Optional.of(new CustoVerdadeiroItem(
                codigoItem,
                custoVerdadeiro,
                estoqueAtual,
                somaQuantidade,
                dataReferencia,
                quantidadeLotesUsados
        ));
    }

    private void carregarCachesSeNecessario() {
    	if (cacheEstoquePorItem != null
    	        && cacheEstoquePorItemEDeposito != null
    	        && cacheLotesPorItem != null
    	        && cacheCustoPorItemEProcesso != null
    	        && cacheImportacoesPorItem != null) {

    	    return;
    	}

        cacheEstoquePorItem = carregarEstoquePorItem();
        cacheLotesPorItem = carregarLotesPorItem();
        cacheImportacoesPorItem = new HashMap<>();
        cacheCustoPorItemEProcesso = carregarCustosPorItemEProcesso();
    }

    private Map<String, BigDecimal>
    carregarEstoquePorItem() {

        Map<String, BigDecimal> mapa =
                new HashMap<>();

        cacheEstoquePorItemEDeposito =
                new HashMap<>();

        String sql = """
                SELECT
                    item,
                    TRIM(
                        CAST(
                            codigo_deposito AS TEXT
                        )
                    ) AS deposito,
                    estoque_atual
                FROM item_deposito
                WHERE TRIM(
                        CAST(
                            codigo_deposito AS TEXT
                        )
                      ) IN (?, ?)
                  AND item IS NOT NULL
                """;

        try (
                Connection conexao =
                        conexaoItemDepoSqlite.abrir();

                PreparedStatement statement =
                        conexao.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    DEPOSITOS_CUSTO_VERDADEIRO.get(0)
            );

            statement.setString(
                    2,
                    DEPOSITOS_CUSTO_VERDADEIRO.get(1)
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    String item =
                            normalizarCodigo(
                                    resultSet.getString(
                                            "item"
                                    )
                            );

                    String deposito =
                            tratarTexto(
                                    resultSet.getString(
                                            "deposito"
                                    )
                            );

                    BigDecimal estoque =
                            converterBigDecimal(
                                    resultSet.getString(
                                            "estoque_atual"
                                    )
                            );

                    if (item.isBlank()
                            || deposito.isBlank()) {

                        continue;
                    }

                    /*
                     * Total combinado dos depósitos 140 e 191.
                     */
                    mapa.merge(
                            item,
                            estoque,
                            BigDecimal::add
                    );

                    /*
                     * Valor separado por depósito para o log
                     * e para as validações futuras.
                     */
                    cacheEstoquePorItemEDeposito
                            .computeIfAbsent(
                                    item,
                                    chave ->
                                            new HashMap<>()
                            )
                            .merge(
                                    deposito,
                                    estoque,
                                    BigDecimal::add
                            );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao carregar estoque atual dos "
                            + "depósitos 140 e 191 para "
                            + "custo verdadeiro.",
                    e
            );
        }

        return mapa;
    }

    private Map<String, List<LoteEstoqueItem>>
    carregarLotesPorItem() {

        Map<String, List<LoteEstoqueItem>> mapa =
                new HashMap<>();

        String sql = """
                SELECT
                    ITEM,
                    TRIM(
                        CAST(
                            CODIGO_DEPOSITO AS TEXT
                        )
                    ) AS DEPOSITO,
                    LOTE_EXTERNO,
                    QTDE
                FROM item_ltoe
                WHERE TRIM(
                        CAST(
                            CODIGO_DEPOSITO AS TEXT
                        )
                      ) IN (?, ?)
                  AND ITEM IS NOT NULL
                  AND QTDE IS NOT NULL
                  AND QTDE > 0
                """;

        try (
                Connection conexao =
                        conexaoItemLtoeSqlite.abrir();

                PreparedStatement statement =
                        conexao.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    DEPOSITOS_CUSTO_VERDADEIRO.get(0)
            );

            statement.setString(
                    2,
                    DEPOSITOS_CUSTO_VERDADEIRO.get(1)
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {
                while (resultSet.next()) {
                    String codigoItem =
                            normalizarCodigo(
                                    resultSet.getString(
                                            "ITEM"
                                    )
                            );

                    if (!ehItemElegivelParaCustoVerdadeiro(
                            codigoItem
                    )) {
                        continue;
                    }

                    String deposito =
                            tratarTexto(
                                    resultSet.getString(
                                            "DEPOSITO"
                                    )
                            );

                    BigDecimal quantidade =
                            converterBigDecimal(
                                    resultSet.getString(
                                            "QTDE"
                                    )
                            ).divide(
                                    DIVISOR_QTDE_LOTE,
                                    6,
                                    RoundingMode.HALF_UP
                            );

                    if (deposito.isBlank()
                            || quantidade.compareTo(
                                    BigDecimal.ZERO
                            ) <= 0) {

                        continue;
                    }

                    LoteEstoqueItem lote =
                            new LoteEstoqueItem(
                                    codigoItem,
                                    deposito,
                                    tratarTexto(
                                            resultSet.getString(
                                                    "LOTE_EXTERNO"
                                            )
                                    ),
                                    quantidade
                            );

                    mapa.computeIfAbsent(
                            codigoItem,
                            chave ->
                                    new ArrayList<>()
                    ).add(lote);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao carregar lotes dos "
                            + "depósitos 140 e 191 para "
                            + "custo verdadeiro.",
                    e
            );
        }

        return mapa;
    }

    private Map<String, CustoLote> carregarCustosPorItemEProcesso() {
        Map<String, CustoLote> mapa = new HashMap<>();

        String sql = """
                SELECT
                    c.id_item,
                    c.id_importacao,
                    c.custo,
                    c.qtd_imp,
                    c.data_custo
                FROM custos c
                WHERE c.id_item IS NOT NULL
                  AND c.id_importacao IS NOT NULL
                  AND c.custo IS NOT NULL
                ORDER BY
                    UPPER(TRIM(c.id_item)) ASC,
                    c.ano_importacao DESC,
                    c.numero_importacao DESC,
                    UPPER(TRIM(c.id_importacao)) ASC,
                    c.id DESC
                """;

        try (
                Connection conexao = conexaoCustosSqlite.abrir();
                PreparedStatement statement = conexao.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                String item = normalizarCodigo(resultSet.getString("id_item"));
                String processo = normalizarProcesso(resultSet.getString("id_importacao"));
                
                BigDecimal custo = converterBigDecimal(resultSet.getString("custo"));
                BigDecimal qtdImp = converterBigDecimal(resultSet.getString("qtd_imp"));
                LocalDate dataCusto = converterData(resultSet.getString("data_custo"));

                if (!ehItemElegivelParaCustoVerdadeiro(item) || processo.isBlank()) {
                    continue;
                }
                
                cacheImportacoesPorItem
                .computeIfAbsent(item, k -> new ArrayList<>())
                .add(new ImportacaoCustoLog(
                        processo,
                        custo,
                        qtdImp,
                        dataCusto
                ));

                String chave = montarChaveCusto(item, processo);

                mapa.putIfAbsent(chave, new CustoLote(
                        processo,
                        custo,
                        dataCusto
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar custos por processo para custo verdadeiro.", e);
        }

        return mapa;
    }

    private void registrarImportacoesDoItemNoLog(StringBuilder diagnostico, String chaveItem) {
        List<ImportacaoCustoLog> importacoes =
                cacheImportacoesPorItem.getOrDefault(chaveItem, List.of());

        diagnostico.append("IMPORTAÇÕES ENCONTRADAS NO custos.db: ")
                .append(importacoes.size())
                .append("\n");

        if (importacoes.isEmpty()) {
            diagnostico.append("Nenhuma importação encontrada para este item no custos.db.\n\n");
            return;
        }

        for (ImportacaoCustoLog importacao : importacoes) {
            diagnostico.append("- ")
                    .append(importacao.processo())
                    .append(" | qtd_imp: ")
                    .append(formatarDecimalLog(importacao.qtdImp()))
                    .append(" | custo: ")
                    .append(formatarDecimalLog(importacao.custo()))
                    .append(" | data: ")
                    .append(importacao.dataCusto())
                    .append("\n");
        }

        diagnostico.append("\n");
    }

    private Optional<ImportacaoCustoLog> buscarUltimaImportacaoValidaDoItem(String chaveItem) {
        List<ImportacaoCustoLog> importacoes =
                cacheImportacoesPorItem.getOrDefault(chaveItem, List.of());

        for (ImportacaoCustoLog importacao : importacoes) {
            if (importacao == null) {
                continue;
            }

            BigDecimal qtdImp = valorSeguro(importacao.qtdImp());
            BigDecimal custo = valorSeguro(importacao.custo());

            if (qtdImp.compareTo(BigDecimal.ZERO) > 0
                    && custo.compareTo(BigDecimal.ZERO) > 0) {
                return Optional.of(importacao);
            }
        }

        return Optional.empty();
    }
    
    private Optional<CustoLote> buscarCustoDoLote(String codigoItemNormalizado, List<String> processosPossiveis) {
        if (processosPossiveis == null || processosPossiveis.isEmpty()) {
            return Optional.empty();
        }

        for (String processo : processosPossiveis) {
            CustoLote custo = cacheCustoPorItemEProcesso.get(
                    montarChaveCusto(codigoItemNormalizado, processo)
            );

            if (custo != null && custo.custo().compareTo(BigDecimal.ZERO) > 0) {
                return Optional.of(custo);
            }
        }

        return Optional.empty();
    }

    private List<String> extrairProcessosPossiveis(String loteExterno) {
        if (loteExterno == null || loteExterno.isBlank()) {
            return List.of();
        }

        Matcher matcher = PADRAO_LOTE_EXTERNO.matcher(loteExterno.trim().toUpperCase());

        if (!matcher.matches()) {
            return List.of();
        }

        String numeroComZeros = matcher.group(1);
        String ano = matcher.group(2);

        int numeroInteiro;
        try {
            numeroInteiro = Integer.parseInt(numeroComZeros);
        } catch (NumberFormatException e) {
            return List.of();
        }

        String numeroSemZeros = String.valueOf(numeroInteiro);

        LinkedHashSet<String> processos = new LinkedHashSet<>();

        processos.add("RPLA" + numeroComZeros + "-" + ano);
        processos.add("RPLA" + numeroSemZeros + "-" + ano);
        processos.add("PLA" + numeroComZeros + "-" + ano);
        processos.add("PLA" + numeroSemZeros + "-" + ano);

        return new ArrayList<>(processos);
    }

    private String montarChaveCusto(String codigoItem, String processo) {
        return normalizarCodigo(codigoItem) + "|" + normalizarProcesso(processo);
    }

    private boolean ehItemElegivelParaCustoVerdadeiro(String codigoItem) {
        if (codigoItem == null) {
            return false;
        }

        String codigoNormalizado = codigoItem.trim().toUpperCase();

        return codigoNormalizado.startsWith("MMR")
                || codigoNormalizado.startsWith("MSC");
    }

    private BigDecimal converterBigDecimal(String texto) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }

        String normalizado = texto.trim();

        if (normalizado.contains(",")) {
            normalizado = normalizado.replace(".", "");
            normalizado = normalizado.replace(",", ".");
        }

        try {
            return new BigDecimal(normalizado);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDate converterData(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(texto.trim().substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String tratarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String normalizarCodigo(String texto) {
        return texto == null ? "" : texto.trim().toUpperCase();
    }

    private String normalizarProcesso(String texto) {
        return texto == null ? "" : texto.trim().toUpperCase();
    }

    private record CustoLote(
            String processo,
            BigDecimal custo,
            LocalDate dataCusto
    ) {
    }
    private void registrarDiagnostico(String conteudo) {
        try {
            Path pastaLogs = Path.of(System.getProperty("user.dir"), "logs");
            Files.createDirectories(pastaLogs);

            Path arquivo = pastaLogs.resolve("diagnostico_custo_verdadeiro.txt");

            Files.writeString(
                    arquivo,
                    conteudo,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatarDecimalLog(BigDecimal valor) {
        if (valor == null) {
            return "null";
        }

        return valor.stripTrailingZeros().toPlainString();
    }

    private String formatarProcessosLog(List<String> processos) {
        if (processos == null || processos.isEmpty()) {
            return "NENHUM - lote fora do padrão esperado";
        }

        return String.join(", ", processos);
    }
    
    private record ImportacaoCustoLog(
            String processo,
            BigDecimal custo,
            BigDecimal qtdImp,
            LocalDate dataCusto
    ) {
    }
}