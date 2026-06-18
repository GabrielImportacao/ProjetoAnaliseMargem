package Controle;

import Modelo.ItemAnalise;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AnaliseService {

	private BigDecimal calcularPrecoComDescontoEstado(BigDecimal precoBase, BigDecimal percentualDesconto) {
	    BigDecimal precoSeguro = valorSeguro(precoBase);
	    BigDecimal descontoSeguro = valorSeguro(percentualDesconto);

	    if (precoSeguro.compareTo(BigDecimal.ZERO) <= 0) {
	        return BigDecimal.ZERO;
	    }

	    if (descontoSeguro.compareTo(BigDecimal.ZERO) <= 0) {
	        return precoSeguro;
	    }

	    BigDecimal fator = BigDecimal.ONE.subtract(
	            descontoSeguro.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
	    );

	    return precoSeguro.multiply(fator).setScale(4, RoundingMode.HALF_UP);
	}
	
    public void recalcular(ItemAnalise item) {
    recalcular(item, BigDecimal.ZERO);
}

public void recalcular(ItemAnalise item, BigDecimal percentualBaseEstado) {
    if (item == null) {
        return;
    }

    BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());
    BigDecimal valorUnitario = valorSeguro(item.getValorUnitario());

    BigDecimal valorTotal = valorUnitario.multiply(quantidade);
    item.setValorTotal(valorTotal);

    BigDecimal precoComparacaoAtual = calcularPrecoComDescontoEstado(
            item.getPrecoPadraoVenda(),
            percentualBaseEstado
    );

    item.setVariacaoAtual(calcularVariacao(valorUnitario, precoComparacaoAtual));
    item.setVariacaoAnterior(calcularVariacao(valorUnitario, precoComparacaoAtual));

    item.setMargemAtual(calcularMargem(item.getCustoAtual(), valorUnitario));
    item.setMargemPromob(calcularMargem(item.getCustoPromob(), valorUnitario));
    item.setMargemAnterior(calcularMargem(item.getCustoAnterior(), valorUnitario));

    item.setIpiProposta(calcularIpi(valorTotal, item.getPercentualIpi()));

    BigDecimal totalAtual = precoComparacaoAtual.multiply(quantidade);
    item.setIpiAtual(calcularIpi(totalAtual, item.getPercentualIpi()));

    BigDecimal totalAnterior = precoComparacaoAtual.multiply(quantidade);
    item.setIpiAnterior(calcularIpi(totalAnterior, item.getPercentualIpi()));
}

    private BigDecimal calcularMargem(BigDecimal custo, BigDecimal valorUnitario) {
        BigDecimal custoSeguro = valorSeguro(custo);
        BigDecimal valorSeguro = valorSeguro(valorUnitario);

        if (custoSeguro.compareTo(BigDecimal.ZERO) <= 0 || valorSeguro.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return custoSeguro
                .divide(valorSeguro, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularVariacao(BigDecimal valorUnitario, BigDecimal precoReferencia) {
        BigDecimal valorSeguro = valorSeguro(valorUnitario);
        BigDecimal referenciaSeguro = valorSeguro(precoReferencia);

        if (valorSeguro.compareTo(BigDecimal.ZERO) <= 0 || referenciaSeguro.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return valorSeguro
                .divide(referenciaSeguro, 6, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularIpi(BigDecimal valorBase, BigDecimal percentualIpi) {
        BigDecimal valorSeguro = valorSeguro(valorBase);
        BigDecimal ipiSeguro = valorSeguro(percentualIpi);

        if (valorSeguro.compareTo(BigDecimal.ZERO) <= 0 || ipiSeguro.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return valorSeguro
                .multiply(ipiSeguro)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}