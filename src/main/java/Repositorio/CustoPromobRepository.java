package Repositorio;

import Modelo.CustoPromobItem;

import java.util.Optional;

public interface CustoPromobRepository {

    Optional<CustoPromobItem> buscarCustoMaisRecentePorItem(String codigoItem);
}