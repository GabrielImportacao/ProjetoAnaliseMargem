package Controle;

import Modelo.LayoutColunaTabela;
import Repositorio.LayoutTabelaRepository;
import Repositorio.LayoutTabelaRepositorySqlite;

import java.util.List;

public class LayoutTabelaService {

    private static final String TELA_INICIAL = "tela_inicial";

    private final LayoutTabelaRepository repository;

    public LayoutTabelaService() {
        this.repository = new LayoutTabelaRepositorySqlite();
    }

    public List<LayoutColunaTabela> carregarLayoutColunasTabelaInicial() {
        return repository.carregarLayoutColunas(TELA_INICIAL);
    }

    public void salvarLayoutColunasTabelaInicial(List<LayoutColunaTabela> layout) {
        repository.salvarLayoutColunas(TELA_INICIAL, layout);
    }
}