package Infraestrutura;

import Configuracao.CaminhosBase;

public class ConexaoMetalSqlite extends ConexaoSqliteArquivo {

    public ConexaoMetalSqlite() {
        super(CaminhosBase.CAMINHO_METAL_DB);
    }
}