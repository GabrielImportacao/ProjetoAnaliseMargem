package Visao;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.scene.image.Image;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import Configuracao.ConfiguracaoUsuario;
import Configuracao.ConfiguracaoUsuarioService;

public class TelaConfiguracoes {

    private static final String COR_FUNDO = "#C7C4BA";
    private static final String COR_BORDA = "#555555";
    private static final String COR_SELECIONADO = "#555555";
    private static final String COR_TEXTO = "#555555";
    private static final String COR_BRANCO = "#F2F2F2";
    private static final String COR_VERMELHO = "#FF0000";

    private final VBox menuAbas = new VBox(0);
    private final StackPane areaConteudo = new StackPane();

    private AbaConfiguracao abaSelecionada = AbaConfiguracao.TABELA;

    private Label abaTabelaLabel;
    private Label abaAparenciaLabel;
    private Label abaEstadosLabel;
    private Label abaUsuarioLabel;
    
    private final ConfiguracaoUsuarioService configuracaoUsuarioService = new ConfiguracaoUsuarioService();

    private ConfiguracaoUsuario configuracaoUsuario = new ConfiguracaoUsuario();
    private ConfiguracaoUsuario configuracaoEdicao = new ConfiguracaoUsuario();

    public void exibir(Window janelaDona) {
        Stage stage = new Stage();
        configuracaoUsuario = configuracaoUsuarioService.carregar();
        configuracaoEdicao = new ConfiguracaoUsuario(configuracaoUsuario);
        stage.setTitle("Configurações");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);

        if (janelaDona != null) {
            stage.initOwner(janelaDona);
        }

        VBox janela = new VBox(0);
        janela.setPadding(new Insets(0));
        janela.setStyle(
                "-fx-background-color: " + COR_FUNDO + ";" +
                "-fx-border-color: " + COR_BORDA + ";" +
                "-fx-border-width: 2;"
        );

        DropShadow sombra = new DropShadow();
        sombra.setRadius(18);
        sombra.setOffsetY(5);
        sombra.setColor(Color.rgb(0, 0, 0, 0.35));
        janela.setEffect(sombra);

        janela.getChildren().addAll(
                criarBarraTitulo(stage),
                criarCorpo(),
                criarRodape(stage)
        );

