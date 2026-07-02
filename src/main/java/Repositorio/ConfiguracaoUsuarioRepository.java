package Repositorio;

import Configuracao.ConfiguracaoUsuario;

public interface ConfiguracaoUsuarioRepository {

    ConfiguracaoUsuario carregar();

    void salvar(ConfiguracaoUsuario configuracao);
}