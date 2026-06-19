package Infraestrutura;

import Configuracao.CaminhosBase;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public final class DiagnosticoBases {

    private DiagnosticoBases() {
    }

    public static void registrar() {
        StringBuilder sb = new StringBuilder();

        sb.append("===== DIAGNOSTICO DE BASES =====\n");
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
        sb.append("\n");

        registrarCaminho(sb, "custos.db", CaminhosBase.CAMINHO_CUSTOS_DB);
        registrarCaminho(sb, "BD_METAL_ITEM_AT.xlsb", CaminhosBase.CAMINHO_BD_METAL_ITEM);
        registrarCaminho(sb, "movestq.db", CaminhosBase.CAMINHO_MOVESTQ_DB);

        gravar(sb.toString());
    }

    private static void registrarCaminho(StringBuilder sb, String nomeBase, Path caminho) {
        sb.append("--------------------------------------------------\n");
        sb.append("BASE: ").append(nomeBase).append("\n");
        sb.append("CAMINHO CONFIGURADO: ").append(caminho).append("\n");

        if (caminho == null) {
            sb.append("EXISTE: false\n");
            sb.append("MOTIVO: caminho nulo\n\n");
            return;
        }

        try {
            Path absoluto = caminho.toAbsolutePath();

            sb.append("CAMINHO ABSOLUTO: ").append(absoluto).append("\n");
            sb.append("EXISTE: ").append(Files.exists(caminho)).append("\n");
            sb.append("É ARQUIVO: ").append(Files.isRegularFile(caminho)).append("\n");
            sb.append("É PASTA: ").append(Files.isDirectory(caminho)).append("\n");
            sb.append("PODE LER: ").append(Files.isReadable(caminho)).append("\n");
            sb.append("PODE ESCREVER: ").append(Files.isWritable(caminho)).append("\n");

            if (Files.exists(caminho) && Files.isRegularFile(caminho)) {
                sb.append("TAMANHO BYTES: ").append(Files.size(caminho)).append("\n");
                sb.append("ÚLTIMA MODIFICAÇÃO: ").append(Files.getLastModifiedTime(caminho)).append("\n");
            }

        } catch (Exception e) {
            sb.append("ERRO AO VERIFICAR CAMINHO: ")
                    .append(e.getClass().getName())
                    .append(" - ")
                    .append(e.getMessage())
                    .append("\n");
        }

        sb.append("\n");
    }

    private static void gravar(String conteudo) {
        try {
            Path pastaLogs = Path.of(System.getProperty("user.dir"), "logs");
            Files.createDirectories(pastaLogs);

            Path arquivo = pastaLogs.resolve("diagnostico_bases.txt");

            Files.writeString(
                    arquivo,
                    conteudo,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}