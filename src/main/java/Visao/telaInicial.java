package Visao;

import Controle.AnaliseService;
import Controle.ItemService;
import Modelo.DadosItem;
import Modelo.EstadoInfo;
import Modelo.ItemAnalise;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import Visao.componentes.MiniTabelaEstadoBase;
import Visao.componentes.TabelaResumo;

public class telaInicial extends Application {
    private final ItemService itemService = new ItemService();
    private final AnaliseService analiseService = new AnaliseService();
    private final ObservableList<ItemAnalise> itens = FXCollections.observableArrayList();

    private final Label totalPropostaLabel = new Label("R$ 0,00");
    private final Label resultadoAtualComIpiLabel = new Label("R$ 0,00");
    private final Label resultadoAnteriorComIpiLabel = new Label("R$ 0,00");
    private final Label totalComIpiLabel = new Label("R$ 0,00");
    private final Label resultadoAtualLabel = new Label("R$ 0,00");
    private final Label resultadoAnteriorLabel = new Label("R$ 0,00");
    private final ComboBox<EstadoInfo> estadoCombo = new ComboBox<>();
    private final Label baseEstadoLabel = new Label("0,00%");
    
    
    private TableView<ItemAnalise> tabela;
    private static final double ALTURA_LINHA_TABELA = 24;
    private static final double ALTURA_CABECALHO_TABELA = 42;
    private static final double ALTURA_BARRA_HORIZONTAL = 18;
    private static final double MARGEM_SEGURANCA_TABELA = 12;
    
    private static final String CODIGO_LINHA_RODAPE_TABELA = "__RODAPE_TABELA__";
    private final ItemAnalise linhaRodapeTabela = new ItemAnalise();
    
    private StackPane overlayAtualizacao;
    
    private final java.util.List<Label> labelsRodapeTabela = new java.util.ArrayList<>();

    private static final int IDX_QUANTIDADE = 2;
    private static final int IDX_VALOR_TOTAL = 4;
    private static final int IDX_MARGEM_ATUAL = 6;
    private static final int IDX_MARGEM_PROMOB = 9;
    private static final int IDX_MARGEM_ANTERIOR = 13;
    
    private ComboBox<String> comboEstado;
    private Label lblBaseEstado;

    private final Map<String, String> basePorEstado = Map.of(
        "SC", "40,22%",
        "PR", "38,10%",
        "RS", "39,05%",
        "SP", "36,80%"
    );
    

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        prepararDadosIniciais();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-app");

        VBox topo = criarTopo();
        TableView<ItemAnalise> tabelaCriada = criarTabela();

        VBox areaCentral = new VBox(tabelaCriada);
        areaCentral.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(tabelaCriada, Priority.NEVER);

        HBox rodape = criarRodape();
        root.setTop(topo);
        root.setCenter(areaCentral);
        root.setBottom(rodape);

        StackPane rootComOverlay = new StackPane(root, criarOverlayAtualizacao());

