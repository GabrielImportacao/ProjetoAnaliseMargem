package Testes;

import Controle.ItemService;
import Modelo.DadosItem;

public class TesteIpiItem {

    public static void main(String[] args) {
        ItemService itemService = new ItemService();

        String codigo = "MMR1501.1108";

        DadosItem item = itemService.buscarPorCodigo(codigo).orElse(null);

        if (item == null) {
            System.out.println("Item não encontrado.");
            return;
        }

        System.out.println("Código: " + item.getCodigo());
        System.out.println("Descrição: " + item.getDescricao());
        System.out.println("IPI: " + item.getIpi());
        System.out.println("Preço padrão venda: " + item.getPrecoPadraoVenda());
        System.out.println("Custo atual: " + item.getCustoAtual());
        System.out.println("Custo Promob: " + item.getCustoPromob());
        System.out.println("Custo anterior: " + item.getCustoAnterior());
    }
}