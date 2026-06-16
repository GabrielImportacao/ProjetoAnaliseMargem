package Infraestrutura;

import Configuracao.CaminhosBase;

public class ConexaoMovestqSqlite extends ConexaoSqliteArquivo {

    public ConexaoMovestqSqlite() {
        super(CaminhosBase.CAMINHO_MOVESTQ_DB);
    }
}