        Scene scene = new Scene(rootComOverlay, 1180, 680);
        scene.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());

        tabelaCriada.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> calcularAlturaTabela(root, topo, rodape),
                        itens,
                        root.heightProperty(),
                        topo.heightProperty(),
                        rodape.heightProperty()
                )
        );

        tabelaCriada.minHeightProperty().bind(tabelaCriada.prefHeightProperty());
        tabelaCriada.maxHeightProperty().bind(tabelaCriada.prefHeightProperty());

        stage.setTitle("Programa de Análise de Margem");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(600);
        stage.show();
        preCarregarBasesAoIniciar();
    }
    
    private boolean isLinhaRodapeTabela(ItemAnalise item) {
        return item == linhaRodapeTabela
                || item != null
                && CODIGO_LINHA_RODAPE_TABELA.equals(item.getCodigo());
    }

    private void adicionarLinhaVazia() {
        int indiceRodape = itens.indexOf(linhaRodapeTabela);

        if (indiceRodape < 0) {
            itens.add(new ItemAnalise());
            itens.add(linhaRodapeTabela);
            return;
        }

        itens.add(indiceRodape, new ItemAnalise());
        recalcularResumo();
        tabela.refresh();
    }

    private void garantirRodapeNoFinal() {
        itens.remove(linhaRodapeTabela);
        itens.add(linhaRodapeTabela);
    }

    private void atualizarLinhaRodapeTabela() {
        int quantidadeTotal = 0;
        BigDecimal valorTotalProposta = BigDecimal.ZERO;

        BigDecimal somaMargemAtual = BigDecimal.ZERO;
        BigDecimal somaMargemPromob = BigDecimal.ZERO;
        BigDecimal somaMargemAnterior = BigDecimal.ZERO;

        int qtdMargemAtual = 0;
        int qtdMargemPromob = 0;
        int qtdMargemAnterior = 0;

        for (ItemAnalise item : itens) {
            if (isLinhaRodapeTabela(item) || !temCodigoPreenchido(item)) {
                continue;
            }

            quantidadeTotal += item.getQuantidade();
            valorTotalProposta = valorTotalProposta.add(valorSeguro(item.getValorTotal()));

            if (margemValidaParaMedia(item.getMargemAtual(), item.getCustoAtual(), item.getValorUnitario())) {
                somaMargemAtual = somaMargemAtual.add(item.getMargemAtual());
                qtdMargemAtual++;
            }

            if (margemValidaParaMedia(item.getMargemPromob(), item.getCustoPromob(), item.getValorUnitario())) {
                somaMargemPromob = somaMargemPromob.add(item.getMargemPromob());
                qtdMargemPromob++;
            }

            if (margemValidaParaMedia(item.getMargemAnterior(), item.getCustoAnterior(), item.getValorUnitario())) {
                somaMargemAnterior = somaMargemAnterior.add(item.getMargemAnterior());
                qtdMargemAnterior++;
            }
        }

        linhaRodapeTabela.setCodigo(CODIGO_LINHA_RODAPE_TABELA);
        linhaRodapeTabela.setDescricao("TOTAL");

        linhaRodapeTabela.setQuantidade(quantidadeTotal);
        linhaRodapeTabela.setValorTotal(valorTotalProposta);

        linhaRodapeTabela.setMargemAtual(calcularMediaOuZero(somaMargemAtual, qtdMargemAtual));
        linhaRodapeTabela.setMargemPromob(calcularMediaOuZero(somaMargemPromob, qtdMargemPromob));
        linhaRodapeTabela.setMargemAnterior(calcularMediaOuZero(somaMargemAnterior, qtdMargemAnterior));

        garantirRodapeNoFinal();
    }

    private boolean margemValidaParaMedia(BigDecimal margem, BigDecimal custo, BigDecimal valorUnitario) {
        return margem != null
                && valorSeguro(custo).compareTo(BigDecimal.ZERO) > 0
                && valorSeguro(valorUnitario).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal calcularMediaOuZero(BigDecimal soma, int quantidade) {
        if (quantidade <= 0) {
            return BigDecimal.ZERO;
        }

        return soma.divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
    }

    private void prepararDadosIniciais() {
        estadoCombo.setItems(FXCollections.observableArrayList(
                new EstadoInfo("SC", new BigDecimal("40.22")),
                new EstadoInfo("PR", new BigDecimal("39.50")),
                new EstadoInfo("RS", new BigDecimal("38.75")),
                new EstadoInfo("SP", new BigDecimal("36.00"))
        ));
        estadoCombo.getSelectionModel().selectFirst();
        estadoCombo.valueProperty().addListener((obs, oldValue, newValue) -> atualizarBaseEstado());
        atualizarBaseEstado();

        for (int i = 0; i < 5; i++) {
            itens.add(new ItemAnalise());
        }

        linhaRodapeTabela.setCodigo(CODIGO_LINHA_RODAPE_TABELA);
        linhaRodapeTabela.setDescricao("TOTAL");
        itens.add(linhaRodapeTabela);

        recalcularResumo();
    }

    private ImageView carregarIcone(String nomeArquivo, double largura, double altura) {
        ImageView icone = new ImageView(
                new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/icons/" + nomeArquivo)
                )
        );

        icone.setFitWidth(largura);
        icone.setFitHeight(altura);
        icone.setPreserveRatio(true);
        icone.setSmooth(true);

        return icone;
    }
    
    private void atualizarDados() {
        mostrarOverlayAtualizacao(true);
        long inicioAtualizacaoMs = System.currentTimeMillis();
        long tempoMinimoExibicaoMs = sortearTempoMinimoAtualizacaoMs();
        

        List<Integer> indices = new ArrayList<>();
        List<String> codigos = new ArrayList<>();

        for (int i = 0; i < itens.size(); i++) {
            ItemAnalise item = itens.get(i);

            if (isLinhaRodapeTabela(item)) {
                continue;
            }

            if (!temCodigoPreenchido(item)) {
                continue;
            }

            indices.add(i);
            codigos.add(item.getCodigo());
        }

        Task<List<AtualizacaoLinha>> tarefa = new Task<>() {
            @Override
            protected List<AtualizacaoLinha> call() throws Exception {
                itemService.recarregarBases();

                List<AtualizacaoLinha> atualizacoes = new ArrayList<>();

                for (int i = 0; i < codigos.size(); i++) {
                    String codigo = codigos.get(i);
                    int indiceLinha = indices.get(i);

                    DadosItem dadosItem = itemService.buscarPorCodigo(codigo).orElse(null);
                    atualizacoes.add(new AtualizacaoLinha(indiceLinha, dadosItem));
                }

                aguardarTempoMinimo(inicioAtualizacaoMs, tempoMinimoExibicaoMs);

                return atualizacoes;
            }
        };

        tarefa.setOnSucceeded(event -> {
            List<AtualizacaoLinha> atualizacoes = tarefa.getValue();

            for (AtualizacaoLinha atualizacao : atualizacoes) {
                int indice = atualizacao.indiceLinha();

                if (indice < 0 || indice >= itens.size()) {
                    continue;
                }

                ItemAnalise item = itens.get(indice);

                if (isLinhaRodapeTabela(item)) {
                    continue;
                }

                item.aplicarDadosItem(atualizacao.dadosItem());
                analiseService.recalcular(item);
            }

            recalcularResumo();
            tabela.refresh();
            mostrarOverlayAtualizacao(false);

            mostrarAviso(
                    "Atualização concluída",
                    "As bases foram recarregadas e os itens da tabela foram atualizados."
            );
        });

        tarefa.setOnFailed(event -> {
            mostrarOverlayAtualizacao(false);

            Throwable erro = tarefa.getException();

            mostrarAviso(
                    "Erro na atualização",
                    erro == null
                            ? "Não foi possível atualizar os dados."
                            : "Não foi possível atualizar os dados:\n" + erro.getMessage()
            );

            if (erro != null) {
                erro.printStackTrace();
            }
        });

        Thread thread = new Thread(tarefa, "AtualizacaoDadosThread");
        thread.setDaemon(true);
        thread.start();
    }
    private VBox criarTopo() {
        VBox topo = new VBox();
        topo.getStyleClass().add("topo");

        HBox cabecalho = new HBox(20);
        cabecalho.getStyleClass().add("cabecalho");
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.setPadding(new Insets(12, 14, 10, 14));cabecalho.setPadding(new Insets(10, 14, 8, 14));
        cabecalho.setMinHeight(72);
        cabecalho.setPrefHeight(72);
        
        
        VBox logoBox = new VBox();
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPrefWidth(150);
        logoBox.setMinWidth(150);
        logoBox.setMaxWidth(150);

        ImageView logo = carregarIcone("logo-plasnox.png", 80, 48);
        logoBox.getChildren().add(logo);

        Label titulo = new Label("ANÁLISE DE MARGEM EM ORÇAMENTO COMERCIAL");
        titulo.getStyleClass().add("titulo-principal");
        HBox.setHgrow(titulo, Priority.ALWAYS);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);

        Button configButton = new Button();
        configButton.setGraphic(carregarIcone("config.png", 18, 18));
        configButton.getStyleClass().add("botao-config");
        configButton.setTooltip(new Tooltip("Configurações"));

        cabecalho.getChildren().addAll(logoBox, titulo, configButton);

        HBox comandos = new HBox(18);
        comandos.getStyleClass().add("area-comandos");
        comandos.setAlignment(Pos.CENTER_LEFT);
        comandos.setPadding(new Insets(8, 14, 8, 14));
        comandos.setFillHeight(false);
        

        Button atualizarButton = new Button("ATUALIZAR DADOS");
        atualizarButton.setGraphic(carregarIcone("atualizar.png", 14, 14));
        atualizarButton.setContentDisplay(ContentDisplay.LEFT);
        atualizarButton.setGraphicTextGap(4);
        atualizarButton.getStyleClass().add("botao-acao");
        atualizarButton.setOnAction(event -> atualizarDados());
        
        MiniTabelaEstadoBase estadoBaseTabela = new MiniTabelaEstadoBase(estadoCombo, baseEstadoLabel);

        Button valorPadraoButton = new Button("BUSCAR VALOR PADRÃO");
        valorPadraoButton.setGraphic(carregarIcone("lupa.png", 14, 14));
        valorPadraoButton.setContentDisplay(ContentDisplay.LEFT);
        valorPadraoButton.setGraphicTextGap(4);
        valorPadraoButton.getStyleClass().add("botao-busca");
        valorPadraoButton.setOnAction(event -> aplicarValorPadrao());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        GridPane resumo = new TabelaResumo(
                totalPropostaLabel,
                totalComIpiLabel,
                resultadoAtualLabel,
                resultadoAtualComIpiLabel,
                resultadoAnteriorLabel,
                resultadoAnteriorComIpiLabel
        		);

        comandos.getChildren().addAll(atualizarButton, estadoBaseTabela, valorPadraoButton, spacer, resumo);

        topo.getChildren().addAll(cabecalho, comandos);
        return topo;
    }
    
    


    
    private TableView<ItemAnalise> criarTabela() {
        tabela = new TableView<>(itens);
        tabela.setEditable(true);
        tabela.getStyleClass().add("tabela-analise");
        tabela.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tabela.setFixedCellSize(ALTURA_LINHA_TABELA);
        
        
        TableColumn<ItemAnalise, String> codigoCol = new TableColumn<>("CÓDIGO");
        codigoCol.setCellValueFactory(data -> data.getValue().codigoProperty());
        codigoCol.setCellFactory(col -> new CodigoComBotaoCell());
        codigoCol.setPrefWidth(170);

        TableColumn<ItemAnalise, String> descricaoCol = new TableColumn<>("DESCRIÇÃO");
        descricaoCol.setCellValueFactory(data -> data.getValue().descricaoProperty());
        descricaoCol.setPrefWidth(340);

        TableColumn<ItemAnalise, Integer> quantidadeCol = new TableColumn<>("QUANTIDADE");
        quantidadeCol.setCellValueFactory(data -> data.getValue().quantidadeProperty().asObject());
        quantidadeCol.setCellFactory(col -> new QuantidadeEditavelCell());
        quantidadeCol.setPrefWidth(105);

        TableColumn<ItemAnalise, BigDecimal> valorUnitarioCol = new TableColumn<>("VALOR UNITÁRIO");
        valorUnitarioCol.setCellValueFactory(data -> data.getValue().valorUnitarioProperty());
        valorUnitarioCol.setCellFactory(col -> new ValorUnitarioEditavelCell());
        valorUnitarioCol.setPrefWidth(125);

        TableColumn<ItemAnalise, BigDecimal> valorTotalCol = new TableColumn<>("VALOR TOTAL");
        valorTotalCol.setCellValueFactory(data -> data.getValue().valorTotalProperty());
        valorTotalCol.setCellFactory(col -> new BigDecimalTableCell(true, true));
        valorTotalCol.setPrefWidth(125);

        TableColumn<ItemAnalise, BigDecimal> variacaoAtualCol = new TableColumn<>("VARIAÇÃO ATUAL");
        variacaoAtualCol.setCellValueFactory(data -> data.getValue().variacaoAtualProperty());
        variacaoAtualCol.setCellFactory(col -> new BigDecimalTableCell(false));
        variacaoAtualCol.getStyleClass().add("coluna-vermelha");
        variacaoAtualCol.setPrefWidth(115);

        TableColumn<ItemAnalise, BigDecimal> margemAtualCol = new TableColumn<>("MARGEM ATUAL");
        margemAtualCol.setCellValueFactory(data -> data.getValue().margemAtualProperty());
        margemAtualCol.setCellFactory(col -> new MargemTableCell());
        margemAtualCol.getStyleClass().add("coluna-vermelha");
        margemAtualCol.setPrefWidth(115);
        
        TableColumn<ItemAnalise, BigDecimal> custoAtualCol = new TableColumn<>("CUSTO");
        custoAtualCol.setCellValueFactory(data ->
        new ReadOnlyObjectWrapper<>(data.getValue().getCustoAtual())
);
        custoAtualCol.setCellFactory(col -> new CustoTableCell());
        custoAtualCol.getStyleClass().add("coluna-vermelha");
        custoAtualCol.setPrefWidth(105);
        
        TableColumn<ItemAnalise, LocalDate> dataCustoCol = new TableColumn<>("DATA CUSTO");
        dataCustoCol.setCellValueFactory(data -> data.getValue().dataCustoAtualProperty());
        dataCustoCol.setCellFactory(col -> new DataTableCell());
        dataCustoCol.getStyleClass().add("coluna-vermelha");
        dataCustoCol.setPrefWidth(110);


        TableColumn<ItemAnalise, BigDecimal> margemPromobCol = new TableColumn<>("MARGEM PROMOB");
        margemPromobCol.setCellValueFactory(data -> data.getValue().margemPromobProperty());
        margemPromobCol.setCellFactory(col -> new MargemTableCell());
        margemPromobCol.getStyleClass().add("coluna-laranja");
        margemPromobCol.setPrefWidth(130);
        
        TableColumn<ItemAnalise, BigDecimal> custoPromobCol = new TableColumn<>("CUSTO");
        custoPromobCol.setCellValueFactory(data ->
        new ReadOnlyObjectWrapper<>(data.getValue().getCustoPromob())
);
        custoPromobCol.setCellFactory(col -> new CustoTableCell());
        custoPromobCol.getStyleClass().add("coluna-laranja");
        custoPromobCol.setPrefWidth(105);
        
        TableColumn<ItemAnalise, LocalDate> dataCustoPromobCol = new TableColumn<>("DATA CUSTO");
        dataCustoPromobCol.setCellValueFactory(data -> data.getValue().dataCustoPromobProperty());
        dataCustoPromobCol.setCellFactory(col -> new DataTableCell());
        dataCustoPromobCol.getStyleClass().add("coluna-laranja");
        dataCustoPromobCol.setPrefWidth(110);


        TableColumn<ItemAnalise, BigDecimal> variacaoAnteriorCol = new TableColumn<>("VARIAÇÃO ANTERIOR");
        variacaoAnteriorCol.setCellValueFactory(data -> data.getValue().variacaoAnteriorProperty());
        variacaoAnteriorCol.setCellFactory(col -> new BigDecimalTableCell(false));
        variacaoAnteriorCol.getStyleClass().add("coluna-azul");
        variacaoAnteriorCol.setPrefWidth(140);

        TableColumn<ItemAnalise, BigDecimal> margemAnteriorCol = new TableColumn<>("MARGEM ANTERIOR");
        margemAnteriorCol.setCellValueFactory(data -> data.getValue().margemAnteriorProperty());
        margemAnteriorCol.setCellFactory(col -> new MargemTableCell());
        margemAnteriorCol.getStyleClass().add("coluna-azul");
        margemAnteriorCol.setPrefWidth(140);
        
        TableColumn<ItemAnalise, BigDecimal> custoAnteriorCol = new TableColumn<>("CUSTO");
        custoAnteriorCol.setCellValueFactory(data ->
        new ReadOnlyObjectWrapper<>(data.getValue().getCustoAnterior())
);
        custoAnteriorCol.setCellFactory(col -> new CustoTableCell());
        custoAnteriorCol.getStyleClass().add("coluna-azul");
        custoAnteriorCol.setPrefWidth(105);
        
        TableColumn<ItemAnalise, LocalDate> dataCustoAnteriorCol = new TableColumn<>("DATA CUSTO");
        dataCustoAnteriorCol.setCellValueFactory(data -> data.getValue().dataCustoAnteriorProperty());
        dataCustoAnteriorCol.setCellFactory(col -> new DataTableCell());
        dataCustoAnteriorCol.getStyleClass().add("coluna-azul");
        dataCustoAnteriorCol.setPrefWidth(110);

        TableColumn<ItemAnalise, String> baseAtualGrupo = new TableColumn<>("BASE ATUAL (GERENCIAL)");
        baseAtualGrupo.getColumns().addAll(
                variacaoAtualCol,
                margemAtualCol,
                custoAtualCol,
                dataCustoCol
        );
        baseAtualGrupo.getStyleClass().add("grupo-vermelho");

        TableColumn<ItemAnalise, String> basePromobGrupo = new TableColumn<>("BASE PROMOB");
        basePromobGrupo.getColumns().addAll(
                margemPromobCol,
                custoPromobCol,
                dataCustoPromobCol
        );
        basePromobGrupo.getStyleClass().add("grupo-laranja");

        TableColumn<ItemAnalise, String> analiseAnteriorGrupo = new TableColumn<>("ANÁLISE ANTERIOR");
        analiseAnteriorGrupo.getColumns().addAll(
                variacaoAnteriorCol,
                margemAnteriorCol,
                custoAnteriorCol,
                dataCustoAnteriorCol
        );
        analiseAnteriorGrupo.getStyleClass().add("grupo-azul");

        tabela.getColumns().addAll(
                codigoCol,
                descricaoCol,
                quantidadeCol,
                valorUnitarioCol,
                valorTotalCol,
                baseAtualGrupo,
                basePromobGrupo,
                analiseAnteriorGrupo
        );

        tabela.setRowFactory(tv -> {
            TableRow<ItemAnalise> row = new TableRow<>();

            row.itemProperty().addListener((obs, itemAntigo, itemNovo) -> {
                row.getStyleClass().remove("linha-rodape-tabela");

                if (isLinhaRodapeTabela(itemNovo)) {
                    row.getStyleClass().add("linha-rodape-tabela");
                }
            });

            row.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER
                        && !row.isEmpty()
                        && !isLinhaRodapeTabela(row.getItem())) {
                    buscarDadosDoItem(row.getItem());
                }
            });

            return row;
        });

        return tabela;
    }

    private HBox criarRodape() {
        HBox rodape = new HBox(12);
        rodape.getStyleClass().add("rodape");
        rodape.setPadding(new Insets(8, 12, 8, 12));
        rodape.setAlignment(Pos.CENTER_LEFT);

        Label ajuda = new Label("Dica: edite Código, Quantidade e Valor Unitário. Ao confirmar o código, a descrição e custos são buscados na base simulada.");
        ajuda.getStyleClass().add("texto-rodape");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button adicionarLinha = new Button("+ Adicionar linha");
        adicionarLinha.getStyleClass().add("botao-acao");
        adicionarLinha.setOnAction(event -> adicionarLinhaVazia());

        Button removerLinha = new Button("- Remover linha");
        removerLinha.getStyleClass().add("botao-acao");
        removerLinha.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> tabela == null
                                || tabela.getSelectionModel().getSelectedItem() == null
                                || isLinhaRodapeTabela(tabela.getSelectionModel().getSelectedItem()),
                        tabela.getSelectionModel().selectedItemProperty()
                )
        );
        removerLinha.setOnAction(event -> {
            ItemAnalise selecionado = tabela.getSelectionModel().getSelectedItem();

            if (selecionado != null && !isLinhaRodapeTabela(selecionado)) {
                itens.remove(selecionado);
                recalcularResumo();
                tabela.refresh();
            }
        });

        rodape.getChildren().addAll(ajuda, spacer, adicionarLinha, removerLinha);
        return rodape;
    }

    private void buscarDadosDoItem(ItemAnalise item) {
        if (item == null || isLinhaRodapeTabela(item)) {
            return;
        }

        item.aplicarDadosItem(itemService.buscarPorCodigo(item.getCodigo()).orElse(null));
        analiseService.recalcular(item);
        recalcularResumo();
        tabela.refresh();
    }
    
    private record AtualizacaoLinha(int indiceLinha, DadosItem dadosItem) {
    }

    private void aplicarValorPadrao() {
        int itensAtualizados = 0;
        int itensSemPrecoPadrao = 0;

        for (ItemAnalise item : itens) {
        	if (isLinhaRodapeTabela(item)) {
        	    continue;
        	}
            if (!temCodigoPreenchido(item)) {
                continue;
            }

            if (!valorUnitarioEstaVazio(item)) {
                continue;
            }

            Optional<BigDecimal> precoPadrao =
                    itemService.buscarPrecoPadraoVendaPorCodigo(item.getCodigo());

            if (precoPadrao.isEmpty()) {
                itensSemPrecoPadrao++;
                continue;
            }

            item.setValorUnitario(precoPadrao.get());

            // Garante que os demais dados do item estejam carregados antes de recalcular.
            if (item.getDescricao() == null || item.getDescricao().isBlank()) {
                item.aplicarDadosItem(itemService.buscarPorCodigo(item.getCodigo()).orElse(null));
            }

            analiseService.recalcular(item);
            itensAtualizados++;
        }

        recalcularResumo();
        tabela.refresh();

        if (itensAtualizados == 0) {
            mostrarAviso(
                    "Valor padrão",
                    "Nenhum item foi atualizado. Verifique se existem itens com código preenchido, valor unitário vazio e preço líquido cadastrado na base de itens."
            );
        }
    }
    
    private boolean temCodigoPreenchido(ItemAnalise item) {
        return item != null
                && item.getCodigo() != null
                && !item.getCodigo().trim().isEmpty();
    }

    private boolean valorUnitarioEstaVazio(ItemAnalise item) {
        if (item == null || item.getValorUnitario() == null) {
            return true;
        }

        return item.getValorUnitario().compareTo(BigDecimal.ZERO) == 0;
    }

    private void atualizarBaseEstado() {
        EstadoInfo estado = estadoCombo.getValue();
        if (estado != null) {
            baseEstadoLabel.setText(formatarPercentual(estado.getBaseMargem()));
        }
    }

    private void recalcularResumo() {
    BigDecimal totalProposta = BigDecimal.ZERO;
    BigDecimal totalIpiProposta = BigDecimal.ZERO;

    BigDecimal resultadoAtual = BigDecimal.ZERO;
    BigDecimal totalIpiAtual = BigDecimal.ZERO;

    BigDecimal resultadoAnterior = BigDecimal.ZERO;
    BigDecimal totalIpiAnterior = BigDecimal.ZERO;

    for (ItemAnalise item : itens) {
        if (isLinhaRodapeTabela(item)) {
            continue;
        }

        if (!temCodigoPreenchido(item)) {
            continue;
        }

        BigDecimal valorTotal = valorSeguro(item.getValorTotal());
        BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());

        BigDecimal custoAtualLinha = valorSeguro(item.getCustoAtual()).multiply(quantidade);
        BigDecimal custoAnteriorLinha = valorSeguro(item.getCustoAnterior()).multiply(quantidade);

        totalProposta = totalProposta.add(valorTotal);
        totalIpiProposta = totalIpiProposta.add(valorSeguro(item.getIpiProposta()));

        resultadoAtual = resultadoAtual.add(valorTotal.subtract(custoAtualLinha));
        totalIpiAtual = totalIpiAtual.add(valorSeguro(item.getIpiAtual()));

        resultadoAnterior = resultadoAnterior.add(valorTotal.subtract(custoAnteriorLinha));
        totalIpiAnterior = totalIpiAnterior.add(valorSeguro(item.getIpiAnterior()));
    }

    totalPropostaLabel.setText(formatarMoeda(totalProposta));
    totalComIpiLabel.setText(formatarMoeda(totalProposta.add(totalIpiProposta)));

    resultadoAtualLabel.setText(formatarMoeda(resultadoAtual));
    resultadoAtualComIpiLabel.setText(formatarMoeda(resultadoAtual.add(totalIpiAtual)));

    resultadoAnteriorLabel.setText(formatarMoeda(resultadoAnterior));
    resultadoAnteriorComIpiLabel.setText(formatarMoeda(resultadoAnterior.add(totalIpiAnterior)));

    atualizarLinhaRodapeTabela();
}

    private void mostrarAviso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private static String formatarMoeda(BigDecimal valor) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return format.format(valor == null ? BigDecimal.ZERO : valor);
    }

    private static String formatarPercentual(BigDecimal valor) {
        if (valor == null) {
            return "0,00%";
        }
        return valor.setScale(2, RoundingMode.HALF_UP).toString().replace('.', ',') + "%";
    }

    private class QuantidadeEditavelCell extends TableCell<ItemAnalise, Integer> {
        private final TextField campo = new TextField();

        public QuantidadeEditavelCell() {
            campo.getStyleClass().add("campo-codigo-tabela");
            campo.setAlignment(Pos.CENTER);
            campo.setMaxWidth(Double.MAX_VALUE);

            campo.setOnAction(event -> confirmarValor());

            campo.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.isShortcutDown() && event.getCode() == KeyCode.V) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();

                    if (!clipboard.hasString()) {
                        return;
                    }

                    String textoColado = clipboard.getString();

                    if (!textoTemMultiplasCelulas(textoColado)) {
                        return;
                    }

                    colarQuantidadesEmLote(textoColado, getIndex());
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(Integer valor, boolean empty) {
            super.updateItem(valor, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            
            if (isLinhaRodapeTabela(getTableRow().getItem())) {
                setGraphic(null);
                setText(valor == null ? "" : valor.toString());
                setAlignment(Pos.CENTER);
                return;
            }

            campo.setText(valor == null || valor == 0 ? "" : valor.toString());
            setText(null);
            setGraphic(campo);
        }

        private void confirmarValor() {
            ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();

            if (item == null) {
                return;
            }

            int novaQuantidade = converterQuantidade(campo.getText());

            if (novaQuantidade != item.getQuantidade()) {
                item.setQuantidade(novaQuantidade);
                analiseService.recalcular(item);
                recalcularResumo();
                tabela.refresh();
            }
        }

        private int converterQuantidade(String texto) {
            if (texto == null || texto.trim().isEmpty()) {
                return 0;
            }

            try {
                return Integer.parseInt(texto.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
    
    private class ValorUnitarioEditavelCell extends TableCell<ItemAnalise, BigDecimal> {
        private final TextField campo = new TextField();

        public ValorUnitarioEditavelCell() {
            campo.getStyleClass().add("campo-codigo-tabela");
            campo.setAlignment(Pos.CENTER_RIGHT);
            campo.setMaxWidth(Double.MAX_VALUE);

            campo.setOnAction(event -> confirmarValor());

            campo.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.isShortcutDown() && event.getCode() == KeyCode.V) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();

                    if (!clipboard.hasString()) {
                        return;
                    }

                    String textoColado = clipboard.getString();

                    if (!textoTemMultiplasCelulas(textoColado)) {
                        return;
                    }

                    colarValoresUnitariosEmLote(textoColado, getIndex());
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(BigDecimal valor, boolean empty) {
            super.updateItem(valor, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            
            if (isLinhaRodapeTabela(getTableRow().getItem())) {
                setGraphic(null);
                setText("");
                return;
            }

            campo.setText(formatarValorEditavel(valor));
            setText(null);
            setGraphic(campo);
        }

        private void confirmarValor() {
            ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();

            if (item == null) {
                return;
            }

            BigDecimal novoValor = converterValor(campo.getText());

            if (novoValor.compareTo(item.getValorUnitario()) != 0) {
                item.setValorUnitario(novoValor);
                analiseService.recalcular(item);
                recalcularResumo();
                tabela.refresh();
            }
        }

        private String formatarValorEditavel(BigDecimal valor) {
            if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
                return "";
            }

            return valor.setScale(2, RoundingMode.HALF_UP)
                    .toString()
                    .replace(".", ",");
        }

        private BigDecimal converterValor(String texto) {
            if (texto == null || texto.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }

            try {
                return new BigDecimal(
                        texto.trim()
                                .replace("R$", "")
                                .replace(".", "")
                                .replace(",", ".")
                );
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
    }
    
    private boolean textoTemMultiplasCelulas(String texto) {
        if (texto == null) {
            return false;
        }

        return texto.contains("\n")
                || texto.contains("\r")
                || texto.contains("\t");
    }

    private void colarQuantidadesEmLote(String textoColado, int indiceInicial) {
        List<String> valores = extrairPrimeiraColunaColada(textoColado);

        if (valores.isEmpty()) {
            return;
        }

        int indiceLinha = Math.max(indiceInicial, 0);

        for (String valorTexto : valores) {
            garantirLinhaExistente(indiceLinha);

            ItemAnalise item = itens.get(indiceLinha);
            item.setQuantidade(converterQuantidadeColada(valorTexto));

            analiseService.recalcular(item);

            indiceLinha++;
        }

        recalcularResumo();
        tabela.refresh();
    }

    private void colarValoresUnitariosEmLote(String textoColado, int indiceInicial) {
        List<String> valores = extrairPrimeiraColunaColada(textoColado);

        if (valores.isEmpty()) {
            return;
        }

        int indiceLinha = Math.max(indiceInicial, 0);

        for (String valorTexto : valores) {
            garantirLinhaExistente(indiceLinha);

            ItemAnalise item = itens.get(indiceLinha);
            item.setValorUnitario(converterMoedaColada(valorTexto));

            analiseService.recalcular(item);

            indiceLinha++;
        }

        recalcularResumo();
        tabela.refresh();
    }

    private List<String> extrairPrimeiraColunaColada(String textoColado) {
        List<String> valores = new ArrayList<>();

        if (textoColado == null || textoColado.isBlank()) {
            return valores;
        }

        String textoNormalizado = textoColado
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        String[] linhas = textoNormalizado.split("\n");

        for (String linha : linhas) {
            if (linha == null || linha.isBlank()) {
                continue;
            }

            String[] celulas = linha.split("\t");

            if (celulas.length == 0) {
                continue;
            }

            String valor = celulas[0] == null ? "" : celulas[0].trim();

            if (!valor.isEmpty()) {
                valores.add(valor);
            }
        }

        return valores;
    }

    private int converterQuantidadeColada(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal converterMoedaColada(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(
                    texto.trim()
                            .replace("R$", "")
                            .replace(".", "")
                            .replace(",", ".")
            );
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
    
    private void colarCodigosEmLote(String textoColado, int indiceInicial) {
        List<String> codigos = extrairCodigosColados(textoColado);

        if (codigos.isEmpty()) {
            return;
        }

        int indiceLinha = Math.max(indiceInicial, 0);

        for (String codigo : codigos) {
            garantirLinhaExistente(indiceLinha);

            ItemAnalise item = itens.get(indiceLinha);
            item.setCodigo(codigo);

            item.aplicarDadosItem(itemService.buscarPorCodigo(codigo).orElse(null));
            analiseService.recalcular(item);

            indiceLinha++;
        }

        recalcularResumo();
        tabela.refresh();
    }

    private List<String> extrairCodigosColados(String textoColado) {
        List<String> codigos = new ArrayList<>();

        if (textoColado == null || textoColado.isBlank()) {
            return codigos;
        }

        String textoNormalizado = textoColado
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        String[] linhas = textoNormalizado.split("\n");

        for (String linha : linhas) {
            if (linha == null || linha.isBlank()) {
                continue;
            }

            String[] celulas = linha.split("\t");

            for (String celula : celulas) {
                String codigo = celula == null ? "" : celula.trim();

                if (!codigo.isEmpty()) {
                    codigos.add(codigo.toUpperCase());
                    break;
                }
            }
        }

        return codigos;
    }

    private void garantirLinhaExistente(int indice) {
        int indiceRodape = itens.indexOf(linhaRodapeTabela);

        if (indiceRodape < 0) {
            itens.add(linhaRodapeTabela);
            indiceRodape = itens.indexOf(linhaRodapeTabela);
        }

        while (itens.size() <= indice || indice >= indiceRodape) {
            itens.add(indiceRodape, new ItemAnalise());
            indiceRodape++;
        }
    }
    
    private StackPane criarOverlayAtualizacao() {
        overlayAtualizacao = new StackPane();
        overlayAtualizacao.setVisible(false);
        overlayAtualizacao.setMouseTransparent(true);
        overlayAtualizacao.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35);");

        VBox caixa = new VBox(12);
        caixa.setAlignment(Pos.CENTER);
        caixa.setPadding(new Insets(22));
        caixa.setMaxWidth(320);
        caixa.setMaxHeight(150);
        caixa.setStyle(
                "-fx-background-color: #eeeeee;" +
                "-fx-border-color: #555555;" +
                "-fx-border-width: 2;" +
                "-fx-background-radius: 6;" +
                "-fx-border-radius: 6;"
        );

        ProgressIndicator progresso = new ProgressIndicator();
        progresso.setPrefSize(42, 42);

        Label texto = new Label("Atualizando dados...");
        texto.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111111;");

        Label subtexto = new Label("Aguarde enquanto as bases são recarregadas.");
        subtexto.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");

        caixa.getChildren().addAll(progresso, texto, subtexto);
        overlayAtualizacao.getChildren().add(caixa);

        return overlayAtualizacao;
    }

    private void preCarregarBasesAoIniciar() {
        mostrarOverlayAtualizacao(true);

        Task<Void> tarefa = new Task<>() {
            @Override
            protected Void call() {
                itemService.preCarregarBases();
                return null;
            }
        };

        tarefa.setOnSucceeded(event -> {
            mostrarOverlayAtualizacao(false);
        });

        tarefa.setOnFailed(event -> {
            mostrarOverlayAtualizacao(false);

            Throwable erro = tarefa.getException();

            mostrarAviso(
                    "Erro ao carregar bases",
                    erro == null
                            ? "Não foi possível carregar as bases ao iniciar o programa."
                            : "Não foi possível carregar as bases ao iniciar o programa:\n" + erro.getMessage()
            );

            if (erro != null) {
                erro.printStackTrace();
            }
        });

        Thread thread = new Thread(tarefa, "PreCarregamentoBasesThread");
        thread.setDaemon(true);
        thread.start();
    }
    
    private void mostrarOverlayAtualizacao(boolean mostrar) {
        if (overlayAtualizacao == null) {
            return;
        }

        overlayAtualizacao.setVisible(mostrar);
        overlayAtualizacao.setMouseTransparent(!mostrar);
    }
    
    private class CodigoComBotaoCell extends TableCell<ItemAnalise, String> {
        private final TextField campoCodigo = new TextField();
        private final Button botaoOpcoes = new Button();
        private final HBox container = new HBox(2);

        public CodigoComBotaoCell() {
            campoCodigo.getStyleClass().add("campo-codigo-tabela");
            campoCodigo.setMaxWidth(Double.MAX_VALUE);

            botaoOpcoes.getStyleClass().add("botao-opcoes-item");
            botaoOpcoes.setGraphic(carregarIcone("item-opcoes.png", 10, 16));
            botaoOpcoes.setFocusTraversable(false);

            botaoOpcoes.setMinSize(18, 22);
            botaoOpcoes.setPrefSize(18, 22);
            botaoOpcoes.setMaxSize(18, 22);

            HBox.setHgrow(campoCodigo, Priority.ALWAYS);

            container.setAlignment(Pos.CENTER);
            container.getChildren().addAll(campoCodigo, botaoOpcoes);

            campoCodigo.setOnAction(event -> confirmarCodigo());
            
            campoCodigo.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.isShortcutDown() && event.getCode() == KeyCode.V) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();

                    if (!clipboard.hasString()) {
                        return;
                    }

                    String textoColado = clipboard.getString();

                    if (!textoTemMultiplasCelulas(textoColado)) {
                        return;
                    }

                    colarCodigosEmLote(textoColado, getIndex());
                    event.consume();
                }
            });

            campoCodigo.focusedProperty().addListener((obs, estavaFocado, estaFocado) -> {
                if (!estaFocado) {
                    confirmarCodigo();
                }
            });

            botaoOpcoes.setOnAction(event -> {
                ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();

                if (item != null) {
                    mostrarAviso(
                            "Opções do item",
                            "Função do botão será definida depois.\nItem: " + item.getCodigo()
                    );
                }
            });
        }

        @Override
        protected void updateItem(String codigo, boolean empty) {
            super.updateItem(codigo, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            
            if (isLinhaRodapeTabela(getTableRow().getItem())) {
                setGraphic(null);
                setText("");
                return;
            }

            campoCodigo.setText(codigo == null ? "" : codigo);
            setText(null);
            setGraphic(container);
        }

        private void confirmarCodigo() {
            ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();

            if (item == null) {
                return;
            }

            String novoCodigo = campoCodigo.getText() == null ? "" : campoCodigo.getText().trim();

            if (!novoCodigo.equals(item.getCodigo())) {
                item.setCodigo(novoCodigo);
                buscarDadosDoItem(item);
            }
        }
    }
    
    private static class InteiroConverter extends StringConverter<Integer> {
        @Override
        public String toString(Integer value) {
            return value == null || value == 0 ? "" : value.toString();
        }

        @Override
        public Integer fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    private static class MoedaEditavelConverter extends StringConverter<BigDecimal> {
        @Override
        public String toString(BigDecimal value) {
            if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
                return "";
            }
            return value.setScale(2, RoundingMode.HALF_UP).toString().replace('.', ',');
        }

        @Override
        public BigDecimal fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            try {
                return new BigDecimal(value.trim().replace("R$", "").replace(".", "").replace(',', '.'));
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
    }
    
    
    private double calcularAlturaTabela(BorderPane root, VBox topo, HBox rodape) {
        int quantidadeLinhas = Math.max(itens.size(), 1);

        double alturaDesejada = ALTURA_CABECALHO_TABELA
                + quantidadeLinhas * ALTURA_LINHA_TABELA
                + ALTURA_BARRA_HORIZONTAL;

        double alturaDisponivel = root.getHeight()
                - topo.getHeight()
                - rodape.getHeight()
                - MARGEM_SEGURANCA_TABELA;

        if (alturaDisponivel <= 0) {
            return alturaDesejada;
        }

        return Math.min(alturaDesejada, alturaDisponivel);
    }
    
    private class BigDecimalTableCell extends TableCell<ItemAnalise, BigDecimal> {
        private final boolean moeda;
        private final boolean exibirNoRodape;

        public BigDecimalTableCell(boolean moeda) {
            this(moeda, false);
        }

        public BigDecimalTableCell(boolean moeda, boolean exibirNoRodape) {
            this.moeda = moeda;
            this.exibirNoRodape = exibirNoRodape;
            setAlignment(Pos.CENTER_RIGHT);
        }

        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText("");
                return;
            }

            ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();

            if (isLinhaRodapeTabela(itemLinha) && !exibirNoRodape) {
                setText("");
                return;
            }

            if (!isLinhaRodapeTabela(itemLinha)
                    && moeda
                    && item.compareTo(BigDecimal.ZERO) == 0) {
                setText("");
                return;
            }

            if (moeda) {
                setText(formatarMoeda(item));
            } else {
                setText(formatarPercentual(item));
            }
        }
    }
    
    
    private class CustoTableCell extends TableCell<ItemAnalise, BigDecimal> {

        public CustoTableCell() {
            setAlignment(Pos.CENTER_RIGHT);
        }

        @Override
        protected void updateItem(BigDecimal valor, boolean empty) {
            super.updateItem(valor, empty);

            if (empty) {
                setText("");
                return;
            }

            ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();

            if (isLinhaRodapeTabela(itemLinha)) {
                setText("");
                return;
            }

            if (!linhaTemCodigo(itemLinha)) {
                setText("");
                return;
            }

            if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
                setText("S./REG.");
                return;
            }

            setText(formatarMoeda(valor));
        }

        private boolean linhaTemCodigo(ItemAnalise item) {
            return item != null
                    && item.getCodigo() != null
                    && !item.getCodigo().trim().isEmpty();
        }
    }

    private class MargemTableCell extends BigDecimalTableCell {
        public MargemTableCell() {
            super(false, true);
        }

        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll("margem-baixa", "margem-media", "margem-boa");

            ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();

            if (!empty && item != null && !isLinhaRodapeTabela(itemLinha)) {
                if (item.compareTo(new BigDecimal("20")) < 0) {
                    getStyleClass().add("margem-baixa");
                } else if (item.compareTo(new BigDecimal("35")) < 0) {
                    getStyleClass().add("margem-media");
                } else {
                    getStyleClass().add("margem-boa");
                }
            }
        }
    }
    
    private long sortearTempoMinimoAtualizacaoMs() {
        int sorteio = ThreadLocalRandom.current().nextInt(100);

        // 80% de chance: comum, entre 1 e 5 segundos
        if (sorteio < 80) {
            return sortearEntreMs(1_000, 5_000);
        }

        // 15% de chance: incomum, entre 5 e 10 segundos
        if (sorteio < 95) {
            return sortearEntreMs(5_000, 10_000);
        }

        // 5% de chance: raro, entre 10 e 15 segundos
        return sortearEntreMs(10_000, 15_000);
    }

    private long sortearEntreMs(int minimoInclusivo, int maximoInclusivo) {
        return ThreadLocalRandom.current().nextLong(minimoInclusivo, maximoInclusivo + 1L);
    }

    private void aguardarTempoMinimo(long inicioMs, long tempoMinimoMs) throws InterruptedException {
        long tempoDecorrido = System.currentTimeMillis() - inicioMs;
        long tempoRestante = tempoMinimoMs - tempoDecorrido;

        if (tempoRestante > 0) {
            Thread.sleep(tempoRestante);
        }
    }
    
    private class DataTableCell extends TableCell<ItemAnalise, LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        public DataTableCell() {
            setAlignment(Pos.CENTER);
        }

        @Override
protected void updateItem(LocalDate data, boolean empty) {
    super.updateItem(data, empty);

    if (empty) {
        setText("");
        return;
    }

    ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();

    if (isLinhaRodapeTabela(itemLinha)) {
        setText("");
        return;
    }

    if (!linhaTemCodigo(itemLinha)) {
        setText("");
        return;
    }

    if (data == null) {
        setText("S./REG.");
        return;
    }

    setText(formatter.format(data));
}

        private boolean linhaTemCodigo(ItemAnalise item) {
            return item != null
                    && item.getCodigo() != null
                    && !item.getCodigo().trim().isEmpty();
        }
    }
}