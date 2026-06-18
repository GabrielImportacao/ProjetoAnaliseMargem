package Infraestrutura;

import Configuracao.CaminhosBase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class ConexaoSqlite {

    public Connection abrir() throws SQLException, IOException {
        Path caminhoDb = CaminhosBase.CAMINHO_CUSTOS_DB;

        if (!Files.isRegularFile(caminhoDb)) {
            throw new FileNotFoundException("Banco de custos não encontrado em: " + caminhoDb);
        }

        String caminhoFormatado = caminhoDb.toAbsolutePath().toString().replace("\\", "/");
        String url = "jdbc:sqlite:" + caminhoFormatado;

        Connection conexao = DriverManager.getConnection(url);

        try (Statement statement = conexao.createStatement()) {
            statement.execute("PRAGMA query_only = ON");
        }

        return conexao;
    }

    public List<String> listarTabelas() throws SQLException, IOException {
        List<String> tabelas = new ArrayList<>();

        String sql = """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                ORDER BY name
                """;

        try (
                Connection conexao = abrir();
                PreparedStatement statement = conexao.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                tabelas.add(resultSet.getString("name"));
            }
        }

        return tabelas;
    }

    public List<String> listarColunas(String nomeTabela) throws SQLException, IOException {
        List<String> colunas = new ArrayList<>();

        String sql = "PRAGMA table_info(" + nomeTabela + ")";

        try (
                Connection conexao = abrir();
                PreparedStatement statement = conexao.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                String nomeColuna = resultSet.getString("name");
                String tipoColuna = resultSet.getString("type");

                colunas.add(nomeColuna + " | " + tipoColuna);
            }
        }

        return colunas;
    }

    public List<Map<String, Object>> listarPrimeirasLinhas(String nomeTabela, int limite) throws SQLException, IOException {
        List<Map<String, Object>> linhas = new ArrayList<>();

        String sql = "SELECT * FROM " + nomeTabela + " LIMIT ?";

        try (
                Connection conexao = abrir();
                PreparedStatement statement = conexao.prepareStatement(sql)
        ) {
            statement.setInt(1, limite);

            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int quantidadeColunas = metaData.getColumnCount();

                while (resultSet.next()) {
                    Map<String, Object> linha = new LinkedHashMap<>();

                    for (int i = 1; i <= quantidadeColunas; i++) {
                        String nomeColuna = metaData.getColumnName(i);
                        Object valor = resultSet.getObject(i);

                        linha.put(nomeColuna, valor);
                    }

                    linhas.add(linha);
                }
            }
        }

        return linhas;
    }
}