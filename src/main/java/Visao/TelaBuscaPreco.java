package Visao;

import Controle.ItemService;
import Infraestrutura.DiagnosticoAmbiente;
import Modelo.DadosItem;
import Modelo.ItemAnalise;
import Visao.componentes.BotaoPadrao;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TelaBuscaPreco {

	private static final String COR_FUNDO_TELA = "#C7C4BA";
	private static final String COR_LINHA_PAR = "#E6E6E6";
	private static final String COR_LINHA_IMPAR = "#D3D3D3";
	private static final String COR_BORDA = "#555555";

	private static final int QUANTIDADE_MINIMA_LINHAS_VISUAIS = 10;
    private final ObservableList<LinhaBuscaPreco> linhas = FXCollections.observableArrayList();

    
    private HBox criarBarraTitulo(Stage stage) {
        HBox barra = new HBox();
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(0, 0, 0, 10));
        barra.setMinHeight(32);
        barra.setPrefHeight(32);
        barra.setMaxHeight(32);
        barra.setStyle(
                "-fx-background-color: " + COR_FUNDO_TELA + ";" +
                "-fx-border-color: " + COR_BORDA + ";" +
                "-fx-border-width: 0 0 1 0;"
        );

        javafx.scene.control.Label titulo = new javafx.scene.control.Label("Buscar Preço");
        titulo.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #000000;"
        );

        javafx.scene.layout.Region espaco = new javafx.scene.layout.Region();
        HBox.setHgrow(espaco, javafx.scene.layout.Priority.ALWAYS);

        Button fechar = new Button("X");
        fechar.setMinSize(36, 32);
        fechar.setPrefSize(36, 32);
        fechar.setMaxSize(36, 32);
        fechar.setFocusTraversable(false);
        fechar.setCursor(Cursor.HAND);
        fechar.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 0;" +
                "-fx-border-color: transparent;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #000000;"
        );

        fechar.setOnMouseEntered(event -> fechar.setStyle(
                "-fx-background-color: #E81123;" +
                "-fx-background-radius: 0;" +
                "-fx-border-color: transparent;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #FFFFFF;"
        ));

        fechar.setOnMouseExited(event -> fechar.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 0;" +
                "-fx-border-color: transparent;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #000000;"
        ));

        fechar.setOnAction(event -> stage.close());

        final double[] deslocamentoX = new double[1];
        final double[] deslocamentoY = new double[1];

        barra.setOnMousePressed(event -> {
            deslocamentoX[0] = event.getSceneX();
            deslocamentoY[0] = event.getSceneY();
        });

        barra.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - deslocamentoX[0]);
            stage.setY(event.getScreenY() - deslocamentoY[0]);
        });

        barra.getChildren().addAll(titulo, espaco, fechar);

        return barra;
    }
    
    private void completarLinhasVisuais() {
        while (linhas.size() < QUANTIDADE_MINIMA_LINHAS_VISUAIS) {
            linhas.add(LinhaBuscaPreco.linhaVazia());
        }
    }
    
    public boolean exibir(Window janelaDona, List<ItemAnalise> itensParaBuscar, ItemService itemService) {
        linhas.clear();

        List<String> codigosNaoEncontrados = new ArrayList<>();

        for (ItemAnalise item : itensParaBuscar) {
            DadosItem dadosItem = itemService.buscarPorCodigo(item.getCodigo()).orElse(null);

            if (dadosItem == null) {
                codigosNaoEncontrados.add(item.getCodigo());
                continue;
            }

            linhas.add(new LinhaBuscaPreco(item, dadosItem));
        }

        if (!codigosNaoEncontrados.isEmpty()) {
            javafx.scene.control.Alert aviso = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING
            );
            aviso.setTitle("Itens não encontrados");
            aviso.setHeaderText(null);
            aviso.setContentText(
                    "Alguns itens não foram encontrados na base e não serão carregados:\n\n"
                            + String.join("\n", codigosNaoEncontrados)
            );

            if (janelaDona != null) {
                aviso.initOwner(janelaDona);
            }

            aviso.showAndWait();
        }

        completarLinhasVisuais();
        
        final boolean[] confirmou = {false};

        Stage stage = new Stage();
        stage.setTitle("Buscar Preço");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);

        if (janelaDona != null) {
            stage.initOwner(janelaDona);
        }

        VBox conteudo = new VBox(0);
        conteudo.setPadding(new Insets(0));
        conteudo.setStyle(
                "-fx-background-color: " + COR_FUNDO_TELA + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: #777777;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;"
        );

        DropShadow sombra = new DropShadow();
        sombra.setRadius(28);
        sombra.setOffsetX(0);
        sombra.setOffsetY(8);
        sombra.setColor(Color.rgb(0, 0, 0, 0.38));
        conteudo.setEffect(sombra);

        StackPane root = new StackPane(conteudo);
        root.setPadding(new Insets(22));
        root.setStyle("-fx-background-color: transparent;");

        TableView<LinhaBuscaPreco> tabela = criarTabela();

        HBox botoes = new HBox(52);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(14, 16, 14, 16));
        botoes.setStyle("-fx-background-color: #C7C4BA;");

        Button cancelarButton = criarBotao("CANCELAR", 100);
        cancelarButton.setOnAction(event -> stage.close());

        Button carregarButton = criarBotao("CARREGAR", 100);
        carregarButton.setOnAction(event -> {
            carregarValoresNaTabelaPrincipal();
            confirmou[0] = true;
            stage.close();
        });

        botoes.getChildren().addAll(cancelarButton, carregarButton);

        conteudo.getChildren().addAll(
                criarBarraTitulo(stage),
                tabela,
                botoes
        );

        Scene scene = new Scene(root, 764, 464);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        return confirmou[0];
    }

    private Button criarBotao(String texto, double largura) {
    return BotaoPadrao.criar(texto, largura);
}

    @SuppressWarnings({ "unchecked", "deprecation" })
	private TableView<LinhaBuscaPreco> criarTabela() {
        TableView<LinhaBuscaPreco> tabela = new TableView<>(linhas);
        tabela.setEditable(true);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setFixedCellSize(34);
        tabela.setPrefHeight(360);

        tabela.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-background-color: #C7C4BA;" +
                "-fx-control-inner-background: #E9E9E9;" +
                "-fx-table-cell-border-color: #555555;" +
                "-fx-selection-bar: #B8B8B8;" +
                "-fx-selection-bar-non-focused: #B8B8B8;"
        );

        TableColumn<LinhaBuscaPreco, String> codigoCol = new TableColumn<>("CÓDIGO");
        codigoCol.setCellValueFactory(data -> data.getValue().codigoProperty());
        codigoCol.setCellFactory(col -> new TextoCell(Pos.CENTER));
        codigoCol.setPrefWidth(100);

        TableColumn<LinhaBuscaPreco, String> descricaoCol = new TableColumn<>("DESCRIÇÃO");
        descricaoCol.setCellValueFactory(data -> data.getValue().descricaoProperty());
        descricaoCol.setCellFactory(col -> new TextoCell(Pos.CENTER));
        descricaoCol.setPrefWidth(210);

        TableColumn<LinhaBuscaPreco, BigDecimal> precoBaseCol = new TableColumn<>("PREÇO BASE");
        precoBaseCol.setCellValueFactory(data -> data.getValue().precoBaseProperty());
        precoBaseCol.setCellFactory(col -> new BigDecimalCampoCell(TipoCampo.PRECO_BASE, LinhaBuscaPreco::setPrecoBase));
        precoBaseCol.setPrefWidth(120);

        TableColumn<LinhaBuscaPreco, BigDecimal> descontoCol = new TableColumn<>("DESCONTO DESEJADO");
        descontoCol.setCellValueFactory(data -> data.getValue().descontoDesejadoProperty());
        descontoCol.setCellFactory(col -> new BigDecimalCampoCell(TipoCampo.DESCONTO, LinhaBuscaPreco::setDescontoDesejado));
        descontoCol.setPrefWidth(165);

        TableColumn<LinhaBuscaPreco, BigDecimal> valorOrcamentoCol = new TableColumn<>("VALOR ORÇAMENTO");
        valorOrcamentoCol.setCellValueFactory(data -> data.getValue().valorOrcamentoProperty());
        valorOrcamentoCol.setCellFactory(col -> new BigDecimalSomenteLeituraCell());
        valorOrcamentoCol.setPrefWidth(150);

        tabela.getColumns().addAll(
                codigoCol,
                descricaoCol,
                precoBaseCol,
                descontoCol,
                valorOrcamentoCol
        );

        aplicarEstiloCabecalho(tabela);

        return tabela;
    }

    private void aplicarEstiloCabecalho(TableView<LinhaBuscaPreco> tabela) {
    Runnable aplicar = () -> {
        tabela.lookupAll(".column-header").forEach(node -> node.setStyle(
                "-fx-background-color: #4A4A4A;" +
                "-fx-border-color: #555555;" +
                "-fx-border-width: 0 1 1 0;"
        ));

        tabela.lookupAll(".column-header .label").forEach(node -> node.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                "-fx-alignment: center;" +
                "-fx-text-alignment: center;"
        ));

        tabela.lookupAll(".filler").forEach(node -> node.setStyle(
                "-fx-background-color: #4A4A4A;"
        ));
    };

    Platform.runLater(aplicar);
    tabela.widthProperty().addListener((obs, oldValue, newValue) -> aplicar.run());
}

    private void carregarValoresNaTabelaPrincipal() {
        for (LinhaBuscaPreco linha : linhas) {
            if (linha.isLinhaVazia()) {
                continue;
            }

            ItemAnalise item = linha.getItemOriginal();

            item.aplicarDadosItem(linha.getDadosItem());
            item.setValorUnitario(linha.getValorOrcamento());
        }
    }

    private enum TipoCampo {
        PRECO_BASE,
        DESCONTO
    }

    
    private static class LinhaBuscaPreco {
        private final ItemAnalise itemOriginal;
        private final DadosItem dadosItem;

        private final StringProperty codigo = new SimpleStringProperty("");
        private final StringProperty descricao = new SimpleStringProperty("");

        private final ObjectProperty<BigDecimal> precoBase = new SimpleObjectProperty<>(BigDecimal.ZERO);
        private final ObjectProperty<BigDecimal> descontoDesejado = new SimpleObjectProperty<>(BigDecimal.ZERO);
        private final ReadOnlyObjectWrapper<BigDecimal> valorOrcamento = new ReadOnlyObjectWrapper<>(BigDecimal.ZERO);
        
        private final boolean linhaVazia;

        private LinhaBuscaPreco() {
            this.linhaVazia = true;
            this.itemOriginal = null;
            this.dadosItem = null;
        }

        public static LinhaBuscaPreco linhaVazia() {
            return new LinhaBuscaPreco();
        }

        public boolean isLinhaVazia() {
            return linhaVazia;
        }
        
        public LinhaBuscaPreco(ItemAnalise itemOriginal, DadosItem dadosItem) {
            this.itemOriginal = itemOriginal;
            this.dadosItem = dadosItem;
            this.linhaVazia = false;

            BigDecimal precoBaseVenda = valorSeguro(dadosItem.getPrecoPadraoVenda());
            BigDecimal valorUnitarioAtual = valorSeguro(itemOriginal.getValorUnitario());

            this.codigo.set(dadosItem.getCodigo());
            this.descricao.set(dadosItem.getDescricao());
            this.precoBase.set(precoBaseVenda);

            if (valorUnitarioAtual.compareTo(BigDecimal.ZERO) > 0) {
                this.valorOrcamento.set(valorUnitarioAtual.setScale(2, RoundingMode.HALF_UP));
                this.descontoDesejado.set(calcularDescontoAplicado(precoBaseVenda, valorUnitarioAtual));
            } else {
                this.valorOrcamento.set(precoBaseVenda.setScale(2, RoundingMode.HALF_UP));
                this.descontoDesejado.set(BigDecimal.ZERO);
            }

            this.precoBase.addListener((obs, antigo, novo) -> recalcularValorOrcamento());
            this.descontoDesejado.addListener((obs, antigo, novo) -> recalcularValorOrcamento());
        }

        public ItemAnalise getItemOriginal() {
            return itemOriginal;
        }

        public DadosItem getDadosItem() {
            return dadosItem;
        }

        public StringProperty codigoProperty() {
            return codigo;
        }

        public StringProperty descricaoProperty() {
            return descricao;
        }

        public ObjectProperty<BigDecimal> precoBaseProperty() {
            return precoBase;
        }

        public BigDecimal getPrecoBase() {
            return valorSeguro(precoBase.get());
        }

        public void setPrecoBase(BigDecimal precoBase) {
            this.precoBase.set(valorSeguro(precoBase));
        }

        public ObjectProperty<BigDecimal> descontoDesejadoProperty() {
            return descontoDesejado;
        }

        public BigDecimal getDescontoDesejado() {
            return valorSeguro(descontoDesejado.get());
        }

        public void setDescontoDesejado(BigDecimal descontoDesejado) {
            this.descontoDesejado.set(valorSeguro(descontoDesejado));
        }

        public ReadOnlyObjectWrapper<BigDecimal> valorOrcamentoProperty() {
            return valorOrcamento;
        }

        public BigDecimal getValorOrcamento() {
            return valorSeguro(valorOrcamento.get());
        }

        private void recalcularValorOrcamento() {
            BigDecimal base = getPrecoBase();
            BigDecimal desconto = getDescontoDesejado();

            if (base.compareTo(BigDecimal.ZERO) <= 0) {
                valorOrcamento.set(BigDecimal.ZERO);
                return;
            }

            if (desconto.compareTo(BigDecimal.ZERO) == 0) {
                valorOrcamento.set(base.setScale(2, RoundingMode.HALF_UP));
                return;
            }

            BigDecimal fatorDesconto = BigDecimal.ONE.subtract(
                    desconto.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)
            );

            BigDecimal valorCalculado = base.multiply(fatorDesconto)
                    .setScale(2, RoundingMode.HALF_UP);

            valorOrcamento.set(valorCalculado);
        }

        private static BigDecimal calcularDescontoAplicado(BigDecimal precoBase, BigDecimal valorOrcamento) {
    BigDecimal base = valorSeguro(precoBase).setScale(2, RoundingMode.HALF_UP);
    BigDecimal valor = valorSeguro(valorOrcamento).setScale(2, RoundingMode.HALF_UP);

    if (base.compareTo(BigDecimal.ZERO) <= 0 || valor.compareTo(BigDecimal.ZERO) <= 0) {
        return BigDecimal.ZERO;
    }

    if (base.compareTo(valor) == 0) {
        return BigDecimal.ZERO;
    }

    BigDecimal desconto = BigDecimal.ONE
            .subtract(valor.divide(base, 8, RoundingMode.HALF_UP))
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP);

    if (desconto.abs().compareTo(new BigDecimal("0.01")) <= 0) {
        return BigDecimal.ZERO;
    }

    return desconto;
}

        private static BigDecimal valorSeguro(BigDecimal valor) {
            return valor == null ? BigDecimal.ZERO : valor;
        }
    }

    private static class TextoCell extends TableCell<LinhaBuscaPreco, String> {
        private final Pos alinhamento;

        public TextoCell(Pos alinhamento) {
            this.alinhamento = alinhamento;
        }

        @Override
        protected void updateItem(String valor, boolean empty) {
            super.updateItem(valor, empty);

            if (empty) {
                setText("");
                setStyle("");
                return;
            }

            LinhaBuscaPreco linha = getTableRow() == null ? null : getTableRow().getItem();

            if (linha == null || linha.isLinhaVazia()) {
                setText("");
                setAlignment(alinhamento);
                aplicarEstiloLinha(this);
                return;
            }

            setText(valor == null ? "" : valor);
            setAlignment(alinhamento);
            aplicarEstiloLinha(this);
        }
    }

    private static class BigDecimalCampoCell extends TableCell<LinhaBuscaPreco, BigDecimal> {
        private final TextField campo = new TextField();
        private final TipoCampo tipoCampo;
        private final BiConsumer<LinhaBuscaPreco, BigDecimal> consumidorValor;

        public BigDecimalCampoCell(TipoCampo tipoCampo, BiConsumer<LinhaBuscaPreco, BigDecimal> consumidorValor) {
            this.tipoCampo = tipoCampo;
            this.consumidorValor = consumidorValor;

            campo.setAlignment(Pos.CENTER);
            campo.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-border-color: transparent;" +
                    "-fx-font-size: 12px;" +
                    "-fx-text-fill: #000000;" +
                    "-fx-alignment: center;"
            );

            campo.setOnAction(event -> confirmarValor());

            campo.focusedProperty().addListener((obs, estavaFocado, estaFocado) -> {
                if (!estaFocado) {
                    confirmarValor();
                }
            });
        }

        @Override
        protected void updateItem(BigDecimal valor, boolean empty) {
            super.updateItem(valor, empty);

            if (empty) {
                setText("");
                setGraphic(null);
                setStyle("");
                return;
            }

            LinhaBuscaPreco linha = getTableRow() == null ? null : getTableRow().getItem();

            if (linha == null || linha.isLinhaVazia()) {
                setText("");
                setGraphic(null);
                aplicarEstiloLinha(this);
                return;
            }

            campo.setText(formatarEditavel(valor));
            setText(null);
            setGraphic(campo);
            setAlignment(tipoCampo == TipoCampo.DESCONTO ? Pos.CENTER : Pos.CENTER);setAlignment(Pos.CENTER);
            aplicarEstiloLinha(this);
        }

        private void confirmarValor() {
            LinhaBuscaPreco linha = getTableRow() == null ? null : getTableRow().getItem();

            if (linha == null) {
                return;
            }

            BigDecimal valorConvertido = converterTextoParaBigDecimal(campo.getText());
            consumidorValor.accept(linha, valorConvertido);

            getTableView().refresh();
        }

        private String formatarEditavel(BigDecimal valor) {
            BigDecimal valorSeguro = valor == null ? BigDecimal.ZERO : valor;

            if (tipoCampo == TipoCampo.DESCONTO) {
                if (valorSeguro.compareTo(BigDecimal.ZERO) == 0) {
                    return "";
                }

                return formatarPercentualMinimo(valorSeguro);
            }

            return formatarMoeda(valorSeguro);
        }
    }

    private static String formatarPercentualMinimo(BigDecimal valor) {
        BigDecimal valorFormatado = valor
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        return valorFormatado
                .toPlainString()
                .replace(".", ",") + "%";
    }
    
    private static class BigDecimalSomenteLeituraCell extends TableCell<LinhaBuscaPreco, BigDecimal> {
    	@Override
    	protected void updateItem(BigDecimal valor, boolean empty) {
    	    super.updateItem(valor, empty);

    	    if (empty) {
    	        setText("");
    	        setStyle("");
    	        return;
    	    }

    	    LinhaBuscaPreco linha = getTableRow() == null ? null : getTableRow().getItem();

    	    if (linha == null || linha.isLinhaVazia()) {
    	        setText("");
    	        aplicarEstiloLinha(this);
    	        return;
    	    }

    	    setText(formatarMoeda(valor == null ? BigDecimal.ZERO : valor));
    	    setAlignment(Pos.CENTER);
    	    aplicarEstiloLinha(this);
    	}
    }

    private static void aplicarEstiloLinha(TableCell<LinhaBuscaPreco, ?> cell) {
        LinhaBuscaPreco linha = cell.getTableRow() == null ? null : cell.getTableRow().getItem();

        String corLinha = cell.getIndex() % 2 == 0 ? COR_LINHA_PAR : COR_LINHA_IMPAR;

        cell.setStyle(
                "-fx-background-color: " + corLinha + ";" +
                "-fx-border-color: " + COR_BORDA + ";" +
                "-fx-border-width: 0 1 1 0;" +
                "-fx-text-fill: #000000;"
        );

        if (linha == null || linha.isLinhaVazia()) {
            cell.setText("");
            cell.setGraphic(null);
        }
    }

    private static BigDecimal converterTextoParaBigDecimal(String texto) {
        if (texto == null || texto.isBlank()) {
            return BigDecimal.ZERO;
        }

        String textoTratado = texto
                .replace("R$", "")
                .replace("%", "")
                .trim();

        if (textoTratado.contains(",")) {
            textoTratado = textoTratado
                    .replace(".", "")
                    .replace(",", ".");
        }

        try {
            return new BigDecimal(textoTratado);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static String formatarMoeda(BigDecimal valor) {
        BigDecimal valorSeguro = valor == null ? BigDecimal.ZERO : valor;

        return "R$ " + valorSeguro
                .setScale(2, RoundingMode.HALF_UP)
                .toString()
                .replace(".", ",");
    }
}