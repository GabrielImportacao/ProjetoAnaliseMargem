package Repositorio;

import Configuracao.CaminhosBase;
import Configuracao.ConfiguracaoUsuario;

import java.math.BigDecimal;
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
            
            configuracao.setCotacaoDolar(
                    lerBigDecimal(
                            valores,
                            "cotacaoDolar",
                            configuracao.getCotacaoDolar()
                    )
            );
            
            configuracao.setFatorImportacaoReposicao(
                    lerBigDecimalOpcional(
                            valores,
                            "fatorImportacaoReposicao"
                    )
            );
            
            configuracao.setZoomInterface(
                    lerInteiro(valores, "zoomInterface", configuracao.getZoomInterface())
            );                        
            
            for (String sigla : configuracao.getBasesEstados().keySet()) {
                BigDecimal valorPadrao =
                        configuracao.getBaseEstado(sigla);

                BigDecimal valorSalvo = lerBigDecimal(
                        valores,
                        "baseEstado." + sigla,
                        valorPadrao
                );

                configuracao.setBaseEstado(
                        sigla,
                        valorSalvo
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar configurações do usuário.", e);
        }

        return configuracao;
    }
    
    private BigDecimal lerBigDecimalOpcional(
            Map<String, String> valores,
            String chave
    ) {
        if (valores == null
                || !valores.containsKey(chave)) {

            return null;
        }

        String texto =
                valores.get(chave);

        if (texto == null
                || texto.isBlank()) {

            return null;
        }

        try {
            return new BigDecimal(
                    texto.trim()
                            .replace(",", ".")
            );

        } catch (Exception e) {
            return null;
        }
    }
    
    private BigDecimal lerBigDecimal(
            Map<String, String> valores,
            String chave,
            BigDecimal padrao
    ) {
        if (valores == null || !valores.containsKey(chave)) {
            return padrao;
        }

        try {
            return new BigDecimal(
                    valores.get(chave)
                            .trim()
                            .replace(",", ".")
            );
        } catch (Exception e) {
            return padrao;
        }
    }
    
    private void salvarValorOpcional(
            Connection conexao,
            String chave,
            BigDecimal valor
    ) throws Exception {
        String sql = """
                INSERT OR REPLACE INTO configuracao_usuario
                    (chave, valor)
                VALUES (?, ?)
                """;

        try (
                PreparedStatement statement =
                        conexao.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    chave
            );

            /*
             * Como a coluna valor é NOT NULL,
             * usamos texto vazio para representar
             * uma configuração sem valor.
             */
            statement.setString(
                    2,
                    valor == null
                            ? ""
                            : valor.setScale(
                                    2,
                                    java.math.RoundingMode.HALF_UP
                            ).toPlainString()
            );

            statement.executeUpdate();
        }
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
            salvarValor(conexao, "cotacaoDolar", configuracao.getCotacaoDolar());
            salvarValorOpcional(conexao,"fatorImportacaoReposicao",configuracao.getFatorImportacaoReposicao());
            
            for (Map.Entry<String, BigDecimal> entrada
                    : configuracao.getBasesEstados().entrySet()) {

                salvarValor(
                        conexao,
                        "baseEstado." + entrada.getKey(),
                        entrada.getValue()
                );
            }

            conexao.commit();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar configurações do usuário.", e);
        }
    }
    
    private void salvarValor(
            Connection conexao,
            String chave,
            BigDecimal valor
    ) throws Exception {
        String sql = """
                INSERT OR REPLACE INTO configuracao_usuario (chave, valor)
                VALUES (?, ?)
                """;

        try (PreparedStatement statement =
                     conexao.prepareStatement(sql)) {

            statement.setString(1, chave);
            statement.setString(
                    2,
                    valor == null
                            ? "50.00"
                            : valor.setScale(
                                    2,
                                    java.math.RoundingMode.HALF_UP
                            ).toPlainString()
            );

            statement.executeUpdate();
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