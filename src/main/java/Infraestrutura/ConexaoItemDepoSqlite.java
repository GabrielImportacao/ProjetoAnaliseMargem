package Infraestrutura;

import Configuracao.CaminhosBase;

public class ConexaoItemDepoSqlite extends ConexaoSqliteArquivo {

    public ConexaoItemDepoSqlite() {
        super(CaminhosBase.CAMINHO_ITEM_DEPO_DB);
    }
}