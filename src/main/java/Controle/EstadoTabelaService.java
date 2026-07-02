package Controle;

import Modelo.CondicaoVenda;
import Modelo.EstadoLinhaTabela;
import Modelo.ItemAnalise;
import Repositorio.EstadoTabelaRepository;
import Repositorio.EstadoTabelaRepositorySqlite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EstadoTabelaService {

    private static final String TELA_INICIAL = "tela_inicial";

    private final EstadoTabelaRepository repository;

    public EstadoTabelaService() {
        this.repository = new EstadoTabelaRepositorySqlite();
    }

    public List<ItemAnalise> carregarLinhasTabelaInicial() {
        List<EstadoLinhaTabela> linhasPersistidas = repository.carregarLinhas(TELA_INICIAL);
        List<ItemAnalise> itens = new ArrayList<>();

        for (EstadoLinhaTabela linhaPersistida : linhasPersistidas) {
            itens.add(converterParaItemAnalise(linhaPersistida));
        }

        return itens;
    }

    public void salvarLinhasTabelaInicial(List<ItemAnalise> itens) {
        List<EstadoLinhaTabela> linhas = new ArrayList<>();

        if (itens != null) {
            for (ItemAnalise item : itens) {
                if (item == null || !linhaDeveSerPersistida(item)) {
                    continue;
                }

                linhas.add(converterParaEstadoLinhaTabela(linhas.size(), item));
            }
        }

        repository.salvarLinhas(TELA_INICIAL, linhas);
    }

    private boolean linhaDeveSerPersistida(ItemAnalise item) {
        return temTexto(item.getCodigo())
                || temTexto(item.getDescricao())
                || item.getQuantidade() > 0
                || maiorQueZero(item.getValorUnitario())
                || item.getCondicaoVenda() != null && item.getCondicaoVenda() != CondicaoVenda.NORMAL;
    }

    private EstadoLinhaTabela converterParaEstadoLinhaTabela(int ordem, ItemAnalise item) {
        return new EstadoLinhaTabela(
                ordem,
                item.getCodigo(),
                item.getDescricao(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getValorTotal(),
                item.getCondicaoVenda() == null ? CondicaoVenda.NORMAL.name() : item.getCondicaoVenda().name(),
                item.getCorEspecialFundo(),
                item.getCorEspecialTexto(),
                item.getVariacaoAtual(),
                item.getMargemAtual(),
                item.getCustoAtual(),
                item.getRegistroCustoAtual(),
                item.getDataCustoAtual(),
                item.getMargemPromob(),
                item.getCustoPromob(),
                item.getRegistroCustoPromob(),
                item.getDataCustoPromob(),
                item.getVariacaoAnterior(),
                item.getMargemAnterior(),
                item.getCustoAnterior(),
                item.getRegistroCustoAnterior(),
                item.getDataCustoAnterior(),
                item.getPrecoPadraoVenda(),
                item.getPercentualIpi(),
                item.getDataUltimaSaida(),
                item.isItemEncalhado(),
                item.isItemEncalhadoConfirmado()
        );
    }

    private ItemAnalise converterParaItemAnalise(EstadoLinhaTabela linha) {
        ItemAnalise item = new ItemAnalise();

        item.setCodigo(linha.getCodigo());
        item.setDescricao(linha.getDescricao());
        item.setQuantidade(linha.getQuantidade());

        item.setValorUnitario(valor(linha.getValorUnitario()));
        item.setValorTotal(valor(linha.getValorTotal()));

        item.setCondicaoVenda(converterCondicaoVenda(linha.getCondicaoVenda()));
        item.setCorEspecialFundo(linha.getCorEspecialFundo());
        item.setCorEspecialTexto(linha.getCorEspecialTexto());

        item.setVariacaoAtual(valor(linha.getVariacaoAtual()));
        item.setMargemAtual(valor(linha.getMargemAtual()));
        item.setCustoAtual(valor(linha.getCustoAtual()));
        item.setRegistroCustoAtual(linha.getRegistroCustoAtual());
        item.setDataCustoAtual(linha.getDataCustoAtual());

        item.setMargemPromob(valor(linha.getMargemPromob()));
        item.setCustoPromob(valor(linha.getCustoPromob()));
        item.setRegistroCustoPromob(linha.getRegistroCustoPromob());
        item.setDataCustoPromob(linha.getDataCustoPromob());

        item.setVariacaoAnterior(valor(linha.getVariacaoAnterior()));
        item.setMargemAnterior(valor(linha.getMargemAnterior()));
        item.setCustoAnterior(valor(linha.getCustoAnterior()));
        item.setRegistroCustoAnterior(linha.getRegistroCustoAnterior());
        item.setDataCustoAnterior(linha.getDataCustoAnterior());

        item.setPrecoPadraoVenda(valor(linha.getPrecoPadraoVenda()));
        item.setPercentualIpi(valor(linha.getPercentualIpi()));
        
        item.setDataUltimaSaida(linha.getDataUltimaSaida());
        item.setItemEncalhado(linha.isItemEncalhado());
        
        item.setItemEncalhadoConfirmado(linha.isItemEncalhadoConfirmado());

        return item;
    }

    private CondicaoVenda converterCondicaoVenda(String texto) {
        if (texto == null || texto.isBlank()) {
            return CondicaoVenda.NORMAL;
        }

        try {
            return CondicaoVenda.valueOf(texto.trim());
        } catch (Exception e) {
            return CondicaoVenda.NORMAL;
        }
    }

    private BigDecimal valor(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private boolean maiorQueZero(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}