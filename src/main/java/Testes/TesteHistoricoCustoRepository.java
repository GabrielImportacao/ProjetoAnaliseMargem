package Testes;

import Modelo.HistoricoCustoItem;
import Repositorio.CustoRepository;
import Repositorio.CustoRepositorySqlite;

public class TesteHistoricoCustoRepository {

    public static void main(String[] args) {
        CustoRepository repository = new CustoRepositorySqlite();

        testarHistorico(repository, "MMR1814.1031");
        System.out.println();
        testarHistorico(repository, "MMP0201.1004");
    }

    private static void testarHistorico(CustoRepository repository, String codigoItem) {
        System.out.println("Buscando histórico principal do item: " + codigoItem);

        HistoricoCustoItem historico = repository.buscarHistoricoPrincipalPorItem(codigoItem);

        if (historico.possuiCustoAtual()) {
            System.out.println("Custo atual:");
            System.out.println(historico.getCustoAtual().get());
        } else {
            System.out.println("Custo atual: não encontrado no banco");
        }

        if (historico.possuiCustoAnterior()) {
            System.out.println("Custo anterior:");
            System.out.println(historico.getCustoAnterior().get());
        } else {
            System.out.println("Custo anterior: não encontrado no banco");
        }
    }
}