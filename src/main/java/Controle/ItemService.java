package Controle;

import Modelo.DadosItem;
import Modelo.DadosItemConsulta;

import java.math.BigDecimal;
import java.util.Optional;

public class ItemService {

    private final DadosItemService dadosItemService;

    public ItemService() {
        this.dadosItemService = new DadosItemService();
    }

    public Optional<DadosItem> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<DadosItemConsulta> dadosConsulta =
                dadosItemService.buscarDadosCompletosPorCodigo(codigo);

        if (dadosConsulta.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(converterParaDadosItem(dadosConsulta.get()));
    }

    private DadosItem converterParaDadosItem(DadosItemConsulta dadosConsulta) {
        return new DadosItem(
        		dadosConsulta.getCodigoItem(),
                dadosConsulta.getDescricao(),

                tratarBigDecimal(dadosConsulta.getCustoAtual()),
                tratarBigDecimal(dadosConsulta.getCustoPromob()),
                tratarBigDecimal(dadosConsulta.getCustoAnterior()),

                tratarTexto(dadosConsulta.getRegistroCustoAtual()),
                tratarTexto(dadosConsulta.getRegistroCustoPromob()),
                tratarTexto(dadosConsulta.getRegistroCustoAnterior()),

                dadosConsulta.getDataCustoAtual(),
                dadosConsulta.getDataCustoPromob(),
                dadosConsulta.getDataCustoAnterior(),
                dadosConsulta.getPrecoPadraoVenda()
        );
    }
    
    public Optional<BigDecimal> buscarPrecoPadraoVendaPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }

        return dadosItemService.buscarPrecoPadraoVendaPorCodigo(codigo);
    }

    private BigDecimal tratarBigDecimal(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String tratarTexto(String texto) {
        return texto == null ? "" : texto;
    }
}