package Configuracao;

import java.nio.file.Path;

public final class CaminhosBase {

    public static final Path CAMINHO_CUSTOS_DB = Path.of(
            "K:\\Importacao\\Troca Publica\\Bancos de Dados (Não Excluir)\\BD_METAL\\AtualizadorBD\\custos.db"
    );

    public static final Path CAMINHO_BD_METAL_ITEM = Path.of(
            "K:\\Importacao\\Troca Publica\\Bancos de Dados (Não Excluir)\\BD_METAL\\BD_METAL_ITEM_AT.xlsb"
    );

    public static final Path CAMINHO_MOVESTQ_DB = Path.of(
            "K:\\Importacao\\Troca Publica\\Bancos de Dados (Não Excluir)\\SistemaBD\\banco\\movestq.db"
    );
    
    public static final Path CAMINHO_METAL_DB = Path.of(
            "K:\\Importacao\\Troca Publica\\Bancos de Dados (Não Excluir)\\SistemaBD\\banco\\metal.db"
    );
    
    public static final Path CAMINHO_ITEM_NFS_DB = Path.of(
            "K:\\Importacao\\Troca Publica\\Bancos de Dados (Não Excluir)\\SistemaBD\\banco\\item_nfs.db"
    );
    
    public static final Path CAMINHO_ITEM_LTOE_DB = Path.of(
            "K:\\Importacao\\Troca Publica\\Bancos de Dados (Não Excluir)\\SistemaBD\\banco\\item_ltoe.db"
    );

    public static final Path CAMINHO_ITEM_DEPO_DB = Path.of(
            "K:\\Importacao\\Troca Publica\\Bancos de Dados (Não Excluir)\\SistemaBD\\banco\\itemdepo.db"
    );
    
    public static final Path CAMINHO_USER_STATE_DB = Path.of(
            obterPastaAppDataUsuario(),
            "ProgramaAnaliseMargem",
            "user_state.db"
    );

    private static String obterPastaAppDataUsuario() {
        String appData = System.getenv("APPDATA");

        if (appData != null && !appData.isBlank()) {
            return appData;
        }

        return System.getProperty("user.home");
    }
    private CaminhosBase() {
    }
}