        StackPane root = new StackPane(janela);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root, 650, 500);
        scene.setFill(Color.TRANSPARENT);
        
        URL css = getClass().getResource("/estilo.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setScene(scene);
        stage.setResizable(false);

        trocarAba(AbaConfiguracao.TABELA);

        stage.showAndWait();
    }

    private HBox criarBarraTitulo(Stage stage) {
        HBox barra = new HBox(10);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(8, 10, 8, 14));
        barra.setMinHeight(50);
        barra.setPrefHeight(50);

        Label titulo = new Label("Configurações");
        titulo.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COR_TEXTO + ";"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button fechar = new Button("×");
        fechar.setCursor(Cursor.HAND);
        fechar.setFocusTraversable(false);
        aplicarEfeitoBotao(
                fechar,
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #CC0000;" +
                        "-fx-font-size: 34px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: -4 0 0 0;",

                "-fx-background-color: #FF3333;" +
                        "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-size: 34px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: -4 0 0 0;",

                "-fx-background-color: #990000;" +
                        "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-size: 34px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: -4 0 0 0;"
        );
        fechar.setOnAction(event -> stage.close());

        barra.getChildren().addAll(titulo, spacer, fechar);

        return barra;
    }

    private BorderPane criarCorpo() {
        BorderPane corpo = new BorderPane();
        corpo.setPadding(new Insets(10, 14, 0, 14));
        corpo.setPrefHeight(360);
        corpo.setStyle(
                "-fx-border-color: " + COR_BORDA + ";" +
                "-fx-border-width: 2;"
        );

        VBox abas = criarMenuAbas();
        areaConteudo.setPadding(new Insets(0));
        areaConteudo.setStyle(
                "-fx-border-color: " + COR_BORDA + ";" +
                "-fx-border-width: 0 0 0 2;"
        );

        corpo.setLeft(abas);
        corpo.setCenter(areaConteudo);

        return corpo;
    }

    private VBox criarMenuAbas() {
        menuAbas.setPrefWidth(200);
        menuAbas.setMinWidth(200);
        menuAbas.setMaxWidth(200);

        Label tituloAbas = criarTituloMenu("Abas");

        abaTabelaLabel = criarBotaoAba("Tabela", AbaConfiguracao.TABELA);
        abaAparenciaLabel = criarBotaoAba("Aparência", AbaConfiguracao.APARENCIA);
        abaEstadosLabel = criarBotaoAba("Estados", AbaConfiguracao.ESTADOS);
        abaUsuarioLabel = criarBotaoAba("Usuário", AbaConfiguracao.USUARIO);

        menuAbas.getChildren().addAll(
                tituloAbas,
                abaTabelaLabel,
                abaAparenciaLabel,
                abaEstadosLabel,
                abaUsuarioLabel,
                criarEspacoFinalMenu()
        );

        return menuAbas;
    }

    private Label criarTituloMenu(String texto) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setPadding(new Insets(0, 12, 0, 12));
        label.setMinHeight(78);
        label.setPrefHeight(78);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COR_TEXTO + ";" +
                "-fx-background-color: " + COR_FUNDO + ";"
        );
        return label;
    }

    private Label criarBotaoAba(String texto, AbaConfiguracao aba) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setPadding(new Insets(0, 12, 0, 12));
        label.setMinHeight(56);
        label.setPrefHeight(56);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setCursor(Cursor.HAND);

        label.setOnMouseEntered(event -> {
            if (abaSelecionada != aba) {
                label.setStyle(
                        "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + COR_TEXTO + ";" +
                        "-fx-background-color: #D8D5CC;"
                );
            }
        });

        label.setOnMouseExited(event -> estilizarAba(label, abaSelecionada == aba));

        label.setOnMousePressed(event -> label.setTranslateY(1));
        label.setOnMouseReleased(event -> label.setTranslateY(0));

        label.setOnMouseClicked(event -> trocarAba(aba));

        return label;
    }

    private Region criarEspacoFinalMenu() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        region.setStyle("-fx-background-color: " + COR_FUNDO + ";");
        return region;
    }

    private void trocarAba(AbaConfiguracao aba) {
        abaSelecionada = aba;

        atualizarVisualAbas();

        areaConteudo.getChildren().clear();

        switch (abaSelecionada) {
        case TABELA -> areaConteudo.getChildren().add(criarConteudoTabela());
        case APARENCIA -> areaConteudo.getChildren().add(criarConteudoAparencia());
        case ESTADOS -> areaConteudo.getChildren().add(criarConteudoEstados());
        case USUARIO -> areaConteudo.getChildren().add(criarConteudoUsuario());
    }
    }

    private void atualizarVisualAbas() {
    	estilizarAba(abaTabelaLabel, abaSelecionada == AbaConfiguracao.TABELA);
    	estilizarAba(abaAparenciaLabel, abaSelecionada == AbaConfiguracao.APARENCIA);
    	estilizarAba(abaEstadosLabel, abaSelecionada == AbaConfiguracao.ESTADOS);
    	estilizarAba(abaUsuarioLabel, abaSelecionada == AbaConfiguracao.USUARIO);
    }

    private void estilizarAba(Label label, boolean selecionada) {
        if (label == null) {
            return;
        }

        label.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + (selecionada ? COR_BRANCO : COR_TEXTO) + ";" +
                "-fx-background-color: " + (selecionada ? COR_SELECIONADO : COR_FUNDO) + ";"
        );
    }

    private VBox criarConteudoBase() {
        VBox box = new VBox(18);
        box.setPadding(new Insets(18, 14, 14, 14));
        box.setAlignment(Pos.TOP_LEFT);
        box.setStyle("-fx-background-color: " + COR_FUNDO + ";");
        return box;
    }

    private Label criarTituloConteudo(String texto) {
        Label label = new Label(texto);
        label.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COR_TEXTO + ";"
        );
        return label;
    }

    private VBox criarConteudoTabela() {
        VBox box = criarConteudoBase();

        Label titulo = criarTituloConteudo("Configurações");

        VBox opcoes = new VBox(18);
        opcoes.setPadding(new Insets(22, 0, 0, 0));

        CheckBoxVisual custoVerdadeiro = new CheckBoxVisual(
                "Custo Verdadeiro",
                configuracaoEdicao.isCustoVerdadeiroAtivo(),
                marcado -> configuracaoEdicao.setCustoVerdadeiroAtivo(marcado)
        );

        CheckBoxVisual custoFuturo = new CheckBoxVisual(
                "Custo Futuro",
                configuracaoEdicao.isCustoFuturo(),
                marcado -> configuracaoEdicao.setCustoFuturo(marcado)
        );

        CheckBoxVisual custoEstimado = new CheckBoxVisual(
                "Custo Estimado",
                configuracaoEdicao.isCustoEstimado(),
                marcado -> configuracaoEdicao.setCustoEstimado(marcado)
        );

        CheckBoxVisual flagEncalhados = new CheckBoxVisual(
                "Flag de Itens Encalhados",
                configuracaoEdicao.isFlagItensEncalhados(),
                marcado -> configuracaoEdicao.setFlagItensEncalhados(marcado)
        );

        CheckBoxVisual margemPorcentagem = new CheckBoxVisual(
                "Margem em Porcentagem",
                configuracaoEdicao.isMargemEmPorcentagem(),
                marcado -> configuracaoEdicao.setMargemEmPorcentagem(marcado)
        );

        opcoes.getChildren().addAll(
                custoVerdadeiro,
                custoFuturo,
                custoEstimado,
                flagEncalhados,
                margemPorcentagem
        );

        ScrollPane scroll = new ScrollPane(opcoes);
        scroll.getStyleClass().add("scroll-config-estados");
        scroll.setFitToWidth(true);
        scroll.setMaxWidth(Double.MAX_VALUE);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPannable(true);
        scroll.setStyle(
                "-fx-background: " + COR_FUNDO + ";" +
                "-fx-background-color: " + COR_FUNDO + ";" +
                "-fx-border-color: transparent;"
        );

        VBox.setVgrow(scroll, Priority.ALWAYS);

        box.getChildren().addAll(titulo, scroll);

        return box;
    }
    
    private VBox criarConteudoAparencia() {
        VBox box = criarConteudoBase();

        Label titulo = criarTituloConteudo("Configurações");

        HBox linhaZoom = new HBox(8);
        linhaZoom.setAlignment(Pos.CENTER_LEFT);
        linhaZoom.setPadding(new Insets(26, 0, 0, 0));

        Label zoomLabel = new Label("Zoom:");
        zoomLabel.setStyle(
                "-fx-font-size: 21px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COR_TEXTO + ";"
        );

        HBox controleZoom = new HBox(0);
        controleZoom.setAlignment(Pos.CENTER_LEFT);
        controleZoom.setPadding(new Insets(0, 4, 0, 4));
        controleZoom.setMinHeight(28);
        controleZoom.setPrefHeight(28);
        controleZoom.setMaxHeight(28);
        controleZoom.setMinWidth(112);
        controleZoom.setPrefWidth(112);
        controleZoom.setMaxWidth(112);
        controleZoom.setStyle(
                "-fx-background-color: " + COR_FUNDO + ";" +
                "-fx-border-color: " + COR_BORDA + ";" +
                "-fx-border-width: 2;"
        );

        StackPane lupaBox = criarCelulaControleZoom(carregarIcone("lupa.png", 16, 16), 22);

        Label diminuir = criarTextoControleZoom("-");
        Label valorZoom = criarValorZoom(configuracaoEdicao.getZoomInterface() + "%");
        Label aumentar = criarTextoControleZoom("+");

        diminuir.setOnMouseClicked(event -> {
            int novoZoom = Math.max(50, configuracaoEdicao.getZoomInterface() - 5);
            configuracaoEdicao.setZoomInterface(novoZoom);
            valorZoom.setText(novoZoom + "%");
        });

        aumentar.setOnMouseClicked(event -> {
            int novoZoom = Math.min(200, configuracaoEdicao.getZoomInterface() + 5);
            configuracaoEdicao.setZoomInterface(novoZoom);
            valorZoom.setText(novoZoom + "%");
        });

        controleZoom.getChildren().addAll(
                lupaBox,
                diminuir,
                valorZoom,
                aumentar
        );

        linhaZoom.getChildren().addAll(zoomLabel, controleZoom);

        box.getChildren().addAll(titulo, linhaZoom);

        return box;
    }
    
    private StackPane criarCelulaControleZoom(Node conteudo, double largura) {
        StackPane celula = new StackPane(conteudo);
        celula.setAlignment(Pos.CENTER);
        celula.setMinWidth(largura);
        celula.setPrefWidth(largura);
        celula.setMaxWidth(largura);
        celula.setMinHeight(24);
        celula.setPrefHeight(24);
        celula.setMaxHeight(24);

        return celula;
    }
    
    private Label criarTextoControleZoom(String texto) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER);
        label.setCursor(Cursor.HAND);
        label.setMinWidth(14);
        label.setPrefWidth(14);
        label.setMaxWidth(14);
        label.setMinHeight(24);
        label.setPrefHeight(24);
        label.setMaxHeight(24);
        label.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COR_TEXTO + ";" +
                "-fx-padding: -1 0 0 0;"
        );

        label.setOnMouseEntered(event -> label.setOpacity(0.75));
        label.setOnMouseExited(event -> {
            label.setOpacity(1.0);
            label.setTranslateY(0);
        });
        label.setOnMousePressed(event -> label.setTranslateY(1));
        label.setOnMouseReleased(event -> label.setTranslateY(0));

        return label;
    }
    
    private Label criarValorZoom(String texto) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER);
        label.setMinWidth(48);
        label.setPrefWidth(48);
        label.setMaxWidth(48);
        label.setMinHeight(24);
        label.setPrefHeight(24);
        label.setMaxHeight(24);
        label.setTextOverrun(OverrunStyle.CLIP);
        label.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-font-weight: normal;" +
                "-fx-text-fill: " + COR_TEXTO + ";" +
                "-fx-padding: -1 0 0 0;"
        );

        return label;
    }

    private VBox criarConteudoEstados() {
        VBox box = criarConteudoBase();

        Label titulo = criarTituloConteudo("Configurações");

        VBox listaEstados = new VBox(18);
        listaEstados.setPadding(new Insets(30, 0, 0, 0));

        List<EstadoConfigVisual> estados = criarEstadosPrototipo();

        for (EstadoConfigVisual estado : estados) {
            listaEstados.getChildren().add(criarLinhaEstado(estado));
        }

        listaEstados.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scroll = new ScrollPane(listaEstados);
        scroll.getStyleClass().add("scroll-config-estados");
        scroll.setFitToWidth(true);
        scroll.setMaxWidth(Double.MAX_VALUE);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle(
                "-fx-background: " + COR_FUNDO + ";" +
                "-fx-background-color: " + COR_FUNDO + ";" +
                "-fx-border-color: transparent;"
        );

        VBox.setVgrow(scroll, Priority.ALWAYS);

        box.getChildren().addAll(titulo, scroll);

        return box;
    }

    private List<EstadoConfigVisual> criarEstadosPrototipo() {
        List<EstadoConfigVisual> estados = new ArrayList<>();

        estados.add(new EstadoConfigVisual("São Paulo", new BigDecimal("50.00")));
        estados.add(new EstadoConfigVisual("Santa Catarina", new BigDecimal("40.22")));
        estados.add(new EstadoConfigVisual("Acre", new BigDecimal("50.00")));
        estados.add(new EstadoConfigVisual("Alagoas", new BigDecimal("50.00")));
        estados.add(new EstadoConfigVisual("Amapá", new BigDecimal("50.00")));
        estados.add(new EstadoConfigVisual("Amazonas", new BigDecimal("50.00")));
        estados.add(new EstadoConfigVisual("Bahia", new BigDecimal("50.00")));
        estados.add(new EstadoConfigVisual("Ceará", new BigDecimal("50.00")));
        estados.add(new EstadoConfigVisual("Distrito Federal", new BigDecimal("50.00")));
        estados.add(new EstadoConfigVisual("Espírito Santo", new BigDecimal("50.00")));

        return estados;
    }

    private HBox criarLinhaEstado(EstadoConfigVisual estado) {
        HBox linha = new HBox(12);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setMinHeight(38);

        Label nome = new Label(estado.nome());
        nome.setMinWidth(180);
        nome.setPrefWidth(180);
        nome.setStyle(
                "-fx-font-size: 21px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COR_TEXTO + ";"
        );

        Label percentual = new Label(formatarPercentual(estado.percentual()));
        percentual.setMinWidth(90);
        percentual.setPrefWidth(90);
        percentual.setAlignment(Pos.CENTER_RIGHT);
        percentual.setStyle(
                "-fx-font-size: 21px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COR_TEXTO + ";"
        );

        StackPane editar = new StackPane(carregarIcone("Lapis.png", 14, 14));
        editar.setCursor(Cursor.HAND);
        editar.setMinSize(24, 24);
        editar.setPrefSize(24, 24);
        editar.setMaxSize(24, 24);
        
        editar.setOnMouseEntered(event -> editar.setOpacity(0.70));
        editar.setOnMouseExited(event -> {
            editar.setOpacity(1.0);
            editar.setTranslateY(0);
        });
        editar.setOnMousePressed(event -> editar.setTranslateY(1));
        editar.setOnMouseReleased(event -> editar.setTranslateY(0));

        linha.getChildren().addAll(nome, percentual, editar);

        return linha;
    }

    private VBox criarConteudoUsuario() {
        VBox box = criarConteudoBase();
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(30, 14, 14, 14));

        StackPane avatar = criarAvatarUsuario();

        Label usuario = new Label(obterNomeUsuarioWindows());
        usuario.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + COR_TEXTO + ";"
        );

        Button limparUserData = new Button("LIMPAR USER DATA");
        limparUserData.setFocusTraversable(false);

        aplicarEfeitoBotao(
                limparUserData,
                estiloBotaoVermelhoNormal(),
                estiloBotaoVermelhoHover(),
                estiloBotaoVermelhoPressionado()
        );

        VBox.setMargin(usuario, new Insets(8, 0, 35, 0));

        box.getChildren().addAll(avatar, usuario, limparUserData);

        return box;
    }

    private StackPane criarAvatarUsuario() {
        StackPane root = new StackPane();
        root.setPrefSize(120, 120);
        root.setMaxSize(120, 120);

        Image fotoUsuario = obterFotoUsuarioWindows();

        if (fotoUsuario != null && !fotoUsuario.isError()) {
            ImageView foto = new ImageView(fotoUsuario);
            foto.setFitWidth(116);
            foto.setFitHeight(116);
            foto.setPreserveRatio(false);
            foto.setSmooth(true);

            Circle clip = new Circle(58, 58, 58);
            foto.setClip(clip);

            Circle borda = new Circle(58);
            borda.setFill(Color.TRANSPARENT);
            borda.setStroke(Color.web(COR_TEXTO));
            borda.setStrokeWidth(2);

            root.getChildren().addAll(foto, borda);

            return root;
        }

        Circle fundo = new Circle(58);
        fundo.setFill(Color.web("#E5E5E5"));

        Circle cabeca = new Circle(22);
        cabeca.setFill(Color.TRANSPARENT);
        cabeca.setStroke(Color.web(COR_TEXTO));
        cabeca.setStrokeWidth(2.2);
        cabeca.setTranslateY(-24);

        Arc corpo = new Arc(0, 0, 35, 27, 0, 180);
        corpo.setType(ArcType.OPEN);
        corpo.setFill(Color.TRANSPARENT);
        corpo.setStroke(Color.web(COR_TEXTO));
        corpo.setStrokeWidth(2.2);
        corpo.setTranslateY(32);

        root.getChildren().addAll(fundo, cabeca, corpo);

        return root;
    }

    private HBox criarRodape(Stage stage) {
        HBox rodape = new HBox(44);
        rodape.setAlignment(Pos.CENTER_RIGHT);
        rodape.setPadding(new Insets(12, 14, 10, 14));

        Button cancelar = criarBotaoRodape("CANCELAR");
        cancelar.setOnAction(event -> stage.close());

        Button ok = criarBotaoRodape("OK");
        ok.setOnAction(event -> {
            configuracaoUsuarioService.salvar(configuracaoEdicao);
            configuracaoUsuario = new ConfiguracaoUsuario(configuracaoEdicao);
            stage.close();
        });

        rodape.getChildren().addAll(cancelar, ok);

        return rodape;
    }

    private Button criarBotaoRodape(String texto) {
        Button botao = new Button(texto);
        botao.setFocusTraversable(false);
        botao.setMinHeight(30);

        aplicarEfeitoBotao(
                botao,
                estiloBotaoRodapeNormal(),
                estiloBotaoRodapeHover(),
                estiloBotaoRodapePressionado()
        );

        return botao;
    }

    private String formatarPercentual(BigDecimal valor) {
        BigDecimal valorSeguro = valor == null ? BigDecimal.ZERO : valor;

        return valorSeguro
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .toString()
                .replace(".", ",") + "%";
    }

    private enum AbaConfiguracao {
    	TABELA,
        APARENCIA,
        ESTADOS,
        USUARIO
    }

    private record EstadoConfigVisual(String nome, BigDecimal percentual) {
    }

    private class CheckBoxVisual extends HBox {

    	private final StackPane caixa = new StackPane();
    	private final Label texto = new Label();

        private boolean marcado;

        CheckBoxVisual(String descricao, boolean marcadoInicial, Consumer<Boolean> aoAlterar) {
            this.marcado = marcadoInicial;

            setAlignment(Pos.CENTER_LEFT);
            setSpacing(18);
            setCursor(Cursor.HAND);

            caixa.setMinSize(42, 42);
            caixa.setPrefSize(42, 42);
            caixa.setMaxSize(42, 42);
            caixa.setMinSize(42, 42);
            caixa.setPrefSize(42, 42);
            caixa.setMaxSize(42, 42);
            caixa.setAlignment(Pos.CENTER);

            texto.setText(descricao);
            

            getChildren().addAll(caixa, texto);

            atualizarVisual();

            setOnMouseClicked(e -> {
                marcado = !marcado;
                atualizarVisual();

                if (aoAlterar != null) {
                    aoAlterar.accept(marcado);
                }
            });

            setOnMouseEntered(e -> {
                setOpacity(0.85);
            });

            setOnMouseExited(e -> {
                setOpacity(1.0);
                setTranslateY(0);
            });

            setOnMousePressed(e -> {
                setTranslateY(1);
            });

            setOnMouseReleased(e -> {
                setTranslateY(0);
            });            
        }
        
             

        private void atualizarVisual() {
            caixa.getChildren().clear();

            texto.setStyle(
                    "-fx-font-size: 22px;" +
                    "-fx-font-weight: " + (marcado ? "bold" : "normal") + ";" +
                    "-fx-text-fill: " + COR_TEXTO + ";"
            );

            if (marcado) {
                caixa.getChildren().add(carregarIcone("Check.png", 30, 30));

                caixa.setStyle(
                        "-fx-background-color: " + COR_SELECIONADO + ";" +
                        "-fx-border-color: " + COR_SELECIONADO + ";" +
                        "-fx-border-width: 3;"
                );
            } else {
                caixa.setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: " + COR_BORDA + ";" +
                        "-fx-border-width: 3;"
                );
            }
        }

        public boolean isMarcado() {
            return marcado;
        }
    }
    
    private ImageView carregarIcone(String nomeArquivo, double largura, double altura) {
        URL url = getClass().getResource("/icons/" + nomeArquivo);

        if (url == null) {
            throw new IllegalStateException("Ícone não encontrado: /icons/" + nomeArquivo);
        }

        ImageView icone = new ImageView(new Image(url.toExternalForm()));
        icone.setFitWidth(largura);
        icone.setFitHeight(altura);
        icone.setPreserveRatio(true);
        icone.setSmooth(true);

        return icone;
    }
    
    private String estiloBotaoRodapeNormal() {
        return "-fx-background-color: transparent;" +
                "-fx-border-color: " + COR_BORDA + ";" +
                "-fx-border-width: 3;" +
                "-fx-text-fill: " + COR_TEXTO + ";" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: -2 7 -2 7;";
    }

    private String estiloBotaoRodapeHover() {
        return "-fx-background-color: #E0DED5;" +
                "-fx-border-color: #333333;" +
                "-fx-border-width: 3;" +
                "-fx-text-fill: #333333;" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: -2 7 -2 7;";
    }

    private String estiloBotaoRodapePressionado() {
        return "-fx-background-color: #B8B5AB;" +
                "-fx-border-color: #222222;" +
                "-fx-border-width: 3;" +
                "-fx-text-fill: #222222;" +
                "-fx-font-size: 20px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: -2 7 -2 7;";
    }

    private String estiloBotaoVermelhoNormal() {
        return "-fx-background-color: " + COR_VERMELHO + ";" +
                "-fx-border-color: #000000;" +
                "-fx-border-width: 3;" +
                "-fx-text-fill: #000000;" +
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 2 8 2 8;";
    }

    private String estiloBotaoVermelhoHover() {
        return "-fx-background-color: #FF3333;" +
                "-fx-border-color: #000000;" +
                "-fx-border-width: 3;" +
                "-fx-text-fill: #000000;" +
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 2 8 2 8;";
    }

    private String estiloBotaoVermelhoPressionado() {
        return "-fx-background-color: #CC0000;" +
                "-fx-border-color: #000000;" +
                "-fx-border-width: 3;" +
                "-fx-text-fill: #000000;" +
                "-fx-font-size: 19px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 2 8 2 8;";
    }
    
    private void aplicarEfeitoBotao(
            javafx.scene.Node node,
            String estiloNormal,
            String estiloHover,
            String estiloPressionado
    ) {
        if (node == null) {
            return;
        }

        node.setCursor(Cursor.HAND);
        node.setStyle(estiloNormal);

        node.setOnMouseEntered(event -> node.setStyle(estiloHover));
        node.setOnMouseExited(event -> {
            node.setTranslateY(0);
            node.setStyle(estiloNormal);
        });

        node.setOnMousePressed(event -> {
            node.setTranslateY(1);
            node.setStyle(estiloPressionado);
        });

        node.setOnMouseReleased(event -> {
            node.setTranslateY(0);

            if (node.isHover()) {
                node.setStyle(estiloHover);
            } else {
                node.setStyle(estiloNormal);
            }
        });
    }
    private String obterNomeUsuarioWindows() {
        String nome = System.getenv("USERNAME");

        if (nome == null || nome.isBlank()) {
            nome = System.getProperty("user.name", "Usuário");
        }

        return formatarNomeUsuario(nome);
    }

    private String formatarNomeUsuario(String nome) {
        if (nome == null || nome.isBlank()) {
            return "Usuário";
        }

        String normalizado = nome.trim()
                .replace(".", " ")
                .replace("_", " ")
                .replace("-", " ");

        normalizado = normalizado.replaceAll("([A-Za-zÀ-ÿ]+)(\\d+)", "$1 $2");

        String[] partes = normalizado.split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String parte : partes) {
            if (parte.isBlank()) {
                continue;
            }

            if (resultado.length() > 0) {
                resultado.append(" ");
            }

            resultado.append(parte.substring(0, 1).toUpperCase());
            resultado.append(parte.length() > 1 ? parte.substring(1).toLowerCase() : "");
        }

        return resultado.isEmpty() ? "Usuário" : resultado.toString();
    }
    
    private Image obterFotoUsuarioWindows() {
        List<Path> pastas = new ArrayList<>();

        adicionarPastaSeExistir(pastas, System.getenv("APPDATA"), "Microsoft", "Windows", "AccountPictures");
        adicionarPastaSeExistir(pastas, System.getenv("LOCALAPPDATA"), "Packages", "Microsoft.Windows.CloudExperienceHost_cw5n1h2txyewy", "LocalState", "AccountPictures");
        adicionarPastaSeExistir(pastas, System.getenv("PUBLIC"), "AccountPictures");
        adicionarPastaSeExistir(pastas, System.getenv("PROGRAMDATA"), "Microsoft", "User Account Pictures");

        for (Path pasta : pastas) {
            Optional<Path> imagem = buscarImagemMaisRecenteNaPasta(pasta);

            if (imagem.isEmpty()) {
                continue;
            }

            try {
                Image foto = new Image(imagem.get().toUri().toString(), 120, 120, false, true);

                if (!foto.isError()) {
                    return foto;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private void adicionarPastaSeExistir(List<Path> pastas, String raiz, String... partes) {
        if (raiz == null || raiz.isBlank()) {
            return;
        }

        Path caminho = Path.of(raiz, partes);

        if (Files.exists(caminho) && Files.isDirectory(caminho)) {
            pastas.add(caminho);
        }
    }

    private Optional<Path> buscarImagemMaisRecenteNaPasta(Path pasta) {
        try (Stream<Path> arquivos = Files.walk(pasta, 3)) {
            return arquivos
                    .filter(Files::isRegularFile)
                    .filter(this::ehArquivoImagem)
                    .max(Comparator.comparingLong(path -> {
                        File arquivo = path.toFile();
                        return arquivo.lastModified();
                    }));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private boolean ehArquivoImagem(Path path) {
        if (path == null || path.getFileName() == null) {
            return false;
        }

        String nome = path.getFileName().toString().toLowerCase();

        return nome.endsWith(".png")
                || nome.endsWith(".jpg")
                || nome.endsWith(".jpeg")
                || nome.endsWith(".bmp");
    }
}