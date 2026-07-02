package Modelo;

public class LayoutColunaTabela {

    private final String idColuna;
    private final String idPai;
    private final int ordem;
    private final double largura;

    public LayoutColunaTabela(
            String idColuna,
            String idPai,
            int ordem,
            double largura
    ) {
        this.idColuna = idColuna;
        this.idPai = idPai;
        this.ordem = ordem;
        this.largura = largura;
    }

    public String getIdColuna() {
        return idColuna;
    }

    public String getIdPai() {
        return idPai;
    }

    public int getOrdem() {
        return ordem;
    }

    public double getLargura() {
        return largura;
    }
}