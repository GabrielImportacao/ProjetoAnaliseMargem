package Testes;

import Controle.ItemService;
import Modelo.DadosItem;

public class TestePreCarregamentoCompleto {

    public static void main(String[] args) {
        ItemService itemService = new ItemService();

        System.out.println("Pré-carregando bases...");
        long inicioPreCarga = System.currentTimeMillis();

        itemService.preCarregarBases();

        long fimPreCarga = System.currentTimeMillis();
        System.out.println("Pré-carregamento concluído em " + (fimPreCarga - inicioPreCarga) + " ms.");

        testarBusca(itemService, "MMR1814.1031");
        testarBusca(itemService, "MRT3803.0801");
        testarBusca(itemService, "MMP0201.1004");
        testarBusca(itemService, "MPR3423.1504");
    }

    private static void testarBusca(ItemService itemService, String codigo) {
        long inicio = System.currentTimeMillis();

        DadosItem item = itemService.buscarPorCodigo(codigo).orElse(null);

        long fim = System.currentTimeMillis();

        System.out.println();
        System.out.println("Item: " + codigo);
        System.out.println("Tempo da busca: " + (fim - inicio) + " ms");

        if (item == null) {
            System.out.println("Não encontrado.");
            return;
        }

        System.out.println("Descrição: " + item.getDescricao());
        System.out.println("Custo atual: " + item.getCustoAtual());
        System.out.println("Custo Promob: " + item.getCustoPromob());
        System.out.println("Custo anterior: " + item.getCustoAnterior());
    }
}