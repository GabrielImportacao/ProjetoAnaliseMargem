package Controle;

import Modelo.EstadoInfo;
import Modelo.ItemAnalise;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AnaliseService {
    private static final BigDecimal CEM = new BigDecimal("100");

    public void recalcular(ItemAnalise item) {
        BigDecimal valorUnitario = valorSeguro(item.getValorUnitario());
        BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());

        item.setValorTotal(valorUnitario.multiply(quantidade).setScale(2, RoundingMode.HALF_UP));

        item.setMargemAtual(calcularMargem(valorUnitario, item.getCustoAtual()));
        item.setMargemPromob(calcularMargem(valorUnitario, item.getCustoPromob()));
        item.setMargemAnterior(calcularMargem(valorUnitario, item.getCustoAnterior()));

        item.setVariacaoAtual(calcularVariacao(valorUnitario, item.getCustoAtual()));
        item.setVariacaoAnterior(calcularVariacao(valorUnitario, item.getCustoAnterior()));
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

    private BigDecimal calcularMargem(BigDecimal valorUnitario, BigDecimal custo) {
        valorUnitario = valorSeguro(valorUnitario);
        custo = valorSeguro(custo);

        if (valorUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return valorUnitario.subtract(custo)
                .divide(valorUnitario, 8, RoundingMode.HALF_UP)
                .multiply(CEM)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularVariacao(BigDecimal valorUnitario, BigDecimal custo) {
        valorUnitario = valorSeguro(valorUnitario);
        custo = valorSeguro(custo);

        if (custo.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return valorUnitario.subtract(custo)
                .divide(custo, 8, RoundingMode.HALF_UP)
                .multiply(CEM)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
