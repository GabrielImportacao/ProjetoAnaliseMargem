package Repositorio;

import Configuracao.CaminhosBase;
import Configuracao.ConfiguracaoUsuario;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ConfiguracaoUsuarioRepositorySqlite implements ConfiguracaoUsuarioRepository {

    @Override
    public ConfiguracaoUsuario carregar() {
        ConfiguracaoUsuario configuracao = new ConfiguracaoUsuario();

        try (
                Connection conexao = abrirConexao()
        ) {
            criarTabelaSeNecessario(conexao);

            Map<String, String> valores = carregarValores(conexao);

            configuracao.setMargemEmPorcentagem(
                    lerBoolean(valores, "margemEmPorcentagem", configuracao.isMargemEmPorcentagem())
            );

            configuracao.setCustoFuturo(
                    lerBoolean(valores, "custoFuturo", configuracao.isCustoFuturo())
            );

            configuracao.setCustoEstimado(
                    lerBoolean(valores, "custoEstimado", configuracao.isCustoEstimado())
            );

            configuracao.setFlagItensEncalhados(
                    lerBoolean(valores, "flagItensEncalhados", configuracao.isFlagItensEncalhados())
            );
            
            configuracao.setCustoVerdadeiroAtivo(
                    lerBoolean(valores, "custoVerdadeiroAtivo", configuracao.isCustoVerdadeiroAtivo())
            );
            
            configuracao.setZoomInterface(
                    lerInteiro(valores, "zoomInterface", configuracao.getZoomInterface())
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar configurações do usuário.", e);
        }

        return configuracao;
    }

    @Override
    public void salvar(ConfiguracaoUsuario configuracao) {
        if (configuracao == null) {
            return;
        }

        try (
                Connection conexao = abrirConexao()
        ) {
            criarTabelaSeNecessario(conexao);

            conexao.setAutoCommit(false);

            salvarValor(conexao, "margemEmPorcentagem", configuracao.isMargemEmPorcentagem());
            salvarValor(conexao, "custoFuturo", configuracao.isCustoFuturo());
            salvarValor(conexao, "custoEstimado", configuracao.isCustoEstimado());
            salvarValor(conexao, "flagItensEncalhados", configuracao.isFlagItensEncalhados());
            salvarValor(conexao, "custoVerdadeiroAtivo", configuracao.isCustoVerdadeiroAtivo());
            salvarValor(conexao, "zoomInterface", configuracao.getZoomInterface());

            conexao.commit();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar configurações do usuário.", e);
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
                CREATE TABLE IF NOT EXISTS configuracao_usuario (
                    chave TEXT PRIMARY KEY,
                    valor TEXT NOT NULL
                )
                """;

        try (Statement statement = conexao.createStatement()) {
            statement.execute(sql);
        }
    }

    private Map<String, String> carregarValores(Connection conexao) throws Exception {
        Map<String, String> valores = new HashMap<>();

        String sql = """
                SELECT chave, valor
                FROM configuracao_usuario
                """;

        try (
                PreparedStatement statement = conexao.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                valores.put(
                        resultSet.getString("chave"),
                        resultSet.getString("valor")
                );
            }
        }

        return valores;
    }

    private void salvarValor(Connection conexao, String chave, boolean valor) throws Exception {
        String sql = """
                INSERT OR REPLACE INTO configuracao_usuario (chave, valor)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, chave);
            statement.setString(2, String.valueOf(valor));
            statement.executeUpdate();
        }
    }
    
    private void salvarValor(Connection conexao, String chave, int valor) throws Exception {
        String sql = """
                INSERT OR REPLACE INTO configuracao_usuario (chave, valor)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement = conexao.prepareStatement(sql)) {
            statement.setString(1, chave);
            statement.setString(2, String.valueOf(valor));
            statement.executeUpdate();
        }
    }

    private boolean lerBoolean(Map<String, String> valores, String chave, boolean padrao) {
        if (valores == null || !valores.containsKey(chave)) {
            return padrao;
        }

        return Boolean.parseBoolean(valores.get(chave));
    }
    
    private int lerInteiro(Map<String, String> valores, String chave, int padrao) {
        if (valores == null || !valores.containsKey(chave)) {
            return padrao;
        }

        try {
            return Integer.parseInt(valores.get(chave));
        } catch (Exception e) {
            return padrao;
        }
    }
}