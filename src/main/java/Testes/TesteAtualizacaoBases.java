package Testes;

import Controle.ItemService;
import Modelo.DadosItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class TesteAtualizacaoBases {

    public static void main(String[] args) {
        ItemService itemService = new ItemService();

        String[] codigosTeste = {
                "MMR1814.1031",
                "MRT3803.0801",
                "MMP0201.1004",
                "MPR3423.1504"
        };

        System.out.println("==============================================");
        System.out.println("TESTE DE ATUALIZAÇÃO DAS BASES");
        System.out.println("==============================================");
        System.out.println();

        System.out.println("Buscando dados ANTES da atualização...");
        Map<String, SnapshotItem> antes = buscarItens(itemService, codigosTeste);
        imprimirSnapshot("ANTES", antes);

        System.out.println();
        System.out.println("Agora, se quiser testar alteração real, modifique/salve alguma base.");
        System.out.println("Exemplos:");
        System.out.println("- alterar/salvar a planilha BD_METAL_ITEM_AT.xlsb");
        System.out.println("- atualizar custos.db");
        System.out.println("- atualizar movestq.db");
        System.out.println();
        System.out.println("Depois pressione ENTER para chamar itemService.recarregarBases()...");
        new Scanner(System.in).nextLine();

        long inicio = System.currentTimeMillis();

        System.out.println();
        System.out.println("Chamando itemService.recarregarBases()...");
        itemService.recarregarBases();

        System.out.println("Buscando dados DEPOIS da atualização...");
        Map<String, SnapshotItem> depois = buscarItens(itemService, codigosTeste);

        long fim = System.currentTimeMillis();

        imprimirSnapshot("DEPOIS", depois);

        System.out.println();
        imprimirComparativo(antes, depois);

        System.out.println();
        System.out.println("Tempo total do teste: " + (fim - inicio) + " ms");
        System.out.println("Teste finalizado.");
    }

    private static Map<String, SnapshotItem> buscarItens(ItemService itemService, String[] codigos) {
        Map<String, SnapshotItem> resultado = new LinkedHashMap<>();

        for (String codigo : codigos) {
            Optional<DadosItem> dadosEncontrados = itemService.buscarPorCodigo(codigo);

            SnapshotItem snapshot = dadosEncontrados
                    .map(dados -> SnapshotItem.from(codigo, dados))
                    .orElse(SnapshotItem.naoEncontrado(codigo));

            resultado.put(codigo, snapshot);
        }

        return resultado;
    }

    private static void imprimirSnapshot(String titulo, Map<String, SnapshotItem> snapshot) {
        System.out.println();
        System.out.println("========== " + titulo + " ==========");

        for (SnapshotItem item : snapshot.values()) {
            System.out.println();
            System.out.println("Código: " + item.codigoOriginal());
            System.out.println("Encontrado: " + (item.encontrado() ? "SIM" : "NÃO"));

            if (!item.encontrado()) {
                continue;
            }

            System.out.println("Descrição: " + item.descricao());

            System.out.println("Custo atual: " + item.custoAtual());
            System.out.println("Registro atual: " + item.registroCustoAtual());
            System.out.println("Data atual: " + item.dataCustoAtual());

            System.out.println("Custo Promob: " + item.custoPromob());
            System.out.println("Registro Promob: " + item.registroCustoPromob());
            System.out.println("Data Promob: " + item.dataCustoPromob());

            System.out.println("Custo anterior: " + item.custoAnterior());
            System.out.println("Registro anterior: " + item.registroCustoAnterior());
            System.out.println("Data anterior: " + item.dataCustoAnterior());
        }
    }

    private static void imprimirComparativo(
            Map<String, SnapshotItem> antes,
            Map<String, SnapshotItem> depois
    ) {
        System.out.println("========== COMPARATIVO ==========");

        for (String codigo : antes.keySet()) {
            SnapshotItem itemAntes = antes.get(codigo);
            SnapshotItem itemDepois = depois.get(codigo);

            boolean mudou = !itemAntes.equals(itemDepois);

            System.out.println();
            System.out.println("Código: " + codigo);
            System.out.println("Mudou após recarregar? " + (mudou ? "SIM" : "NÃO"));

            if (mudou) {
                System.out.println("ANTES : " + itemAntes.resumoCurto());
                System.out.println("DEPOIS: " + itemDepois.resumoCurto());
            }
        }
    }

    private record SnapshotItem(
            String codigoOriginal,
            boolean encontrado,
            String codigo,
            String descricao,

            BigDecimal custoAtual,
            String registroCustoAtual,
            LocalDate dataCustoAtual,

            BigDecimal custoPromob,
            String registroCustoPromob,
            LocalDate dataCustoPromob,

            BigDecimal custoAnterior,
            String registroCustoAnterior,
            LocalDate dataCustoAnterior
    ) {
        static SnapshotItem from(String codigoOriginal, DadosItem dados) {
            return new SnapshotItem(
                    codigoOriginal,
                    true,
                    dados.getCodigo(),
                    dados.getDescricao(),

                    dados.getCustoAtual(),
                    dados.getRegistroCustoAtual(),
                    dados.getDataCustoAtual(),

                    dados.getCustoPromob(),
                    dados.getRegistroCustoPromob(),
                    dados.getDataCustoPromob(),

                    dados.getCustoAnterior(),
                    dados.getRegistroCustoAnterior(),
                    dados.getDataCustoAnterior()
            );
        }

        static SnapshotItem naoEncontrado(String codigoOriginal) {
            return new SnapshotItem(
                    codigoOriginal,
                    false,
                    "",
                    "",
                    BigDecimal.ZERO,
                    "",
                    null,
                    BigDecimal.ZERO,
                    "",
                    null,
                    BigDecimal.ZERO,
                    "",
                    null
            );
        }

        String resumoCurto() {
            if (!encontrado) {
                return "Item não encontrado";
            }

            return "atual=" + custoAtual +
                    " [" + registroCustoAtual + " | " + dataCustoAtual + "]" +
                    ", promob=" + custoPromob +
                    " [" + registroCustoPromob + " | " + dataCustoPromob + "]" +
                    ", anterior=" + custoAnterior +
                    " [" + registroCustoAnterior + " | " + dataCustoAnterior + "]";
        }
    }
}