package Controle;

import Modelo.*;
import Repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class DadosItemService {

    private final ItemRepository itemRepository;
    private final CustoRepository custoRepository;
    private final CustoPromobRepository custoPromobRepository;
    
    
    public void preCarregarBases() {
        if (itemRepository instanceof ItemRepositoryXlsb repositoryXlsb) {
            repositoryXlsb.preCarregarCache();
        }

        if (custoRepository instanceof CustoRepositorySqlite repositorySqlite) {
            repositorySqlite.preCarregarCache();
        }

        if (custoPromobRepository instanceof CustoPromobRepositorySqlite repositoryPromobSqlite) {
            repositoryPromobSqlite.preCarregarCache();
        }
    }

    public void recarregarBases() {
        if (itemRepository instanceof ItemRepositoryXlsb repositoryXlsb) {
            repositoryXlsb.limparCache();
        }

        if (custoRepository instanceof CustoRepositorySqlite repositorySqlite) {
            repositorySqlite.limparCache();
        }

        if (custoPromobRepository instanceof CustoPromobRepositorySqlite repositoryPromobSqlite) {
            repositoryPromobSqlite.limparCache();
        }

        preCarregarBases();
    }

    public DadosItemService() {
        this.itemRepository = new ItemRepositoryXlsb();
        this.custoRepository = new CustoRepositorySqlite();
        this.custoPromobRepository = new CustoPromobRepositorySqlite();
    }

    public DadosItemService(
            ItemRepository itemRepository,
            CustoRepository custoRepository,
            CustoPromobRepository custoPromobRepository
    ) {
        this.itemRepository = itemRepository;
        this.custoRepository = custoRepository;
        this.custoPromobRepository = custoPromobRepository;
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

        CustoResolvido custoAtual = resolverCustoAtual(item, historicoCusto, itemImportado);
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

                custoAtual.custo(),
                custoAtual.registro(),
                custoAtual.data(),
                custoAtual.fonte(),

                custoPromob.custo(),
                custoPromob.registro(),
                custoPromob.data(),
                custoPromob.fonte(),

                custoAnterior.custo(),
                custoAnterior.registro(),
                custoAnterior.data(),
                custoAnterior.fonte()
        );

        return Optional.of(dados);
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
                || codigoNormalizado.startsWith("MPR");
    }
    
    private record CustoResolvido(
            BigDecimal custo,
            String registro,
            LocalDate data,
            FonteCusto fonte
    ) {
    }
}