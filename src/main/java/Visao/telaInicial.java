package Visao;

import Controle.AnaliseService;
import Controle.ItemService;
import Modelo.EstadoInfo;
import Modelo.ItemAnalise;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private final Label totalComIpiLabel = new Label("R$ 0,00");
    private final Label resultadoAtualLabel = new Label("R$ 0,00");
    private final Label resultadoAnteriorLabel = new Label("R$ 0,00");
    private final ComboBox<EstadoInfo> estadoCombo = new ComboBox<>();
    private final Label baseEstadoLabel = new Label("0,00%");
    
    
    private TableView<ItemAnalise> tabela;
    
    
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
        root.setTop(criarTopo());
        root.setCenter(criarTabela());
        root.setBottom(criarRodape());

        Scene scene = new Scene(root, 1180, 680);
        scene.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());

        stage.setTitle("Programa de Análise de Margem");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(600);
        stage.show();
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

        for (int i = 0; i < 18; i++) {
            ItemAnalise item = new ItemAnalise();
            if (i < 3) {
                item.setCodigo("MMR0000.0000");
            }
            itens.add(item);
        }

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
        atualizarButton.setOnAction(event -> mostrarAviso("Atualização", "A integração com as bases reais será adicionada depois. Por enquanto, o sistema usa uma base simulada."));
        
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
                resultadoAnteriorLabel
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

        TableColumn<ItemAnalise, String> codigoCol = new TableColumn<>("CÓDIGO");
        codigoCol.setCellValueFactory(data -> data.getValue().codigoProperty());
        codigoCol.setCellFactory(col -> new CodigoComBotaoCell());
        codigoCol.setPrefWidth(170);

        TableColumn<ItemAnalise, String> descricaoCol = new TableColumn<>("DESCRIÇÃO");
        descricaoCol.setCellValueFactory(data -> data.getValue().descricaoProperty());
        descricaoCol.setPrefWidth(340);

        TableColumn<ItemAnalise, Integer> quantidadeCol = new TableColumn<>("QUANTIDADE");
        quantidadeCol.setCellValueFactory(data -> data.getValue().quantidadeProperty().asObject());
        quantidadeCol.setCellFactory(TextFieldTableCell.forTableColumn(new InteiroConverter()));
        quantidadeCol.setOnEditCommit(event -> {
            ItemAnalise item = event.getRowValue();
            item.setQuantidade(event.getNewValue());
            analiseService.recalcular(item);
            recalcularResumo();
            tabela.refresh();
        });
        quantidadeCol.setPrefWidth(105);

        TableColumn<ItemAnalise, BigDecimal> valorUnitarioCol = new TableColumn<>("VALOR UNITÁRIO");
        valorUnitarioCol.setCellValueFactory(data -> data.getValue().valorUnitarioProperty());
        valorUnitarioCol.setCellFactory(TextFieldTableCell.forTableColumn(new MoedaEditavelConverter()));
        valorUnitarioCol.setOnEditCommit(event -> {
            ItemAnalise item = event.getRowValue();
            item.setValorUnitario(event.getNewValue());
            analiseService.recalcular(item);
            recalcularResumo();
            tabela.refresh();
        });
        valorUnitarioCol.setPrefWidth(125);

        TableColumn<ItemAnalise, BigDecimal> valorTotalCol = new TableColumn<>("VALOR TOTAL");
        valorTotalCol.setCellValueFactory(data -> data.getValue().valorTotalProperty());
        valorTotalCol.setCellFactory(col -> new BigDecimalTableCell(true));
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
        custoAtualCol.setCellFactory(col -> new BigDecimalTableCell(true));
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
        custoPromobCol.setCellFactory(col -> new BigDecimalTableCell(true));
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
        custoAnteriorCol.setCellFactory(col -> new BigDecimalTableCell(true));
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
            row.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER && !row.isEmpty()) {
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
        adicionarLinha.setOnAction(event -> itens.add(new ItemAnalise()));

        Button removerLinha = new Button("- Remover linha");
        removerLinha.getStyleClass().add("botao-acao");
        removerLinha.disableProperty().bind(Bindings.isEmpty(tabela.getSelectionModel().getSelectedItems()));
        removerLinha.setOnAction(event -> {
            ItemAnalise selecionado = tabela.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                itens.remove(selecionado);
                recalcularResumo();
            }
        });

        rodape.getChildren().addAll(ajuda, spacer, adicionarLinha, removerLinha);
        return rodape;
    }

    private void buscarDadosDoItem(ItemAnalise item) {
        if (item == null) {
            return;
        }

        item.aplicarDadosItem(itemService.buscarPorCodigo(item.getCodigo()).orElse(null));
        analiseService.recalcular(item);
        recalcularResumo();
        tabela.refresh();
    }

    private void aplicarValorPadrao() {
        int itensAtualizados = 0;
        int itensSemPrecoPadrao = 0;

        for (ItemAnalise item : itens) {
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
        BigDecimal resultadoAtual = BigDecimal.ZERO;
        BigDecimal resultadoAnterior = BigDecimal.ZERO;

        for (ItemAnalise item : itens) {
            BigDecimal total = valorSeguro(item.getValorTotal());
            totalProposta = totalProposta.add(total);

            BigDecimal custoAtualLinha = valorSeguro(item.getCustoAtual()).multiply(BigDecimal.valueOf(item.getQuantidade()));
            BigDecimal custoAnteriorLinha = valorSeguro(item.getCustoAnterior()).multiply(BigDecimal.valueOf(item.getQuantidade()));

            resultadoAtual = resultadoAtual.add(total.subtract(custoAtualLinha));
            resultadoAnterior = resultadoAnterior.add(total.subtract(custoAnteriorLinha));
        }

        BigDecimal ipiDemonstrativo = new BigDecimal("0.00");
        BigDecimal totalComIpi = totalProposta.multiply(BigDecimal.ONE.add(ipiDemonstrativo.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)));

        totalPropostaLabel.setText(formatarMoeda(totalProposta));
        totalComIpiLabel.setText(formatarMoeda(totalComIpi));
        resultadoAtualLabel.setText(formatarMoeda(resultadoAtual));
        resultadoAnteriorLabel.setText(formatarMoeda(resultadoAnterior));
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

    private static class BigDecimalTableCell extends TableCell<ItemAnalise, BigDecimal> {
        private final boolean moeda;

        public BigDecimalTableCell(boolean moeda) {
            this.moeda = moeda;
            setAlignment(Pos.CENTER_RIGHT);
        }

        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText("");
                return;
            }

            if (moeda && item.compareTo(BigDecimal.ZERO) == 0) {
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

    private static class MargemTableCell extends BigDecimalTableCell {
        public MargemTableCell() {
            super(false);
        }

        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll("margem-baixa", "margem-media", "margem-boa");

            if (!empty && item != null) {
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

    private static class DataTableCell extends TableCell<ItemAnalise, LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        public DataTableCell() {
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText("");
            } else {
                setText(formatter.format(item));
            }
        }
    }
}
