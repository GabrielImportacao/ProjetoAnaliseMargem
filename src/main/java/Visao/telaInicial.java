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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.Map;

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

    private VBox criarTopo() {
        VBox topo = new VBox();
        topo.getStyleClass().add("topo");

        HBox cabecalho = new HBox(20);
        cabecalho.getStyleClass().add("cabecalho");
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.setPadding(new Insets(12, 14, 10, 14));

        VBox logoBox = new VBox(0);
        Label logoIcone = new Label("◒");
        logoIcone.getStyleClass().add("logo-icone");
        Label logoTexto = new Label("PLASNOX");
        logoTexto.getStyleClass().add("logo-texto");
        logoBox.getChildren().addAll(logoIcone, logoTexto);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPrefWidth(150);

        Label titulo = new Label("ANÁLISE DE MARGEM EM ORÇAMENTO COMERCIAL");
        titulo.getStyleClass().add("titulo-principal");
        HBox.setHgrow(titulo, Priority.ALWAYS);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);

        Button configButton = new Button("⚙");
        configButton.getStyleClass().add("botao-config");
        configButton.setTooltip(new Tooltip("Configurações"));

        cabecalho.getChildren().addAll(logoBox, titulo, configButton);

        HBox comandos = new HBox(18);
        comandos.getStyleClass().add("area-comandos");
        comandos.setAlignment(Pos.CENTER_LEFT);
        comandos.setPadding(new Insets(8, 14, 8, 14));

        Button atualizarButton = new Button("↻ ATUALIZAR DADOS");
        atualizarButton.getStyleClass().add("botao-acao");
        atualizarButton.setOnAction(event -> mostrarAviso("Atualização", "A integração com as bases reais será adicionada depois. Por enquanto, o sistema usa uma base simulada."));
        VBox estadoBaseTabela = criarMiniTabelaEstadoBase();

        Button valorPadraoButton = new Button("🔍 BUSCAR VALOR PADRÃO");
        valorPadraoButton.getStyleClass().add("botao-busca");
        valorPadraoButton.setOnAction(event -> aplicarValorPadrao());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        GridPane resumo = criarResumoSuperior();

        comandos.getChildren().addAll(atualizarButton, estadoBaseTabela, valorPadraoButton, spacer, resumo);

        topo.getChildren().addAll(cabecalho, comandos);
        return topo;
    }

    
    private VBox criarMiniTabelaEstadoBase() {
        Label tituloEstado = new Label("ESTADO");
        tituloEstado.setPrefSize(95, 26);
        tituloEstado.setAlignment(Pos.CENTER);
        tituloEstado.setStyle(
            "-fx-background-color: #d9d9d9;" +
            "-fx-border-color: #555555;" +
            "-fx-border-width: 0 2 2 0;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #555555;"
        );

        Label tituloBase = new Label("BASE PARA O ESTADO");
        tituloBase.setPrefSize(245, 26);
        tituloBase.setAlignment(Pos.CENTER);
        tituloBase.setStyle(
            "-fx-background-color: #d9d9d9;" +
            "-fx-border-color: #555555;" +
            "-fx-border-width: 0 0 2 0;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #555555;"
        );

        estadoCombo.setPrefWidth(95);
        estadoCombo.setMaxWidth(95);
        estadoCombo.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 0;"
        );

        baseEstadoLabel.setPrefWidth(245);
        baseEstadoLabel.setMaxWidth(245);
        baseEstadoLabel.setAlignment(Pos.CENTER);
        baseEstadoLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #555555;"
        );

        StackPane celulaEstado = new StackPane(estadoCombo);
        celulaEstado.setPrefSize(95, 28);
        celulaEstado.setStyle(
            "-fx-background-color: #eeeeee;" +
            "-fx-border-color: #555555;" +
            "-fx-border-width: 0 2 0 0;"
        );

        StackPane celulaBase = new StackPane(baseEstadoLabel);
        celulaBase.setPrefSize(245, 28);
        celulaBase.setStyle(
            "-fx-background-color: #eeeeee;"
        );

        HBox linhaCabecalho = new HBox(tituloEstado, tituloBase);
        HBox linhaValores = new HBox(celulaEstado, celulaBase);

        VBox tabela = new VBox(linhaCabecalho, linhaValores);
        tabela.setStyle(
            "-fx-border-color: #555555;" +
            "-fx-border-width: 2;" +
            "-fx-background-color: #eeeeee;"
        );

        return tabela;
    }
    
    private GridPane criarResumoSuperior() {
        GridPane resumo = new GridPane();
        resumo.getStyleClass().add("resumo-superior");
        resumo.setHgap(0);
        resumo.setVgap(0);

        adicionarCelulaResumo(resumo, "RESULTADOS", 0, 0, true);
        adicionarCelulaResumo(resumo, "VALOR TOTAL", 1, 0, true);
        adicionarCelulaResumo(resumo, "VALOR TOTAL + IPI", 2, 0, true);

        adicionarCelulaResumo(resumo, "RESULTADO PROPOSTA", 0, 1, false);
        adicionarValorResumo(resumo, totalPropostaLabel, 1, 1);
        adicionarValorResumo(resumo, totalComIpiLabel, 2, 1);

        adicionarCelulaResumo(resumo, "RESULTADO ATUAL", 0, 2, false);
        adicionarValorResumo(resumo, resultadoAtualLabel, 1, 2);
        adicionarCelulaResumo(resumo, "", 2, 2, false);

        adicionarCelulaResumo(resumo, "RESULTADO ANTERIOR", 0, 3, false);
        adicionarValorResumo(resumo, resultadoAnteriorLabel, 1, 3);
        adicionarCelulaResumo(resumo, "", 2, 3, false);

        return resumo;
    }

    private void adicionarCelulaResumo(GridPane grid, String texto, int col, int row, boolean titulo) {
        Label label = new Label(texto);
        label.getStyleClass().add(titulo ? "resumo-cabecalho" : "resumo-celula");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(col == 0 ? 155 : 120);
        grid.add(label, col, row);
    }

    private void adicionarValorResumo(GridPane grid, Label label, int col, int row) {
        label.getStyleClass().add("resumo-celula");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setPrefWidth(120);
        grid.add(label, col, row);
    }

    private TableView<ItemAnalise> criarTabela() {
        tabela = new TableView<>(itens);
        tabela.setEditable(true);
        tabela.getStyleClass().add("tabela-analise");
        tabela.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<ItemAnalise, String> codigoCol = new TableColumn<>("CÓDIGO");
        codigoCol.setCellValueFactory(data -> data.getValue().codigoProperty());
        codigoCol.setCellFactory(TextFieldTableCell.forTableColumn());
        codigoCol.setOnEditCommit(event -> {
            ItemAnalise item = event.getRowValue();
            item.setCodigo(event.getNewValue());
            buscarDadosDoItem(item);
        });
        codigoCol.setPrefWidth(145);

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

        TableColumn<ItemAnalise, LocalDate> dataCustoCol = new TableColumn<>("DATA CUSTO");
        dataCustoCol.setCellValueFactory(data -> data.getValue().dataCustoAtualProperty());
        dataCustoCol.setCellFactory(col -> new DataTableCell());
        dataCustoCol.getStyleClass().add("coluna-vermelha");
        dataCustoCol.setPrefWidth(110);

        TableColumn<ItemAnalise, String> registroAtualCol = new TableColumn<>("REG. CUSTO ATUAL");
        registroAtualCol.setCellValueFactory(data -> data.getValue().registroCustoAtualProperty());
        registroAtualCol.getStyleClass().add("coluna-vermelha");
        registroAtualCol.setPrefWidth(155);

        TableColumn<ItemAnalise, BigDecimal> margemPromobCol = new TableColumn<>("MARGEM PROMOB");
        margemPromobCol.setCellValueFactory(data -> data.getValue().margemPromobProperty());
        margemPromobCol.setCellFactory(col -> new MargemTableCell());
        margemPromobCol.getStyleClass().add("coluna-laranja");
        margemPromobCol.setPrefWidth(130);

        TableColumn<ItemAnalise, String> registroPromobCol = new TableColumn<>("REG. CUSTO PROMOB");
        registroPromobCol.setCellValueFactory(data -> data.getValue().registroCustoPromobProperty());
        registroPromobCol.getStyleClass().add("coluna-laranja");
        registroPromobCol.setPrefWidth(160);

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

        TableColumn<ItemAnalise, String> registroAnteriorCol = new TableColumn<>("REG. CUSTO ANTERIOR");
        registroAnteriorCol.setCellValueFactory(data -> data.getValue().registroCustoAnteriorProperty());
        registroAnteriorCol.getStyleClass().add("coluna-azul");
        registroAnteriorCol.setPrefWidth(170);

        TableColumn<ItemAnalise, String> baseAtualGrupo = new TableColumn<>("BASE ATUAL (GERENCIAL)");
        baseAtualGrupo.getColumns().addAll(variacaoAtualCol, margemAtualCol, dataCustoCol, registroAtualCol);
        baseAtualGrupo.getStyleClass().add("grupo-vermelho");

        TableColumn<ItemAnalise, String> basePromobGrupo = new TableColumn<>("BASE PROMOB");
        basePromobGrupo.getColumns().addAll(margemPromobCol, registroPromobCol);
        basePromobGrupo.getStyleClass().add("grupo-laranja");

        TableColumn<ItemAnalise, String> analiseAnteriorGrupo = new TableColumn<>("ANÁLISE ANTERIOR");
        analiseAnteriorGrupo.getColumns().addAll(variacaoAnteriorCol, margemAnteriorCol, registroAnteriorCol);
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
        ObservableList<ItemAnalise> selecionados = tabela.getSelectionModel().getSelectedItems();
        ObservableList<ItemAnalise> alvo = selecionados.isEmpty() ? itens : selecionados;

        for (ItemAnalise item : alvo) {
            if (item.getCustoAtual().compareTo(BigDecimal.ZERO) > 0) {
                item.setValorUnitario(analiseService.calcularValorPadrao(item, estadoCombo.getValue()));
                analiseService.recalcular(item);
            }
        }

        recalcularResumo();
        tabela.refresh();
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
            if (empty || item == null || item.compareTo(BigDecimal.ZERO) == 0) {
                setText("");
            } else if (moeda) {
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
