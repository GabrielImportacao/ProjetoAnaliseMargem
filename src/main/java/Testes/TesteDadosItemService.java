package Testes;

import Controle.DadosItemService;
import Modelo.DadosItemConsulta;

import java.util.Optional;

public class TesteDadosItemService {

    public static void main(String[] args) {
        DadosItemService service = new DadosItemService();

        testarItem(service, "MMR1814.1031");
        System.out.println();
        testarItem(service, "MRT3803.0801");
        System.out.println();
        testarItem(service, "MMP0201.1004");
    }

    private static void testarItem(DadosItemService service, String codigoItem) {
        System.out.println("Buscando dados completos do item: " + codigoItem);

        Optional<DadosItemConsulta> dadosEncontrados =
                service.buscarDadosCompletosPorCodigo(codigoItem);

        if (dadosEncontrados.isEmpty()) {
            System.out.println("Item não encontrado.");
            return;
        }

        DadosItemConsulta dados = dadosEncontrados.get();

        System.out.println("Item encontrado:");
        System.out.println(dados);

        System.out.println("Resumo:");
        System.out.println("Descrição: " + dados.getDescricao());
        System.out.println("IPI: " + dados.getIpi());
        System.out.println("NCM: " + dados.getNcm());
        System.out.println("Preço líquido atual: " + dados.getPrecoUnitarioLiquidoAtual());

        System.out.println("Custo atual: " + dados.getCustoAtual());
        System.out.println("Registro custo atual: " + dados.getRegistroCustoAtual());
        System.out.println("Data custo atual: " + dados.getDataCustoAtual());
        System.out.println("Fonte custo atual: " + dados.getFonteCustoAtual().getDescricao());

        System.out.println("Custo Promob: " + dados.getCustoPromob());
        System.out.println("Registro custo Promob: " + dados.getRegistroCustoPromob());
        System.out.println("Data custo Promob: " + dados.getDataCustoPromob());
        System.out.println("Fonte custo Promob: " + dados.getFonteCustoPromob().getDescricao());

        System.out.println("Custo anterior: " + dados.getCustoAnterior());
        System.out.println("Registro custo anterior: " + dados.getRegistroCustoAnterior());
        System.out.println("Data custo anterior: " + dados.getDataCustoAnterior());
        System.out.println("Fonte custo anterior: " + dados.getFonteCustoAnterior().getDescricao());
    }
}