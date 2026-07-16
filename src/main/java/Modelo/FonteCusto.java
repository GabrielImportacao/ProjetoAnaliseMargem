package Modelo;

public enum FonteCusto {

    BANCO_CUSTOS("Banco de custos"),
    BANCO_PROMOB("Banco Promob"),
    PLANILHA_FALLBACK("Planilha de itens - fallback"),
    NAO_ENCONTRADO("Não encontrado"),
	CUSTO_VERDADEIRO("Custo verdadeiro");

    private final String descricao;

    FonteCusto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}