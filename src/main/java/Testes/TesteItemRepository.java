package Testes;

import Modelo.ItemCadastro;
import Repositorio.ItemRepository;
import Repositorio.ItemRepositoryXlsb;

import java.util.Optional;

public class TesteItemRepository {

    public static void main(String[] args) {
        ItemRepository repository = new ItemRepositoryXlsb();

        String codigoTeste = "MMP0201.1004";

        System.out.println("Buscando item na planilha: " + codigoTeste);

        Optional<ItemCadastro> itemEncontrado = repository.buscarPorCodigo(codigoTeste);

        if (itemEncontrado.isPresent()) {
            System.out.println("Item encontrado:");
            System.out.println(itemEncontrado.get());
        } else {
            System.out.println("Item não encontrado.");
        }
    }
}