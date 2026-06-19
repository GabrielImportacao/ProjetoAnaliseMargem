package Infraestrutura.diagnostico;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class RegistroErro {

    private static final DateTimeFormatter FORMATO_ARQUIVO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private RegistroErro() {
    }

    public static Path registrar(DiagnosticoErro diagnostico, Throwable erro) {
        try {
            Path pastaLogs = obterPastaLogs();
            Files.createDirectories(pastaLogs);

            Path arquivoLog = pastaLogs.resolve(
                    "erros_" + LocalDateTime.now().format(FORMATO_ARQUIVO) + ".txt"
            );

            String conteudo = montarConteudoLog(diagnostico, erro);

            Files.writeString(
                    arquivoLog,
                    conteudo,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            return arquivoLog;

        } catch (Exception erroAoRegistrar) {
            erroAoRegistrar.printStackTrace();
            return null;
        }
    }

    private static Path obterPastaLogs() {
        Path pastaAoLadoDoPrograma = Paths.get(System.getProperty("user.dir"), "logs");

        try {
            Files.createDirectories(pastaAoLadoDoPrograma);
            return pastaAoLadoDoPrograma;
        } catch (Exception ignored) {
            String appData = System.getenv("APPDATA");

            if (appData != null && !appData.isBlank()) {
                return Paths.get(appData, "AnaliseMargem", "logs");
            }

            return Paths.get(System.getProperty("user.home"), "AnaliseMargem", "logs");
        }
    }

    private static String montarConteudoLog(DiagnosticoErro diagnostico, Throwable erro) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n============================================================\n");
        sb.append("DATA/HORA: ")
                .append(LocalDateTime.now().format(FORMATO_DATA_HORA))
                .append("\n");

        sb.append("CONTEXTO: ")
                .append(valorOuVazio(diagnostico.contexto()))
                .append("\n");

        sb.append("ORIGEM PROVAVEL: ")
                .append(valorOuVazio(diagnostico.origemProvavel()))
                .append("\n");

        sb.append("MENSAGEM AO USUARIO: ")
                .append(valorOuVazio(diagnostico.mensagemUsuario()))
                .append("\n");

        sb.append("ORIENTACAO: ")
                .append(valorOuVazio(diagnostico.orientacao()))
                .append("\n");

        sb.append("LOCAL TECNICO: ")
                .append(valorOuVazio(diagnostico.localTecnico()))
                .append("\n");

        sb.append("THREAD: ")
                .append(Thread.currentThread().getName())
                .append("\n");

        sb.append("JAVA: ")
                .append(System.getProperty("java.version"))
                .append("\n");

        sb.append("SISTEMA: ")
                .append(System.getProperty("os.name"))
                .append(" ")
                .append(System.getProperty("os.version"))
                .append("\n");

        sb.append("PASTA EXECUCAO: ")
                .append(System.getProperty("user.dir"))
                .append("\n");

        sb.append("\nERRO:\n");
        sb.append(obterStackTrace(erro));
        sb.append("\n============================================================\n");

        return sb.toString();
    }

    public static String obterStackTrace(Throwable erro) {
        if (erro == null) {
            return "Erro não informado.";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        erro.printStackTrace(pw);
        return sw.toString();
    }

    private static String valorOuVazio(String texto) {
        return texto == null ? "" : texto;
    }
}