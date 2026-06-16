package Repositorio;

import Infraestrutura.ConexaoMovestqSqlite;
import Modelo.CustoPromobItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;

public class CustoPromobRepositorySqlite implements CustoPromobRepository {

    private final ConexaoMovestqSqlite conexaoMovestqSqlite;

    public CustoPromobRepositorySqlite() {
        this.conexaoMovestqSqlite = new ConexaoMovestqSqlite();
    }

    @Override
    public Optional<CustoPromobItem> buscarCustoMaisRecentePorItem(String codigoItem) {
        if (codigoItem == null || codigoItem.isBlank()) {
            return Optional.empty();
        }

        String sql = """
                SELECT
                    item,
                    dt_moviment,
                    vl_unit_pond
                FROM movestq
                WHERE UPPER(TRIM(item)) = UPPER(TRIM(?))
                  AND vl_unit_pond IS NOT NULL
                ORDER BY
                    date(dt_moviment) DESC,
                    rowid DESC
                LIMIT 1
                """;

        try (
                Connection conexao = conexaoMovestqSqlite.abrir();
                PreparedStatement statement = conexao.prepareStatement(sql)
        ) {
            statement.setString(1, codigoItem);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(converterResultSetParaCustoPromob(resultSet));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar custo Promob do item: " + codigoItem, e);
        }

        return Optional.empty();
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