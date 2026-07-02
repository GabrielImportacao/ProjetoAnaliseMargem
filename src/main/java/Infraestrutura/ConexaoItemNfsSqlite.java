package Infraestrutura;

import Configuracao.CaminhosBase;

public class ConexaoItemNfsSqlite extends ConexaoSqliteArquivo {

    public ConexaoItemNfsSqlite() {
        super(CaminhosBase.CAMINHO_ITEM_NFS_DB);
    }
}