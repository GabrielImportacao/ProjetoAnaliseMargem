package Repositorio;

import Modelo.HistoricoSaidaItem;

import java.util.Optional;

public interface ItemNfsRepository {

    Optional<HistoricoSaidaItem> buscarUltimaSaidaPorItem(String codigoItem);
}