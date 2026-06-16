package Repositorio;

import Modelo.CustoItem;

import java.util.List;
import java.util.Optional;

public interface CustoRepository {

    Optional<CustoItem> buscarCustoMaisRecentePorItem(String codigoItem);

    List<CustoItem> listarCustosPorItem(String codigoItem);
}