package Modelo;

import java.math.BigDecimal;

public class LoteEstoqueItem {

    private final String codigoItem;
    private final String deposito;
    private final String loteExterno;
    private final BigDecimal quantidade;

    public LoteEstoqueItem(
            String codigoItem,
            String deposito,
            String loteExterno,
            BigDecimal quantidade
    ) {
        this.codigoItem = codigoItem;
        this.deposito = deposito;
        this.loteExterno = loteExterno;
        this.quantidade = quantidade;
    }

    public String getCodigoItem() {
        return codigoItem;
    }

    public String getDeposito() {
        return deposito;
    }

    public String getLoteExterno() {
        return loteExterno;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }
}