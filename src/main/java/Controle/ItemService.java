package Controle;

import Modelo.DadosItem;
import Modelo.DadosItemConsulta;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import Controle.SincronizacaoBasesService.OuvinteProgresso;
import Controle.SincronizacaoBasesService.ResultadoBase;
import Controle.SincronizacaoBasesService.ResultadoSincronizacao;

import Infraestrutura.DiagnosticoBases;

public class ItemService {

    private final DadosItemService dadosItemService;
    private final SincronizacaoBasesService sincronizacaoBasesService;
    /*
     * Impede que duas sincronizações tentem substituir
     * os bancos locais ao mesmo tempo.
     */
    private final Object bloqueioSincronizacao = new Object();
    
    public ItemService() {
        this.dadosItemService =
                new DadosItemService();

        this.sincronizacaoBasesService =
                new SincronizacaoBasesService();
    }
    

    public void recarregarBases() {
        recarregarBases(null);
    }

    public ResultadoSincronizacao recarregarBases(
            OuvinteProgresso ouvinte
    ) {
        /*
         * Primeiro atualizamos os arquivos locais.
         *
         * Depois reconstruímos os caches utilizando
         * exclusivamente as cópias locais.
         */
        ResultadoSincronizacao resultado =
                sincronizarBasesLocais(ouvinte);

        dadosItemService.recarregarBases();

        return resultado;
    }

    public void preCarregarBases() {
        preCarregarBases(null);
    }

    public ResultadoSincronizacao preCarregarBases(
            OuvinteProgresso ouvinte
    ) {
        /*
         * Na inicialização, sincronizamos antes de qualquer
         * repositório abrir os bancos locais.
         */
        ResultadoSincronizacao resultado =
                sincronizarBasesLocais(ouvinte);

        DiagnosticoBases.registrar();

        dadosItemService.preCarregarBases();
        aquecerConsultasIniciais();

        return resultado;
    }

    private ResultadoSincronizacao sincronizarBasesLocais(
            OuvinteProgresso ouvinte
    ) {
        synchronized (bloqueioSincronizacao) {
            ResultadoSincronizacao resultado =
                    sincronizacaoBasesService.sincronizarTodas(
                            (
                                    atual,
                                    total,
                                    base,
                                    etapa
                            ) -> {
                                System.out.println(
                                        "[SINCRONIZAÇÃO "
                                                + atual
                                                + "/"
                                                + total
                                                + "] "
                                                + base.getNomeArquivo()
                                                + " - "
                                                + etapa
                                );

                                if (ouvinte != null) {
                                    ouvinte.atualizar(
                                            atual,
                                            total,
                                            base,
                                            etapa
                                    );
                                }
                            }
                    );

            /*
             * Resultado individual de cada banco.
             */
            for (
                    ResultadoBase resultadoBase
                    : resultado.resultados()
            ) {
                System.out.println(
                        "[BASE] "
                                + resultadoBase
                                        .base()
                                        .getNomeArquivo()
                                + " | "
                                + resultadoBase
                                        .status()
                                        .getDescricao()
                                + " | "
                                + resultadoBase.mensagem()
                );
            }

            if (resultado.podeIniciarPrograma()) {
                return resultado;
            }

            StringBuilder detalhes =
                    new StringBuilder();

            for (
                    ResultadoBase resultadoBase
                    : resultado.resultados()
            ) {
                if (!resultadoBase.falhouObrigatoria()) {
                    continue;
                }

                if (!detalhes.isEmpty()) {
                    detalhes.append(
                            System.lineSeparator()
                    );
                }

                detalhes.append("- ")
                        .append(
                                resultadoBase
                                        .base()
                                        .getNomeArquivo()
                        )
                        .append(": ")
                        .append(
                                resultadoBase.mensagem()
                        );
            }

            throw new IllegalStateException(
                    "Não foi possível disponibilizar uma ou "
                            + "mais bases obrigatórias."
                            + System.lineSeparator()
                            + detalhes
            );
        }
    }

    private void aquecerConsultasIniciais() {
        // Consultas internas apenas para aquecer o fluxo completo:
        // itemRepository + custoRepository + custoPromobRepository + conversão para DadosItem.
        buscarPorCodigo("MMR1814.1031");
        buscarPorCodigo("MRT3803.0801");
        buscarPorCodigo("MMP0201.1004");
        buscarPorCodigo("MPR3423.1504");
    }
    
