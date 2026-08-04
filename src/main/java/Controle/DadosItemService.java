package Controle;

import Modelo.*;
import Repositorio.*;

import Configuracao.ConfiguracaoUsuario;
import Configuracao.ConfiguracaoUsuarioService;
import Infraestrutura.DiagnosticoCustoReposicao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DadosItemService {

    private final ItemRepository itemRepository;
    private final CustoRepository custoRepository;
    private final CustoPromobRepository custoPromobRepository;
    private final ItemNfsRepository itemNfsRepository;
    private final CustoVerdadeiroService custoVerdadeiroService;
    private final ConfiguracaoUsuarioService configuracaoUsuarioService = new ConfiguracaoUsuarioService();
    
    
    private boolean custoVerdadeiroAtivo() {
        return configuracaoUsuarioService.carregar().isCustoVerdadeiroAtivo();
    }
    
    public void preCarregarBases() {
        if (itemRepository instanceof ItemRepositorySqlite repositorySqliteItens) {
            repositorySqliteItens.preCarregarCache();
        }

        if (itemRepository instanceof ItemRepositoryXlsb repositoryXlsb) {
            repositoryXlsb.preCarregarCache();
        }

        if (custoRepository instanceof CustoRepositorySqlite repositorySqlite) {
            repositorySqlite.preCarregarCache();
        }

        if (custoPromobRepository instanceof CustoPromobRepositorySqlite repositoryPromobSqlite) {
            repositoryPromobSqlite.preCarregarCache();
        }
        
        if (itemNfsRepository instanceof ItemNfsRepositorySqlite repositoryItemNfsSqlite) {
            repositoryItemNfsSqlite.preCarregarCache();
        }
        if (custoVerdadeiroAtivo()) {
            custoVerdadeiroService.preCarregarCache();
        }
    }

    public void recarregarBases() {
        if (itemRepository instanceof ItemRepositorySqlite repositorySqliteItens) {
            repositorySqliteItens.limparCache();
        }

        if (itemRepository instanceof ItemRepositoryXlsb repositoryXlsb) {
            repositoryXlsb.limparCache();
        }

        if (custoRepository instanceof CustoRepositorySqlite repositorySqlite) {
            repositorySqlite.limparCache();
        }

        if (custoPromobRepository instanceof CustoPromobRepositorySqlite repositoryPromobSqlite) {
            repositoryPromobSqlite.limparCache();
        }
        
        if (itemNfsRepository instanceof ItemNfsRepositorySqlite repositoryItemNfsSqlite) {
            repositoryItemNfsSqlite.limparCache();
        }
        
        if (custoVerdadeiroAtivo()) {
            custoVerdadeiroService.limparCache();
        }

        preCarregarBases();
    }

    public DadosItemService() {
    	this.itemRepository = new ItemRepositorySqlite();
        this.custoRepository = new CustoRepositorySqlite();
        this.custoPromobRepository = new CustoPromobRepositorySqlite();
        this.itemNfsRepository = new ItemNfsRepositorySqlite();
        this.custoVerdadeiroService = new CustoVerdadeiroService();
    }

    public DadosItemService(
            ItemRepository itemRepository,
            CustoRepository custoRepository,
            CustoPromobRepository custoPromobRepository
    ) {
        this(
                itemRepository,
                custoRepository,
                custoPromobRepository,
                new ItemNfsRepositorySqlite()
        );
    }

    public DadosItemService(
            ItemRepository itemRepository,
            CustoRepository custoRepository,
            CustoPromobRepository custoPromobRepository,
            ItemNfsRepository itemNfsRepository
    ) {
        this.itemRepository = itemRepository;
        this.custoRepository = custoRepository;
        this.custoPromobRepository = custoPromobRepository;
        this.itemNfsRepository = itemNfsRepository;
        this.custoVerdadeiroService = new CustoVerdadeiroService();
    }
    
    public Optional<BigDecimal> buscarPrecoPadraoVendaPorCodigo(String codigoItem) {
        if (codigoItem == null || codigoItem.isBlank()) {
            return Optional.empty();
        }

        Optional<ItemCadastro> itemEncontrado = itemRepository.buscarPorCodigo(codigoItem);

        if (itemEncontrado.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal precoLiquidoAtual = tratarBigDecimal(
                itemEncontrado.get().getPrecoUnitarioLiquidoAtual()
        );

        if (precoLiquidoAtual.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        return Optional.of(precoLiquidoAtual.multiply(new BigDecimal("2")));
    }
    
    private boolean calcularItemEncalhado(Optional<HistoricoSaidaItem> ultimaSaidaEncontrada) {
        if (ultimaSaidaEncontrada == null || ultimaSaidaEncontrada.isEmpty()) {
            return false;
        }

        LocalDate dataUltimaSaida = ultimaSaidaEncontrada.get().getDataSaida();

        if (dataUltimaSaida == null) {
            return false;
        }

        LocalDate limite = LocalDate.now().minusMonths(6);

        return !dataUltimaSaida.isAfter(limite);
    }

    public Optional<DadosItemConsulta> buscarDadosCompletosPorCodigo(String codigoItem) {
        if (codigoItem == null || codigoItem.isBlank()) {
            return Optional.empty();
        }

        Optional<ItemCadastro> itemEncontrado = itemRepository.buscarPorCodigo(codigoItem);

        if (itemEncontrado.isEmpty()) {
            return Optional.empty();
        }

        ItemCadastro item = itemEncontrado.get();

        boolean itemImportado = ehItemImportado(codigoItem);

        HistoricoCustoItem historicoCusto = itemImportado
                ? custoRepository.buscarHistoricoPrincipalPorItem(codigoItem)
                : new HistoricoCustoItem(null, null);

        Optional<CustoPromobItem> custoPromobEncontrado =
                custoPromobRepository.buscarCustoMaisRecentePorItem(codigoItem);
        
        Optional<HistoricoSaidaItem> ultimaSaidaEncontrada =
                itemNfsRepository.buscarUltimaSaidaPorItem(codigoItem);

        LocalDate dataUltimaSaida = ultimaSaidaEncontrada
                .map(HistoricoSaidaItem::getDataSaida)
                .orElse(null);

        boolean itemEncalhado = calcularItemEncalhado(ultimaSaidaEncontrada);
        
        CustoReposicaoResolvido
        custoReposicao =
        resolverCustoReposicao(
                codigoItem,
                item
        );

        CustoResolvido custoAtual = resolverCustoAtual(item, historicoCusto, itemImportado);
        CustoResolvido custoVerdadeiro = resolverCustoVerdadeiro(codigoItem);
        CustoResolvido custoAnterior = resolverCustoAnterior(historicoCusto, itemImportado);
        CustoResolvido custoPromob = resolverCustoPromob(custoPromobEncontrado);

        DadosItemConsulta dados = new DadosItemConsulta(
                item.getCodigoItem(),
                item.getDescricao(),
                item.getIpi(),
                item.getNcm(),
                item.getUnidadeVenda(),
                item.getPesoBruto(),
                item.getPesoLiquido(),
                item.getPrecoUnitarioLiquidoAtual(),
                
                custoReposicao.custoBrl(),
                custoReposicao.processo(),

                custoAtual.custo(),
                custoAtual.registro(),
                custoAtual.data(),
                custoAtual.fonte(),
                
                custoVerdadeiro.custo(),
                custoVerdadeiro.registro(),
                custoVerdadeiro.data(),
                custoVerdadeiro.fonte(),

                custoPromob.custo(),
                custoPromob.registro(),
                custoPromob.data(),
                custoPromob.fonte(),

                custoAnterior.custo(),
                custoAnterior.registro(),
                custoAnterior.data(),
                custoAnterior.fonte(),

                dataUltimaSaida,
                itemEncalhado
        );

        return Optional.of(dados);
    }
    
    private CustoReposicaoResolvido
    resolverCustoReposicao(
            String codigoItem
    ) {
        Optional<CustoItem> processoMaisRecente =
                custoRepository
                        .buscarProcessoReposicaoMaisRecentePorItem(
                                codigoItem
                        );

        if (processoMaisRecente.isEmpty()) {
            return new CustoReposicaoResolvido(
                    null,
                    ""
            );
        }

        CustoItem custoItem =
                processoMaisRecente.get();

        /*
         * O próprio CustoItem aplica:
         *
         * CI válida
         * → usa CI
         *
         * CI vazia ou zerada
         * → usa PI
         */
        BigDecimal valorReposicaoUsd =
                custoItem
                        .getValorReposicaoUsd();

        if (valorReposicaoUsd == null
                || valorReposicaoUsd.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            return new CustoReposicaoResolvido(
                    null,
                    ""
            );
        }

        String processoReposicao =
                custoItem.getRegistroImportacao();

        return new CustoReposicaoResolvido(
                valorReposicaoUsd,
                processoReposicao == null
                        ? ""
                        : processoReposicao.trim()
        );
    }
    
    private CustoResolvido resolverCustoVerdadeiro(String codigoItem) {
        if (!custoVerdadeiroAtivo()) {
            return new CustoResolvido(
                    null,
                    "",
                    null,
                    FonteCusto.NAO_ENCONTRADO
            );
        }

        Optional<CustoVerdadeiroItem> custoVerdadeiroEncontrado =
                custoVerdadeiroService.buscarPorItem(codigoItem);
    	
        if (custoVerdadeiroEncontrado.isEmpty()) {
            return new CustoResolvido(
                    null,
                    "",
                    null,
                    FonteCusto.NAO_ENCONTRADO
            );
        }

        CustoVerdadeiroItem custoVerdadeiro = custoVerdadeiroEncontrado.get();

        return new CustoResolvido(
                custoVerdadeiro.getCusto(),
                "LOTES: " + custoVerdadeiro.getQuantidadeLotesUsados(),
                custoVerdadeiro.getDataReferencia(),
                FonteCusto.CUSTO_VERDADEIRO
        );
    }

    private CustoResolvido resolverCustoAtual(
            ItemCadastro item,
            HistoricoCustoItem historicoCusto,
            boolean itemImportado
    ) {
        if (!itemImportado) {
            return new CustoResolvido(
                    null,
                    "",
                    null,
                    FonteCusto.NAO_ENCONTRADO
            );
        }

        if (historicoCusto.possuiCustoAtual()) {
            CustoItem custoBanco = historicoCusto.getCustoAtual().get();

            return new CustoResolvido(
                    custoBanco.getCusto(),
                    custoBanco.getRegistroImportacao(),
                    custoBanco.getDataCusto(),
                    FonteCusto.BANCO_CUSTOS
            );
        }

        BigDecimal custoPlanilha = tratarBigDecimal(item.getCustoUnitarioPlanilha());

        if (custoPlanilha.compareTo(BigDecimal.ZERO) > 0) {
            return new CustoResolvido(
                    custoPlanilha,
                    item.getRegistroCustoPlanilha(),
                    null,
                    FonteCusto.PLANILHA_FALLBACK
            );
        }

        return new CustoResolvido(
                null,
                "",
                null,
                FonteCusto.NAO_ENCONTRADO
        );
    }

    private CustoResolvido resolverCustoAnterior(
            HistoricoCustoItem historicoCusto,
            boolean itemImportado
    ) {
        if (!itemImportado) {
            return new CustoResolvido(
                    null,
                    "",
                    null,
                    FonteCusto.NAO_ENCONTRADO
            );
        }

        if (historicoCusto.possuiCustoAnterior()) {
            CustoItem custoBanco = historicoCusto.getCustoAnterior().get();

            return new CustoResolvido(
                    custoBanco.getCusto(),
                    custoBanco.getRegistroImportacao(),
                    custoBanco.getDataCusto(),
                    FonteCusto.BANCO_CUSTOS
            );
        }

        return new CustoResolvido(
                null,
                "",
                null,
                FonteCusto.NAO_ENCONTRADO
        );
    }

    private CustoResolvido resolverCustoPromob(Optional<CustoPromobItem> custoPromobEncontrado) {
        if (custoPromobEncontrado.isPresent()) {
            CustoPromobItem custoPromob = custoPromobEncontrado.get();

            return new CustoResolvido(
                    custoPromob.getCusto(),
                    "PROMOB",
                    custoPromob.getDataMovimento(),
                    FonteCusto.BANCO_PROMOB
            );
        }

        return new CustoResolvido(
                null,
                "",
                null,
                FonteCusto.NAO_ENCONTRADO
        );
    }

    private BigDecimal tratarBigDecimal(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private boolean ehItemImportado(String codigoItem) {
        if (codigoItem == null) {
            return false;
        }

        String codigoNormalizado = codigoItem.trim().toUpperCase();

        return codigoNormalizado.startsWith("MMR")
                || codigoNormalizado.startsWith("MSC")
                || codigoNormalizado.startsWith("MPR")
        		|| codigoNormalizado.startsWith("MPA");
    }
    
    private record CustoReposicaoResolvido(
            BigDecimal valorUsd,
            String processo
    ) {
    }
    
    private record CustoResolvido(
            BigDecimal custo,
            String registro,
            LocalDate data,
            FonteCusto fonte
    ) {
    }
}