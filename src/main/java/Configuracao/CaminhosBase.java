package Configuracao;

import java.nio.file.Path;

public final class CaminhosBase {

    public static final Path CAMINHO_CUSTOS_DB = Path.of(
            "K:\\Importacao\\Troca Publica\\Bancos de Dados (Não Excluir)\\BD_METAL\\AtualizadorBD\\custos.db"
    );

    public static final Path CAMINHO_BD_METAL_ITEM = Path.of(
            "K:\\Importacao\\Troca Publica\\Bancos de Dados (Não Excluir)\\BD_METAL\\BD_METAL_ITEM_AT.xlsb"
    );

    private CaminhosBase() {
    }
}