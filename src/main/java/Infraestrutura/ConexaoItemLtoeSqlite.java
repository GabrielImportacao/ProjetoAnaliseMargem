package Infraestrutura;

import Configuracao.CaminhosBase;

public class ConexaoItemLtoeSqlite extends ConexaoSqliteArquivo {

    public ConexaoItemLtoeSqlite() {
        super(CaminhosBase.CAMINHO_ITEM_LTOE_DB);
    }
}