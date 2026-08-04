package Repositorio;

import Modelo.CustoItem;
import Modelo.HistoricoCustoItem;

import java.util.List;
import java.util.Optional;

public interface CustoRepository {

    Optional<CustoItem>
    buscarCustoMaisRecentePorItem(
            String codigoItem
    );

    Optional<CustoItem>
    buscarProcessoReposicaoMaisRecentePorItem(
            String codigoItem
    );

    List<CustoItem> listarCustosPorItem(
            String codigoItem
    );

    HistoricoCustoItem
    buscarHistoricoPrincipalPorItem(
            String codigoItem
    );
    
    Optional<CustoItem>
    buscarCustoFechadoMaisRecenteComFatorPorItem(
            String codigoItem
    );
}