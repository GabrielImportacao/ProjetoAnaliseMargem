package Testes;

import Modelo.CustoItem;
import Repositorio.CustoRepository;
import Repositorio.CustoRepositorySqlite;

import java.util.List;
import java.util.Optional;

public class TesteCustoRepository {

    public static void main(String[] args) {
        CustoRepository repository = new CustoRepositorySqlite();

        String codigoTeste = "MMR1814.1031";

        testarCustoMaisRecente(repository, codigoTeste);
        System.out.println();
        testarHistoricoCustos(repository, codigoTeste);
    }
    
    private static void testarFatorCustoFechado(
            CustoRepository repository,
            String codigoItem
    ) {
        System.out.println(
                "Buscando fator do custo fechado "
                        + "mais recente: "
                        + codigoItem
        );

        Optional<CustoItem> encontrado =
                repository
                        .buscarCustoFechadoMaisRecenteComFatorPorItem(
                                codigoItem
                        );

        if (encontrado.isEmpty()) {
            System.out.println(
                    "Nenhum fator encontrado."
            );

            return;
        }

        CustoItem custoItem =
                encontrado.get();

        System.out.println(
                "Processo do fator: "
                        + custoItem
                                .getRegistroImportacao()
        );

        System.out.println(
                "Fator Líquido BRL: "
                        + custoItem
                                .getFatorLiquidoBrl()
        );
    }

    private static void testarCustoMaisRecente(CustoRepository repository, String codigoItem) {
        System.out.println("Buscando custo mais recente do item: " + codigoItem);

        Optional<CustoItem> custoEncontrado = repository.buscarCustoMaisRecentePorItem(codigoItem);

        if (custoEncontrado.isPresent()) {
            System.out.println("Custo mais recente encontrado:");
            System.out.println(custoEncontrado.get());
        } else {
            System.out.println("Nenhum custo encontrado para o item.");
        }
    }

    private static void testarHistoricoCustos(CustoRepository repository, String codigoItem) {
        System.out.println("Listando histórico de custos do item: " + codigoItem);

        List<CustoItem> custos = repository.listarCustosPorItem(codigoItem);

        if (custos.isEmpty()) {
            System.out.println("Nenhum custo encontrado.");
            return;
        }

        for (CustoItem custo : custos) {
            System.out.println(custo);
        }
    }
}