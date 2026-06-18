package Repositorio;

import Modelo.ItemCadastro;

import java.util.Optional;

public interface ItemRepository {

    Optional<ItemCadastro> buscarPorCodigo(String codigoItem);
}