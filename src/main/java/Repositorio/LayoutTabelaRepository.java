package Repositorio;

import Modelo.LayoutColunaTabela;

import java.util.List;

public interface LayoutTabelaRepository {

    List<LayoutColunaTabela> carregarLayoutColunas(String tela);

    void salvarLayoutColunas(String tela, List<LayoutColunaTabela> layout);
}