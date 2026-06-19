package Infraestrutura.diagnostico;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

import java.nio.file.Path;

public final class TratadorErros {

    private TratadorErros() {
    }

    public static void instalarHandlerGlobal() {
        Thread.setDefaultUncaughtExceptionHandler((thread, erro) -> {
            Runnable acao = () -> tratar(
                    null,
                    "Erro inesperado na thread: " + thread.getName(),
                    erro
            );

            try {
                if (Platform.isFxApplicationThread()) {
                    acao.run();
                } else {
                    Platform.runLater(acao);
                }
            } catch (Exception e) {
                DiagnosticoErro diagnostico = ClassificadorErro.classificar(
                        "Erro inesperado fora da interface",
                        erro
                );

                RegistroErro.registrar(diagnostico, erro);
            }
        });
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