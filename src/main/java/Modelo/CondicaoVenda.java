package Modelo;

public enum CondicaoVenda {

    NORMAL("Normal", "", "#000000"),
    AUMENTAR_PRECO("Aumentar preço", "#FFF2CC", "#000000"),
    SOMENTE_NO_PACOTE("Somente no pacote", "#F4B183", "#000000"),
    ESPECIAL("Especial", "", "#000000");

    private final String descricao;
    private final String corFundo;
    private final String corTexto;

    CondicaoVenda(String descricao, String corFundo, String corTexto) {
        this.descricao = descricao;
        this.corFundo = corFundo;
        this.corTexto = corTexto;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCorFundo() {
        return corFundo;
    }

    public String getCorTexto() {
        return corTexto;
    }

    public boolean possuiCor() {
        return corFundo != null && !corFundo.isBlank();
    }

    @Override
    public String toString() {
        return descricao;
    }
}