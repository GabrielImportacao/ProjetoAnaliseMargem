package Repositorio;

import Infraestrutura.ConexaoSqlite;
import Modelo.CustoItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustoRepositorySqlite implements CustoRepository {

    private final ConexaoSqlite conexaoSqlite;

    public CustoRepositorySqlite() {
        this.conexaoSqlite = new ConexaoSqlite();
    }

    @Override
    public Optional<CustoItem> buscarCustoMaisRecentePorItem(String codigoItem) {
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
                    al.ultima_modificacao AS ultima_modificacao_arquivo
                FROM custos c
                LEFT JOIN arquivos_lidos al
                    ON TRIM(c.arquivo) = TRIM(al.arquivo)
                WHERE UPPER(TRIM(c.id_item)) = UPPER(TRIM(?))
                ORDER BY
                    c.ano_importacao DESC,
                    COALESCE(al.ultima_modificacao, 0) DESC,
                    c.numero_importacao DESC,
                    c.data_custo DESC,
                    c.id DESC
                LIMIT 1
                """;

        try (
                Connection conexao = conexaoSqlite.abrir();
                PreparedStatement statement = conexao.prepareStatement(sql)
        ) {
            statement.setString(1, codigoItem);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(converterResultSetParaCustoItem(resultSet));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar custo mais recente do item: " + codigoItem, e);
        }

        return Optional.empty();
    }

    @Override
    public List<CustoItem> listarCustosPorItem(String codigoItem) {
        List<CustoItem> custos = new ArrayList<>();

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
                    al.ultima_modificacao AS ultima_modificacao_arquivo
                FROM custos c
                LEFT JOIN arquivos_lidos al
                    ON TRIM(c.arquivo) = TRIM(al.arquivo)
                WHERE UPPER(TRIM(c.id_item)) = UPPER(TRIM(?))
                ORDER BY
                    c.ano_importacao DESC,
                    COALESCE(al.ultima_modificacao, 0) DESC,
                    c.numero_importacao DESC,
                    c.data_custo DESC,
                    c.id DESC
                """;

        try (
                Connection conexao = conexaoSqlite.abrir();
                PreparedStatement statement = conexao.prepareStatement(sql)
        ) {
            statement.setString(1, codigoItem);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    custos.add(converterResultSetParaCustoItem(resultSet));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar custos do item: " + codigoItem, e);
        }

        return custos;
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