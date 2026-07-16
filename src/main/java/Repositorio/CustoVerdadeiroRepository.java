package Repositorio;

import Modelo.CustoVerdadeiroItem;

import java.util.Optional;

public interface CustoVerdadeiroRepository {

    Optional<CustoVerdadeiroItem> buscarCustoVerdadeiroPorItem(String codigoItem);
}