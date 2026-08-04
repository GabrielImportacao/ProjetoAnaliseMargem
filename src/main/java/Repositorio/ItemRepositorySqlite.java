package Repositorio;

import Infraestrutura.ConexaoMetalSqlite;
import Modelo.ItemCadastro;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemRepositorySqlite implements ItemRepository {
	
	private static final String
    COLUNA_FATOR_IMPORTACAO_FALLBACK =
    "fator_liquido_brl";

    private final ConexaoMetalSqlite conexaoMetalSqlite;

    private Map<String, ItemCadastro> cacheItens;
    private String nomeTabelaItens;

    public ItemRepositorySqlite() {
        this.conexaoMetalSqlite = new ConexaoMetalSqlite();
    }

    public void limparCache() {
        cacheItens = null;
        nomeTabelaItens = null;
    }

    public void preCarregarCache() {
        carregarCacheSeNecessario();
    }

    @Override
    public Optional<ItemCadastro> buscarPorCodigo(String codigoItem) {
        if (codigoItem == null || codigoItem.isBlank()) {
            return Optional.empty();
        }

        carregarCacheSeNecessario();

        String chave = normalizarCodigo(codigoItem);

        return Optional.ofNullable(cacheItens.get(chave));
    }

    private void carregarCacheSeNecessario() {
        if (cacheItens != null) {
            return;
        }

        cacheItens = new HashMap<>();

        try (
                Connection conexao = conexaoMetalSqlite.abrir()
        ) {
            nomeTabelaItens = descobrirUnicaTabela(conexao);

            String sql = "SELECT * FROM \"" + nomeTabelaItens.replace("\"", "\"\"") + "\"";

            try (
                    PreparedStatement statement = conexao.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()
            ) {
                Map<String, String> colunas = mapearColunas(resultSet.getMetaData());

                validarColunaObrigatoria(colunas, "CODIGO");
                validarColunaObrigatoria(colunas, "DESCRICAO");
                validarColunaObrigatoria(colunas, "IPI");
                validarColunaObrigatoria(colunas, "UN");
                validarColunaObrigatoria(colunas, "CUSTO_ATUAL");
                validarColunaObrigatoria(colunas, "PRECO_LIQUIDO");

                while (resultSet.next()) {
                    ItemCadastro item = converterResultSetParaItem(resultSet, colunas);

                    if (item == null || item.getCodigoItem() == null || item.getCodigoItem().isBlank()) {
                        continue;
                    }

                    cacheItens.put(normalizarCodigo(item.getCodigoItem()), item);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar itens do banco metal.db.", e);
        }
    }

    private String descobrirUnicaTabela(Connection conexao) throws Exception {
        String sql = """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                  AND name NOT LIKE 'sqlite_%'
                ORDER BY name
                """;

        try (
                Statement statement = conexao.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)
        ) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Nenhuma tabela encontrada no metal.db.");
            }

            String tabela = resultSet.getString("name");

            if (resultSet.next()) {
                throw new IllegalStateException(
                        "O metal.db possui mais de uma tabela. Informe qual tabela deve ser usada."
                );
            }

            return tabela;
        }
    }

    private Map<String, String> mapearColunas(ResultSetMetaData metaData) throws Exception {
        Map<String, String> mapa = new HashMap<>();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String nomeReal = metaData.getColumnName(i);
            String nomeNormalizado = normalizarTexto(nomeReal);

            mapa.put(nomeNormalizado, nomeReal);
        }

        return mapa;
    }

    private void validarColunaObrigatoria(Map<String, String> colunas, String nomeColunaNormalizado) {
        if (!colunas.containsKey(nomeColunaNormalizado)) {
            throw new IllegalStateException(
                    "Coluna obrigatória não encontrada no metal.db: " + nomeColunaNormalizado
            );
        }
    }

    private ItemCadastro converterResultSetParaItem(
            ResultSet resultSet,
            Map<String, String> colunas
    ) throws Exception {
        String codigoItem = valorTexto(resultSet, colunas, "CODIGO");

        if (codigoItem == null || codigoItem.isBlank()) {
            return null;
        }

        return new ItemCadastro(
                codigoItem,
                valorTexto(
                        resultSet,
                        colunas,
                        "DESCRICAO"
                ),
                valorBigDecimal(
                        resultSet,
                        colunas,
                        "IPI"
                ),
                "",
                valorTexto(
                        resultSet,
                        colunas,
                        "UN"
                ),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                valorBigDecimal(
                        resultSet,
                        colunas,
                        "CUSTO_ATUAL"
                ),
                "METAL.DB",
                valorBigDecimal(
                        resultSet,
                        colunas,
                        "PRECO_LIQUIDO"
                ),
                valorBigDecimalOpcional(
                        resultSet,
                        colunas,
                        COLUNA_FATOR_IMPORTACAO_FALLBACK
                )
        );
    }

    private String valorTexto(ResultSet resultSet, Map<String, String> colunas, String nomeNormalizado) throws Exception {
        String nomeReal = colunas.get(nomeNormalizado);

        if (nomeReal == null) {
            return "";
        }

        String valor = resultSet.getString(nomeReal);

        return valor == null ? "" : valor.trim();
    }
    
    private BigDecimal valorBigDecimalOpcional(
            ResultSet resultSet,
            Map<String, String> colunas,
            String nomeColunaConfigurada
    ) throws Exception {
        if (nomeColunaConfigurada == null
                || nomeColunaConfigurada.isBlank()) {

            return null;
        }

        /*
         * Normaliza o nome informado na constante
         * exatamente da mesma forma que os nomes
         * encontrados no banco.
         */
        String nomeNormalizado =
                normalizarTexto(
                        nomeColunaConfigurada
                );

        String nomeReal =
                colunas.get(
                        nomeNormalizado
                );

        /*
         * A coluna ainda não existe no banco atual.
         * Não interrompe o carregamento dos itens.
         */
        if (nomeReal == null
                || nomeReal.isBlank()) {

            return null;
        }

        String texto =
                resultSet.getString(
                        nomeReal
                );

        if (texto == null
                || texto.isBlank()
                || texto.trim().equals("-")) {

            return null;
        }

        BigDecimal valor =
                converterBigDecimal(
                        texto
                );

        /*
         * Zero ou negativo não constitui
         * um fator válido para divisão.
         */
        if (valor.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            return null;
        }

        return valor;
    }

    private BigDecimal valorBigDecimal(ResultSet resultSet, Map<String, String> colunas, String nomeNormalizado) throws Exception {
        String texto = valorTexto(resultSet, colunas, nomeNormalizado);

        return converterBigDecimal(texto);
    }

    private BigDecimal converterBigDecimal(String texto) {
        if (texto == null || texto.isBlank() || texto.equals("-")) {
            return BigDecimal.ZERO;
        }

        String normalizado = texto
                .replace("R$", "")
                .replace("%", "")
                .trim();

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

    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return semAcento
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", " ");
    }
}