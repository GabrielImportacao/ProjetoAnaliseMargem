package Testes;

import Configuracao.CaminhosBase;
import Infraestrutura.ConexaoMovestqSqlite;

import java.util.List;
import java.util.Map;

public class TesteConexaoMovestq {

    public static void main(String[] args) {
        testarBancoMovestq();
    }

    private static void testarBancoMovestq() {
        System.out.println("Testando conexão com movestq.db...");
        System.out.println("Caminho: " + CaminhosBase.CAMINHO_MOVESTQ_DB);

        try {
            ConexaoMovestqSqlite conexao = new ConexaoMovestqSqlite();

            System.out.println("Conexão com movestq.db OK.");
            System.out.println();

            List<String> tabelas = conexao.listarTabelas();

            System.out.println("Tabelas encontradas:");
            for (String tabela : tabelas) {
                System.out.println("- " + tabela);
            }

            System.out.println();
            System.out.println("Estrutura das tabelas:");

            for (String tabela : tabelas) {
                System.out.println();
                System.out.println("Tabela: " + tabela);

                for (String coluna : conexao.listarColunas(tabela)) {
                    System.out.println("  - " + coluna);
                }
            }

            System.out.println();
            System.out.println("Amostra das tabelas:");

            for (String tabela : tabelas) {
                System.out.println();
                System.out.println("Tabela: " + tabela);

                List<Map<String, Object>> linhas = conexao.listarPrimeirasLinhas(tabela, 5);

                if (linhas.isEmpty()) {
                    System.out.println("  Nenhuma linha encontrada.");
                    continue;
                }

                for (Map<String, Object> linha : linhas) {
                    System.out.println(linha);
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao conectar no movestq.db:");
            e.printStackTrace();
        }
    }
}