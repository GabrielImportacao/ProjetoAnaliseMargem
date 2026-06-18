package Testes;

import Controle.ItemService;
import Modelo.DadosItem;

import java.util.Optional;

public class TesteItemServiceReal {

    public static void main(String[] args) {
        ItemService itemService = new ItemService();

        testarItem(itemService, "MMR1814.1031");
        System.out.println();
        testarItem(itemService, "MRT2401.1412");
        System.out.println();
        testarItem(itemService, "MMP0201.1004");
    }

    private static void testarItem(ItemService itemService, String codigoItem) {
        System.out.println("Buscando item pelo ItemService: " + codigoItem);

        Optional<DadosItem> itemEncontrado = itemService.buscarPorCodigo(codigoItem);

        if (itemEncontrado.isEmpty()) {
            System.out.println("Item não encontrado.");
            return;
        }

        DadosItem item = itemEncontrado.get();

        System.out.println("Código: " + item.getCodigo());
        System.out.println("Descrição: " + item.getDescricao());

        System.out.println("Custo atual: " + item.getCustoAtual());
        System.out.println("Registro custo atual: " + item.getRegistroCustoAtual());
        System.out.println("Data custo atual: " + item.getDataCustoAtual());

        System.out.println("Custo Promob: " + item.getCustoPromob());
        System.out.println("Registro custo Promob: " + item.getRegistroCustoPromob());

        System.out.println("Custo anterior: " + item.getCustoAnterior());
        System.out.println("Registro custo anterior: " + item.getRegistroCustoAnterior());
    }
}