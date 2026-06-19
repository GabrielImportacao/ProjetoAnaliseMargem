package Infraestrutura.diagnostico;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.sql.SQLException;

public final class ClassificadorErro {

    private ClassificadorErro() {
    }

    public static DiagnosticoErro classificar(String contexto, Throwable erro) {
        String textoErro = juntarMensagens(erro).toLowerCase();
        String localTecnico = encontrarLocalTecnico(erro);

        if (contemCausa(erro, FileNotFoundException.class)
                || contemCausa(erro, NoSuchFileException.class)
                || textoErro.contains("k:\\")
                || textoErro.contains(".xlsb")
                || textoErro.contains(".xlsx")
                || textoErro.contains(".db")
                || textoErro.contains("arquivo")
                || textoErro.contains("file")) {

            return new DiagnosticoErro(
                    contexto,
                    "EXTERNO",
                    "O programa encontrou problema ao acessar algum arquivo, planilha, banco de dados ou caminho externo.",
                    "Verifique se os arquivos existem, se a unidade K: está acessível, se a planilha/banco não foi movido e se o usuário tem permissão.",
                    localTecnico
            );
        }

        if (contemCausa(erro, SQLException.class)
                || textoErro.contains("sqlite")
                || textoErro.contains("database")
                || textoErro.contains("banco")
                || textoErro.contains("odbc")
                || textoErro.contains("sql")) {

            return new DiagnosticoErro(
                    contexto,
                    "EXTERNO",
                    "O programa encontrou problema ao consultar ou abrir uma base de dados.",
                    "Verifique se o banco existe, se não está corrompido, se o caminho está correto e se não há bloqueio de acesso.",
                    localTecnico
            );
        }

        if (contemCausa(erro, IOException.class)
                || textoErro.contains("access denied")
                || textoErro.contains("permiss")
                || textoErro.contains("denied")) {

            return new DiagnosticoErro(
                    contexto,
                    "EXTERNO",
                    "O programa encontrou problema de leitura, gravação ou permissão.",
                    "Verifique permissões de pasta, acesso à rede, arquivos abertos por outro usuário e bloqueios do Windows.",
                    localTecnico
            );
        }

        if (contemCausa(erro, NullPointerException.class)
                || contemCausa(erro, IllegalStateException.class)
                || contemCausa(erro, IndexOutOfBoundsException.class)
                || contemCausa(erro, NumberFormatException.class)) {

            return new DiagnosticoErro(
                    contexto,
                    "INTERNO",
                    "O programa encontrou um erro interno durante a execução.",
                    "Envie o arquivo de log para manutenção. Provavelmente será necessário ajustar o código.",
                    localTecnico
            );
        }

        return new DiagnosticoErro(
                contexto,
                "INDEFINIDO",
                "O programa encontrou um erro inesperado.",
                "Envie o arquivo de log para manutenção para identificar se é problema interno ou externo.",
                localTecnico
        );
    }

    private static boolean contemCausa(Throwable erro, Class<? extends Throwable> tipo) {
        Throwable atual = erro;

        while (atual != null) {
            if (tipo.isInstance(atual)) {
                return true;
            }

            atual = atual.getCause();
        }

        return false;
    }

    private static String juntarMensagens(Throwable erro) {
        StringBuilder sb = new StringBuilder();

        Throwable atual = erro;

        while (atual != null) {
            if (atual.getClass() != null) {
                sb.append(atual.getClass().getName()).append(" ");
            }

            if (atual.getMessage() != null) {
                sb.append(atual.getMessage()).append(" ");
            }

            atual = atual.getCause();
        }

        return sb.toString();
    }

    private static String encontrarLocalTecnico(Throwable erro) {
        if (erro == null || erro.getStackTrace() == null || erro.getStackTrace().length == 0) {
            return "Local técnico não identificado.";
        }

        for (StackTraceElement elemento : erro.getStackTrace()) {
            String classe = elemento.getClassName();

            if (classe.startsWith("Visao.")
                    || classe.startsWith("Controle.")
                    || classe.startsWith("Repositorio.")
                    || classe.startsWith("Infraestrutura.")
                    || classe.startsWith("Modelo.")) {

                return elemento.getClassName()
                        + "."
                        + elemento.getMethodName()
                        + "("
                        + elemento.getFileName()
                        + ":"
                        + elemento.getLineNumber()
                        + ")";
            }
        }

        StackTraceElement primeiro = erro.getStackTrace()[0];

        return primeiro.getClassName()
                + "."
                + primeiro.getMethodName()
                + "("
                + primeiro.getFileName()
                + ":"
                + primeiro.getLineNumber()
                + ")";
    }
}