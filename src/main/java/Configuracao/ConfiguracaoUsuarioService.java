package Configuracao;

import Repositorio.ConfiguracaoUsuarioRepository;
import Repositorio.ConfiguracaoUsuarioRepositorySqlite;

public class ConfiguracaoUsuarioService {

    private final ConfiguracaoUsuarioRepository repository;

    public ConfiguracaoUsuarioService() {
        this.repository = new ConfiguracaoUsuarioRepositorySqlite();
    }

    public ConfiguracaoUsuario carregar() {
        return repository.carregar();
    }

    public void salvar(ConfiguracaoUsuario configuracao) {
        repository.salvar(configuracao);
    }
}