    public Optional<DadosItem> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            registrarLogConsulta(codigo, null, "Código vazio ou nulo.");
            return Optional.empty();
        }

        Optional<DadosItemConsulta> dadosConsulta =
                dadosItemService.buscarDadosCompletosPorCodigo(codigo);

        if (dadosConsulta.isEmpty()) {
            registrarLogConsulta(codigo, null, "DadosItemConsulta vazio.");
            return Optional.empty();
        }

        DadosItem dadosItem = converterParaDadosItem(dadosConsulta.get());

        registrarLogConsulta(codigo, dadosItem, "Consulta OK.");

        return Optional.of(dadosItem);
    }

    private DadosItem converterParaDadosItem(DadosItemConsulta dadosConsulta) {
        return new DadosItem(
        		dadosConsulta.getCodigoItem(),
        		dadosConsulta.getDescricao(),

        		dadosConsulta.getCustoReposicao(),

        		tratarTexto(
        		        dadosConsulta
        		                .getProcessoReposicao()
        		),

        		tratarBigDecimal(
        		        dadosConsulta.getCustoAtual()
        		),
                tratarBigDecimal(dadosConsulta.getCustoVerdadeiro()),
                tratarBigDecimal(dadosConsulta.getCustoPromob()),
                tratarBigDecimal(dadosConsulta.getCustoAnterior()),

                tratarTexto(dadosConsulta.getRegistroCustoAtual()),
                tratarTexto(dadosConsulta.getRegistroCustoVerdadeiro()),
                tratarTexto(dadosConsulta.getRegistroCustoPromob()),
                tratarTexto(dadosConsulta.getRegistroCustoAnterior()),

                dadosConsulta.getDataCustoAtual(),
                dadosConsulta.getDataCustoVerdadeiro(),
                dadosConsulta.getDataCustoPromob(),
                dadosConsulta.getDataCustoAnterior(),
                dadosConsulta.getPrecoPadraoVenda(),
                dadosConsulta.getIpi(),
                dadosConsulta.getDataUltimaSaida(),
                dadosConsulta.isItemEncalhado()
        );
    }
    
    public Optional<BigDecimal> buscarPrecoPadraoVendaPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }

        return dadosItemService.buscarPrecoPadraoVendaPorCodigo(codigo);
    }

    private BigDecimal tratarBigDecimal(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private String tratarTexto(String texto) {
        return texto == null ? "" : texto;
    }
    private void registrarLogConsulta(String codigo, DadosItem dadosItem, String status) {
        try {
            Path pastaLogs = Path.of(System.getProperty("user.dir"), "logs");
            Files.createDirectories(pastaLogs);

            Path arquivo = pastaLogs.resolve("diagnostico_consultas_itens.txt");

            StringBuilder sb = new StringBuilder();

            sb.append("\n=================================================\n");
            sb.append("Data/Hora: ").append(LocalDateTime.now()).append("\n");
            sb.append("Status: ").append(status).append("\n");
            sb.append("Código pesquisado: ").append(codigo).append("\n");

            if (dadosItem == null) {
                sb.append("DadosItem: null\n");
            } else {
                sb.append("Código retornado: ").append(dadosItem.getCodigo()).append("\n");
                sb.append("Descrição: ").append(dadosItem.getDescricao()).append("\n");

                sb.append("Preço padrão venda: ").append(dadosItem.getPrecoPadraoVenda()).append("\n");
                sb.append("IPI: ").append(dadosItem.getIpi()).append("\n");

                sb.append("Custo atual: ").append(dadosItem.getCustoAtual()).append("\n");
                sb.append("Registro custo atual: ").append(dadosItem.getRegistroCustoAtual()).append("\n");
                sb.append("Data custo atual: ").append(dadosItem.getDataCustoAtual()).append("\n");

                sb.append("Custo anterior: ").append(dadosItem.getCustoAnterior()).append("\n");
                sb.append("Registro custo anterior: ").append(dadosItem.getRegistroCustoAnterior()).append("\n");
                sb.append("Data custo anterior: ").append(dadosItem.getDataCustoAnterior()).append("\n");

                sb.append("Custo Promob: ").append(dadosItem.getCustoPromob()).append("\n");
                sb.append("Registro custo Promob: ").append(dadosItem.getRegistroCustoPromob()).append("\n");
                sb.append("Data custo Promob: ").append(dadosItem.getDataCustoPromob()).append("\n");
            }

            Files.writeString(
                    arquivo,
                    sb.toString(),
                    StandardCharsets.UTF_8,
                    Files.exists(arquivo)
                            ? java.nio.file.StandardOpenOption.APPEND
                            : java.nio.file.StandardOpenOption.CREATE
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}