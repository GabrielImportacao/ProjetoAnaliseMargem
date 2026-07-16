package Repositorio;

import Configuracao.CaminhosBase;
import Modelo.EstadoLinhaTabela;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EstadoTabelaRepositorySqlite implements EstadoTabelaRepository {

    @Override
    public List<EstadoLinhaTabela> carregarLinhas(String tela) {
        List<EstadoLinhaTabela> linhas = new ArrayList<>();

        try (
                Connection conexao = abrirConexao()
        ) {
            criarTabelaSeNecessario(conexao);

            String sql = """
                    SELECT *
                    FROM estado_linha_tabela
                    WHERE tela = ?
                    ORDER BY ordem
                    """;

            try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                statement.setString(1, tela);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        linhas.add(converterResultSet(resultSet));
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar estado das linhas da tabela.", e);
        }

        return linhas;
    }

    @Override
    public void salvarLinhas(String tela, List<EstadoLinhaTabela> linhas) {
        if (tela == null || tela.isBlank() || linhas == null) {
            return;
        }

        try (
                Connection conexao = abrirConexao()
        ) {
            criarTabelaSeNecessario(conexao);

            conexao.setAutoCommit(false);

            try (PreparedStatement delete = conexao.prepareStatement(
                    "DELETE FROM estado_linha_tabela WHERE tela = ?"
            )) {
                delete.setString(1, tela);
                delete.executeUpdate();
            }

            String sqlInsert = """
                    INSERT INTO estado_linha_tabela (
                        tela,
                        ordem,
                        codigo,
                        descricao,
                        quantidade,
                        valor_unitario,
                        valor_total,
                        condicao_venda,
                        cor_especial_fundo,
                        cor_especial_texto,
                        variacao_atual,
                        margem_atual,
                        custo_atual,
                        registro_custo_atual,
                        data_custo_atual,
                        margem_verdadeira,
						custo_verdadeiro,
						registro_custo_verdadeiro,
						data_custo_verdadeiro,
                        margem_promob,
                        custo_promob,
                        registro_custo_promob,
                        data_custo_promob,
                        variacao_anterior,
                        margem_anterior,
                        custo_anterior,
                        registro_custo_anterior,
                        data_custo_anterior,
                        preco_padrao_venda,
                        percentual_ipi,
                        data_ultima_saida,
            			item_encalhado,
            			item_encalhado_confirmado
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement insert = conexao.prepareStatement(sqlInsert)) {
                for (EstadoLinhaTabela linha : linhas) {
                    if (linha == null) {
                        continue;
                    }

                    insert.setString(1, tela);
                    insert.setInt(2, linha.getOrdem());
                    insert.setString(3, texto(linha.getCodigo()));
                    insert.setString(4, texto(linha.getDescricao()));
                    insert.setInt(5, linha.getQuantidade());

                    insert.setString(6, decimalTexto(linha.getValorUnitario()));
                    insert.setString(7, decimalTexto(linha.getValorTotal()));

                    insert.setString(8, texto(linha.getCondicaoVenda()));
                    insert.setString(9, texto(linha.getCorEspecialFundo()));
                    insert.setString(10, texto(linha.getCorEspecialTexto()));

                    insert.setString(11, decimalTexto(linha.getVariacaoAtual()));
                    insert.setString(12, decimalTexto(linha.getMargemAtual()));
                    insert.setString(13, decimalTexto(linha.getCustoAtual()));
                    insert.setString(14, texto(linha.getRegistroCustoAtual()));
                    insert.setString(15, dataTexto(linha.getDataCustoAtual()));
                    
                    insert.setString(16, decimalTexto(linha.getMargemVerdadeira()));
                    insert.setString(17, decimalTexto(linha.getCustoVerdadeiro()));
                    insert.setString(18, texto(linha.getRegistroCustoVerdadeiro()));
                    insert.setString(19, dataTexto(linha.getDataCustoVerdadeiro()));

                    insert.setString(16, decimalTexto(linha.getMargemPromob()));
                    insert.setString(17, decimalTexto(linha.getCustoPromob()));
                    insert.setString(18, texto(linha.getRegistroCustoPromob()));
                    insert.setString(19, dataTexto(linha.getDataCustoPromob()));

                    insert.setString(20, decimalTexto(linha.getMargemPromob()));
                    insert.setString(21, decimalTexto(linha.getCustoPromob()));
                    insert.setString(22, texto(linha.getRegistroCustoPromob()));
                    insert.setString(23, dataTexto(linha.getDataCustoPromob()));

                    insert.setString(24, decimalTexto(linha.getVariacaoAnterior()));
                    insert.setString(25, decimalTexto(linha.getMargemAnterior()));
                    insert.setString(26, decimalTexto(linha.getCustoAnterior()));
                    insert.setString(27, texto(linha.getRegistroCustoAnterior()));
                    insert.setString(28, dataTexto(linha.getDataCustoAnterior()));

                    insert.setString(29, decimalTexto(linha.getPrecoPadraoVenda()));
                    insert.setString(30, decimalTexto(linha.getPercentualIpi()));

                    insert.setString(31, dataTexto(linha.getDataUltimaSaida()));
                    insert.setInt(32, linha.isItemEncalhado() ? 1 : 0);
                    insert.setInt(33, linha.isItemEncalhadoConfirmado() ? 1 : 0);

                    insert.addBatch();
                }

                insert.executeBatch();
            }

            conexao.commit();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar estado das linhas da tabela.", e);
        }
    }

    private Connection abrirConexao() throws Exception {
        Files.createDirectories(CaminhosBase.CAMINHO_USER_STATE_DB.getParent());

        String caminhoFormatado = CaminhosBase.CAMINHO_USER_STATE_DB
                .toAbsolutePath()
                .toString()
                .replace("\\", "/");

        return DriverManager.getConnection("jdbc:sqlite:" + caminhoFormatado);
    }

    private void criarTabelaSeNecessario(Connection conexao) throws Exception {
        String sql = """
                CREATE TABLE IF NOT EXISTS estado_linha_tabela (
                    tela TEXT NOT NULL,
                    ordem INTEGER NOT NULL,
                    codigo TEXT,
                    descricao TEXT,
                    quantidade INTEGER,
                    valor_unitario TEXT,
                    valor_total TEXT,
                    condicao_venda TEXT,
                    cor_especial_fundo TEXT,
                    cor_especial_texto TEXT,
                    variacao_atual TEXT,
                    margem_atual TEXT,
                    custo_atual TEXT,
                    registro_custo_atual TEXT,
                    data_custo_atual TEXT,
                    margem_verdadeira TEXT,
					custo_verdadeiro TEXT,
					registro_custo_verdadeiro TEXT,
					data_custo_verdadeiro TEXT,
                    margem_promob TEXT,
                    custo_promob TEXT,
                    registro_custo_promob TEXT,
                    data_custo_promob TEXT,
                    variacao_anterior TEXT,
                    margem_anterior TEXT,
                    custo_anterior TEXT,
                    registro_custo_anterior TEXT,
                    data_custo_anterior TEXT,
                    preco_padrao_venda TEXT,
                    percentual_ipi TEXT,
                    data_ultima_saida TEXT,
        			item_encalhado INTEGER,
        			item_encalhado_confirmado INTEGER,
                    PRIMARY KEY (tela, ordem)
                )
                """;

        try (Statement statement = conexao.createStatement()) {
            statement.execute(sql);
            garantirColunasNovas(conexao);
        }
    }

    private EstadoLinhaTabela converterResultSet(ResultSet resultSet) throws Exception {
        return new EstadoLinhaTabela(
                resultSet.getInt("ordem"),
                resultSet.getString("codigo"),
                resultSet.getString("descricao"),
                resultSet.getInt("quantidade"),
                decimal(resultSet.getString("valor_unitario")),
                decimal(resultSet.getString("valor_total")),
                resultSet.getString("condicao_venda"),
                resultSet.getString("cor_especial_fundo"),
                resultSet.getString("cor_especial_texto"),
                decimal(resultSet.getString("variacao_atual")),
                decimal(resultSet.getString("margem_atual")),
                decimal(resultSet.getString("custo_atual")),
                resultSet.getString("registro_custo_atual"),
                data(resultSet.getString("data_custo_atual")),
                decimal(resultSet.getString("margem_verdadeira")),
                decimal(resultSet.getString("custo_verdadeiro")),
                resultSet.getString("registro_custo_verdadeiro"),
                data(resultSet.getString("data_custo_verdadeiro")),
                decimal(resultSet.getString("margem_promob")),
                decimal(resultSet.getString("custo_promob")),
                resultSet.getString("registro_custo_promob"),
                data(resultSet.getString("data_custo_promob")),
                decimal(resultSet.getString("variacao_anterior")),
                decimal(resultSet.getString("margem_anterior")),
                decimal(resultSet.getString("custo_anterior")),
                resultSet.getString("registro_custo_anterior"),
                data(resultSet.getString("data_custo_anterior")),
                decimal(resultSet.getString("preco_padrao_venda")),
                decimal(resultSet.getString("percentual_ipi")),
                data(resultSet.getString("data_ultima_saida")),
                resultSet.getInt("item_encalhado") == 1,
                resultSet.getInt("item_encalhado_confirmado") == 1
        );
    }

    private void garantirColunasNovas(Connection conexao) throws Exception {
        adicionarColunaSeNaoExistir(conexao, "estado_linha_tabela", "data_ultima_saida", "TEXT");
        adicionarColunaSeNaoExistir(conexao, "estado_linha_tabela", "item_encalhado", "INTEGER");
        adicionarColunaSeNaoExistir(conexao, "estado_linha_tabela", "item_encalhado_confirmado", "INTEGER");
        adicionarColunaSeNaoExistir(conexao, "estado_linha_tabela", "margem_verdadeira", "TEXT");
        adicionarColunaSeNaoExistir(conexao, "estado_linha_tabela", "custo_verdadeiro", "TEXT");
        adicionarColunaSeNaoExistir(conexao, "estado_linha_tabela", "registro_custo_verdadeiro", "TEXT");
        adicionarColunaSeNaoExistir(conexao, "estado_linha_tabela", "data_custo_verdadeiro", "TEXT");
    }

    private void adicionarColunaSeNaoExistir(
            Connection conexao,
            String tabela,
            String coluna,
            String tipo
    ) throws Exception {
        String sqlVerificar = "PRAGMA table_info(" + tabela + ")";

        try (
                Statement statement = conexao.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlVerificar)
        ) {
            while (resultSet.next()) {
                String nomeColuna = resultSet.getString("name");

                if (coluna.equalsIgnoreCase(nomeColuna)) {
                    return;
                }
            }
        }

        try (Statement statement = conexao.createStatement()) {
            statement.execute("ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " " + tipo);
        }
    }
    
    private String texto(String texto) {
        return texto == null ? "" : texto;
    }

    private String decimalTexto(BigDecimal valor) {
        return valor == null ? "" : valor.toPlainString();
    }

    private BigDecimal decimal(String texto) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String dataTexto(LocalDate data) {
        return data == null ? "" : data.toString();
    }

    private LocalDate data(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(texto.trim());
        } catch (Exception e) {
            return null;
        }
    }
}