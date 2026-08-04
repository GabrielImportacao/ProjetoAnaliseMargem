package Infraestrutura.diagnostico;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import java.nio.file.Path;

public final class TratadorErros {

    private TratadorErros() {
    }
    
    /*
     * Cada assinatura de erro global será exibida apenas uma vez
     * durante a execução atual do programa.
     *
     * Isso impede milhares de janelas quando o JavaFX lança
     * repetidamente o mesmo erro durante os pulsos de CSS/layout.
     */
    private static final Set<String> ERROS_GLOBAIS_EXIBIDOS =
            ConcurrentHashMap.newKeySet();

    private static final AtomicBoolean ALERTA_GLOBAL_EM_EXIBICAO =
            new AtomicBoolean(false);

    public static void instalarHandlerGlobal() {
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, erro) -> {

                    Runnable acao = () ->
                            tratarErroGlobal(
                                    thread,
                                    erro
                            );

                    try {
                        if (Platform.isFxApplicationThread()) {
                            acao.run();
                        } else {
                            Platform.runLater(acao);
                        }

                    } catch (Exception erroNoTratador) {
                        DiagnosticoErro diagnostico =
                                ClassificadorErro.classificar(
                                        "Erro inesperado fora da interface",
                                        erro
                                );

                        RegistroErro.registrar(
                                diagnostico,
                                erro
                        );
                    }
                }
        );
    }
    
    private static void tratarErroGlobal(
            Thread thread,
            Throwable erro
    ) {
        if (erro == null) {
            erro = new RuntimeException(
                    "Erro global desconhecido."
            );
        }

        String contexto =
                "Erro inesperado na thread: "
                        + thread.getName();

        DiagnosticoErro diagnostico =
                ClassificadorErro.classificar(
                        contexto,
                        erro
                );

        /*
         * O log é criado antes de qualquer decisão sobre
         * exibir ou não a janela.
         *
         * Assim, inclusive erros silenciosos e repetidos
         * continuam registrados para manutenção.
         */
        Path caminhoLog =
                RegistroErro.registrar(
                        diagnostico,
                        erro
                );

        /*
         * Ignora visualmente apenas o erro interno conhecido
         * do processamento de CSS do JavaFX.
         *
         * O erro continua registrado no arquivo de log.
         */
        if (deveOcultarJanelaErroGlobal(erro)) {
            System.err.println(
                    "[ERRO GLOBAL SILENCIADO] "
                            + erro.getClass().getName()
                            + ": "
                            + erro.getMessage()
                            + " | Log: "
                            + (
                            caminhoLog == null
                                    ? "não criado"
                                    : caminhoLog.toAbsolutePath()
                    )
            );

            return;
        }

        String assinatura =
                criarAssinaturaErro(erro);

        /*
         * Para os demais erros, a mesma falha abre somente
         * uma janela durante esta execução.
         *
         * O registro no log já foi feito acima.
         */
        if (!ERROS_GLOBAIS_EXIBIDOS.add(assinatura)) {
            return;
        }

        /*
         * Caso outro erro diferente aconteça enquanto uma
         * janela já estiver aberta, ele permanece registrado
         * no log, mas não abre outra janela simultaneamente.
         */
        if (!ALERTA_GLOBAL_EM_EXIBICAO.compareAndSet(
                false,
                true
        )) {
            return;
        }

        try {
            exibirNaTela(
                    null,
                    diagnostico,
                    caminhoLog,
                    erro
            );
        } finally {
            ALERTA_GLOBAL_EM_EXIBICAO.set(false);
        }
    }
    
    private static boolean deveOcultarJanelaErroGlobal(
            Throwable erro
    ) {
        Throwable atual = erro;

        while (atual != null) {
            boolean excecaoDeIndice =
                    atual instanceof IndexOutOfBoundsException;

            if (excecaoDeIndice
                    && ocorreuNoCssInternoJavaFx(atual)) {

                return true;
            }

            Throwable causa =
                    atual.getCause();

            if (causa == null
                    || causa == atual) {

                break;
            }

            atual = causa;
        }

        return false;
    }

    private static boolean ocorreuNoCssInternoJavaFx(
            Throwable erro
    ) {
        StackTraceElement[] elementos =
                erro.getStackTrace();

        if (elementos == null) {
            return false;
        }

        for (StackTraceElement elemento : elementos) {
            if (elemento == null) {
                continue;
            }

            String classe =
                    elemento.getClassName();

            String metodo =
                    elemento.getMethodName();

            boolean processamentoCssNo =
                    "javafx.scene.Node".equals(classe)
                            && "processCSS".equals(metodo);

            boolean passagemCssNaCena =
                    "javafx.scene.Scene".equals(classe)
                            && "doCSSPass".equals(metodo);

            if (processamentoCssNo
                    || passagemCssNaCena) {

                return true;
            }
        }

        return false;
    }

    private static String criarAssinaturaErro(
            Throwable erro
    ) {
        Throwable raiz = erro;

        while (raiz.getCause() != null
                && raiz.getCause() != raiz) {

            raiz = raiz.getCause();
        }

        String local = "sem-local";

        StackTraceElement[] elementos =
                raiz.getStackTrace();

        if (elementos != null
                && elementos.length > 0) {

            local = elementos[0].toString();
        }

        return raiz.getClass().getName()
                + "|"
                + String.valueOf(raiz.getMessage())
                + "|"
                + local;
    }

    public static void tratar(Window owner, String contexto, Throwable erro) {
        if (erro == null) {
            erro = new RuntimeException("Erro desconhecido.");
        }

        DiagnosticoErro diagnostico = ClassificadorErro.classificar(contexto, erro);
        Path caminhoLog = RegistroErro.registrar(diagnostico, erro);

        exibirNaTela(owner, diagnostico, caminhoLog, erro);
    }

    public static void registrarSemTela(String contexto, Throwable erro) {
        if (erro == null) {
            erro = new RuntimeException("Erro desconhecido.");
        }

        DiagnosticoErro diagnostico = ClassificadorErro.classificar(contexto, erro);
        RegistroErro.registrar(diagnostico, erro);
    }

    private static void exibirNaTela(
            Window owner,
            DiagnosticoErro diagnostico,
            Path caminhoLog,
            Throwable erro
    ) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro no programa");
        alert.setHeaderText("Ocorreu um erro durante a execução.");

        if (owner != null) {
            alert.initOwner(owner);
        }

        String textoLog = caminhoLog == null
                ? "Não foi possível criar o arquivo de log."
                : caminhoLog.toAbsolutePath().toString();

        alert.setContentText(
                "Ação: " + diagnostico.contexto()
                        + "\n\nOrigem provável: " + diagnostico.origemProvavel()
                        + "\n\nProblema: " + diagnostico.mensagemUsuario()
                        + "\n\nOnde ocorreu: " + diagnostico.localTecnico()
                        + "\n\nO que fazer: " + diagnostico.orientacao()
                        + "\n\nLog gerado em:\n" + textoLog
        );

        TextArea areaDetalhes = new TextArea(RegistroErro.obterStackTrace(erro));
        areaDetalhes.setEditable(false);
        areaDetalhes.setWrapText(false);
        areaDetalhes.setMaxWidth(Double.MAX_VALUE);
        areaDetalhes.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(areaDetalhes, Priority.ALWAYS);
        GridPane.setHgrow(areaDetalhes, Priority.ALWAYS);

        GridPane conteudoExpansivel = new GridPane();
        conteudoExpansivel.setMaxWidth(Double.MAX_VALUE);
        conteudoExpansivel.add(new Label("Detalhes técnicos do erro:"), 0, 0);
        conteudoExpansivel.add(areaDetalhes, 0, 1);

        alert.getDialogPane().setExpandableContent(conteudoExpansivel);
        alert.showAndWait();
    }
}