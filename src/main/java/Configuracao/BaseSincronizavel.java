package Configuracao;

import java.nio.file.Path;

public enum BaseSincronizavel {

    CUSTOS(
            "custos",
            CaminhosBase.CAMINHO_SERVIDOR_CUSTOS_DB,
            CaminhosBase.CAMINHO_LOCAL_CUSTOS_DB,
            true
    ),

    MOVESTQ(
            "movestq",
            CaminhosBase.CAMINHO_SERVIDOR_MOVESTQ_DB,
            CaminhosBase.CAMINHO_LOCAL_MOVESTQ_DB,
            true
    ),

    METAL(
            "metal",
            CaminhosBase.CAMINHO_SERVIDOR_METAL_DB,
            CaminhosBase.CAMINHO_LOCAL_METAL_DB,
            true
    ),

    ITEM_DEPO(
            "itemdepo",
            CaminhosBase.CAMINHO_SERVIDOR_ITEM_DEPO_DB,
            CaminhosBase.CAMINHO_LOCAL_ITEM_DEPO_DB,
            true
    ),

    ITEM_NFS(
            "item_nfs",
            CaminhosBase.CAMINHO_SERVIDOR_ITEM_NFS_DB,
            CaminhosBase.CAMINHO_LOCAL_ITEM_NFS_DB,
            true
    ),

    ITEM_LTOE(
            "item_ltoe",
            CaminhosBase.CAMINHO_SERVIDOR_ITEM_LTOE_DB,
            CaminhosBase.CAMINHO_LOCAL_ITEM_LTOE_DB,
            true
    ),

    /*
     * Esta base ainda não é utilizada pelo programa.
     * Ela será sincronizada quando estiver disponível,
     * mas sua ausência não impedirá o funcionamento atual.
     */
    ITENS_PEDIDO(
            "itemspedido",
            CaminhosBase.CAMINHO_SERVIDOR_ITENS_PEDIDO_DB,
            CaminhosBase.CAMINHO_LOCAL_ITENS_PEDIDO_DB,
            false
    );

    private final String identificador;
    private final Path caminhoServidor;
    private final Path caminhoLocal;
    private final boolean obrigatoria;

    BaseSincronizavel(
            String identificador,
            Path caminhoServidor,
            Path caminhoLocal,
            boolean obrigatoria
    ) {
        this.identificador = identificador;
        this.caminhoServidor = caminhoServidor;
        this.caminhoLocal = caminhoLocal;
        this.obrigatoria = obrigatoria;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Path getCaminhoServidor() {
        return caminhoServidor;
    }

    public Path getCaminhoLocal() {
        return caminhoLocal;
    }

    public boolean isObrigatoria() {
        return obrigatoria;
    }

    public String getNomeArquivo() {
        return caminhoLocal
                .getFileName()
                .toString();
    }
}