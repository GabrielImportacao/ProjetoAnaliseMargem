package Repositorio;

import Configuracao.CaminhosBase;
import Modelo.LayoutColunaTabela;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LayoutTabelaRepositorySqlite implements LayoutTabelaRepository {

    @Override
    public List<LayoutColunaTabela> carregarLayoutColunas(String tela) {
        List<LayoutColunaTabela> layout = new ArrayList<>();

        try (
                Connection conexao = abrirConexao()
        ) {
            criarTabelaSeNecessario(conexao);

            String sql = """
                    SELECT
                        id_coluna,
                        id_pai,
                        ordem,
                        largura
                    FROM layout_coluna_tabela
                    WHERE tela = ?
                    ORDER BY
                        CASE WHEN id_pai IS NULL THEN 0 ELSE 1 END,
                        id_pai,
                        ordem
                    """;

            try (PreparedStatement statement = conexao.prepareStatement(sql)) {
                statement.setString(1, tela);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        layout.add(new LayoutColunaTabela(
                                resultSet.getString("id_coluna"),
                                resultSet.getString("id_pai"),
                                resultSet.getInt("ordem"),
                                resultSet.getDouble("largura")
                        ));
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar layout das colunas da tabela.", e);
        }

        return layout;
    }

    @Override
    public void salvarLayoutColunas(String tela, List<LayoutColunaTabela> layout) {
        if (tela == null || tela.isBlank() || layout == null) {
            return;
        }

        try (
                Connection conexao = abrirConexao()
        ) {
            criarTabelaSeNecessario(conexao);

            conexao.setAutoCommit(false);

            try (
                    PreparedStatement delete = conexao.prepareStatement(
                            "DELETE FROM layout_coluna_tabela WHERE tela = ?"
                    )
            ) {
                delete.setString(1, tela);
                delete.executeUpdate();
            }

            String sqlInsert = """
                    INSERT INTO layout_coluna_tabela (
                        tela,
                        id_coluna,
                        id_pai,
                        ordem,
                        largura
                    )
                    VALUES (?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement insert = conexao.prepareStatement(sqlInsert)) {
                for (LayoutColunaTabela coluna : layout) {
                    if (coluna == null || coluna.getIdColuna() == null || coluna.getIdColuna().isBlank()) {
                        continue;
                    }

                    insert.setString(1, tela);
                    insert.setString(2, coluna.getIdColuna());
                    insert.setString(3, coluna.getIdPai());
                    insert.setInt(4, coluna.getOrdem());
                    insert.setDouble(5, coluna.getLargura());
                    insert.addBatch();
                }

                insert.executeBatch();
            }

            conexao.commit();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar layout das colunas da tabela.", e);
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
                CREATE TABLE IF NOT EXISTS layout_coluna_tabela (
                    tela TEXT NOT NULL,
                    id_coluna TEXT NOT NULL,
                    id_pai TEXT,
                    ordem INTEGER NOT NULL,
                    largura REAL NOT NULL,
                    PRIMARY KEY (tela, id_coluna)
                )
                """;

        try (Statement statement = conexao.createStatement()) {
            statement.execute(sql);
        }
    }
}