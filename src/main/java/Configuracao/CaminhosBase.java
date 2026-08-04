package Configuracao;

import java.nio.file.Path;

public final class CaminhosBase {

    /*
     * =========================================================
     * PASTAS DO SERVIDOR
     * =========================================================
     */

    private static final Path PASTA_SERVIDOR_CUSTOS = Path.of(
            "K:\\Importacao\\Troca Publica"
                    + "\\Bancos de Dados (Não Excluir)"
                    + "\\BD_METAL"
                    + "\\AtualizadorBD"
    );

    private static final Path PASTA_SERVIDOR_SISTEMA = Path.of(
            "K:\\Importacao\\Troca Publica"
                    + "\\Bancos de Dados (Não Excluir)"
                    + "\\SistemaBD"
                    + "\\banco"
    );

    /*
     * =========================================================
     * CAMINHOS DOS BANCOS NO SERVIDOR
     * =========================================================
     */

    public static final Path CAMINHO_SERVIDOR_CUSTOS_DB =
            PASTA_SERVIDOR_CUSTOS.resolve("custos.db");

    public static final Path CAMINHO_SERVIDOR_MOVESTQ_DB =
            PASTA_SERVIDOR_SISTEMA.resolve("movestq.db");

    public static final Path CAMINHO_SERVIDOR_METAL_DB =
            PASTA_SERVIDOR_SISTEMA.resolve("metal.db");

    public static final Path CAMINHO_SERVIDOR_ITEM_DEPO_DB =
            PASTA_SERVIDOR_SISTEMA.resolve("itemdepo.db");

    public static final Path CAMINHO_SERVIDOR_ITEM_NFS_DB =
            PASTA_SERVIDOR_SISTEMA.resolve("item_nfs.db");

    public static final Path CAMINHO_SERVIDOR_ITEM_LTOE_DB =
            PASTA_SERVIDOR_SISTEMA.resolve("item_ltoe.db");

    public static final Path CAMINHO_SERVIDOR_ITENS_PEDIDO_DB =
            PASTA_SERVIDOR_SISTEMA.resolve("itemspedido.db");

    /*
     * A planilha continua no servidor por enquanto.
     *
     * Ela não faz parte das sete bases SQLite informadas
     * para a sincronização.
     */
    public static final Path CAMINHO_BD_METAL_ITEM = Path.of(
            "K:\\Importacao\\Troca Publica"
                    + "\\Bancos de Dados (Não Excluir)"
                    + "\\BD_METAL"
                    + "\\BD_METAL_ITEM_AT.xlsb"
    );

    public static final Path PASTA_BASES_LOCAIS = Path.of(
            obterPastaLocalAppDataUsuario(),
            "ProgramaAnaliseMargem",
            "bases"
    );

    /*
     * =========================================================
     * CAMINHOS DOS BANCOS LOCAIS
     * =========================================================
     */

    public static final Path CAMINHO_LOCAL_CUSTOS_DB =
            PASTA_BASES_LOCAIS.resolve("custos.db");

    public static final Path CAMINHO_LOCAL_MOVESTQ_DB =
            PASTA_BASES_LOCAIS.resolve("movestq.db");

    public static final Path CAMINHO_LOCAL_METAL_DB =
            PASTA_BASES_LOCAIS.resolve("metal.db");

    public static final Path CAMINHO_LOCAL_ITEM_DEPO_DB =
            PASTA_BASES_LOCAIS.resolve("itemdepo.db");

    public static final Path CAMINHO_LOCAL_ITEM_NFS_DB =
            PASTA_BASES_LOCAIS.resolve("item_nfs.db");

    public static final Path CAMINHO_LOCAL_ITEM_LTOE_DB =
            PASTA_BASES_LOCAIS.resolve("item_ltoe.db");

    public static final Path CAMINHO_LOCAL_ITENS_PEDIDO_DB =
            PASTA_BASES_LOCAIS.resolve("itemspedido.db");

    /*
     * =========================================================
     * CAMINHOS UTILIZADOS PELO PROGRAMA
     * =========================================================
     *
     * O programa consulta exclusivamente as cópias locais.
     *
     * Os arquivos do servidor são acessados apenas pelo
     * SincronizacaoBasesService, responsável por atualizar
     * essas cópias antes do carregamento dos caches.
     */

    public static final Path CAMINHO_CUSTOS_DB =
            CAMINHO_LOCAL_CUSTOS_DB;

    public static final Path CAMINHO_MOVESTQ_DB =
            CAMINHO_LOCAL_MOVESTQ_DB;

    public static final Path CAMINHO_METAL_DB =
            CAMINHO_LOCAL_METAL_DB;

    public static final Path CAMINHO_ITEM_DEPO_DB =
            CAMINHO_LOCAL_ITEM_DEPO_DB;

    public static final Path CAMINHO_ITEM_NFS_DB =
            CAMINHO_LOCAL_ITEM_NFS_DB;

    public static final Path CAMINHO_ITEM_LTOE_DB =
            CAMINHO_LOCAL_ITEM_LTOE_DB;

    public static final Path CAMINHO_ITENS_PEDIDO_DB =
            CAMINHO_LOCAL_ITENS_PEDIDO_DB;

    /*
     * =========================================================
     * BANCO LOCAL DE CONFIGURAÇÕES DO USUÁRIO
     * =========================================================
     */

    public static final Path CAMINHO_USER_STATE_DB = Path.of(
            obterPastaAppDataUsuario(),
            "ProgramaAnaliseMargem",
            "user_state.db"
    );

    private static String obterPastaLocalAppDataUsuario() {
        String localAppData =
                System.getenv("LOCALAPPDATA");

        if (localAppData != null
                && !localAppData.isBlank()) {

            return localAppData;
        }

        return System.getProperty("user.home");
    }

    private static String obterPastaAppDataUsuario() {
        String appData =
                System.getenv("APPDATA");

        if (appData != null
                && !appData.isBlank()) {

            return appData;
        }

        return System.getProperty("user.home");
    }

    private CaminhosBase() {
    }
}