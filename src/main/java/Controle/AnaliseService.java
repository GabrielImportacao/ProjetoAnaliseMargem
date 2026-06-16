package Controle;

import Modelo.EstadoInfo;
import Modelo.ItemAnalise;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AnaliseService {
    private static final BigDecimal CEM = new BigDecimal("100");

    public void recalcular(ItemAnalise item) {
        if (item == null) {
            return;
        }

        BigDecimal valorUnitario = valorSeguro(item.getValorUnitario());
        BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());

        item.setValorTotal(valorUnitario.multiply(quantidade));

        item.setVariacaoAtual(
                calcularVariacao(valorUnitario, item.getPrecoPadraoVenda())
        );

        item.setMargemAtual(
                calcularMargem(item.getCustoAtual(), valorUnitario)
        );

        item.setMargemPromob(
                calcularMargem(item.getCustoPromob(), valorUnitario)
        );

        item.setVariacaoAnterior(
                calcularVariacao(valorUnitario, item.getPrecoPadraoVenda())
        );

        item.setMargemAnterior(
                calcularMargem(item.getCustoAnterior(), valorUnitario)
        );
    }

    public BigDecimal calcularValorPadrao(ItemAnalise item, EstadoInfo estadoInfo) {
        BigDecimal custo = valorSeguro(item.getCustoAtual());
        BigDecimal margemDesejada = estadoInfo == null ? BigDecimal.ZERO : valorSeguro(estadoInfo.getBaseMargem());

        if (custo.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal divisor = BigDecimal.ONE.subtract(margemDesejada.divide(CEM, 8, RoundingMode.HALF_UP));
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return custo.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularVariacao(BigDecimal propostaUnitaria, BigDecimal precoPadraoVenda) {
        propostaUnitaria = valorSeguro(propostaUnitaria);
        precoPadraoVenda = valorSeguro(precoPadraoVenda);

        if (propostaUnitaria.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        if (precoPadraoVenda.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return propostaUnitaria
                .divide(precoPadraoVenda, 8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal calcularMargem(BigDecimal custoUnitario, BigDecimal propostaUnitaria) {
        custoUnitario = valorSeguro(custoUnitario);
        propostaUnitaria = valorSeguro(propostaUnitaria);

        if (propostaUnitaria.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        if (custoUnitario.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return custoUnitario
                .divide(propostaUnitaria, 8, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
