package Infraestrutura;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DiagnosticoCustoReposicao {

    private static final Object BLOQUEIO =
            new Object();

    private static final DateTimeFormatter
            FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm:ss"
            );

    private DiagnosticoCustoReposicao() {
    }

    public static void registrarConsulta(
            String detalhes
    ) {
        gravarBloco(
                "TIPO: CONSULTA DE CUSTO REPOSIÇÃO\n"
                        + textoSeguro(detalhes)
        );
    }

    public static void registrarAlteracao(
            String campo,
            BigDecimal valorAnterior,
            BigDecimal valorNovo,
            String efeito
    ) {
        StringBuilder bloco =
                new StringBuilder();

        bloco.append(
                "TIPO: ALTERAÇÃO DE PARÂMETRO\n"
        );

        bloco.append("CAMPO: ")
                .append(textoSeguro(campo))
                .append("\n");

        bloco.append("VALOR ANTERIOR: ")
                .append(
                        formatarDecimal(
                                valorAnterior
                        )
                )
                .append("\n");

        bloco.append("VALOR NOVO: ")
                .append(
                        formatarDecimal(
                                valorNovo
                        )
                )
                .append("\n");

        if (efeito != null
                && !efeito.isBlank()) {

            bloco.append("EFEITO: ")
                    .append(efeito.trim())
                    .append("\n");
        }

        gravarBloco(
                bloco.toString()
        );
    }

    public static String formatarDecimal(
            BigDecimal valor
    ) {
        return valor == null
                ? "vazio"
                : valor.stripTrailingZeros()
                        .toPlainString();
    }

    private static String textoSeguro(
            String texto
    ) {
        return texto == null
                ? ""
                : texto;
    }

    private static void gravarBloco(
            String conteudo
    ) {
        synchronized (BLOQUEIO) {
            try {
                Path pastaLogs =
                        Path.of(
                                System.getProperty(
                                        "user.dir"
                                ),
                                "logs"
                        );

                Files.createDirectories(
                        pastaLogs
                );

                Path arquivo =
                        pastaLogs.resolve(
                                "diagnostico_custo_reposicao.txt"
                        );

                String bloco =
                        System.lineSeparator()
                                + "================================================="
                                + System.lineSeparator()
                                + "DATA/HORA: "
                                + LocalDateTime.now()
                                        .format(
                                                FORMATO_DATA_HORA
                                        )
                                + System.lineSeparator()
                                + conteudo.stripTrailing()
                                + System.lineSeparator();

                Files.writeString(
                        arquivo,
                        bloco,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );

            } catch (Exception erro) {
                /*
                 * Falha no diagnóstico não pode impedir
                 * o funcionamento do programa.
                 */
                System.err.println(
                        "Não foi possível registrar o "
                                + "diagnóstico do Custo Reposição: "
                                + erro.getMessage()
                );
            }
        }
    }
}