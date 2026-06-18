package Repositorio;

import Infraestrutura.ConexaoMovestqSqlite;
import Modelo.CustoPromobItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CustoPromobRepositorySqlite implements CustoPromobRepository {

    private final ConexaoMovestqSqlite conexaoMovestqSqlite;
    private Map<String, CustoPromobItem> cachePromobPorItem;

    public CustoPromobRepositorySqlite() {
        this.conexaoMovestqSqlite = new ConexaoMovestqSqlite();
    }

    public void limparCache() {
        cachePromobPorItem = null;
    }

    public void preCarregarCache() {
        carregarCacheSeNecessario();
    }

    private void carregarCacheSeNecessario() {
        if (cachePromobPorItem != null) {
            return;
        }

        cachePromobPorItem = new HashMap<>();

        String sql = """
                SELECT
                    item,
                    dt_moviment,
                    vl_unit_pond
                FROM movestq
                WHERE item IS NOT NULL
                  AND vl_unit_pond IS NOT NULL
                ORDER BY
                    UPPER(TRIM(item)) ASC,
                    date(dt_moviment) DESC,
                    rowid DESC
                """;

        try (
                Connection conexao = conexaoMovestqSqlite.abrir();
                PreparedStatement statement = conexao.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                CustoPromobItem custoPromob = converterResultSetParaCustoPromob(resultSet);

                String chave = normalizarCodigo(custoPromob.getCodigoItem());

                // Como o SELECT está ordenado do mais recente para o mais antigo,
                // só gravamos o primeiro registro de cada item.
                cachePromobPorItem.putIfAbsent(chave, custoPromob);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao pré-carregar cache de custos Promob.", e);
        }
    }
    
    @Override
    public Optional<CustoPromobItem> buscarCustoMaisRecentePorItem(String codigoItem) {
        if (codigoItem == null || codigoItem.isBlank()) {
            return Optional.empty();
        }

        carregarCacheSeNecessario();

        String chave = normalizarCodigo(codigoItem);

        return Optional.ofNullable(cachePromobPorItem.get(chave));
    }
    private String normalizarCodigo(String codigoItem) {
        return codigoItem == null ? "" : codigoItem.trim().toUpperCase();
    }

    private CustoPromobItem converterResultSetParaCustoPromob(ResultSet resultSet) throws Exception {
        String codigoItem = resultSet.getString("item");
        String dataTexto = resultSet.getString("dt_moviment");
        String custoTexto = resultSet.getString("vl_unit_pond");

        BigDecimal custo = custoTexto == null || custoTexto.isBlank()
                ? BigDecimal.ZERO
                : new BigDecimal(custoTexto);

        LocalDate dataMovimento = dataTexto == null || dataTexto.isBlank()
                ? null
                : LocalDate.parse(dataTexto);

        return new CustoPromobItem(
                codigoItem,
                custo,
                dataMovimento
        );
    }
}