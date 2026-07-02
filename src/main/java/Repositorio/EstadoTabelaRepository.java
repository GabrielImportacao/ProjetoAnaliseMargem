package Repositorio;

import Modelo.EstadoLinhaTabela;

import java.util.List;

public interface EstadoTabelaRepository {

    List<EstadoLinhaTabela> carregarLinhas(String tela);

    void salvarLinhas(String tela, List<EstadoLinhaTabela> linhas);
}