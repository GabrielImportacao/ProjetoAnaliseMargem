package Controle;

import Modelo.CustoVerdadeiroItem;
import Repositorio.CustoVerdadeiroRepository;
import Repositorio.CustoVerdadeiroRepositorySqlite;

import java.util.Optional;

public class CustoVerdadeiroService {

    private final CustoVerdadeiroRepository repository;

    public CustoVerdadeiroService() {
        this.repository = new CustoVerdadeiroRepositorySqlite();
    }

    public Optional<CustoVerdadeiroItem> buscarPorItem(String codigoItem) {
        return repository.buscarCustoVerdadeiroPorItem(codigoItem);
    }

    public void preCarregarCache() {
        if (repository instanceof CustoVerdadeiroRepositorySqlite repositorySqlite) {
            repositorySqlite.preCarregarCache();
        }
    }

    public void limparCache() {
        if (repository instanceof CustoVerdadeiroRepositorySqlite repositorySqlite) {
            repositorySqlite.limparCache();
        }
    }
}