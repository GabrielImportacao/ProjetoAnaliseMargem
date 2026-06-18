package Testes;

import Modelo.CustoPromobItem;
import Repositorio.CustoPromobRepository;
import Repositorio.CustoPromobRepositorySqlite;

import java.util.Optional;

public class TesteCustoPromobRepository {

    public static void main(String[] args) {
        CustoPromobRepository repository = new CustoPromobRepositorySqlite();

        testarPromob(repository, "MRT3803.0801");
        System.out.println();
        testarPromob(repository, "MMR1814.1031");
        System.out.println();
        testarPromob(repository, "MMP0201.1004");
    }

    private static void testarPromob(CustoPromobRepository repository, String codigoItem) {
        System.out.println("Buscando custo Promob mais recente do item: " + codigoItem);

        Optional<CustoPromobItem> custoEncontrado =
                repository.buscarCustoMaisRecentePorItem(codigoItem);

        if (custoEncontrado.isPresent()) {
            System.out.println("Custo Promob encontrado:");
            System.out.println(custoEncontrado.get());
        } else {
            System.out.println("Custo Promob não encontrado.");
        }
    }
}