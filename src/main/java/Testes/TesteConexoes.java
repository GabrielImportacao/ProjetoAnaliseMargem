package Testes;

import Configuracao.CaminhosBase;
import Infraestrutura.ConexaoPlanilhaXlsb;
import Infraestrutura.ConexaoSqlite;

import java.util.List;
import java.util.Map;

public class TesteConexoes {

    public static void main(String[] args) {
        testarBancoCustos();
        System.out.println();
        testarPlanilhaItens();
    }

    private static void testarBancoCustos() {
        System.out.println("Testando conexão com custos.db...");
        System.out.println("Caminho: " + CaminhosBase.CAMINHO_CUSTOS_DB);

        try {
            ConexaoSqlite conexaoSqlite = new ConexaoSqlite();

            System.out.println("Conexão com custos.db OK.");
            System.out.println();

            List<String> tabelas = conexaoSqlite.listarTabelas();

            System.out.println("Tabelas encontradas:");
            for (String tabela : tabelas) {
                System.out.println("- " + tabela);
            }

            System.out.println();
            System.out.println("Estrutura das tabelas:");

            for (String tabela : tabelas) {
                System.out.println();
                System.out.println("Tabela: " + tabela);

                for (String coluna : conexaoSqlite.listarColunas(tabela)) {
                    System.out.println("  - " + coluna);
                }
            }

            System.out.println();
            System.out.println("Amostra da tabela custos:");

            List<Map<String, Object>> linhasCustos = conexaoSqlite.listarPrimeirasLinhas("custos", 5);

            for (Map<String, Object> linha : linhasCustos) {
                System.out.println(linha);
            }

        } catch (Exception e) {
            System.out.println("Erro ao conectar no custos.db:");
            e.printStackTrace();
        }
    }

    private static void testarPlanilhaItens() {
        System.out.println("Testando conexão com BD_METAL_ITEM_AT.xlsb...");
        System.out.println("Caminho: " + CaminhosBase.CAMINHO_BD_METAL_ITEM);

        try {
            ConexaoPlanilhaXlsb conexaoPlanilha = new ConexaoPlanilhaXlsb();

            System.out.println("Abertura da planilha OK.");
            System.out.println("Abas encontradas:");

            for (String aba : conexaoPlanilha.listarAbas()) {
                System.out.println("- " + aba);
            }

            System.out.println();
            System.out.println("Primeiras linhas da aba Banco_Itens:");

            List<List<String>> linhas = conexaoPlanilha.listarPrimeirasLinhas("Banco_Itens", 10);

            for (int i = 0; i < linhas.size(); i++) {
                System.out.println("Linha " + (i + 1) + ": " + linhas.get(i));
            }

        } catch (Exception e) {
            System.out.println("Erro ao abrir a planilha BD_METAL_ITEM_AT.xlsb:");
            e.printStackTrace();
        }
    }
}