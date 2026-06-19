package Infraestrutura;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public final class DiagnosticoAmbiente {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private DiagnosticoAmbiente() {
    }

    public static void registrarAmbienteInicial() {
        StringBuilder sb = new StringBuilder();

        sb.append("===== DIAGNOSTICO DE AMBIENTE =====\n");
        sb.append("Data/Hora: ").append(LocalDateTime.now()).append("\n");
        sb.append("user.dir: ").append(System.getProperty("user.dir")).append("\n");
        sb.append("user.home: ").append(System.getProperty("user.home")).append("\n");
        sb.append("java.version: ").append(System.getProperty("java.version")).append("\n");
        sb.append("os.name: ").append(System.getProperty("os.name")).append("\n");
        sb.append("os.version: ").append(System.getProperty("os.version")).append("\n");
        sb.append("APPDATA: ").append(System.getenv("APPDATA")).append("\n");
        sb.append("LOCALAPPDATA: ").append(System.getenv("LOCALAPPDATA")).append("\n");
        sb.append("USERNAME: ").append(System.getenv("USERNAME")).append("\n");
        sb.append("COMPUTERNAME: ").append(System.getenv("COMPUTERNAME")).append("\n");
        sb.append("===================================\n\n");

        gravar("diagnostico_ambiente", sb.toString());
    }

    public static void registrarCaminhosBases(Map<String, Path> caminhos) {
        StringBuilder sb = new StringBuilder();

        sb.append("===== DIAGNOSTICO DE CAMINHOS DAS BASES =====\n");
        sb.append("Data/Hora: ").append(LocalDateTime.now()).append("\n\n");

        for (Map.Entry<String, Path> entrada : caminhos.entrySet()) {
            String nome = entrada.getKey();
            Path caminho = entrada.getValue();

            sb.append("Base: ").append(nome).append("\n");
            sb.append("Caminho: ").append(caminho).append("\n");
            sb.append("Absoluto: ").append(caminho == null ? "" : caminho.toAbsolutePath()).append("\n");

            if (caminho == null) {
                sb.append("Existe: NÃO - caminho nulo\n");
                sb.append("\n");
                continue;
            }

            try {
                sb.append("Existe: ").append(Files.exists(caminho)).append("\n");
                sb.append("É arquivo: ").append(Files.isRegularFile(caminho)).append("\n");
                sb.append("É pasta: ").append(Files.isDirectory(caminho)).append("\n");
                sb.append("Pode ler: ").append(Files.isReadable(caminho)).append("\n");

                if (Files.exists(caminho) && Files.isRegularFile(caminho)) {
                    sb.append("Tamanho bytes: ").append(Files.size(caminho)).append("\n");
                    sb.append("Última modificação: ").append(Files.getLastModifiedTime(caminho)).append("\n");
                }

            } catch (Exception e) {
                sb.append("Erro ao verificar caminho:\n");
                sb.append(stackTrace(e)).append("\n");
            }

            sb.append("\n");
        }

        sb.append("=============================================\n\n");

        gravar("diagnostico_bases", sb.toString());
    }

    public static void registrarTexto(String nomeArquivo, String texto) {
        gravar(nomeArquivo, texto + "\n");
    }

    private static void gravar(String nomeArquivo, String conteudo) {
        try {
            Path pastaLogs = Path.of(System.getProperty("user.dir"), "logs");
            Files.createDirectories(pastaLogs);

            Path arquivo = pastaLogs.resolve(
                    nomeArquivo + "_" + LocalDateTime.now().format(FORMATO) + ".txt"
            );

            Files.writeString(
                    arquivo,
                    conteudo,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String stackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}