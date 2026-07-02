package Configuracao;

public class ConfiguracaoUsuario {

    private boolean margemEmPorcentagem;
    private boolean custoFuturo;
    private boolean custoEstimado;
    private boolean flagItensEncalhados;

    public ConfiguracaoUsuario() {
        this.margemEmPorcentagem = false;
        this.custoFuturo = true;
        this.custoEstimado = false;
        this.flagItensEncalhados = true;
    }

    public ConfiguracaoUsuario(ConfiguracaoUsuario outra) {
        if (outra == null) {
            this.margemEmPorcentagem = false;
            this.custoFuturo = true;
            this.custoEstimado = false;
            this.flagItensEncalhados = true;
            return;
        }

        this.margemEmPorcentagem = outra.isMargemEmPorcentagem();
        this.custoFuturo = outra.isCustoFuturo();
        this.custoEstimado = outra.isCustoEstimado();
        this.flagItensEncalhados = outra.isFlagItensEncalhados();
    }

    public boolean isMargemEmPorcentagem() {
        return margemEmPorcentagem;
    }

    public void setMargemEmPorcentagem(boolean margemEmPorcentagem) {
        this.margemEmPorcentagem = margemEmPorcentagem;
    }

    public boolean isCustoFuturo() {
        return custoFuturo;
    }

    public void setCustoFuturo(boolean custoFuturo) {
        this.custoFuturo = custoFuturo;
    }

    public boolean isCustoEstimado() {
        return custoEstimado;
    }

    public void setCustoEstimado(boolean custoEstimado) {
        this.custoEstimado = custoEstimado;
    }

    public boolean isFlagItensEncalhados() {
        return flagItensEncalhados;
    }

    public void setFlagItensEncalhados(boolean flagItensEncalhados) {
        this.flagItensEncalhados = flagItensEncalhados;
    }
}