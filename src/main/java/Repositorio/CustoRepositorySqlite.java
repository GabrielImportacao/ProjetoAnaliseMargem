package Repositorio;

import Infraestrutura.ConexaoSqlite;
import Modelo.CustoItem;
import Modelo.HistoricoCustoItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public HistoricoCustoItem buscarHistoricoPrincipalPorItem(String codigoItem) {
        List<CustoItem> custos = listarCustosPorItem(codigoItem);

        CustoItem custoAtual = custos.size() >= 1 ? custos.get(0) : null;
        CustoItem custoAnterior = custos.size() >= 2 ? custos.get(1) : null;

        return new HistoricoCustoItem(custoAtual, custoAnterior);
    }
    
    private String normalizarCodigo(String codigoItem) {
        return codigoItem == null ? "" : codigoItem.trim().toUpperCase();
    }
    
    @Override
    public Optional<CustoItem> buscarCustoMaisRecentePorItem(String codigoItem) {
        List<CustoItem> custos = listarCustosPorItem(codigoItem);

        if (custos.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(custos.get(0));
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

    private CustoItem converterResultSetParaCustoItem(ResultSet resultSet) throws SQLException {
        String custoTexto = resultSet.getString("custo");
        String dataTexto = resultSet.getString("data_custo");

        BigDecimal custo = custoTexto == null || custoTexto.isBlank()
                ? BigDecimal.ZERO
                : new BigDecimal(custoTexto);

        LocalDate dataCusto = dataTexto == null || dataTexto.isBlank()
                ? null
                : LocalDate.parse(dataTexto);

        LocalDateTime ultimaModificacaoArquivo = converterUltimaModificacaoArquivo(resultSet);

        return new CustoItem(
                resultSet.getInt("id"),
                resultSet.getString("id_item"),
                resultSet.getString("id_importacao"),
                resultSet.getInt("ano_importacao"),
                resultSet.getInt("numero_importacao"),
                custo,
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