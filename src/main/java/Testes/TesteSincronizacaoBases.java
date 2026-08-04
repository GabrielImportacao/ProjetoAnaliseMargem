package Testes;

import Controle.SincronizacaoBasesService;
import Controle.SincronizacaoBasesService.ResultadoBase;
import Controle.SincronizacaoBasesService.ResultadoSincronizacao;

public class TesteSincronizacaoBases {

    public static void main(String[] args) {
        System.out.println(
                "=============================================="
        );

        System.out.println(
                "TESTE DE SINCRONIZAÇÃO DAS BASES LOCAIS"
        );

        System.out.println(
                "=============================================="
        );

        System.out.println();

        SincronizacaoBasesService service =
                new SincronizacaoBasesService();

        ResultadoSincronizacao resultado =
                service.sincronizarTodas(
                        (
                                atual,
                                total,
                                base,
                                etapa
                        ) -> System.out.println(
                                "["
                                        + atual
                                        + "/"
                                        + total
                                        + "] "
                                        + base.getNomeArquivo()
                                        + " - "
                                        + etapa
                        )
                );

        System.out.println();
        System.out.println(
                "============== RESULTADOS =============="
        );

        for (
                ResultadoBase resultadoBase
                : resultado.resultados()
        ) {
            System.out.println();

            System.out.println(
                    "Base: "
                            + resultadoBase.base()
                            .getNomeArquivo()
            );

            System.out.println(
                    "Obrigatória: "
                            + (
                            resultadoBase.base()
                                    .isObrigatoria()
                            ? "SIM"
                            : "NÃO"
                    )
            );

            System.out.println(
                    "Servidor: "
                            + resultadoBase.base()
                            .getCaminhoServidor()
            );

            System.out.println(
                    "Local: "
                            + resultadoBase.base()
                            .getCaminhoLocal()
            );

            System.out.println(
                    "Status: "
                            + resultadoBase.status()
                            .getDescricao()
            );

            System.out.println(
                    "Mensagem: "
                            + resultadoBase.mensagem()
            );

            System.out.println(
                    "Tamanho local: "
                            + resultadoBase.tamanhoBytes()
                            + " bytes"
            );

            System.out.println(
                    "Duração: "
                            + resultadoBase.duracaoMillis()
                            + " ms"
            );

            if (resultadoBase.erro() != null) {
                System.out.println(
                        "Erro original: "
                                + resultadoBase.erro()
                                .getClass()
                                .getName()
                                + " - "
                                + resultadoBase.erro()
                                .getMessage()
                );
            }
        }

        System.out.println();
        System.out.println(
                "============== RESUMO =============="
        );

        System.out.println(
                "Bases copiadas nesta execução: "
                        + resultado.quantidadeAtualizada()
        );

        System.out.println(
                "Bases usando fallback local: "
                        + resultado.quantidadeFallbackLocal()
        );

        System.out.println(
                "Pode iniciar o programa: "
                        + (
                        resultado.podeIniciarPrograma()
                        ? "SIM"
                        : "NÃO"
                )
        );

        if (!resultado.podeIniciarPrograma()) {
            throw new IllegalStateException(
                    "Uma ou mais bases obrigatórias não "
                            + "estão disponíveis no servidor "
                            + "nem localmente."
            );
        }

        System.out.println();
        System.out.println(
                "Teste finalizado com sucesso."
        );
    }
}