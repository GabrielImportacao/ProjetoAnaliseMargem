package Repositorio;

import Infraestrutura.ConexaoItemNfsSqlite;
import Modelo.HistoricoSaidaItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemNfsRepositorySqlite implements ItemNfsRepository {

    private final ConexaoItemNfsSqlite conexaoItemNfsSqlite;
    private Map<String, HistoricoSaidaItem> cacheUltimaSaidaPorItem;

    public ItemNfsRepositorySqlite() {
        this.conexaoItemNfsSqlite = new ConexaoItemNfsSqlite();
    }

    public void limparCache() {
        cacheUltimaSaidaPorItem = null;
    }

    public void preCarregarCache() {
        carregarCacheSeNecessario();
    }

    @Override
    public Optional<HistoricoSaidaItem> buscarUltimaSaidaPorItem(String codigoItem) {
        if (codigoItem == null || codigoItem.isBlank()) {
            return Optional.empty();
        }

        carregarCacheSeNecessario();

        return Optional.ofNullable(
                cacheUltimaSaidaPorItem.get(normalizarCodigo(codigoItem))
        );
    }

    private void carregarCacheSeNecessario() {
        if (cacheUltimaSaidaPorItem != null) {
            return;
        }

        cacheUltimaSaidaPorItem = new HashMap<>();

        String sql = """
                SELECT
                    item,
                    dt_saida,
                    qtde_fatur,
                    nro_nfs,
                    serie,
                    seq,
                    nro_pedido
                FROM item_nfs
                WHERE item IS NOT NULL
                  AND TRIM(item) <> ''
                  AND dt_saida IS NOT NULL
                  AND TRIM(dt_saida) <> ''
                """;

        try (
                Connection conexao = conexaoItemNfsSqlite.abrir();
                PreparedStatement statement = conexao.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                HistoricoSaidaItem historico = converterResultSet(resultSet);

                if (historico == null || historico.getCodigoItem() == null || historico.getDataSaida() == null) {
                    continue;
                }

                String chave = normalizarCodigo(historico.getCodigoItem());

                HistoricoSaidaItem atual = cacheUltimaSaidaPorItem.get(chave);

                if (atual == null || historico.getDataSaida().isAfter(atual.getDataSaida())) {
                    cacheUltimaSaidaPorItem.put(chave, historico);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar histórico de saída dos itens em item_nfs.db.", e);
        }
    }

    private HistoricoSaidaItem converterResultSet(ResultSet resultSet) throws Exception {
        String codigoItem = resultSet.getString("item");
        LocalDate dataSaida = converterData(resultSet.getString("dt_saida"));

        if (codigoItem == null || codigoItem.isBlank() || dataSaida == null) {
            return null;
        }

        return new HistoricoSaidaItem(
                codigoItem,
                dataSaida,
                converterBigDecimal(resultSet.getString("qtde_fatur")),
                tratarTexto(resultSet.getString("nro_nfs")),
                tratarTexto(resultSet.getString("serie")),
                tratarTexto(resultSet.getString("seq")),
                tratarTexto(resultSet.getString("nro_pedido"))
        );
    }

    private LocalDate converterData(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        String normalizado = texto.trim();

        if (normalizado.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            try {
                return LocalDate.parse(normalizado.substring(0, 10));
            } catch (Exception ignored) {
            }
        }

        List<DateTimeFormatter> formatosData = List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        );

        for (DateTimeFormatter formatter : formatosData) {
            try {
                return LocalDate.parse(normalizado, formatter);
            } catch (Exception ignored) {
            }
        }

        List<DateTimeFormatter> formatosDataHora = List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss"),
                DateTimeFormatter.ofPattern("d/M/yyyy HH:mm")
        );

        for (DateTimeFormatter formatter : formatosDataHora) {
            try {
                return LocalDateTime.parse(normalizado, formatter).toLocalDate();
            } catch (Exception ignored) {
            }
        }

        return null;
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

    private String tratarTexto(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String normalizarCodigo(String codigoItem) {
        return codigoItem == null ? "" : codigoItem.trim().toUpperCase();
    }
}