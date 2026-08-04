package Visao;

import Controle.AnaliseService;
import Controle.ItemService;
import Infraestrutura.DiagnosticoAmbiente;
import Infraestrutura.diagnostico.TratadorErros;
import Modelo.CondicaoVenda;
import Modelo.DadosItem;
import Modelo.EstadoInfo;
import Modelo.ItemAnalise;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import Controle.SincronizacaoBasesService.ResultadoBase;
import Controle.SincronizacaoBasesService.ResultadoSincronizacao;

import java.util.function.UnaryOperator;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.scene.transform.Scale;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import Visao.componentes.MiniTabelaEstadoBase;
import Visao.componentes.TabelaResumo;

import Controle.LayoutTabelaService;
import Modelo.LayoutColunaTabela;
import javafx.collections.ListChangeListener;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import Controle.EstadoTabelaService;

import Configuracao.ConfiguracaoUsuario;
import Configuracao.ConfiguracaoUsuarioService;
import javafx.css.PseudoClass;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDateTime;

public class telaInicial extends Application {
    private final ItemService itemService = new ItemService();
    private final AnaliseService analiseService = new AnaliseService();
    private final ObservableList<ItemAnalise> itens = FXCollections.observableArrayList();
    private final LayoutTabelaService layoutTabelaService = new LayoutTabelaService();
    private final EstadoTabelaService estadoTabelaService = new EstadoTabelaService();
    private boolean atualizandoListaEstadosProgramaticamente = false;
    
    private final Label totalPropostaLabel = new Label("R$ 0,00");
    private final Label resultadoAtualComIpiLabel = new Label("R$ 0,00");
    private final Label resultadoAnteriorComIpiLabel = new Label("R$ 0,00");
    private final Label totalComIpiLabel = new Label("R$ 0,00");
    private final Label totalTabelaLabel = new Label("R$ 0,00");
    private final Label resultadoAtualLabel = new Label("R$ 0,00");
    private final Label resultadoAnteriorLabel = new Label("R$ 0,00");
    private final ComboBox<EstadoInfo> estadoCombo = new ComboBox<>();
    private final Label baseEstadoLabel = new Label("0,00%");
    
    private StackPane rootComOverlayPrincipal;
    private ProgressIndicator progressoOverlayAtualizacao;
    private Label textoOverlayAtualizacao;
    private Label subtextoOverlayAtualizacao;
    private final Scale escalaInterface = new Scale(1, 1, 0, 0);
    
    private final Label ultimaAtualizacaoLabel = new Label("Última atualização: --/--/----\nàs --:--");
    
    private TableView<ItemAnalise> tabela;
    private static final double ALTURA_LINHA_TABELA = 24;
    private static final double ALTURA_CABECALHO_TABELA = 42;
    private static final double ALTURA_BARRA_HORIZONTAL = 18;
    private static final double MARGEM_SEGURANCA_TABELA = 12;
    private static final double FOLGA_INTERNA_TABELA = 4;
    
    private static final String CODIGO_LINHA_RODAPE_TABELA = "__RODAPE_TABELA__";
    private final ItemAnalise linhaRodapeTabela = new ItemAnalise();
    
    private final List<TableColumn<ItemAnalise, ?>> ordemPadraoColunasTabela = new ArrayList<>();
    private final Map<TableColumn<ItemAnalise, ?>, List<TableColumn<ItemAnalise, ?>>> ordemPadraoSubcolunasTabela = new HashMap<>();
    
    private StackPane overlayAtualizacao;
    
    private static final String COLUNA_CODIGO = "CÓDIGO";
    private static final String COLUNA_QUANTIDADE = "QUANTIDADE";
    private static final String COLUNA_VALOR_UNITARIO = "VALOR UNITÁRIO";
    
    private int indiceLinhaFocoPendente = -1;
    private String colunaFocoPendente = null;
    private boolean colagemEmLoteEmAndamento = false;    
    private int indiceInicioSelecaoArrastada = -1;
    private boolean aplicandoLayoutTabela = false;
    private boolean salvamentoLayoutTabelaPendente = false;
    
    private TableColumn<ItemAnalise, String>
    custoReposicaoGrupoCol;
    
    private final ConfiguracaoUsuarioService configuracaoUsuarioService = new ConfiguracaoUsuarioService();
    private ConfiguracaoUsuario configuracaoUsuario = configuracaoUsuarioService.carregar();
    
    private static final PseudoClass PSEUDO_ITEM_ENCALHADO =
            PseudoClass.getPseudoClass("item-encalhado");
    
    private static final PseudoClass PSEUDO_CONDICAO_COLORIDA =
            PseudoClass.getPseudoClass("condicao-colorida");
    
    private static final PseudoClass PSEUDO_LINHA_RODAPE_TABELA =
            PseudoClass.getPseudoClass(
                    "linha-rodape-tabela"
            );
    
    private Pane painelConteudoZoom;
    private ScrollPane scrollConteudoPrincipal;
    
    private Timeline atualizacaoAutomaticaTimeline;
    private boolean atualizacaoDadosEmAndamento = false;
    
    private static final Duration INTERVALO_ATUALIZACAO_AUTOMATICA = Duration.hours(1);
    private static final DateTimeFormatter FORMATADOR_DATA_ATUALIZACAO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATADOR_HORA_ATUALIZACAO =
            DateTimeFormatter.ofPattern("HH:mm");
    
    private TableColumn<ItemAnalise, Void> okEncalhadoCol;
    private TableColumn<ItemAnalise, String> custoVerdadeiroGrupoCol;
    
    private final DoubleProperty fatorZoomInterfaceProperty = new SimpleDoubleProperty(1.0);
    
    private HBox linhaCotacaoDolar;
    private TextField campoCotacaoDolar;
    
    private HBox linhaFatorImportacao;
    private TextField campoFatorImportacao;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private void carregarEstadosConfigurados(
            String siglaSelecionada
    ) {
        atualizandoListaEstadosProgramaticamente = true;

        try {
            estadoCombo.setItems(
                    FXCollections.observableArrayList(
                            criarEstadoConfigurado("SP"),
                            criarEstadoConfigurado("SC"),
                            criarEstadoConfigurado("AC"),
                            criarEstadoConfigurado("AL"),
                            criarEstadoConfigurado("AP"),
                            criarEstadoConfigurado("AM"),
                            criarEstadoConfigurado("BA"),
                            criarEstadoConfigurado("CE"),
                            criarEstadoConfigurado("DF"),
                            criarEstadoConfigurado("ES"),
                            criarEstadoConfigurado("GO"),
                            criarEstadoConfigurado("MA"),
                            criarEstadoConfigurado("MT"),
                            criarEstadoConfigurado("MS"),
                            criarEstadoConfigurado("MG"),
                            criarEstadoConfigurado("PA"),
                            criarEstadoConfigurado("PB"),
                            criarEstadoConfigurado("PR"),
                            criarEstadoConfigurado("PE"),
                            criarEstadoConfigurado("PI"),
                            criarEstadoConfigurado("RJ"),
                            criarEstadoConfigurado("RN"),
                            criarEstadoConfigurado("RS"),
                            criarEstadoConfigurado("RO"),
                            criarEstadoConfigurado("RR"),
                            criarEstadoConfigurado("SE"),
                            criarEstadoConfigurado("TO")
                    )
            );

            String siglaParaSelecionar =
                    siglaSelecionada == null
                            || siglaSelecionada.isBlank()
                            ? "SP"
                            : siglaSelecionada;

            EstadoInfo estadoEncontrado =
                    estadoCombo.getItems()
                            .stream()
                            .filter(estado ->
                                    siglaParaSelecionar.equals(
                                            estado.getSigla()
                                    )
                            )
                            .findFirst()
                            .orElse(
                                    estadoCombo.getItems().get(0)
                            );

            estadoCombo.getSelectionModel().select(
                    estadoEncontrado
            );
        } finally {
            atualizandoListaEstadosProgramaticamente = false;
        }
    }
    
    private EstadoInfo criarEstadoConfigurado(String sigla) {
        BigDecimal percentual =
                configuracaoUsuario == null
                        ? new BigDecimal("50.00")
                        : configuracaoUsuario.getBaseEstado(sigla);

        return new EstadoInfo(
                sigla,
                percentual
        );
    }
    
    private void tratarNavegacaoCampoEditavel(
        KeyEvent event,
        int indiceAtual,
        String colunaAtual,
        Runnable confirmarValorAtual
    		) {
    KeyCode tecla = event.getCode();

    boolean teclaVertical = tecla == KeyCode.ENTER
            || tecla == KeyCode.UP
            || tecla == KeyCode.DOWN;

    boolean teclaHorizontal = tecla == KeyCode.TAB
            || tecla == KeyCode.LEFT
            || tecla == KeyCode.RIGHT;

    if (!teclaVertical && !teclaHorizontal) {
        return;
    }

    event.consume();

    confirmarValorAtual.run();

    if (tecla == KeyCode.ENTER) {
        moverFocoVertical(indiceAtual, colunaAtual, event.isShiftDown());
        return;
    }

    if (tecla == KeyCode.UP) {
        moverFocoVertical(indiceAtual, colunaAtual, true);
        return;
    }

    if (tecla == KeyCode.DOWN) {
        moverFocoVertical(indiceAtual, colunaAtual, false);
        return;
    }

    if (tecla == KeyCode.TAB) {
        moverFocoHorizontal(indiceAtual, colunaAtual, event.isShiftDown());
        return;
    }

    if (tecla == KeyCode.LEFT) {
        moverFocoHorizontal(indiceAtual, colunaAtual, true);
        return;
    }

    if (tecla == KeyCode.RIGHT) {
        moverFocoHorizontal(indiceAtual, colunaAtual, false);
    }
}
    
    private int obterZoomInterfaceSeguro() {
        if (configuracaoUsuario == null) {
            return 100;
        }

        int zoom = configuracaoUsuario.getZoomInterface();

        if (zoom < 50) {
            return 50;
        }

        if (zoom > 200) {
            return 200;
        }

        return zoom;
    }

    private double obterFatorZoomInterface() {
        return obterZoomInterfaceSeguro() / 100.0;
    }

    
    private void aplicarZoomInterface() {
        double fator =
                obterFatorZoomInterface();

        fatorZoomInterfaceProperty.set(
                fator
        );

        escalaInterface.setX(
                fator
        );

        escalaInterface.setY(
                fator
        );

        Platform.runLater(() -> {
            atualizarDimensoesConteudoZoom();

            if (rootComOverlayPrincipal != null) {
                rootComOverlayPrincipal.requestLayout();
            }

            if (painelConteudoZoom != null) {
                painelConteudoZoom.requestLayout();
            }

            if (scrollConteudoPrincipal != null) {
                scrollConteudoPrincipal.requestLayout();

                /*
                 * Sempre inicia pelo lado esquerdo após alterar
                 * o zoom. O usuário poderá navegar pela barra
                 * horizontal até o lado direito.
                 */
                scrollConteudoPrincipal.setHvalue(0.0);
            }
        });
    }
    
    private void atualizarDimensoesConteudoZoom() {
        if (rootComOverlayPrincipal == null
                || painelConteudoZoom == null
                || scrollConteudoPrincipal == null) {

            return;
        }

        double fator =
                obterFatorZoomInterface();

        if (fator <= 0) {
            fator = 1.0;
        }

        double larguraViewport =
                scrollConteudoPrincipal
                        .getViewportBounds()
                        .getWidth();

        double alturaViewport =
                scrollConteudoPrincipal
                        .getViewportBounds()
                        .getHeight();

        /*
         * Antes da primeira exibição, o viewport pode
         * ainda não possuir dimensões válidas.
         */
        if (larguraViewport <= 0) {
            larguraViewport =
                    scrollConteudoPrincipal.getWidth();
        }

        if (alturaViewport <= 0) {
            alturaViewport =
                    scrollConteudoPrincipal.getHeight();
        }

        if (larguraViewport <= 0
                || alturaViewport <= 0) {

            return;
        }

        /*
         * Quando o zoom é maior que 100%, mantemos a largura
         * lógica original da tela.
         *
         * O Scale aumentará essa largura visualmente e o
         * ScrollPane passará a oferecer navegação horizontal.
         *
         * Quando o zoom é menor que 100%, ampliamos a largura
         * lógica para continuar ocupando todo o viewport.
         */
        double larguraLogica;

        if (fator > 1.0) {
            larguraLogica =
                    Math.max(
                            1180,
                            larguraViewport
                    );
        } else {
            larguraLogica =
                    Math.max(
                            1180,
                            larguraViewport / fator
                    );
        }

        /*
         * A altura continua ajustada ao viewport porque não
         * queremos criar navegação vertical na tela inteira.
         */
        double alturaLogica =
                Math.max(
                        1,
                        alturaViewport / fator
                );

        rootComOverlayPrincipal.setMinSize(
                larguraLogica,
                alturaLogica
        );

        rootComOverlayPrincipal.setPrefSize(
                larguraLogica,
                alturaLogica
        );

        rootComOverlayPrincipal.setMaxSize(
                larguraLogica,
                alturaLogica
        );

        rootComOverlayPrincipal.resizeRelocate(
                0,
                0,
                larguraLogica,
                alturaLogica
        );

        /*
         * O tamanho do Pane representa o tamanho visual
         * depois da aplicação do zoom.
         */
        double larguraVisual =
                Math.ceil(
                        larguraLogica * fator
                );

        double alturaVisual =
                Math.ceil(
                        alturaLogica * fator
                );

        painelConteudoZoom.setMinSize(
                larguraVisual,
                alturaVisual
        );

        painelConteudoZoom.setPrefSize(
                larguraVisual,
                alturaVisual
        );

        painelConteudoZoom.setMaxSize(
                larguraVisual,
                alturaVisual
        );

        painelConteudoZoom.resize(
                larguraVisual,
                alturaVisual
        );
    }
    
    private int obterIndiceRodapeTabela() {
        int indiceRodape = itens.indexOf(linhaRodapeTabela);

        if (indiceRodape >= 0) {
            return indiceRodape;
        }

        return itens.size();
    }

    private void limparFocoTabela() {
        indiceLinhaFocoPendente = -1;
        colunaFocoPendente = null;

        if (tabela == null) {
            return;
        }

        tabela.getSelectionModel().clearSelection();

        if (tabela.getFocusModel() != null) {
            tabela.getFocusModel().focus(-1);
        }

        Platform.runLater(() -> {
            if (tabela.getScene() != null && tabela.getScene().getRoot() != null) {
                tabela.getScene().getRoot().requestFocus();
            }
        });
    }
    
    private void moverFocoVertical(int indiceAtual, String colunaAtual, boolean subir) {
    int indiceDestino = subir ? indiceAtual - 1 : indiceAtual + 1;

    if (indiceDestino < 0) {
        limparFocoTabela();
        return;
    }

    if (!subir && indiceDestino >= obterIndiceRodapeTabela()) {
        limparFocoTabela();
        return;
    }

    focarCelulaEditavel(indiceDestino, colunaAtual);
}

    private void moverFocoHorizontal(int indiceAtual, String colunaAtual, boolean voltar) {
        List<String> colunasEditaveis = obterColunasEditaveisVisiveisNaOrdem();

        int posicaoAtual = colunasEditaveis.indexOf(colunaAtual);

        if (posicaoAtual < 0) {
            return;
        }

        int indiceDestino = indiceAtual;
        int posicaoDestino = voltar ? posicaoAtual - 1 : posicaoAtual + 1;

        if (posicaoDestino < 0) {
            indiceDestino--;
            posicaoDestino = colunasEditaveis.size() - 1;
        } else if (posicaoDestino >= colunasEditaveis.size()) {
            indiceDestino++;
            posicaoDestino = 0;
        }

        if (indiceDestino < 0) {
            return;
        }

        focarCelulaEditavel(indiceDestino, colunasEditaveis.get(posicaoDestino));
    }

    private List<String> obterColunasEditaveisVisiveisNaOrdem() {
        List<String> colunas = new ArrayList<>();

        for (TableColumn<ItemAnalise, ?> coluna : tabela.getVisibleLeafColumns()) {
            String nome = coluna.getText();

            if (COLUNA_CODIGO.equals(nome)
                    || COLUNA_QUANTIDADE.equals(nome)
                    || COLUNA_VALOR_UNITARIO.equals(nome)) {
                colunas.add(nome);
            }
        }

        return colunas;
    }

    private void focarCelulaEditavel(int indiceLinha, String coluna) {
        if (indiceLinha < 0) {
            return;
        }

        garantirLinhaExistente(indiceLinha);

        if (indiceLinha >= itens.size()) {
            return;
        }

        if (isLinhaRodapeTabela(itens.get(indiceLinha))) {
            garantirLinhaExistente(indiceLinha);
        }

        indiceLinhaFocoPendente = indiceLinha;
        colunaFocoPendente = coluna;

        tabela.scrollTo(indiceLinha);
        tabela.getSelectionModel().clearAndSelect(indiceLinha);
        tabela.getFocusModel().focus(indiceLinha);
        tabela.refresh();
    }

    private void focarCampoSePendente(String coluna, int indiceLinha, TextField campo) {
        if (campo == null) {
            return;
        }

        if (indiceLinha != indiceLinhaFocoPendente || !coluna.equals(colunaFocoPendente)) {
            return;
        }

        Platform.runLater(() -> {
            if (indiceLinha != indiceLinhaFocoPendente || !coluna.equals(colunaFocoPendente)) {
                return;
            }

            campo.requestFocus();
            campo.selectAll();

            indiceLinhaFocoPendente = -1;
            colunaFocoPendente = null;
        });
    }
    
    @Override
    public void start(Stage stage) {
        TratadorErros.instalarHandlerGlobal();
        DiagnosticoAmbiente.registrarAmbienteInicial();

        try {
            prepararDadosIniciais();
        } catch (Exception e) {
            TratadorErros.tratar(
                    stage,
                    "Inicialização > preparar dados iniciais",
                    e
            );
            return;
        }

        /*
         * Cabeçalho fixo:
         * logo, título e engrenagem não recebem zoom.
         */
        HBox cabecalhoFixo = criarCabecalhoFixo();

        /*
         * Área que receberá zoom.
         */
        BorderPane conteudoComZoom = new BorderPane();
        conteudoComZoom.getStyleClass().add("root-app");

        VBox topoComZoom = criarTopoComZoom();

        TableView<ItemAnalise> tabelaCriada = criarTabela();
        HBox barraAcoesTabela = criarBarraAcoesTabela();

        VBox areaCentral = new VBox(
                0,
                barraAcoesTabela,
                tabelaCriada
        );

        areaCentral.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(tabelaCriada, Priority.NEVER);

        HBox rodape = criarRodape();

        conteudoComZoom.setTop(topoComZoom);
        conteudoComZoom.setCenter(areaCentral);
        conteudoComZoom.setBottom(rodape);

        /*
         * Overlay e conteúdo principal recebem zoom.
         * O cabeçalho fixo está fora deste StackPane.
         */
        rootComOverlayPrincipal = new StackPane(
                conteudoComZoom,
                criarOverlayAtualizacao()
        );

        rootComOverlayPrincipal.setFocusTraversable(true);
        rootComOverlayPrincipal
                .getTransforms()
                .setAll(escalaInterface);

        /*
         * O root ampliado fica dentro de um Pane de tamanho fixado
         * manualmente.
         *
         * Assim o ScrollPane não precisa calcular seu tamanho através
         * das células virtualizadas da TableView.
         */
        painelConteudoZoom =
                new Pane();

        rootComOverlayPrincipal.setManaged(
                false
        );

        painelConteudoZoom
                .getChildren()
                .add(rootComOverlayPrincipal);

        /*
         * Somente o corpo da tela possui navegação.
         * O cabeçalho com logo, título e configurações permanece fixo.
         */
        scrollConteudoPrincipal =
                new ScrollPane(
                        painelConteudoZoom
                );

        scrollConteudoPrincipal.setFitToWidth(false);
        scrollConteudoPrincipal.setFitToHeight(false);
        scrollConteudoPrincipal.setPannable(true);
        scrollConteudoPrincipal.setFocusTraversable(false);

        scrollConteudoPrincipal.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollConteudoPrincipal.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollConteudoPrincipal.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background: transparent;" +
                "-fx-padding: 0;"
        );

        /*
         * Mantém o corpo preenchendo pelo menos a área disponível.
         * Acima de 100%, o tamanho mínimo natural gera a navegação
         * horizontal sem afetar o cabeçalho fixo.
         */
        scrollConteudoPrincipal
        .viewportBoundsProperty()
        .addListener(
                (
                        observavel,
                        valorAnterior,
                        valorAtual
                ) -> atualizarDimensoesConteudoZoom()
        );

        /*
         * Estrutura final:
         * cabeçalho fixo em cima e corpo rolável embaixo.
         */
        BorderPane raizCena = new BorderPane();
        raizCena.getStyleClass().add("root-app");

        raizCena.setTop(cabecalhoFixo);
        raizCena.setCenter(scrollConteudoPrincipal);

        System.out.println("JAR NOVO - TESTE CSS");

        Scene scene = new Scene(
                raizCena,
                1180,
                680
        );

        scene.getStylesheets().add(
                localizarRecurso("estilo.css").toExternalForm()
        );

        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                this::tratarColagemGlobalTabela
        );

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!(event.getTarget() instanceof Node alvo)) {
                return;
            }

            boolean clicouNaTabela =
                    cliqueFoiDentroDoNo(alvo, tabela);

            boolean clicouNoRodape =
                    cliqueFoiDentroDoNo(alvo, rodape);

            if (!clicouNaTabela && !clicouNoRodape) {
                tabela.getSelectionModel().clearSelection();

                if (tabela.getFocusModel() != null) {
                    tabela.getFocusModel().focus(-1);
                }

                rootComOverlayPrincipal.requestFocus();
            }
        });

        /*
         * Altura da tabela calculada somente dentro da área ampliável.
         */
        tabelaCriada.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> calcularAlturaTabela(
                                conteudoComZoom,
                                topoComZoom,
                                rodape,
                                barraAcoesTabela
                        ),
                        itens,
                        conteudoComZoom.heightProperty(),
                        topoComZoom.heightProperty(),
                        rodape.heightProperty(),
                        barraAcoesTabela.heightProperty(),
                        fatorZoomInterfaceProperty
                )
        );

        tabelaCriada.minHeightProperty().bind(
                tabelaCriada.prefHeightProperty()
        );

        tabelaCriada.maxHeightProperty().bind(
                tabelaCriada.prefHeightProperty()
        );

        stage.setTitle("Programa de Análise de Margem");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(600);

        stage.setOnCloseRequest(event -> {
            salvarEstadoTabelaAtual();
            pararAtualizacaoAutomatica();
        });

        stage.show();

        aplicarZoomInterface();
        iniciarAtualizacaoAutomatica();
        preCarregarBasesAoIniciar();
    }
    
    private void salvarEstadoTabelaAtual() {
        try {
            estadoTabelaService.salvarLinhasTabelaInicial(itens);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private HBox criarBarraAcoesTabela() {
        HBox barra = new HBox(6);
        barra.getStyleClass().add("barra-acoes-tabela");
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(2, 0, 2, 6));
        barra.setMinHeight(34);
        barra.setPrefHeight(34);
        barra.setMaxHeight(34);

        Button reordenarButton = criarBotaoIconeTabela(
                "reordenar.png",
                "Reordenar colunas"
        );
        reordenarButton.setOnAction(event -> reordenarColunasTabela());

        Button limparButton = criarBotaoIconeTabela(
                "LIMPAR.png",
                "Limpar tabela"
        );
        limparButton.setOnAction(event -> limparTabela());

        barra.getChildren().addAll(reordenarButton, limparButton);

        return barra;
    }

    private Button criarBotaoIconeTabela(String nomeIcone, String tooltip) {
        Button botao = new Button();
        botao.getStyleClass().add("botao-icone-tabela");
        botao.setGraphic(carregarIcone(nomeIcone, 24, 24));
        botao.setTooltip(new Tooltip(tooltip));
        botao.setCursor(Cursor.HAND);
        botao.setFocusTraversable(false);

        botao.setMinSize(30, 30);
        botao.setPrefSize(30, 30);
        botao.setMaxSize(30, 30);

        return botao;
    }
    
    private void executarComTratamento(String contexto, Runnable acao) {
        try {
            acao.run();
        } catch (Exception e) {
            mostrarOverlayAtualizacao(false);
            TratadorErros.tratar(getJanelaAtual(), contexto, e);
        }
    }

    private Window getJanelaAtual() {
        if (tabela != null && tabela.getScene() != null) {
            return tabela.getScene().getWindow();
        }

        return null;
    }

    private void mostrarErro(String contexto, Throwable erro) {
        mostrarOverlayAtualizacao(false);
        TratadorErros.tratar(getJanelaAtual(), contexto, erro);
    }
    
    private boolean isLinhaRodapeTabela(ItemAnalise item) {
        return item == linhaRodapeTabela
                || item != null
                && CODIGO_LINHA_RODAPE_TABELA.equals(item.getCodigo());
    }

    private void adicionarLinhaVazia() {
        /*
         * Finaliza uma possível edição antes de alterar
         * estruturalmente a lista da tabela.
         */
        if (tabela != null) {
            tabela.edit(-1, null);
        }

        int indiceRodape =
                itens.indexOf(linhaRodapeTabela);

        if (indiceRodape < 0) {
            itens.add(new ItemAnalise());
            itens.add(linhaRodapeTabela);
        } else {
            itens.add(
                    indiceRodape,
                    new ItemAnalise()
            );
        }

        recalcularResumo();

        /*
         * Não utilizamos tabela.refresh().
         *
         * A ObservableList já notifica a TableView sobre
         * a nova linha. O refresh reconstruía todas as células
         * e podia disparar repetidamente o CSS das linhas.
         */
        if (tabela != null) {
            tabela.requestLayout();
        }
    }
    
    private void removerLinhasSelecionadas() {
        if (tabela == null) {
            return;
        }

        /*
         * Criamos uma cópia independente porque a seleção
         * muda automaticamente conforme os itens são removidos.
         */
        List<ItemAnalise> selecionados =
                new ArrayList<>(
                        obterLinhasSelecionadasEditaveis()
                );

        selecionados.removeIf(
                item -> item == null
                        || isLinhaRodapeTabela(item)
        );

        if (selecionados.isEmpty()) {
            return;
        }

        /*
         * Encerra qualquer edição antes de modificar
         * estruturalmente a lista.
         */
        tabela.edit(-1, null);

        /*
         * Remove seleção e foco antes de descartar as linhas.
         */
        tabela.getSelectionModel().clearSelection();

        if (tabela.getFocusModel() != null) {
            tabela.getFocusModel().focus(-1);
        }

        indiceInicioSelecaoArrastada = -1;
        indiceLinhaFocoPendente = -1;
        colunaFocoPendente = null;

        /*
         * A ObservableList atualiza a TableView automaticamente.
         */
        itens.removeAll(selecionados);

        garantirRodapeNoFinal();
        recalcularResumo();
        atualizarVisibilidadeColunaOkEncalhado();

        salvarEstadoTabelaAtual();

        /*
         * Não utilizar:
         * - tabela.refresh();
         * - Platform.runLater();
         * - tabela.requestLayout();
         *
         * A alteração da ObservableList já invalida o layout.
         */
    }

    private void garantirRodapeNoFinal() {
        int indiceRodape = itens.indexOf(linhaRodapeTabela);

        /*
         * O rodapé já está corretamente no final.
         * Não fazemos nenhuma alteração na lista.
         */
        if (indiceRodape == itens.size() - 1) {
            return;
        }

        /*
         * Caso esteja em outra posição, remove antes de recolocá-lo.
         */
        if (indiceRodape >= 0) {
            itens.remove(indiceRodape);
        }

        itens.add(linhaRodapeTabela);
    }

    private void atualizarLinhaRodapeTabela() {
        int quantidadeTotal = 0;
        BigDecimal valorTotalProposta = BigDecimal.ZERO;

        BigDecimal custoTotalAtual = BigDecimal.ZERO;
        BigDecimal valorTotalAtual = BigDecimal.ZERO;
        
        BigDecimal custoTotalVerdadeiro = BigDecimal.ZERO;
        BigDecimal valorTotalVerdadeiro = BigDecimal.ZERO;

        BigDecimal custoTotalPromob = BigDecimal.ZERO;
        BigDecimal valorTotalPromob = BigDecimal.ZERO;

        BigDecimal custoTotalAnterior = BigDecimal.ZERO;
        BigDecimal valorTotalAnterior = BigDecimal.ZERO;

        for (ItemAnalise item : itens) {
            if (isLinhaRodapeTabela(item) || !temCodigoPreenchido(item)) {
                continue;
            }

            int quantidade = item.getQuantidade();
            BigDecimal quantidadeDecimal = BigDecimal.valueOf(quantidade);
            BigDecimal valorTotalLinha = valorSeguro(item.getValorTotal());

            quantidadeTotal += quantidade;
            valorTotalProposta = valorTotalProposta.add(valorTotalLinha);

            if (custoValidoParaRodape(item.getCustoAtual(), valorTotalLinha, quantidade)) {
                custoTotalAtual = custoTotalAtual.add(
                        valorSeguro(item.getCustoAtual()).multiply(quantidadeDecimal)
                );
                valorTotalAtual = valorTotalAtual.add(valorTotalLinha);
            }
            
            if (custoValidoParaRodape(item.getCustoVerdadeiro(), valorTotalLinha, quantidade)) {
                custoTotalVerdadeiro = custoTotalVerdadeiro.add(
                        valorSeguro(item.getCustoVerdadeiro()).multiply(quantidadeDecimal)
                );
                valorTotalVerdadeiro = valorTotalVerdadeiro.add(valorTotalLinha);
            }

            if (custoValidoParaRodape(item.getCustoPromob(), valorTotalLinha, quantidade)) {
                custoTotalPromob = custoTotalPromob.add(
                        valorSeguro(item.getCustoPromob()).multiply(quantidadeDecimal)
                );
                valorTotalPromob = valorTotalPromob.add(valorTotalLinha);
            }

            if (custoValidoParaRodape(item.getCustoAnterior(), valorTotalLinha, quantidade)) {
                custoTotalAnterior = custoTotalAnterior.add(
                        valorSeguro(item.getCustoAnterior()).multiply(quantidadeDecimal)
                );
                valorTotalAnterior = valorTotalAnterior.add(valorTotalLinha);
            }
        }

        linhaRodapeTabela.setCodigo(CODIGO_LINHA_RODAPE_TABELA);
        linhaRodapeTabela.setDescricao("TOTAL");

        linhaRodapeTabela.setQuantidade(quantidadeTotal);
        linhaRodapeTabela.setValorTotal(valorTotalProposta);

        linhaRodapeTabela.setCustoAtual(custoTotalAtual);
        linhaRodapeTabela.setCustoVerdadeiro(custoTotalVerdadeiro);
        linhaRodapeTabela.setCustoPromob(custoTotalPromob);
        linhaRodapeTabela.setCustoAnterior(custoTotalAnterior);

        linhaRodapeTabela.setMargemAtual(calcularCustoSobreValor(custoTotalAtual, valorTotalAtual));
        linhaRodapeTabela.setMargemVerdadeira(
                calcularCustoSobreValor(custoTotalVerdadeiro, valorTotalVerdadeiro)
        );
        linhaRodapeTabela.setMargemPromob(calcularCustoSobreValor(custoTotalPromob, valorTotalPromob));
        linhaRodapeTabela.setMargemAnterior(calcularCustoSobreValor(custoTotalAnterior, valorTotalAnterior));

        garantirRodapeNoFinal();
    }
    
    private boolean custoValidoParaRodape(BigDecimal custo, BigDecimal valorTotalLinha, int quantidade) {
        return quantidade > 0
                && valorSeguro(custo).compareTo(BigDecimal.ZERO) > 0
                && valorSeguro(valorTotalLinha).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal calcularCustoSobreValor(BigDecimal custoTotal, BigDecimal valorTotal) {
        BigDecimal custoSeguro = valorSeguro(custoTotal);
        BigDecimal valorSeguro = valorSeguro(valorTotal);

        if (custoSeguro.compareTo(BigDecimal.ZERO) <= 0 || valorSeguro.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return custoSeguro.divide(valorSeguro, 6, RoundingMode.HALF_UP);
    }
    
    private static String formatarMargemDecimal(BigDecimal valor) {
        BigDecimal valorSeguro = valor == null ? BigDecimal.ZERO : valor;

        return valorSeguro
                .setScale(2, RoundingMode.HALF_UP)
                .toString()
                .replace('.', ',');
    }

    private void prepararDadosIniciais() {
    /*
     * Recarrega explicitamente para garantir que os estados
     * venham do arquivo de configurações.
     */
    configuracaoUsuario =
            configuracaoUsuarioService.carregar();

    estadoCombo.valueProperty().addListener(
            (obs, oldValue, newValue) -> {
                if (atualizandoListaEstadosProgramaticamente) {
                    return;
                }

                atualizarBaseEstado();
                recalcularTudoAposAlterarEstado();
            }
    );

    carregarEstadosConfigurados("SP");

    atualizarBaseEstado();

    carregarEstadoTabelaPersistidoOuPadrao();

    recalcularResumo();
}

    private void carregarEstadoTabelaPersistidoOuPadrao() {
        itens.clear();

        List<ItemAnalise> linhasPersistidas = estadoTabelaService.carregarLinhasTabelaInicial();

        if (linhasPersistidas != null && !linhasPersistidas.isEmpty()) {
            itens.addAll(linhasPersistidas);
        }

        while (itens.size() < 5) {
            itens.add(new ItemAnalise());
        }

        linhaRodapeTabela.setCodigo(CODIGO_LINHA_RODAPE_TABELA);
        linhaRodapeTabela.setDescricao("TOTAL");

        itens.add(linhaRodapeTabela);
        garantirRodapeNoFinal();
        atualizarVisibilidadeColunaOkEncalhado();
    }
    
    private ImageView carregarIcone(String nomeArquivo, double largura, double altura) {
    URL url = localizarRecurso("icons/" + nomeArquivo);

    ImageView icone = new ImageView(
            new javafx.scene.image.Image(url.toExternalForm())
    );

    icone.setFitWidth(largura);
    icone.setFitHeight(altura);
    icone.setPreserveRatio(true);
    icone.setSmooth(true);

    return icone;
}
    
    private void iniciarAtualizacaoAutomatica() {
        pararAtualizacaoAutomatica();

        atualizacaoAutomaticaTimeline = new Timeline(
                new KeyFrame(INTERVALO_ATUALIZACAO_AUTOMATICA, event -> atualizarDadosAutomaticamente())
        );

        atualizacaoAutomaticaTimeline.setCycleCount(Timeline.INDEFINITE);
        atualizacaoAutomaticaTimeline.play();
    }

    private void pararAtualizacaoAutomatica() {
        if (atualizacaoAutomaticaTimeline != null) {
            atualizacaoAutomaticaTimeline.stop();
            atualizacaoAutomaticaTimeline = null;
        }
    }

    private void atualizarDadosAutomaticamente() {
        atualizarDados(false);
    }

    private void atualizarTextoUltimaAtualizacao() {
        atualizarTextoUltimaAtualizacao(null);
    }

    private void atualizarTextoUltimaAtualizacao(
            ResultadoSincronizacao resultadoSincronizacao
    ) {
        LocalDateTime agora =
                LocalDateTime.now();

        boolean usandoCopiaLocal =
                resultadoSincronizacao != null
                        && resultadoSincronizacao
                                .quantidadeFallbackLocal() > 0;

        String complemento =
                usandoCopiaLocal
                        ? " (cópia local)"
                        : "";

        ultimaAtualizacaoLabel.setText(
                "Última atualização: "
                        + agora.format(
                                FORMATADOR_DATA_ATUALIZACAO
                        )
                        + "\nàs "
                        + agora.format(
                                FORMATADOR_HORA_ATUALIZACAO
                        )
                        + complemento
        );
    }
    
    private void atualizarDados() {
        atualizarDados(true);
    }

    private void atualizarDados(
            boolean mostrarAvisoConclusao
    ) {
        /*
         * Impede clique duplo, atualização automática simultânea
         * ou atualização durante o pré-carregamento inicial.
         */
        if (atualizacaoDadosEmAndamento) {
            return;
        }

        atualizacaoDadosEmAndamento = true;

        prepararOverlayAtualizacao(
                "Atualizando dados...",
                "Preparando sincronização das bases."
        );

        List<Integer> indices =
                new ArrayList<>();

        List<String> codigos =
                new ArrayList<>();

        for (int i = 0; i < itens.size(); i++) {
            ItemAnalise item =
                    itens.get(i);

            if (isLinhaRodapeTabela(item)) {
                continue;
            }

            if (!temCodigoPreenchido(item)) {
                continue;
            }

            indices.add(i);
            codigos.add(item.getCodigo());
        }

        Task<ResultadoAtualizacaoDados> tarefa =
                new Task<>() {

            @Override
            protected ResultadoAtualizacaoDados call()
                    throws Exception {

                ResultadoSincronizacao resultadoSincronizacao =
                        itemService.recarregarBases(
                                (
                                        atual,
                                        total,
                                        base,
                                        etapa
                                ) -> atualizarOverlaySincronizacao(
                                        atual,
                                        total,
                                        base.getNomeArquivo(),
                                        etapa
                                )
                        );

                atualizarOverlayAtualizacao(
                        "Atualizando dados...",
                        codigos.isEmpty()
                                ? "Finalizando atualização."
                                : "Consultando "
                                        + codigos.size()
                                        + (
                                        codigos.size() == 1
                                                ? " item da tabela."
                                                : " itens da tabela."
                                ),
                        ProgressIndicator.INDETERMINATE_PROGRESS
                );

                List<AtualizacaoLinha> atualizacoes =
                        new ArrayList<>();

                for (int i = 0; i < codigos.size(); i++) {
                    String codigo =
                            codigos.get(i);

                    int indiceLinha =
                            indices.get(i);

                    DadosItem dadosItem =
                            itemService
                                    .buscarPorCodigo(codigo)
                                    .orElse(null);

                    atualizacoes.add(
                            new AtualizacaoLinha(
                                    indiceLinha,
                                    dadosItem
                            )
                    );
                }

                return new ResultadoAtualizacaoDados(
                        atualizacoes,
                        resultadoSincronizacao
                );
            }
        };

        tarefa.setOnSucceeded(event -> {
            ResultadoAtualizacaoDados resultadoTarefa =
                    tarefa.getValue();

            List<AtualizacaoLinha> atualizacoes =
                    resultadoTarefa == null
                            ? List.of()
                            : resultadoTarefa.atualizacoes();

            ResultadoSincronizacao resultadoSincronizacao =
                    resultadoTarefa == null
                            ? null
                            : resultadoTarefa.sincronizacao();

            for (
                    AtualizacaoLinha atualizacao
                    : atualizacoes
            ) {
                int indice =
                        atualizacao.indiceLinha();

                if (indice < 0
                        || indice >= itens.size()) {

                    continue;
                }

                ItemAnalise item =
                        itens.get(indice);

                if (isLinhaRodapeTabela(item)) {
                    continue;
                }

                item.aplicarDadosItem(
                        atualizacao.dadosItem()
                );

                recalcularItem(item);
            }

            recalcularResumo();
            atualizarVisibilidadeColunaOkEncalhado();

            tabela.refresh();

            atualizarTextoUltimaAtualizacao(
                    resultadoSincronizacao
            );

            mostrarOverlayAtualizacao(false);
            atualizacaoDadosEmAndamento = false;

            if (mostrarAvisoConclusao) {
                mostrarAviso(
                        "Atualização concluída",
                        criarMensagemConclusaoAtualizacao(
                                resultadoSincronizacao
                        )
                );
            }
        });

        tarefa.setOnFailed(event -> {
            Throwable erro =
                    tarefa.getException();

            mostrarOverlayAtualizacao(false);
            atualizacaoDadosEmAndamento = false;

            if (mostrarAvisoConclusao) {
                mostrarErro(
                        "Atualização de dados > sincronizar "
                                + "bases e consultar itens",
                        erro
                );

            } else if (erro != null) {
                erro.printStackTrace();
            }
        });

        tarefa.setOnCancelled(event -> {
            mostrarOverlayAtualizacao(false);
            atualizacaoDadosEmAndamento = false;
        });

        Thread thread =
                new Thread(
                        tarefa,
                        "AtualizacaoDadosThread"
                );

        thread.setDaemon(true);
        thread.start();
    }
       
    private HBox criarCabecalhoFixo() {
        HBox cabecalho = new HBox(20);

        cabecalho.getStyleClass().add("cabecalho");
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.setPadding(new Insets(10, 14, 8, 14));
        cabecalho.setMinHeight(72);
        cabecalho.setPrefHeight(72);
        cabecalho.setMaxHeight(72);

        VBox logoBox = new VBox();
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPrefWidth(150);
        logoBox.setMinWidth(150);
        logoBox.setMaxWidth(150);

        ImageView logo = carregarIcone(
                "logo-plasnox.png",
                80,
                48
        );

        logoBox.getChildren().add(logo);

        Label titulo = new Label(
                "ANÁLISE DE MARGEM EM ORÇAMENTO COMERCIAL"
        );

        titulo.getStyleClass().add("titulo-principal");
        titulo.setAlignment(Pos.CENTER);
        titulo.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(
                titulo,
                Priority.ALWAYS
        );

        Button configButton = new Button();

        configButton.setGraphic(
                carregarIcone("config.png", 18, 18)
        );

        configButton.getStyleClass().add("botao-config");
        configButton.setTooltip(new Tooltip("Configurações"));
        configButton.setFocusTraversable(false);

        configButton.setOnAction(event ->
                executarComTratamento(
                        "Botão CONFIGURAÇÕES",
                        this::abrirTelaConfiguracoes
                )
        );

        cabecalho.getChildren().addAll(
                logoBox,
                titulo,
                configButton
        );

        return cabecalho;
    }
    
    private VBox criarTopoComZoom() {
        VBox topo = new VBox();
        topo.getStyleClass().add("topo");

        HBox comandos = new HBox(18);
        comandos.getStyleClass().add("area-comandos");
        comandos.setAlignment(Pos.TOP_LEFT);
        comandos.setPadding(new Insets(8, 14, 8, 14));
        comandos.setFillHeight(false);

        Button atualizarButton = new Button("ATUALIZAR DADOS");

        atualizarButton.setGraphic(
                carregarIcone("atualizar.png", 14, 14)
        );

        atualizarButton.setContentDisplay(ContentDisplay.LEFT);
        atualizarButton.setGraphicTextGap(4);
        atualizarButton.getStyleClass().add("botao-acao");

        atualizarButton.setOnAction(event ->
                executarComTratamento(
                        "Botão ATUALIZAR DADOS",
                        this::atualizarDados
                )
        );

        atualizarButton.setMinWidth(190);
        atualizarButton.setPrefWidth(190);
        atualizarButton.setMaxWidth(190);

        ultimaAtualizacaoLabel.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #555555;"
        );

        ultimaAtualizacaoLabel.setWrapText(false);
        ultimaAtualizacaoLabel.setTextOverrun(OverrunStyle.CLIP);

        ultimaAtualizacaoLabel.setMinWidth(255);
        ultimaAtualizacaoLabel.setPrefWidth(255);
        ultimaAtualizacaoLabel.setMaxWidth(255);

        VBox atualizarBox = new VBox(3);
        atualizarBox.setAlignment(Pos.CENTER_LEFT);

        atualizarBox.setMinWidth(255);
        atualizarBox.setPrefWidth(255);
        atualizarBox.setMaxWidth(255);

        atualizarBox.getChildren().addAll(
                atualizarButton,
                ultimaAtualizacaoLabel
        );

        MiniTabelaEstadoBase estadoBaseTabela =
                new MiniTabelaEstadoBase(
                        estadoCombo,
                        baseEstadoLabel
                );
        
        linhaCotacaoDolar =
                criarLinhaCotacaoDolar();

        linhaFatorImportacao =
                criarLinhaFatorImportacao();

        VBox estadoECotacaoBox =
                new VBox(
                        4,
                        estadoBaseTabela,
                        linhaCotacaoDolar,
                        linhaFatorImportacao
                );

        estadoECotacaoBox.setAlignment(
                Pos.TOP_LEFT
        );

        /*
         * A MiniTabelaEstadoBase possui:
         *
         * Estado: 82 px
         * Base:   230 px
         *
         * Total:  312 px
         */
        estadoECotacaoBox.setMinWidth(312);
        estadoECotacaoBox.setPrefWidth(312);
        estadoECotacaoBox.setMaxWidth(312);

        Button valorPadraoButton =
                new Button("BUSCAR VALOR PADRÃO");

        valorPadraoButton.setGraphic(
                carregarIcone("lupa.png", 14, 14)
        );

        valorPadraoButton.setContentDisplay(
                ContentDisplay.LEFT
        );

        valorPadraoButton.setGraphicTextGap(4);
        valorPadraoButton.getStyleClass().add("botao-busca");

        valorPadraoButton.setOnAction(event ->
                executarComTratamento(
                        "Botão BUSCAR VALOR PADRÃO",
                        this::abrirTelaBuscaPreco
                )
        );

        valorPadraoButton.setMinWidth(220);
        valorPadraoButton.setPrefWidth(220);
        valorPadraoButton.setMaxWidth(220);
        valorPadraoButton.setTextOverrun(OverrunStyle.CLIP);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        GridPane resumo = new TabelaResumo(
                totalTabelaLabel,
                totalPropostaLabel,
                totalComIpiLabel,
                resultadoAtualLabel,
                resultadoAtualComIpiLabel,
                resultadoAnteriorLabel,
                resultadoAnteriorComIpiLabel
        );

        comandos.getChildren().addAll(
                atualizarBox,
                estadoECotacaoBox,
                valorPadraoButton,
                spacer,
                resumo
        );

        topo.getChildren().add(comandos);

        return topo;
    }
    
    private HBox criarLinhaFatorImportacao() {
        Label rotuloFator =
                new Label("FATOR IMP.:");

        /*
         * Reutilizamos as mesmas classes CSS
         * do dólar para manter o desenho idêntico.
         */
        rotuloFator
                .getStyleClass()
                .add("rotulo-cotacao-dolar");

        rotuloFator.setAlignment(
                Pos.CENTER
        );

        rotuloFator.setMinWidth(120);
        rotuloFator.setPrefWidth(120);
        rotuloFator.setMaxWidth(120);

        campoFatorImportacao =
                new TextField();

        campoFatorImportacao
                .getStyleClass()
                .add("campo-cotacao-dolar");

        campoFatorImportacao.setAlignment(
                Pos.CENTER
        );

        campoFatorImportacao.setMinWidth(0);

        campoFatorImportacao.setMaxWidth(
                Double.MAX_VALUE
        );

        /*
         * Exemplos aceitos:
         *
         * 0
         * 0,00
         * 0.00
         * 12,50
         *
         * Não permite números negativos.
         */
        UnaryOperator<TextFormatter.Change>
                filtroFator =
                alteracao -> {

                    String novoTexto =
                            alteracao
                                    .getControlNewText();

                    if (novoTexto.matches(
                            "\\d{0,4}([,.]\\d{0,2})?"
                    )) {
                        return alteracao;
                    }

                    return null;
                };

        campoFatorImportacao.setTextFormatter(
                new TextFormatter<>(
                        filtroFator
                )
        );

        /*
         * Carrega o valor salvo antes
         * de instalar os listeners.
         */
        atualizarCampoFatorImportacao();

        campoFatorImportacao
                .textProperty()
                .addListener(
                        (
                                observavel,
                                textoAnterior,
                                textoNovo
                        ) ->
                                persistirFatorImportacao(
                                        textoNovo
                                )
                );

        /*
         * Ao perder o foco, mostra sempre
         * duas casas decimais.
         */
        campoFatorImportacao
                .focusedProperty()
                .addListener(
                        (
                                observavel,
                                estavaFocado,
                                estaFocado
                        ) -> {

                            if (!estaFocado) {
                                atualizarCampoFatorImportacao();
                            }
                        }
                );

        campoFatorImportacao.setOnAction(
                evento -> {
                    atualizarCampoFatorImportacao();

                    if (tabela != null) {
                        tabela.requestFocus();
                    }
                }
        );

        HBox linha =
                new HBox(
                        0,
                        rotuloFator,
                        campoFatorImportacao
                );

        linha.getStyleClass()
                .add("bloco-cotacao-dolar");

        linha.setAlignment(
                Pos.CENTER_LEFT
        );

        /*
         * Mesma largura da tabela
         * Estado/Base e da linha do dólar.
         */
        linha.setMinWidth(312);
        linha.setPrefWidth(312);
        linha.setMaxWidth(312);

        linha.setMinHeight(32);
        linha.setPrefHeight(32);
        linha.setMaxHeight(32);

        HBox.setHgrow(
                campoFatorImportacao,
                Priority.ALWAYS
        );

        boolean deveMostrar =
                configuracaoUsuario != null
                        && configuracaoUsuario
                                .isCustoFuturo();

        linha.setVisible(
                deveMostrar
        );

        linha.setManaged(
                deveMostrar
        );

        return linha;
    }
    
    private void persistirFatorImportacao(
            String texto
    ) {
        if (configuracaoUsuario == null) {
            configuracaoUsuario =
                    new ConfiguracaoUsuario();
        }

        /*
         * Campo apagado:
         * remove também o valor da configuração.
         */
        if (texto == null
                || texto.isBlank()) {

            if (configuracaoUsuario
                    .getFatorImportacaoReposicao()
                    == null) {

                return;
            }

            configuracaoUsuario
                    .setFatorImportacaoReposicao(
                            null
                    );

            configuracaoUsuarioService.salvar(
                    configuracaoUsuario
            );

            return;
        }

        BigDecimal novoFator =
                converterTextoFatorImportacao(
                        texto
                );

        if (novoFator == null) {
            return;
        }

        BigDecimal fatorAtual =
                configuracaoUsuario
                        .getFatorImportacaoReposicao();

        /*
         * Evita gravações repetidas.
         */
        if (fatorAtual != null
                && fatorAtual.compareTo(
                        novoFator
                ) == 0) {

            return;
        }

        configuracaoUsuario
                .setFatorImportacaoReposicao(
                        novoFator
                );

        configuracaoUsuarioService.salvar(
                configuracaoUsuario
        );
    }

    private BigDecimal converterTextoFatorImportacao(
            String texto
    ) {
        if (texto == null
                || texto.isBlank()) {

            return null;
        }

        try {
            String textoNormalizado =
                    texto.trim()
                            .replace(',', '.');

            /*
             * Permite digitação temporária como:
             *
             * 0,
             * 5.
             */
            if (textoNormalizado.endsWith(".")) {
                textoNormalizado += "0";
            }

            BigDecimal valor =
                    new BigDecimal(
                            textoNormalizado
                    );

            /*
             * Zero é válido.
             * Apenas negativos seriam rejeitados.
             */
            if (valor.compareTo(
                    BigDecimal.ZERO
            ) < 0) {

                return null;
            }

            return valor.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void atualizarCampoFatorImportacao() {
        if (campoFatorImportacao == null) {
            return;
        }

        BigDecimal fator =
                configuracaoUsuario == null
                        ? null
                        : configuracaoUsuario
                                .getFatorImportacaoReposicao();

        String textoFormatado;

        if (fator == null) {
            textoFormatado = "";

        } else {
            textoFormatado =
                    fator.setScale(
                            2,
                            RoundingMode.HALF_UP
                    )
                            .toPlainString()
                            .replace('.', ',');
        }

        if (!textoFormatado.equals(
                campoFatorImportacao.getText()
        )) {
            campoFatorImportacao.setText(
                    textoFormatado
            );
        }
    }
    
    private HBox criarLinhaCotacaoDolar() {
        Label rotuloDolar =
                new Label("DÓLAR:");

        rotuloDolar
                .getStyleClass()
                .add("rotulo-cotacao-dolar");

        rotuloDolar.setAlignment(
                Pos.CENTER
        );

        rotuloDolar.setMinWidth(120);
        rotuloDolar.setPrefWidth(120);
        rotuloDolar.setMaxWidth(120);

        campoCotacaoDolar =
                new TextField();

        campoCotacaoDolar
                .getStyleClass()
                .add("campo-cotacao-dolar");

        campoCotacaoDolar.setAlignment(
                Pos.CENTER
        );

        campoCotacaoDolar.setMinWidth(0);
        campoCotacaoDolar.setMaxWidth(
                Double.MAX_VALUE
        );

        /*
         * Aceita, por exemplo:
         *
         * 5
         * 5,30
         * 5.30
         */
        UnaryOperator<TextFormatter.Change>
                filtroCotacao =
                alteracao -> {

                    String novoTexto =
                            alteracao
                                    .getControlNewText();

                    if (novoTexto.matches(
                            "\\d{0,4}([,.]\\d{0,2})?"
                    )) {
                        return alteracao;
                    }

                    return null;
                };

        campoCotacaoDolar.setTextFormatter(
                new TextFormatter<>(
                        filtroCotacao
                )
        );

        /*
         * Carrega o valor persistido antes
         * de instalar o listener.
         */
        atualizarCampoCotacaoDolar();

        campoCotacaoDolar
                .textProperty()
                .addListener(
                        (
                                observavel,
                                textoAnterior,
                                textoNovo
                        ) ->
                                persistirCotacaoDolarSeValida(
                                        textoNovo
                                )
                );

        /*
         * Ao sair do campo, normaliza para
         * duas casas decimais.
         */
        campoCotacaoDolar
                .focusedProperty()
                .addListener(
                        (
                                observavel,
                                estavaFocado,
                                estaFocado
                        ) -> {

                            if (!estaFocado) {
                                atualizarCampoCotacaoDolar();
                            }
                        }
                );

        campoCotacaoDolar.setOnAction(
                evento -> {
                    atualizarCampoCotacaoDolar();

                    if (tabela != null) {
                        tabela.requestFocus();
                    }
                }
        );

        HBox linha =
                new HBox(
                        0,
                        rotuloDolar,
                        campoCotacaoDolar
                );

        linha.getStyleClass()
                .add("bloco-cotacao-dolar");

        linha.setAlignment(
                Pos.CENTER_LEFT
        );

        /*
         * Mesma largura da MiniTabelaEstadoBase.
         */
        linha.setMinWidth(312);
        linha.setPrefWidth(312);
        linha.setMaxWidth(312);

        linha.setMinHeight(32);
        linha.setPrefHeight(32);
        linha.setMaxHeight(32);

        HBox.setHgrow(
                campoCotacaoDolar,
                Priority.ALWAYS
        );

        boolean deveMostrar =
                configuracaoUsuario != null
                        && configuracaoUsuario
                                .isCustoFuturo();

        linha.setVisible(
                deveMostrar
        );

        linha.setManaged(
                deveMostrar
        );

        return linha;
    }
        
    private boolean cliqueFoiDentroDoNo(Node alvo, Node noPai) {
        Node atual = alvo;

        while (atual != null) {
            if (atual == noPai) {
                return true;
            }

            atual = atual.getParent();
        }

        return false;
    }
    
    private boolean ordenarTabelaMantendoRodape(TableView<ItemAnalise> tableView) {
        if (tableView == null || tableView.getItems() == null) {
            return true;
        }

        Comparator<ItemAnalise> comparator = tableView.getComparator();

        List<ItemAnalise> linhasNormais = new ArrayList<>();
        ItemAnalise rodapeEncontrado = null;

        for (ItemAnalise item : tableView.getItems()) {
            if (isLinhaRodapeTabela(item)) {
                rodapeEncontrado = item;
            } else {
                linhasNormais.add(item);
            }
        }

        if (comparator != null) {
            linhasNormais.sort(comparator);
        }

        tableView.getItems().setAll(linhasNormais);

        if (rodapeEncontrado != null) {
            tableView.getItems().add(rodapeEncontrado);
        }

        return true;
    }
    
    private boolean eventoMouseVeioDeAreaEditavel(MouseEvent event) {
        if (!(event.getTarget() instanceof Node node)) {
            return false;
        }

        Node atual = node;

        while (atual != null) {
            if (atual instanceof TextInputControl) {
                return true;
            }

            if (atual instanceof TableCell<?, ?> cell) {
                TableColumn<?, ?> coluna = cell.getTableColumn();

                if (coluna != null) {
                    String nomeColuna = coluna.getText();

                    if (COLUNA_CODIGO.equals(nomeColuna)
                            || COLUNA_QUANTIDADE.equals(nomeColuna)
                            || COLUNA_VALOR_UNITARIO.equals(nomeColuna)) {
                        return true;
                    }
                }
            }

            atual = atual.getParent();
        }

        return false;
    }

    private void selecionarIntervaloTabela(int indiceInicial, int indiceFinal, boolean adicionarSelecao) {
        if (tabela == null || tabela.getSelectionModel() == null) {
            return;
        }

        int inicio = Math.min(indiceInicial, indiceFinal);
        int fim = Math.max(indiceInicial, indiceFinal);

        if (!adicionarSelecao) {
            tabela.getSelectionModel().clearSelection();
        }

        for (int i = inicio; i <= fim; i++) {
            if (i < 0 || i >= itens.size()) {
                continue;
            }

            ItemAnalise item = itens.get(i);

            if (isLinhaRodapeTabela(item)) {
                continue;
            }

            tabela.getSelectionModel().select(i);
        }
    }

    private List<ItemAnalise> obterLinhasSelecionadasEditaveis() {
        List<ItemAnalise> selecionados = new ArrayList<>();

        if (tabela == null || tabela.getSelectionModel() == null) {
            return selecionados;
        }

        for (ItemAnalise item : tabela.getSelectionModel().getSelectedItems()) {
            if (item == null || isLinhaRodapeTabela(item)) {
                continue;
            }

            selecionados.add(item);
        }

        return selecionados;
    }
    
    @SuppressWarnings("unchecked")
	private TableView<ItemAnalise> criarTabela() {
        tabela = new TableView<>(itens);
        tabela.setEditable(true);
        tabela.getStyleClass().add("tabela-analise");
        tabela.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tabela.setFixedCellSize(ALTURA_LINHA_TABELA);
        tabela.setSortPolicy(this::ordenarTabelaMantendoRodape);
        tabela.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        TableColumn<ItemAnalise, ItemAnalise> numeroLinhaCol =
                new TableColumn<>("");

        numeroLinhaCol.setId("numero_linha");
        numeroLinhaCol.setSortable(false);
        numeroLinhaCol.setReorderable(false);
        numeroLinhaCol.setResizable(false);

        numeroLinhaCol.setMinWidth(42);
        numeroLinhaCol.setPrefWidth(42);
        numeroLinhaCol.setMaxWidth(42);

        numeroLinhaCol.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(
                        data.getValue()
                )
        );

        numeroLinhaCol.setCellFactory(coluna ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            ItemAnalise itemLinha,
                            boolean empty
                    ) {
                        super.updateItem(itemLinha, empty);

                        int indice = getIndex();

                        if (empty
                                || indice < 0
                                || itemLinha == null
                                || isLinhaRodapeTabela(itemLinha)) {

                            setText("");
                            setGraphic(null);
                            return;
                        }

                        setText(
                                String.valueOf(indice + 1)
                        );

                        setGraphic(null);
                        setAlignment(Pos.CENTER);
                    }
                }
        );
        
        TableColumn<ItemAnalise, String> codigoCol = new TableColumn<>("CÓDIGO");
        codigoCol.setCellValueFactory(data -> data.getValue().codigoProperty());
        codigoCol.setCellFactory(col -> new CodigoComBotaoCell());
        codigoCol.setPrefWidth(170);

        TableColumn<ItemAnalise, String> descricaoCol = new TableColumn<>("DESCRIÇÃO");
        descricaoCol.setCellValueFactory(data -> data.getValue().descricaoProperty());
        descricaoCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText("");
                    setStyle("");
                    return;
                }

                setText(item == null ? "" : item);
                setAlignment(Pos.CENTER_LEFT);

                ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();
                aplicarEstiloCondicao(this, itemLinha);
            }
        });
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
        valorTotalCol.setCellFactory(col -> new BigDecimalTableCell(true, true,true));
        valorTotalCol.setPrefWidth(125);
        
        okEncalhadoCol = new TableColumn<>("");
        okEncalhadoCol.setCellFactory(col -> new OkItemEncalhadoCell());
        okEncalhadoCol.setPrefWidth(48);
        okEncalhadoCol.setMinWidth(42);
        okEncalhadoCol.setMaxWidth(58);
        okEncalhadoCol.setSortable(false);
        okEncalhadoCol.setReorderable(false);
        okEncalhadoCol.setVisible(false);

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

        TableColumn<ItemAnalise, BigDecimal> margemVerdadeiraCol = new TableColumn<>("MARGEM VERD.");
        margemVerdadeiraCol.setCellValueFactory(data -> data.getValue().margemVerdadeiraProperty());
        margemVerdadeiraCol.setCellFactory(col -> new MargemTableCell());
        margemVerdadeiraCol.getStyleClass().add("coluna-cinza");
        margemVerdadeiraCol.setPrefWidth(125);

        TableColumn<ItemAnalise, BigDecimal> custoVerdadeiroCol = new TableColumn<>("CUSTO");
        custoVerdadeiroCol.setCellValueFactory(data ->
                new ReadOnlyObjectWrapper<>(data.getValue().getCustoVerdadeiro())
        );
        custoVerdadeiroCol.setCellFactory(col -> new CustoTableCell());
        custoVerdadeiroCol.getStyleClass().add("coluna-cinza");
        custoVerdadeiroCol.setPrefWidth(105);
        
        /*
         * Coluna com o valor do custo que será
         * utilizado para reposição.
         */
        TableColumn<ItemAnalise, BigDecimal>
                custoReposicaoCol =
                new TableColumn<>("CUSTO");

        custoReposicaoCol.setCellValueFactory(
                data ->
                        data.getValue()
                                .custoReposicaoProperty()
        );

        custoReposicaoCol.setCellFactory(
                coluna ->
                        new CustoReposicaoTableCell()
        );

        custoReposicaoCol
                .getStyleClass()
                .add("coluna-verde");

        custoReposicaoCol.setPrefWidth(115);


        /*
         * Processo de importação responsável
         * pela reposição futura.
         */
        TableColumn<ItemAnalise, String>
                processoReposicaoCol =
                new TableColumn<>("PROCESSO");

        processoReposicaoCol.setCellValueFactory(
                data ->
                        data.getValue()
                                .processoReposicaoProperty()
        );

        processoReposicaoCol.setCellFactory(
                coluna ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    String processo,
                                    boolean empty
                            ) {
                                super.updateItem(
                                        processo,
                                        empty
                                );

                                ItemAnalise itemLinha =
                                        getTableRow() == null
                                                ? null
                                                : getTableRow()
                                                        .getItem();

                                if (empty
                                        || isLinhaRodapeTabela(
                                                itemLinha
                                        )
                                        || !temCodigoPreenchido(
                                                itemLinha
                                        )) {

                                    setText("");
                                    setStyle("");
                                    return;
                                }

                                setText(
                                        processo == null
                                                ? ""
                                                : processo
                                );

                                setAlignment(
                                        Pos.CENTER
                                );

                                setStyle("");
                            }
                        }
        );

        processoReposicaoCol
                .getStyleClass()
                .add("coluna-verde");

        processoReposicaoCol.setPrefWidth(125);


        /*
         * Grupo visível apenas quando a opção
         * Custo Reposição estiver habilitada.
         */
        custoReposicaoGrupoCol =
                new TableColumn<>(
                        "CUSTO REPOSIÇÃO"
                );

        custoReposicaoGrupoCol
                .getColumns()
                .addAll(
                        custoReposicaoCol,
                        processoReposicaoCol
                );

        custoReposicaoGrupoCol
                .getStyleClass()
                .add("grupo-verde");


        /*
         * Identificadores utilizados para salvar
         * ordem e largura das colunas.
         */
        custoReposicaoGrupoCol.setId(
                "custo_reposicao"
        );

        custoReposicaoCol.setId(
                "custo_reposicao_custo"
        );

        processoReposicaoCol.setId(
                "custo_reposicao_processo"
        );

        TableColumn<ItemAnalise, LocalDate> dataCustoVerdadeiroCol = new TableColumn<>("DATA CUSTO");
        dataCustoVerdadeiroCol.setCellValueFactory(data -> data.getValue().dataCustoVerdadeiroProperty());
        dataCustoVerdadeiroCol.setCellFactory(col -> new DataTableCell());
        dataCustoVerdadeiroCol.getStyleClass().add("coluna-cinza");
        dataCustoVerdadeiroCol.setPrefWidth(110);

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
        
        custoVerdadeiroGrupoCol = new TableColumn<>("CUSTO VERDADEIRO");
        custoVerdadeiroGrupoCol.getColumns().addAll(
                margemVerdadeiraCol,
                custoVerdadeiroCol,
                dataCustoVerdadeiroCol
        );
        custoVerdadeiroGrupoCol.getStyleClass().add("grupo-cinza");

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
        
        configurarIdentificadoresColunasTabela(
                codigoCol,
                descricaoCol,
                quantidadeCol,
                valorUnitarioCol,
                valorTotalCol,

                baseAtualGrupo,
                variacaoAtualCol,
                margemAtualCol,
                custoAtualCol,
                dataCustoCol,

                basePromobGrupo,
                margemPromobCol,
                custoPromobCol,
                dataCustoPromobCol,
                
                custoVerdadeiroGrupoCol,
                margemVerdadeiraCol,
                custoVerdadeiroCol,
                dataCustoVerdadeiroCol,

                analiseAnteriorGrupo,
                variacaoAnteriorCol,
                margemAnteriorCol,
                custoAnteriorCol,
                dataCustoAnteriorCol
        );

        tabela.getColumns().addAll(
                numeroLinhaCol,
                codigoCol,
                descricaoCol,
                quantidadeCol,
                valorUnitarioCol,
                valorTotalCol,
                okEncalhadoCol,
                baseAtualGrupo,
                custoVerdadeiroGrupoCol,
                custoReposicaoGrupoCol,
                basePromobGrupo,
                analiseAnteriorGrupo
        );
        
        registrarOrdemPadraoColunasTabela(
                numeroLinhaCol,
                codigoCol,
                descricaoCol,
                quantidadeCol,
                valorUnitarioCol,
                valorTotalCol,
                okEncalhadoCol,
                baseAtualGrupo,
                custoVerdadeiroGrupoCol,
                custoReposicaoGrupoCol,
                basePromobGrupo,
                analiseAnteriorGrupo
        );
        
        aplicarLayoutPersistidoTabela();
        tabela.getColumns().remove(numeroLinhaCol);
        tabela.getColumns().add(0, numeroLinhaCol);
        registrarListenersPersistenciaLayoutTabela();
        
        atualizarVisibilidadeColunaCustoVerdadeiro();
        atualizarVisibilidadeColunaCustoReposicao();

        tabela.setRowFactory(tv -> {
        	TableRow<ItemAnalise> row = new TableRow<>() {
        		@Override
        		protected void updateItem(
        		        ItemAnalise item,
        		        boolean empty
        		) {
        		    super.updateItem(item, empty);

        		    boolean linhaRodape =
        		            !empty
        		                    && item != null
        		                    && isLinhaRodapeTabela(item);
        		    
        		    pseudoClassStateChanged(
        		            PSEUDO_LINHA_RODAPE_TABELA,
        		            linhaRodape
        		    );

        		    atualizarPseudoClassesLinha(this);
        		}};

            row.setOnMousePressed(event -> {
                if (event.getButton() != MouseButton.PRIMARY) {
                    return;
                }

                if (row.isEmpty() || isLinhaRodapeTabela(row.getItem())) {
                    return;
                }

                if (eventoMouseVeioDeAreaEditavel(event)) {
                    return;
                }

                indiceInicioSelecaoArrastada = row.getIndex();

                selecionarIntervaloTabela(
                        indiceInicioSelecaoArrastada,
                        row.getIndex(),
                        event.isShortcutDown() || event.isControlDown()
                );

                event.consume();
            });

            row.setOnDragDetected(event -> {
                if (indiceInicioSelecaoArrastada >= 0) {
                    row.startFullDrag();
                    event.consume();
                }
            });

            row.setOnMouseDragEntered(event -> {
                if (indiceInicioSelecaoArrastada < 0) {
                    return;
                }

                if (row.isEmpty() || isLinhaRodapeTabela(row.getItem())) {
                    return;
                }

                selecionarIntervaloTabela(
                        indiceInicioSelecaoArrastada,
                        row.getIndex(),
                        false
                );

                event.consume();
            });

            row.setOnMouseReleased(event -> {
                indiceInicioSelecaoArrastada = -1;
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

    private void posicionarColunaDepoisDe(
            ObservableList<TableColumn<ItemAnalise, ?>> colunas,
            String idColunaMover,
            String idColunaReferencia
    ) {
        if (colunas == null || idColunaMover == null || idColunaReferencia == null) {
            return;
        }

        TableColumn<ItemAnalise, ?> colunaMover = null;
        TableColumn<ItemAnalise, ?> colunaReferencia = null;

        for (TableColumn<ItemAnalise, ?> coluna : colunas) {
            String id = obterIdColunaTabela(coluna);

            if (idColunaMover.equals(id)) {
                colunaMover = coluna;
            }

            if (idColunaReferencia.equals(id)) {
                colunaReferencia = coluna;
            }
        }

        if (colunaMover == null || colunaReferencia == null || colunaMover == colunaReferencia) {
            return;
        }

        colunas.remove(colunaMover);

        int indiceReferencia = colunas.indexOf(colunaReferencia);

        if (indiceReferencia < 0) {
            colunas.add(colunaMover);
            return;
        }

        int indiceDestino = indiceReferencia + 1;

        if (indiceDestino >= colunas.size()) {
            colunas.add(colunaMover);
        } else {
            colunas.add(indiceDestino, colunaMover);
        }
    }

    private void atualizarEstiloCondicaoLinha(
            TableRow<ItemAnalise> row
    ) {
        if (row == null) {
            return;
        }

        ItemAnalise item = row.getItem();

        /*
         * Linhas vazias, o TOTAL e itens obsoletos não recebem
         * a cor da condição.
         *
         * O destaque de item obsoleto continua tendo prioridade.
         */
        if (row.isEmpty()
                || item == null
                || isLinhaRodapeTabela(item)
                || deveDestacarItemEncalhado(item)) {

            limparEstiloCondicaoLinha(row);
            return;
        }

        CondicaoVenda condicao =
                item.getCondicaoVenda();

        if (condicao == null
                || condicao == CondicaoVenda.NORMAL) {

            limparEstiloCondicaoLinha(row);
            return;
        }

        String corFundo;
        String corTexto;

        if (condicao == CondicaoVenda.ESPECIAL) {
            corFundo =
                    item.getCorEspecialFundo();

            corTexto =
                    item.getCorEspecialTexto();
        } else {
            corFundo =
                    condicao.getCorFundo();

            corTexto =
                    condicao.getCorTexto();
        }

        if (corFundo == null
                || corFundo.isBlank()) {

            limparEstiloCondicaoLinha(row);
            return;
        }

        if (corTexto == null
                || corTexto.isBlank()) {

            corTexto = "#000000";
        }

        /*
         * Essas duas propriedades serão lidas pelo CSS
         * da própria linha e de todas as células filhas.
         */
        String novoEstilo =
                "-cor-condicao-fundo: "
                        + corFundo
                        + ";"
                        + "-cor-condicao-texto: "
                        + corTexto
                        + ";";

        if (!novoEstilo.equals(row.getStyle())) {
            row.setStyle(novoEstilo);
        }

        if (!row.getPseudoClassStates().contains(
                PSEUDO_CONDICAO_COLORIDA
        )) {
            row.pseudoClassStateChanged(
                    PSEUDO_CONDICAO_COLORIDA,
                    true
            );
        }
    }
    
    private void limparEstiloCondicaoLinha(
            TableRow<ItemAnalise> row
    ) {
        if (row == null) {
            return;
        }

        if (row.getPseudoClassStates().contains(
                PSEUDO_CONDICAO_COLORIDA
        )) {
            row.pseudoClassStateChanged(
                    PSEUDO_CONDICAO_COLORIDA,
                    false
            );
        }

        String estiloAtual = row.getStyle();

        if (estiloAtual != null
                && !estiloAtual.isBlank()) {

            row.setStyle("");
        }
    }
    
    private URL localizarRecurso(String caminhoRelativo) {
        String caminhoNormal = "/" + caminhoRelativo;
        String caminhoComResources = "/resources/" + caminhoRelativo;

        URL url = getClass().getResource(caminhoNormal);

        if (url != null) {
            return url;
        }

        url = getClass().getResource(caminhoComResources);

        if (url != null) {
            return url;
        }

        throw new IllegalStateException(
                "Recurso não encontrado. Tentativas: "
                        + caminhoNormal
                        + " e "
                        + caminhoComResources
        );
    }
    
    private void configurarIdentificadoresColunasTabela(
            TableColumn<ItemAnalise, ?> codigoCol,
            TableColumn<ItemAnalise, ?> descricaoCol,
            TableColumn<ItemAnalise, ?> quantidadeCol,
            TableColumn<ItemAnalise, ?> valorUnitarioCol,
            TableColumn<ItemAnalise, ?> valorTotalCol,

            TableColumn<ItemAnalise, ?> baseAtualGrupo,
            TableColumn<ItemAnalise, ?> variacaoAtualCol,
            TableColumn<ItemAnalise, ?> margemAtualCol,
            TableColumn<ItemAnalise, ?> custoAtualCol,
            TableColumn<ItemAnalise, ?> dataCustoCol,
            
            TableColumn<ItemAnalise, ?> custoVerdadeiroGrupoCol,
            TableColumn<ItemAnalise, ?> margemVerdadeiraCol,
            TableColumn<ItemAnalise, ?> custoVerdadeiroCol,
            TableColumn<ItemAnalise, ?> dataCustoVerdadeiroCol,

            TableColumn<ItemAnalise, ?> basePromobGrupo,
            TableColumn<ItemAnalise, ?> margemPromobCol,
            TableColumn<ItemAnalise, ?> custoPromobCol,
            TableColumn<ItemAnalise, ?> dataCustoPromobCol,

            TableColumn<ItemAnalise, ?> analiseAnteriorGrupo,
            TableColumn<ItemAnalise, ?> variacaoAnteriorCol,
            TableColumn<ItemAnalise, ?> margemAnteriorCol,
            TableColumn<ItemAnalise, ?> custoAnteriorCol,
            TableColumn<ItemAnalise, ?> dataCustoAnteriorCol
    ) {
        codigoCol.setId("codigo");
        descricaoCol.setId("descricao");
        quantidadeCol.setId("quantidade");
        valorUnitarioCol.setId("valor_unitario");
        valorTotalCol.setId("valor_total");
        okEncalhadoCol.setId("ok_encalhado");

        baseAtualGrupo.setId("base_atual");
        variacaoAtualCol.setId("base_atual_variacao");
        margemAtualCol.setId("base_atual_margem");
        custoAtualCol.setId("base_atual_custo");
        dataCustoCol.setId("base_atual_data_custo");
        
        custoVerdadeiroGrupoCol.setId("custo_verdadeiro");
        margemVerdadeiraCol.setId("custo_verdadeiro_margem");
        custoVerdadeiroCol.setId("custo_verdadeiro_custo");
        dataCustoVerdadeiroCol.setId("custo_verdadeiro_data_custo");

        basePromobGrupo.setId("base_promob");
        margemPromobCol.setId("base_promob_margem");
        custoPromobCol.setId("base_promob_custo");
        dataCustoPromobCol.setId("base_promob_data_custo");

        analiseAnteriorGrupo.setId("analise_anterior");
        variacaoAnteriorCol.setId("analise_anterior_variacao");
        margemAnteriorCol.setId("analise_anterior_margem");
        custoAnteriorCol.setId("analise_anterior_custo");
        dataCustoAnteriorCol.setId("analise_anterior_data_custo");
    }

    private boolean deveExibirBotaoOkEncalhado(ItemAnalise item) {
        return configuracaoUsuario != null
                && configuracaoUsuario.isFlagItensEncalhados()
                && item != null
                && !isLinhaRodapeTabela(item)
                && item.isItemEncalhado()
                && !item.isItemEncalhadoConfirmado();
    }
    
    private void atualizarVisibilidadeColunaCustoVerdadeiro() {
        if (custoVerdadeiroGrupoCol == null) {
            return;
        }

        boolean deveMostrar = configuracaoUsuario == null
                || configuracaoUsuario.isCustoVerdadeiroAtivo();

        custoVerdadeiroGrupoCol.setVisible(deveMostrar);
    }
    
    private void
    atualizarVisibilidadeColunaCustoReposicao() {
        boolean deveMostrar =
                configuracaoUsuario != null
                        && configuracaoUsuario
                                .isCustoFuturo();

        if (custoReposicaoGrupoCol != null) {
            custoReposicaoGrupoCol.setVisible(
                    deveMostrar
            );
        }

        if (linhaCotacaoDolar != null) {
            linhaCotacaoDolar.setVisible(
                    deveMostrar
            );

            linhaCotacaoDolar.setManaged(
                    deveMostrar
            );
        }

        if (linhaFatorImportacao != null) {
            linhaFatorImportacao.setVisible(
                    deveMostrar
            );

            linhaFatorImportacao.setManaged(
                    deveMostrar
            );
        }
    }

    private void atualizarVisibilidadeColunaOkEncalhado() {
        if (okEncalhadoCol == null) {
            return;
        }

        boolean deveMostrar = itens.stream()
                .anyMatch(this::deveExibirBotaoOkEncalhado);

        okEncalhadoCol.setVisible(deveMostrar);
    }

    private void confirmarItemEncalhado(ItemAnalise item) {
        if (item == null || isLinhaRodapeTabela(item)) {
            return;
        }

        item.setItemEncalhadoConfirmado(true);

        atualizarVisibilidadeColunaOkEncalhado();

        if (tabela != null) {
            tabela.refresh();
        }

        salvarEstadoTabelaAtual();
    }
    
    private void aplicarLayoutPersistidoTabela() {
        if (tabela == null) {
            return;
        }

        List<LayoutColunaTabela> layout =
                layoutTabelaService
                        .carregarLayoutColunasTabelaInicial();

        if (layout == null || layout.isEmpty()) {
            return;
        }

        /*
         * Layouts salvos antes da criação do Custo Reposição
         * ainda não possuem essa coluna registrada.
         */
        boolean layoutPossuiCustoReposicao =
                layout.stream()
                        .filter(Objects::nonNull)
                        .map(
                                LayoutColunaTabela::getIdColuna
                        )
                        .filter(Objects::nonNull)
                        .anyMatch(
                                "custo_reposicao"::equals
                        );

        aplicandoLayoutTabela = true;

        try {
            aplicarOrdemLayoutNasColunas(
                    tabela.getColumns(),
                    null,
                    layout
            );

            posicionarColunaDepoisDe(
                    tabela.getColumns(),
                    "ok_encalhado",
                    "valor_total"
            );

            posicionarColunaDepoisDe(
                    tabela.getColumns(),
                    "custo_verdadeiro",
                    "base_atual"
            );

            /*
             * Só define a posição inicial quando o layout salvo
             * é antigo e ainda não conhece essa coluna.
             *
             * Depois que ela estiver registrada, a ordem escolhida
             * pelo usuário será respeitada.
             */
            if (!layoutPossuiCustoReposicao) {
                posicionarColunaDepoisDe(
                        tabela.getColumns(),
                        "custo_reposicao",
                        "custo_verdadeiro"
                );
            }

            aplicarLargurasLayoutNasColunas(
                    tabela.getColumns(),
                    layout
            );

        } finally {
            aplicandoLayoutTabela = false;
        }
    }

    private void aplicarOrdemLayoutNasColunas(
            ObservableList<TableColumn<ItemAnalise, ?>> colunas,
            String idPai,
            List<LayoutColunaTabela> layout
    ) {
        if (colunas == null || colunas.isEmpty() || layout == null || layout.isEmpty()) {
            return;
        }

        Map<String, TableColumn<ItemAnalise, ?>> colunasPorId = new HashMap<>();

        for (TableColumn<ItemAnalise, ?> coluna : colunas) {
            String id = obterIdColunaTabela(coluna);

            if (!id.isBlank()) {
                colunasPorId.put(id, coluna);
            }
        }

        List<LayoutColunaTabela> layoutNivel = layout.stream()
                .filter(item -> Objects.equals(normalizarIdPai(item.getIdPai()), normalizarIdPai(idPai)))
                .sorted(Comparator.comparingInt(LayoutColunaTabela::getOrdem))
                .toList();

        List<TableColumn<ItemAnalise, ?>> novaOrdem = new ArrayList<>();
        Set<TableColumn<ItemAnalise, ?>> adicionadas = new HashSet<>();

        for (LayoutColunaTabela itemLayout : layoutNivel) {
            TableColumn<ItemAnalise, ?> coluna = colunasPorId.get(itemLayout.getIdColuna());

            if (coluna == null) {
                continue;
            }

            novaOrdem.add(coluna);
            adicionadas.add(coluna);
        }

        for (TableColumn<ItemAnalise, ?> coluna : colunas) {
            if (!adicionadas.contains(coluna)) {
                novaOrdem.add(coluna);
            }
        }

        if (!novaOrdem.isEmpty()) {
            colunas.setAll(novaOrdem);
        }

        for (TableColumn<ItemAnalise, ?> coluna : colunas) {
            if (!coluna.getColumns().isEmpty()) {
                aplicarOrdemLayoutNasColunas(
                        coluna.getColumns(),
                        obterIdColunaTabela(coluna),
                        layout
                );
            }
        }
    }

    private void aplicarLargurasLayoutNasColunas(
            ObservableList<TableColumn<ItemAnalise, ?>> colunas,
            List<LayoutColunaTabela> layout
    ) {
        if (colunas == null || layout == null || layout.isEmpty()) {
            return;
        }

        Map<String, LayoutColunaTabela> layoutPorId = new HashMap<>();

        for (LayoutColunaTabela itemLayout : layout) {
            if (itemLayout != null && itemLayout.getIdColuna() != null) {
                layoutPorId.put(itemLayout.getIdColuna(), itemLayout);
            }
        }

        aplicarLargurasLayoutNasColunasRecursivo(colunas, layoutPorId);
    }

    private void aplicarLargurasLayoutNasColunasRecursivo(
            ObservableList<TableColumn<ItemAnalise, ?>> colunas,
            Map<String, LayoutColunaTabela> layoutPorId
    ) {
        for (TableColumn<ItemAnalise, ?> coluna : colunas) {
            String id = obterIdColunaTabela(coluna);
            LayoutColunaTabela itemLayout = layoutPorId.get(id);

            if (itemLayout != null && itemLayout.getLargura() > 20) {
                coluna.setPrefWidth(itemLayout.getLargura());
            }

            if (!coluna.getColumns().isEmpty()) {
                aplicarLargurasLayoutNasColunasRecursivo(coluna.getColumns(), layoutPorId);
            }
        }
    }

    private void registrarListenersPersistenciaLayoutTabela() {
        if (tabela == null) {
            return;
        }

        registrarListenersPersistenciaLayoutColunas(tabela.getColumns());
    }

    private void registrarListenersPersistenciaLayoutColunas(
            ObservableList<TableColumn<ItemAnalise, ?>> colunas
    ) {
        if (colunas == null) {
            return;
        }

        colunas.addListener((ListChangeListener<TableColumn<ItemAnalise, ?>>) change -> solicitarSalvarLayoutTabela());

        for (TableColumn<ItemAnalise, ?> coluna : colunas) {
            coluna.widthProperty().addListener((obs, larguraAnterior, larguraNova) -> solicitarSalvarLayoutTabela());

            if (!coluna.getColumns().isEmpty()) {
                registrarListenersPersistenciaLayoutColunas(coluna.getColumns());
            }
        }
    }

    private void solicitarSalvarLayoutTabela() {
        if (aplicandoLayoutTabela || tabela == null) {
            return;
        }

        if (salvamentoLayoutTabelaPendente) {
            return;
        }

        salvamentoLayoutTabelaPendente = true;

        Platform.runLater(() -> {
            salvamentoLayoutTabelaPendente = false;

            if (aplicandoLayoutTabela || tabela == null) {
                return;
            }

            try {
                layoutTabelaService.salvarLayoutColunasTabelaInicial(capturarLayoutColunasTabela());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private List<LayoutColunaTabela> capturarLayoutColunasTabela() {
        List<LayoutColunaTabela> layout = new ArrayList<>();

        if (tabela == null) {
            return layout;
        }

        capturarLayoutColunasTabela(tabela.getColumns(), null, layout);

        return layout;
    }

    private void capturarLayoutColunasTabela(
            ObservableList<TableColumn<ItemAnalise, ?>> colunas,
            String idPai,
            List<LayoutColunaTabela> layout
    ) {
        if (colunas == null || layout == null) {
            return;
        }

        for (int i = 0; i < colunas.size(); i++) {
            TableColumn<ItemAnalise, ?> coluna = colunas.get(i);
            String id = obterIdColunaTabela(coluna);

            if (id.isBlank()) {
                continue;
            }

            layout.add(new LayoutColunaTabela(
                    id,
                    normalizarIdPai(idPai),
                    i,
                    coluna.getWidth()
            ));

            if (!coluna.getColumns().isEmpty()) {
                capturarLayoutColunasTabela(
                        coluna.getColumns(),
                        id,
                        layout
                );
            }
        }
    }

    private String obterIdColunaTabela(TableColumn<ItemAnalise, ?> coluna) {
        if (coluna == null || coluna.getId() == null) {
            return "";
        }

        return coluna.getId().trim();
    }

    private String normalizarIdPai(String idPai) {
        if (idPai == null || idPai.isBlank()) {
            return null;
        }

        return idPai.trim();
    }
    
    @SafeVarargs
    private final void registrarOrdemPadraoColunasTabela(TableColumn<ItemAnalise, ?>... colunas) {
        ordemPadraoColunasTabela.clear();
        ordemPadraoColunasTabela.addAll(Arrays.asList(colunas));

        ordemPadraoSubcolunasTabela.clear();

        for (TableColumn<ItemAnalise, ?> coluna : colunas) {
            registrarOrdemPadraoSubcolunasTabela(coluna);
        }
    }

    private void registrarOrdemPadraoSubcolunasTabela(TableColumn<ItemAnalise, ?> coluna) {
        if (coluna == null || coluna.getColumns().isEmpty()) {
            return;
        }

        ordemPadraoSubcolunasTabela.put(
                coluna,
                new ArrayList<>(coluna.getColumns())
        );

        for (TableColumn<ItemAnalise, ?> subcoluna : coluna.getColumns()) {
            registrarOrdemPadraoSubcolunasTabela(subcoluna);
        }
    }

    private void reordenarColunasTabela() {
        if (tabela == null || ordemPadraoColunasTabela.isEmpty()) {
            return;
        }

        for (Map.Entry<TableColumn<ItemAnalise, ?>, List<TableColumn<ItemAnalise, ?>>> entrada
                : ordemPadraoSubcolunasTabela.entrySet()) {

            entrada.getKey().getColumns().setAll(entrada.getValue());
        }

        tabela.getColumns().setAll(ordemPadraoColunasTabela);
        posicionarColunaDepoisDe(tabela.getColumns(), "ok_encalhado", "valor_total");
        posicionarColunaDepoisDe(
                tabela.getColumns(),
                "custo_verdadeiro",
                "base_atual"
        );

        posicionarColunaDepoisDe(
                tabela.getColumns(),
                "custo_reposicao",
                "custo_verdadeiro"
        );

        tabela.refresh();
        tabela.refresh();

        solicitarSalvarLayoutTabela();
    }
    
    private void limparTabela() {
        Alert confirmacao =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacao.setTitle("Limpar tabela");
        confirmacao.setHeaderText(null);

        confirmacao.setContentText(
                "Deseja limpar todos os itens da tabela?"
        );

        if (tabela != null
                && tabela.getScene() != null) {

            confirmacao.initOwner(
                    tabela.getScene().getWindow()
            );
        }

        if (confirmacao
                .showAndWait()
                .orElse(ButtonType.CANCEL)
                != ButtonType.OK) {

            return;
        }

        limparTabelaSemConfirmacao();
    }
    
    private void limparTabelaSemConfirmacao() {
        if (tabela != null) {
            /*
             * Encerra qualquer edição e remove seleção/foco
             * antes de descartar os itens antigos.
             */
            tabela.edit(-1, null);

            tabela.getSelectionModel()
                    .clearSelection();

            if (tabela.getFocusModel() != null) {
                tabela.getFocusModel().focus(-1);
            }
        }

        indiceInicioSelecaoArrastada = -1;
        indiceLinhaFocoPendente = -1;
        colunaFocoPendente = null;

        /*
         * Descarta completamente as linhas antigas.
         *
         * Isso remove:
         * - condições de venda;
         * - cores especiais;
         * - confirmação de item obsoleto;
         * - códigos, quantidades, valores e custos.
         */
        itens.clear();

        for (int i = 0; i < 5; i++) {
            itens.add(new ItemAnalise());
        }

        linhaRodapeTabela.setCodigo(
                CODIGO_LINHA_RODAPE_TABELA
        );

        linhaRodapeTabela.setDescricao("TOTAL");

        itens.add(linhaRodapeTabela);

        recalcularResumo();
        atualizarVisibilidadeColunaOkEncalhado();

        /*
         * Sobrescreve o estado persistido com
         * a tabela novamente vazia.
         */
        salvarEstadoTabelaAtual();

        if (tabela != null) {
            tabela.requestLayout();
        }
    }
    
    private void restaurarPadraoCompletoUsuario() {
        /*
         * 1. Restaura e persiste todas as configurações.
         */
        ConfiguracaoUsuario configuracaoPadrao =
                new ConfiguracaoUsuario();

        configuracaoUsuarioService.salvar(
                configuracaoPadrao
        );

        configuracaoUsuario =
                configuracaoUsuarioService.carregar();
        
        atualizarCampoCotacaoDolar();
        atualizarCampoFatorImportacao();

        /*
         * 2. Limpa totalmente a tabela.
         *
         * Ao substituir os ItemAnalise, todas as condições,
         * cores e confirmações antigas também são eliminadas.
         */
        limparTabelaSemConfirmacao();

        /*
         * 3. Remove qualquer ordenação aplicada pelos
         * cabeçalhos das colunas.
         */
        if (tabela != null) {
            tabela.getSortOrder().clear();
        }

        /*
         * 4. Restaura e persiste a ordem padrão das colunas.
         */
        reordenarColunasTabela();

        /*
         * 5. Restaura a seleção padrão para SP.
         *
         * SP será 50,00%. SC continuará em 40,22%.
         */
        carregarEstadosConfigurados("SP");

        atualizarBaseEstado();

        /*
         * 6. Atualiza configurações visuais.
         *
         * Apenas a Flag de Itens Obsoletos ficará ativa.
         */
        atualizarVisibilidadeColunaCustoVerdadeiro();
        atualizarVisibilidadeColunaCustoReposicao();
        atualizarVisibilidadeColunaOkEncalhado();

        /*
         * 7. Aplica imediatamente o zoom padrão de 100%.
         */
        aplicarZoomInterface();

        recalcularTudoAposAlterarEstado();

        if (tabela != null) {
            tabela.refresh();
            tabela.requestLayout();
        }
    }
    
    private HBox criarRodape() {
        HBox rodape = new HBox(12);
        rodape.getStyleClass().add("rodape");
        rodape.setPadding(new Insets(8, 12, 8, 12));
        rodape.setAlignment(Pos.CENTER_LEFT);

        Label ajuda = new Label("Dica: é possível editar Código, Quantidade e Valor Unitário. "
        									+ "Ao confirmar o código, a descrição e custos são "
        								+ "buscados nas nossas bases internas! Qualquer dúvida, "
        													   + "entre em contato com o suporte!");
        ajuda.getStyleClass().add("texto-rodape");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button adicionarLinha = new Button("+ Adicionar linha");
        adicionarLinha.getStyleClass().add("botao-acao");
        adicionarLinha.setOnAction(event ->
        	executarComTratamento("Botão ADICIONAR LINHA", this::adicionarLinhaVazia)
        		);

        Button removerLinha = new Button("- Remover linha");
        removerLinha.getStyleClass().add("botao-acao");
        removerLinha.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> obterLinhasSelecionadasEditaveis().isEmpty(),
                        tabela.getSelectionModel().getSelectedItems()
                )
        );

        removerLinha.setOnAction(event ->
        executarComTratamento(
                "Botão REMOVER LINHA",
                this::removerLinhasSelecionadas
        		)
        );

        rodape.getChildren().addAll(ajuda, spacer, adicionarLinha, removerLinha);
        return rodape;
    }

    private void buscarDadosDoItem(ItemAnalise item) {
        if (item == null || isLinhaRodapeTabela(item)) {
            return;
        }

        String codigo = item.getCodigo();

        executarComTratamento("Busca por código do item: " + codigo, () -> {
            item.aplicarDadosItem(itemService.buscarPorCodigo(codigo).orElse(null));
            recalcularItem(item);
            atualizarVisibilidadeColunaOkEncalhado();
            recalcularResumo();
            tabela.refresh();
        });
    }
    
    private record AtualizacaoLinha(int indiceLinha, DadosItem dadosItem) {
    }
    
    private record ResultadoAtualizacaoDados(
            List<AtualizacaoLinha> atualizacoes,
            ResultadoSincronizacao sincronizacao
    ) {
    }
    
    private void abrirTelaConfiguracoes() {
        String siglaSelecionada =
                estadoCombo.getValue() == null
                        ? "SP"
                        : estadoCombo
                                .getValue()
                                .getSigla();

        TelaConfiguracoes telaConfiguracoes =
                new TelaConfiguracoes();

        boolean solicitouLimpeza =
                telaConfiguracoes.exibir(
                        getJanelaAtual()
                );

        /*
         * LIMPAR USER DATA possui um fluxo separado,
         * porque também afeta tabela e layout.
         */
        if (solicitouLimpeza) {
            restaurarPadraoCompletoUsuario();
            return;
        }

        /*
         * Fluxo normal do botão OK, Cancelar ou X.
         */
        configuracaoUsuario =
                configuracaoUsuarioService.carregar();

        atualizarCampoCotacaoDolar();
        
        atualizarCampoFatorImportacao();

        carregarEstadosConfigurados(
                siglaSelecionada
        );

        atualizarBaseEstado();
        recalcularTudoAposAlterarEstado();

        atualizarVisibilidadeColunaOkEncalhado();
        atualizarVisibilidadeColunaCustoVerdadeiro();
        atualizarVisibilidadeColunaCustoReposicao();

        aplicarZoomInterface();

        if (tabela != null) {
            tabela.refresh();
        }
    }
    
    private class OkItemEncalhadoCell extends TableCell<ItemAnalise, Void> {

        private final Button okButton = new Button("OK");

        public OkItemEncalhadoCell() {
            okButton.getStyleClass().add("botao-ok-encalhado");
            okButton.setFocusTraversable(false);
            okButton.setCursor(Cursor.HAND);
            okButton.setMinWidth(34);
            okButton.setPrefWidth(34);
            okButton.setMaxWidth(34);
            okButton.setMinHeight(22);
            okButton.setPrefHeight(22);

            okButton.setOnAction(event -> {
                ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();
                confirmarItemEncalhado(item);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();

            if (empty || !deveExibirBotaoOkEncalhado(itemLinha)) {
                setText(null);
                setGraphic(null);
                setStyle("");
                return;
            }

            setText(null);
            setGraphic(okButton);
            setAlignment(Pos.CENTER);

            aplicarEstiloCondicao(this, itemLinha);
        }
    }
    
    private boolean deveDestacarItemEncalhado(ItemAnalise item) {
        return configuracaoUsuario != null
                && configuracaoUsuario.isFlagItensEncalhados()
                && item != null
                && !isLinhaRodapeTabela(item)
                && item.isItemEncalhado()
                && !item.isItemEncalhadoConfirmado();
    }

    private void atualizarPseudoClassesLinha(
        TableRow<ItemAnalise> row
    		) {
    if (row == null) {
        return;
    }

    ItemAnalise item = row.getItem();

    boolean destacarComoObsoleto =
            !row.isEmpty()
                    && deveDestacarItemEncalhado(item);

    row.pseudoClassStateChanged(
            PSEUDO_ITEM_ENCALHADO,
            destacarComoObsoleto
    );

    atualizarEstiloCondicaoLinha(row);
}
    
    private void abrirTelaCondicao(ItemAnalise item) {
        executarComTratamento("Abrir tela de condição do item", () -> {
        	if (item == null
        	        || isLinhaRodapeTabela(item)
        	        || !temCodigoPreenchido(item)) {

        	    return;
        	}

            List<ItemAnalise> itensParaAplicar = obterLinhasSelecionadasEditaveis();

            if (!itensParaAplicar.contains(item)) {
                itensParaAplicar.clear();
                itensParaAplicar.add(item);
            }
            
            itensParaAplicar.removeIf(
                    itemSelecionado ->
                            !temCodigoPreenchido(itemSelecionado)
            );

            TelaCondicao telaCondicao = new TelaCondicao();
            boolean alterou = telaCondicao.exibir(tabela.getScene().getWindow(), item);

            if (!alterou) {
                return;
            }

            CondicaoVenda condicaoAplicada = item.getCondicaoVenda();
            String corEspecialFundo = item.getCorEspecialFundo();
            String corEspecialTexto = item.getCorEspecialTexto();

            for (ItemAnalise itemSelecionado : itensParaAplicar) {
                if (itemSelecionado == item) {
                    continue;
                }

                itemSelecionado.setCondicaoVenda(condicaoAplicada);
                itemSelecionado.setCorEspecialFundo(corEspecialFundo);
                itemSelecionado.setCorEspecialTexto(corEspecialTexto);
            }

            tabela.refresh();
        });
    }
    
    private void aplicarEstiloCondicao(
            TableCell<ItemAnalise, ?> cell,
            ItemAnalise item
    ) {
        if (cell == null) {
            return;
        }

        TableRow<ItemAnalise> row =
                cell.getTableRow();

        if (row == null
                || row.getItem() != item) {

            return;
        }

        /*
         * A cor agora pertence à linha, não à célula.
         */
        atualizarPseudoClassesLinha(row);
    }
    
    private String escurecerCorHex(String cor, double fator) {
        if (cor == null || !cor.matches("#[0-9a-fA-F]{6}")) {
            return cor;
        }

        int r = Integer.parseInt(cor.substring(1, 3), 16);
        int g = Integer.parseInt(cor.substring(3, 5), 16);
        int b = Integer.parseInt(cor.substring(5, 7), 16);

        r = limitarCanalCor((int) Math.round(r * fator));
        g = limitarCanalCor((int) Math.round(g * fator));
        b = limitarCanalCor((int) Math.round(b * fator));

        return String.format("#%02X%02X%02X", r, g, b);
    }

    private int limitarCanalCor(int valor) {
        return Math.max(0, Math.min(255, valor));
    }

    private String montarEstiloCampoEditavelPorCondicao(ItemAnalise item) {
    if (item == null || isLinhaRodapeTabela(item)) {
        return "";
    }
    
    if (deveDestacarItemEncalhado(item)) {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: #9C0006;" +
                "-fx-font-weight: bold;";
    }

    CondicaoVenda condicao = item.getCondicaoVenda();

    if (condicao == null || condicao == CondicaoVenda.NORMAL) {
        return "";
    }

    String corTexto;

    if (condicao == CondicaoVenda.ESPECIAL) {
        corTexto = item.getCorEspecialTexto();
    } else {
        corTexto = condicao.getCorTexto();
    }

    return "-fx-background-color: transparent;" +
            "-fx-text-fill: " + corTexto + ";" +
            "-fx-font-weight: bold;";
}
    
    private boolean temCodigoPreenchido(ItemAnalise item) {
        return item != null
                && item.getCodigo() != null
                && !item.getCodigo().trim().isEmpty();
    }

    private void atualizarBaseEstado() {
        EstadoInfo estado = estadoCombo.getValue();
        if (estado != null) {
            baseEstadoLabel.setText(formatarPercentual(estado.getBaseMargem()));
        }
    }

    private BigDecimal calcularValorTabelaLinha(ItemAnalise item) {
        if (item == null || isLinhaRodapeTabela(item)) {
            return BigDecimal.ZERO;
        }

        BigDecimal quantidade = BigDecimal.valueOf(item.getQuantidade());

        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal precoPadraoVenda = valorSeguro(item.getPrecoPadraoVenda());

        if (precoPadraoVenda.compareTo(BigDecimal.ZERO) > 0) {
            return precoPadraoVenda.multiply(quantidade);
        }

        return valorSeguro(item.getValorUnitario()).multiply(quantidade);
    }
    
    private void recalcularResumo() {
    BigDecimal totalProposta = BigDecimal.ZERO;
    BigDecimal totalIpiProposta = BigDecimal.ZERO;
    BigDecimal totalTabela = BigDecimal.ZERO;

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
        BigDecimal valorTabelaLinha = calcularValorTabelaLinha(item);

        BigDecimal custoAtualLinha = valorSeguro(item.getCustoAtual()).multiply(quantidade);
        BigDecimal custoAnteriorLinha = valorSeguro(item.getCustoAnterior()).multiply(quantidade);
        
        totalTabela = totalTabela.add(valorTabelaLinha);

        totalProposta = totalProposta.add(valorTotal);
        totalIpiProposta = totalIpiProposta.add(valorSeguro(item.getIpiProposta()));

        resultadoAtual = resultadoAtual.add(valorTotal.subtract(custoAtualLinha));
        totalIpiAtual = totalIpiAtual.add(valorSeguro(item.getIpiAtual()));

        resultadoAnterior = resultadoAnterior.add(valorTotal.subtract(custoAnteriorLinha));
        totalIpiAnterior = totalIpiAnterior.add(valorSeguro(item.getIpiAnterior()));
    }
    
    totalTabelaLabel.setText(formatarMoeda(totalTabela));

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
    
    private String criarMensagemConclusaoAtualizacao(
            ResultadoSincronizacao resultado
    ) {
        if (resultado == null
                || resultado.quantidadeFallbackLocal() == 0) {

            return "As bases foram sincronizadas e os itens "
                    + "da tabela foram atualizados.";
        }

        StringBuilder nomesBases =
                new StringBuilder();

        for (
                ResultadoBase resultadoBase
                : resultado.resultados()
        ) {
            if (!resultadoBase.utilizouFallbackLocal()) {
                continue;
            }

            if (!nomesBases.isEmpty()) {
                nomesBases.append(", ");
            }

            nomesBases.append(
                    resultadoBase
                            .base()
                            .getNomeArquivo()
            );
        }

        return "Os itens da tabela foram atualizados, mas "
                + "não foi possível acessar todas as bases "
                + "do servidor."
                + System.lineSeparator()
                + System.lineSeparator()
                + "Foram utilizadas as últimas cópias locais "
                + "válidas de: "
                + nomesBases
                + ".";
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
    
    private static String formatarMoedaUsd(
            BigDecimal valor
    ) {
        NumberFormat formato =
                NumberFormat.getNumberInstance(
                        new Locale(
                                "pt",
                                "BR"
                        )
                );

        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);

        return "US$ "
                + formato.format(
                        valor == null
                                ? BigDecimal.ZERO
                                : valor
                );
    }
    
    private void persistirCotacaoDolarSeValida(
            String texto
    ) {
        BigDecimal novaCotacao =
                converterTextoCotacaoDolar(
                        texto
                );

        if (novaCotacao == null) {
            return;
        }

        if (configuracaoUsuario == null) {
            configuracaoUsuario =
                    new ConfiguracaoUsuario();
        }

        BigDecimal cotacaoAtual =
                configuracaoUsuario
                        .getCotacaoDolar();

        /*
         * Evita gravações repetidas quando apenas
         * atualizamos o texto programaticamente.
         */
        if (cotacaoAtual != null
                && cotacaoAtual.compareTo(
                        novaCotacao
                ) == 0) {

            return;
        }

        configuracaoUsuario.setCotacaoDolar(
                novaCotacao
        );

        configuracaoUsuarioService.salvar(
                configuracaoUsuario
        );
    }

    private BigDecimal converterTextoCotacaoDolar(
            String texto
    ) {
        if (texto == null
                || texto.isBlank()) {

            return null;
        }

        try {
            String textoNormalizado =
                    texto.trim()
                            .replace(',', '.');

            /*
             * Permite que o usuário digite temporariamente
             * algo como "5," sem causar erro.
             */
            if (textoNormalizado.endsWith(".")) {
                textoNormalizado += "0";
            }

            BigDecimal valor =
                    new BigDecimal(
                            textoNormalizado
                    );

            if (valor.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

                return null;
            }

            return valor.setScale(
                    2,
                    RoundingMode.HALF_UP
            );

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void atualizarCampoCotacaoDolar() {
        if (campoCotacaoDolar == null) {
            return;
        }

        BigDecimal cotacao =
                configuracaoUsuario == null
                        ? new BigDecimal("5.30")
                        : configuracaoUsuario
                                .getCotacaoDolar();

        if (cotacao == null
                || cotacao.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            cotacao =
                    new BigDecimal("5.30");
        }

        String textoFormatado =
                cotacao.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                        .toPlainString()
                        .replace('.', ',');

        if (!textoFormatado.equals(
                campoCotacaoDolar.getText()
        )) {
            campoCotacaoDolar.setText(
                    textoFormatado
            );
        }
    }

    private static String formatarMoeda(BigDecimal valor) {
        @SuppressWarnings("deprecation")
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

            campo.focusedProperty().addListener((obs, estavaFocado, estaFocado) -> {
                if (!estaFocado) {
                    confirmarValor();
                }
            });
            
            campo.setOnAction(event -> confirmarValor());

            campo.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (isAtalhoColar(event)) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();

                    if (!clipboard.hasString()) {
                        return;
                    }

                    String textoColado = clipboard.getString();

                    colarQuantidadesEmLote(textoColado, getIndex());

                    event.consume();
                    return;
                }

                tratarNavegacaoCampoEditavel(
                        event,
                        getIndex(),
                        COLUNA_QUANTIDADE,
                        this::confirmarValor
                );
            });
        }

        @Override
        protected void updateItem(Integer valor, boolean empty) {
            super.updateItem(valor, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                setText("");
                setStyle("");
                campo.setStyle("");
                return;
            }

            ItemAnalise itemLinha = getTableRow().getItem();

            if (isLinhaRodapeTabela(itemLinha)) {
                setGraphic(null);
                setText(valor == null ? "" : valor.toString());
                setAlignment(Pos.CENTER);

                // IMPORTANTE: limpa qualquer cor herdada de célula reaproveitada
                setStyle("");
                campo.setStyle("");

                return;
            }

            campo.setText(valor == null || valor == 0 ? "" : valor.toString());
            setText(null);
            setGraphic(campo);
            setAlignment(Pos.CENTER);

            aplicarEstiloCondicao(this, itemLinha);
            campo.setStyle(montarEstiloCampoEditavelPorCondicao(itemLinha));
            focarCampoSePendente(COLUNA_QUANTIDADE, getIndex(), campo);
        }

        private void confirmarValor() {
            ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();
            
            if (colagemEmLoteEmAndamento) {
                return;
            }

            if (item == null) {
                return;
            }

            int novaQuantidade = converterQuantidade(campo.getText());

            if (novaQuantidade != item.getQuantidade()) {
                item.setQuantidade(novaQuantidade);
                recalcularItem(item);
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
            campo.setAlignment(Pos.CENTER);
            campo.setMaxWidth(Double.MAX_VALUE);

            campo.focusedProperty().addListener((obs, estavaFocado, estaFocado) -> {
                if (!estaFocado) {
                    confirmarValor();
                }
            });
            
            campo.setOnAction(event -> confirmarValor());

            campo.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (isAtalhoColar(event)) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();

                    if (!clipboard.hasString()) {
                        return;
                    }

                    String textoColado = clipboard.getString();

                    colarValoresUnitariosEmLote(textoColado, getIndex());

                    event.consume();
                    return;
                }

                tratarNavegacaoCampoEditavel(
                        event,
                        getIndex(),
                        COLUNA_VALOR_UNITARIO,
                        this::confirmarValor
                );
            });
        }

        @Override
        protected void updateItem(BigDecimal valor, boolean empty) {
        	if (empty) {
        	    setText("");
        	    setGraphic(null);
        	    setStyle("");
        	    campo.setStyle("");
        	    return;
        	}
            super.updateItem(valor, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            
            if (isLinhaRodapeTabela(getTableRow().getItem())) {
                setGraphic(null);
                setText("");
                setStyle("");
                campo.setStyle("");
                return;
            }

            campo.setText(formatarValorEditavel(valor));
            setText(null);
            setGraphic(campo);
            setAlignment(Pos.CENTER);
            
            ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();
            aplicarEstiloCondicao(this, itemLinha);
            campo.setStyle(montarEstiloCampoEditavelPorCondicao(itemLinha));
            focarCampoSePendente(COLUNA_VALOR_UNITARIO, getIndex(), campo);
        }

        private void confirmarValor() {
            ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();
            
            if (colagemEmLoteEmAndamento) {
                return;
            }

            if (item == null) {
                return;
            }

            BigDecimal novoValor = converterValor(campo.getText());

            if (novoValor.compareTo(item.getValorUnitario()) != 0) {
                item.setValorUnitario(novoValor);
                recalcularItem(item);
                recalcularResumo();
                tabela.refresh();
            }
        }

        private String formatarValorEditavel(BigDecimal valor) {
    if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0) {
        return "";
    }

    return formatarMoeda(valor);
}

        private BigDecimal converterValor(String texto) {
    if (texto == null || texto.trim().isEmpty()) {
        return BigDecimal.ZERO;
    }

    String normalizado = texto
            .replace("R$", "")
            .replace("\u00A0", " ")
            .trim();

    if (normalizado.contains(",")) {
        normalizado = normalizado
                .replace(".", "")
                .replace(",", ".");
    }

    normalizado = normalizado.trim();

    try {
        return new BigDecimal(normalizado);
    } catch (NumberFormatException e) {
        return BigDecimal.ZERO;
    }
}
    }
    
    private void colarQuantidadesEmLote(String textoColado, int indiceInicial) {
    List<String> valores = extrairPrimeiraColunaColada(textoColado);

    if (valores.isEmpty()) {
        return;
    }

    executarDuranteColagemEmLote(() -> {
        int indiceLinha = Math.max(indiceInicial, 0);

        for (String valorTexto : valores) {
            garantirLinhaExistente(indiceLinha);

            ItemAnalise item = itens.get(indiceLinha);
            item.setQuantidade(converterQuantidadeColada(valorTexto));

            recalcularItem(item);

            indiceLinha++;
        }

        recalcularResumo();
        tabela.refresh();
    });
}

    private boolean isAtalhoColar(KeyEvent event) {
        return event.getCode() == KeyCode.V
                && (event.isShortcutDown() || event.isControlDown());
    }
    
    private void tratarColagemGlobalTabela(KeyEvent event) {
        if (!isAtalhoColar(event)) {
            return;
        }

        Node alvo = obterNoEvento(event);

        if (!cliqueFoiDentroDoNo(alvo, tabela)) {
            return;
        }

        Clipboard clipboard = Clipboard.getSystemClipboard();

        if (!clipboard.hasString()) {
            return;
        }

        String textoColado = clipboard.getString();

        if (textoColado == null || textoColado.isBlank()) {
            return;
        }

        int indiceInicial = obterIndiceLinhaColagem(event);
        String nomeColuna = obterNomeColunaColagem(event);

        event.consume();

        if (COLUNA_QUANTIDADE.equals(nomeColuna)) {
            colarQuantidadesEmLote(textoColado, indiceInicial);
            return;
        }

        if (COLUNA_VALOR_UNITARIO.equals(nomeColuna)) {
            colarValoresUnitariosEmLote(textoColado, indiceInicial);
            return;
        }

        colarCodigosEmLote(textoColado, indiceInicial);
    }

    private Node obterNoEvento(KeyEvent event) {
        if (event.getTarget() instanceof Node node) {
            return node;
        }

        return null;
    }

    private TableCell<?, ?> obterCelulaDoEvento(KeyEvent event) {
        Node atual = obterNoEvento(event);

        while (atual != null) {
            if (atual instanceof TableCell<?, ?> cell && cell.getTableView() == tabela) {
                return cell;
            }

            atual = atual.getParent();
        }

        return null;
    }

    private int obterIndiceLinhaColagem(KeyEvent event) {
        TableCell<?, ?> cell = obterCelulaDoEvento(event);

        if (cell != null && cell.getIndex() >= 0) {
            return cell.getIndex();
        }

        int indiceInicial = tabela.getFocusModel() == null
                ? -1
                : tabela.getFocusModel().getFocusedIndex();

        if (indiceInicial < 0) {
            indiceInicial = tabela.getSelectionModel().getSelectedIndex();
        }

        if (indiceInicial < 0) {
            indiceInicial = 0;
        }

        return indiceInicial;
    }

    private String obterNomeColunaColagem(KeyEvent event) {
        TableCell<?, ?> cell = obterCelulaDoEvento(event);

        if (cell != null && cell.getTableColumn() != null) {
            return cell.getTableColumn().getText();
        }

        if (tabela.getFocusModel() != null && tabela.getFocusModel().getFocusedCell() != null) {
            @SuppressWarnings("unchecked")
			TableColumn<ItemAnalise, ?> coluna = tabela.getFocusModel().getFocusedCell().getTableColumn();

            if (coluna != null) {
                return coluna.getText();
            }
        }

        return COLUNA_CODIGO;
    }

    private void executarDuranteColagemEmLote(Runnable acao) {
        colagemEmLoteEmAndamento = true;

        try {
            acao.run();
        } finally {
            Platform.runLater(() -> colagemEmLoteEmAndamento = false);
        }
    }
    
    private void colarValoresUnitariosEmLote(String textoColado, int indiceInicial) {
        List<String> valores = extrairPrimeiraColunaColada(textoColado);

        if (valores.isEmpty()) {
            return;
        }

        executarDuranteColagemEmLote(() -> {
            int indiceLinha = Math.max(indiceInicial, 0);

            for (String valorTexto : valores) {
                garantirLinhaExistente(indiceLinha);

                ItemAnalise item = itens.get(indiceLinha);
                item.setValorUnitario(converterMoedaColada(valorTexto));

                recalcularItem(item);

                indiceLinha++;
            }

            recalcularResumo();
            tabela.refresh();
        });
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

        executarDuranteColagemEmLote(() -> {
            int indiceLinha = Math.max(indiceInicial, 0);

            for (String codigo : codigos) {
                garantirLinhaExistente(indiceLinha);

                ItemAnalise item = itens.get(indiceLinha);

                if (isLinhaRodapeTabela(item)) {
                    garantirLinhaExistente(indiceLinha);
                    item = itens.get(indiceLinha);
                }

                item.setCodigo(codigo);
                item.aplicarDadosItem(itemService.buscarPorCodigo(codigo).orElse(null));
                recalcularItem(item);
                atualizarVisibilidadeColunaOkEncalhado();

                indiceLinha++;
            }

            recalcularResumo();

            if (tabela != null) {
                tabela.getSelectionModel().clearSelection();

                if (tabela.getFocusModel() != null) {
                    tabela.getFocusModel().focus(-1);
                }

                tabela.refresh();
            }
        });
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
        overlayAtualizacao =
                new StackPane();

        overlayAtualizacao.setVisible(false);
        overlayAtualizacao.setMouseTransparent(true);

        overlayAtualizacao.setStyle(
                "-fx-background-color: "
                        + "rgba(0, 0, 0, 0.35);"
        );

        VBox caixa =
                new VBox(12);

        caixa.setAlignment(Pos.CENTER);
        caixa.setPadding(new Insets(22));
        caixa.setMaxWidth(390);
        caixa.setMaxHeight(175);

        caixa.setStyle(
                "-fx-background-color: #eeeeee;"
                        + "-fx-border-color: #555555;"
                        + "-fx-border-width: 2;"
                        + "-fx-background-radius: 6;"
                        + "-fx-border-radius: 6;"
        );

        progressoOverlayAtualizacao =
                new ProgressIndicator(0);

        progressoOverlayAtualizacao.setPrefSize(
                48,
                48
        );

        textoOverlayAtualizacao =
                new Label("Atualizando dados...");

        textoOverlayAtualizacao.setStyle(
                "-fx-font-size: 15px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #111111;"
        );

        subtextoOverlayAtualizacao =
                new Label(
                        "Preparando sincronização das bases."
                );

        subtextoOverlayAtualizacao.setStyle(
                "-fx-font-size: 12px;"
                        + "-fx-text-fill: #333333;"
        );

        subtextoOverlayAtualizacao.setWrapText(true);
        subtextoOverlayAtualizacao.setMaxWidth(340);
        subtextoOverlayAtualizacao.setAlignment(
                Pos.CENTER
        );

        caixa.getChildren().addAll(
                progressoOverlayAtualizacao,
                textoOverlayAtualizacao,
                subtextoOverlayAtualizacao
        );

        overlayAtualizacao
                .getChildren()
                .add(caixa);

        return overlayAtualizacao;
    }
    
    private void prepararOverlayAtualizacao(
            String titulo,
            String detalhe
    ) {
        if (textoOverlayAtualizacao != null) {
            textoOverlayAtualizacao.setText(titulo);
        }

        if (subtextoOverlayAtualizacao != null) {
            subtextoOverlayAtualizacao.setText(detalhe);
        }

        if (progressoOverlayAtualizacao != null) {
            progressoOverlayAtualizacao.setProgress(0);
        }

        mostrarOverlayAtualizacao(true);
    }

    private void atualizarOverlaySincronizacao(
            int atual,
            int total,
            String nomeBase,
            String etapa
    ) {
        boolean verificando =
                "Verificando base"
                        .equalsIgnoreCase(etapa);

        int quantidadeConcluida =
                verificando
                        ? atual - 1
                        : atual;

        double progresso =
                total <= 0
                        ? ProgressIndicator
                                .INDETERMINATE_PROGRESS
                        : quantidadeConcluida
                                / (double) total;

        atualizarOverlayAtualizacao(
                "Sincronizando bases locais...",
                "Base "
                        + atual
                        + " de "
                        + total
                        + ": "
                        + nomeBase
                        + System.lineSeparator()
                        + etapa,
                progresso
        );
    }

    private void atualizarOverlayAtualizacao(
            String titulo,
            String detalhe,
            double progresso
    ) {
        Runnable atualizacaoVisual = () -> {
            if (textoOverlayAtualizacao != null) {
                textoOverlayAtualizacao.setText(
                        titulo
                );
            }

            if (subtextoOverlayAtualizacao != null) {
                subtextoOverlayAtualizacao.setText(
                        detalhe
                );
            }

            if (progressoOverlayAtualizacao != null) {
                progressoOverlayAtualizacao.setProgress(
                        progresso
                );
            }
        };

        if (Platform.isFxApplicationThread()) {
            atualizacaoVisual.run();
        } else {
            Platform.runLater(
                    atualizacaoVisual
            );
        }
    }

    private void preCarregarBasesAoIniciar() {
    if (atualizacaoDadosEmAndamento) {
        return;
    }

    /*
     * Bloqueia o botão ATUALIZAR DADOS enquanto
     * a inicialização ainda está carregando as bases.
     */
    atualizacaoDadosEmAndamento = true;

    prepararOverlayAtualizacao(
            "Inicializando programa...",
            "Preparando sincronização das bases."
    );

    ultimaAtualizacaoLabel.setText(
            "Última atualização:\natualizando..."
    );

    /*
     * A lista da TableView pertence à thread do JavaFX.
     * Por isso, coletamos os códigos e índices antes
     * de iniciar a tarefa em segundo plano.
     */
    List<Integer> indices =
            new ArrayList<>();

    List<String> codigos =
            new ArrayList<>();

    for (int i = 0; i < itens.size(); i++) {
        ItemAnalise item =
                itens.get(i);

        if (isLinhaRodapeTabela(item)) {
            continue;
        }

        if (!temCodigoPreenchido(item)) {
            continue;
        }

        indices.add(i);
        codigos.add(item.getCodigo());
    }

    Task<ResultadoAtualizacaoDados> tarefa =
            new Task<>() {

        @Override
        protected ResultadoAtualizacaoDados call()
                throws Exception {

            /*
             * Primeiro sincroniza os arquivos e monta
             * novamente os caches em memória.
             */
            ResultadoSincronizacao resultadoSincronizacao =
                    itemService.preCarregarBases(
                            (
                                    atual,
                                    total,
                                    base,
                                    etapa
                            ) -> atualizarOverlaySincronizacao(
                                    atual,
                                    total,
                                    base.getNomeArquivo(),
                                    etapa
                            )
                    );

            atualizarOverlayAtualizacao(
                    "Inicializando programa...",
                    codigos.isEmpty()
                            ? "Finalizando inicialização."
                            : "Carregando os dados de "
                                    + codigos.size()
                                    + (
                                    codigos.size() == 1
                                            ? " item salvo."
                                            : " itens salvos."
                            ),
                    ProgressIndicator.INDETERMINATE_PROGRESS
            );

            /*
             * Depois que os caches estiverem prontos,
             * consulta novamente os itens restaurados.
             *
             * Isso carrega Custo Reposição, processo,
             * custos e demais informações dependentes
             * das bases.
             */
            List<AtualizacaoLinha> atualizacoes =
                    new ArrayList<>();

            for (int i = 0; i < codigos.size(); i++) {
                String codigo =
                        codigos.get(i);

                int indiceLinha =
                        indices.get(i);

                DadosItem dadosItem =
                        itemService
                                .buscarPorCodigo(codigo)
                                .orElse(null);

                atualizacoes.add(
                        new AtualizacaoLinha(
                                indiceLinha,
                                dadosItem
                        )
                );
            }

            return new ResultadoAtualizacaoDados(
                    atualizacoes,
                    resultadoSincronizacao
            );
        }
    };

    tarefa.setOnSucceeded(event -> {
        ResultadoAtualizacaoDados resultadoTarefa =
                tarefa.getValue();

        List<AtualizacaoLinha> atualizacoes =
                resultadoTarefa == null
                        ? List.of()
                        : resultadoTarefa.atualizacoes();

        ResultadoSincronizacao resultadoSincronizacao =
                resultadoTarefa == null
                        ? null
                        : resultadoTarefa.sincronizacao();

        /*
         * Aplica os dados somente depois de retornar
         * à thread visual do JavaFX.
         */
        for (
                AtualizacaoLinha atualizacao
                : atualizacoes
        ) {
            int indice =
                    atualizacao.indiceLinha();

            if (indice < 0
                    || indice >= itens.size()) {

                continue;
            }

            ItemAnalise item =
                    itens.get(indice);

            if (isLinhaRodapeTabela(item)) {
                continue;
            }

            item.aplicarDadosItem(
                    atualizacao.dadosItem()
            );

            recalcularItem(item);
        }

        recalcularResumo();
        atualizarVisibilidadeColunaOkEncalhado();

        if (tabela != null) {
            tabela.refresh();
        }

        atualizarTextoUltimaAtualizacao(
                resultadoSincronizacao
        );

        atualizacaoDadosEmAndamento = false;
        mostrarOverlayAtualizacao(false);
    });

    tarefa.setOnFailed(event -> {
        Throwable erro =
                tarefa.getException();

        ultimaAtualizacaoLabel.setText(
                "Última atualização:\nfalhou"
        );

        atualizacaoDadosEmAndamento = false;
        mostrarOverlayAtualizacao(false);

        mostrarErro(
                "Inicialização > sincronização, "
                        + "pré-carregamento das bases e "
                        + "consulta dos itens salvos",
                erro
        );
    });

    tarefa.setOnCancelled(event -> {
        atualizacaoDadosEmAndamento = false;
        mostrarOverlayAtualizacao(false);
    });

    Thread thread =
            new Thread(
                    tarefa,
                    "PreCarregamentoBasesThread"
            );

    thread.setDaemon(true);
    thread.start();
}

    private void mostrarOverlayAtualizacao(
            boolean mostrar
    ) {
        if (overlayAtualizacao == null) {
            return;
        }

        overlayAtualizacao.setVisible(
                mostrar
        );

        overlayAtualizacao.setMouseTransparent(
                !mostrar
        );
    }
    
    private class CodigoComBotaoCell extends TableCell<ItemAnalise, String> {
    	private final TextField campoCodigo = new TextField() {
    	    @Override
    	    public void paste() {
    	        colarCodigoPeloCampo();
    	    }
    	};
        private final Button botaoOpcoes = new Button();
        private final HBox container = new HBox(2);

        
        private void colarCodigoPeloCampo() {
            Clipboard clipboard = Clipboard.getSystemClipboard();

            if (!clipboard.hasString()) {
                return;
            }

            String textoColado = clipboard.getString();

            if (textoColado == null || textoColado.isBlank()) {
                return;
            }

            int indiceLinha = getTableRow() == null ? getIndex() : getTableRow().getIndex();

            if (indiceLinha < 0) {
                indiceLinha = getIndex();
            }

            colarCodigosEmLote(textoColado, indiceLinha);
        }
        private void atualizarIconeOpcoes(ItemAnalise itemLinha) {
            if (deveUsarIconeBranco(itemLinha)) {
                botaoOpcoes.setGraphic(carregarIcone("item-opcoes-branco.png", 10, 16));
            } else {
                botaoOpcoes.setGraphic(carregarIcone("item-opcoes.png", 10, 16));
            }
        }

        private boolean deveUsarIconeBranco(ItemAnalise itemLinha) {
            if (itemLinha == null || isLinhaRodapeTabela(itemLinha)) {
                return false;
            }

            CondicaoVenda condicao = itemLinha.getCondicaoVenda();

            if (condicao == null || condicao == CondicaoVenda.NORMAL) {
                return false;
            }

            String corTexto;

            if (condicao == CondicaoVenda.ESPECIAL) {
                corTexto = itemLinha.getCorEspecialTexto();
            } else {
                corTexto = condicao.getCorTexto();
            }

            return "#FFFFFF".equalsIgnoreCase(corTexto)
                    || "white".equalsIgnoreCase(corTexto);
        }
        
        private void abrirCondicaoDaLinhaAtual() {
            confirmarCodigo();

            ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();

            if (item == null || isLinhaRodapeTabela(item) || !temCodigoPreenchido(item)) {
                return;
            }

            abrirTelaCondicao(item);
        }
        
        public CodigoComBotaoCell() {
        	campoCodigo.getStyleClass().add("campo-codigo-tabela");
            campoCodigo.setMaxWidth(Double.MAX_VALUE);
            campoCodigo.setAlignment(Pos.CENTER);

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
                tratarNavegacaoCampoEditavel(
                        event,
                        getIndex(),
                        COLUNA_CODIGO,
                        this::confirmarCodigo
                );
            });

            campoCodigo.focusedProperty().addListener((obs, estavaFocado, estaFocado) -> {
                if (!estaFocado) {
                    confirmarCodigo();
                }
            });

            botaoOpcoes.setOnAction(event -> abrirCondicaoDaLinhaAtual());
            
            
            
        }

        @Override
        protected void updateItem(String codigo, boolean empty) {
            super.updateItem(codigo, empty);
            
            if (empty) {
                setText("");
                setGraphic(null);
                setStyle("");
                campoCodigo.setStyle("");
                botaoOpcoes.setGraphic(carregarIcone("item-opcoes.png", 10, 16));
                return;
            }

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
            ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();

            aplicarEstiloCondicao(this, itemLinha);
            campoCodigo.setStyle(montarEstiloCampoEditavelPorCondicao(itemLinha));
            atualizarIconeOpcoes(itemLinha);
            focarCampoSePendente(COLUNA_CODIGO, getIndex(), campoCodigo);
        }

        private void confirmarCodigo() {
            ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();
            
            if (colagemEmLoteEmAndamento) {
                return;
            }

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
        
    private void abrirTelaBuscaPreco() {
        executarComTratamento("Abrir tela BUSCAR VALOR PADRÃO", () -> {
            List<ItemAnalise> itensParaBuscarPreco = new ArrayList<>();

            for (ItemAnalise item : itens) {
                if (item == null || isLinhaRodapeTabela(item)) {
                    continue;
                }

                if (!temCodigoPreenchido(item)) {
                    continue;
                }

                itensParaBuscarPreco.add(item);
            }

            if (itensParaBuscarPreco.isEmpty()) {
                mostrarAviso(
                        "Buscar preço",
                        "Não há itens com código preenchido para buscar preço."
                );
                return;
            }

            TelaBuscaPreco telaBuscaPreco = new TelaBuscaPreco();
            boolean carregou = telaBuscaPreco.exibir(
                    tabela.getScene().getWindow(),
                    itensParaBuscarPreco,
                    itemService
            );

            if (!carregou) {
                return;
            }

            for (ItemAnalise item : itensParaBuscarPreco) {
                recalcularItem(item);
            }

            recalcularResumo();
            tabela.refresh();
        });
    }
    
    private double calcularAlturaTabela(BorderPane root, VBox topo, HBox rodape, HBox barraAcoesTabela) {
        int quantidadeLinhas = Math.max(itens.size(), 1);

        double alturaDesejada = ALTURA_CABECALHO_TABELA
                + quantidadeLinhas * ALTURA_LINHA_TABELA
                + ALTURA_BARRA_HORIZONTAL
                + FOLGA_INTERNA_TABELA;

        double alturaBarraAcoes = barraAcoesTabela == null ? 0 : barraAcoesTabela.getHeight();

        double alturaDisponivel = root.getHeight()
                - topo.getHeight()
                - rodape.getHeight()
                - alturaBarraAcoes
                - MARGEM_SEGURANCA_TABELA;

        if (alturaDisponivel <= 0) {
            return alturaDesejada;
        }

        return Math.min(alturaDesejada, alturaDisponivel);
    }
    
    private class BigDecimalTableCell extends TableCell<ItemAnalise, BigDecimal> {
        private final boolean moeda;
        private final boolean exibirNoRodape;
        private final boolean aplicarCondicaoVisual;

        public BigDecimalTableCell(boolean moeda) {
            this(moeda, false, false);
        }

        public BigDecimalTableCell(boolean moeda, boolean exibirNoRodape, boolean aplicarCondicaoVisual) {
            this.moeda = moeda;
            this.exibirNoRodape = exibirNoRodape;
            this.aplicarCondicaoVisual = aplicarCondicaoVisual;
            setAlignment(Pos.CENTER_RIGHT);
        }

        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText("");
                setStyle("");
                return;
            }

            ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();

            if (isLinhaRodapeTabela(itemLinha) && !exibirNoRodape) {
                setText("");
                setStyle("");
                return;
            }

            BigDecimal valor = item == null ? BigDecimal.ZERO : item;

            if (moeda) {
                setText(formatarMoeda(valor));
            } else {
                setText(formatarPercentual(valor));
            }

            if (aplicarCondicaoVisual) {
                aplicarEstiloCondicao(this, itemLinha);
            } else {
                setStyle("");
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
                BigDecimal valorRodape = valor == null ? BigDecimal.ZERO : valor;
                setText(formatarMoeda(valorRodape));
                setStyle("");
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
    
    private class CustoReposicaoTableCell
    extends TableCell<
            ItemAnalise,
            BigDecimal
    > {

public CustoReposicaoTableCell() {
    setAlignment(
            Pos.CENTER_RIGHT
    );
}

@Override
protected void updateItem(
        BigDecimal valor,
        boolean empty
) {
    super.updateItem(
            valor,
            empty
    );

    ItemAnalise itemLinha =
            getTableRow() == null
                    ? null
                    : getTableRow().getItem();

    /*
     * Nesta primeira etapa o custo
     * deve permanecer visualmente vazio.
     */
    if (empty
            || isLinhaRodapeTabela(
                    itemLinha
            )
            || !temCodigoPreenchido(
                    itemLinha
            )
            || valor == null
            || valor.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {

        setText("");
        setStyle("");
        return;
    }

    /*
     * Este trecho já está preparado
     * para a próxima etapa.
     */
    setText(
            formatarMoedaUsd(valor)
    );

    setStyle("");
}
}

    private class MargemTableCell extends TableCell<ItemAnalise, BigDecimal> {

        public MargemTableCell() {
            setAlignment(Pos.CENTER_RIGHT);
        }

        @Override
        protected void updateItem(BigDecimal margem, boolean empty) {
            super.updateItem(margem, empty);

            getStyleClass().removeAll("margem-baixa", "margem-media", "margem-boa");

            if (empty) {
                setText("");
                setStyle("");
                return;
            }

            ItemAnalise itemLinha = getTableRow() == null ? null : getTableRow().getItem();

            if (isLinhaRodapeTabela(itemLinha)) {
                setText(formatarMargemDecimal(margem));
                setStyle("");
                return;
            }

            if (!linhaTemCodigo(itemLinha)) {
                setText("");
                setStyle("");
                return;
            }

            BigDecimal margemSegura = margem == null ? BigDecimal.ZERO : margem;

            setText(formatarMargemDecimal(margemSegura));
            setStyle("");

            if (margemSegura.compareTo(new BigDecimal("0.20")) < 0) {
                getStyleClass().add("margem-baixa");
            } else if (margemSegura.compareTo(new BigDecimal("0.35")) < 0) {
                getStyleClass().add("margem-media");
            } else {
                getStyleClass().add("margem-boa");
            }
        }

        private boolean linhaTemCodigo(ItemAnalise item) {
            return item != null
                    && item.getCodigo() != null
                    && !item.getCodigo().trim().isEmpty();
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
    
    private void recalcularItem(ItemAnalise item) {
        analiseService.recalcular(item, getPercentualBaseEstadoSelecionado());
    }
    
    private void recalcularTudoAposAlterarEstado() {
        for (ItemAnalise item : itens) {
            if (item == null || isLinhaRodapeTabela(item)) {
                continue;
            }

            if (!temCodigoPreenchido(item)) {
                continue;
            }

            recalcularItem(item);
        }

        recalcularResumo();

        if (tabela != null) {
            tabela.refresh();
        }
    }
    
    private BigDecimal getPercentualBaseEstadoSelecionado() {
        EstadoInfo estadoSelecionado = estadoCombo.getSelectionModel().getSelectedItem();

        if (estadoSelecionado == null) {
            return BigDecimal.ZERO;
        }

        return estadoSelecionado.getBaseMargem();
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
            boolean colunaDataCustoAtual =
                    getTableColumn() != null
                            && "base_atual_data_custo".equals(
                                    getTableColumn().getId()
                            );

            boolean possuiCustoAtual =
                    itemLinha != null
                            && itemLinha.getCustoAtual() != null
                            && itemLinha.getCustoAtual()
                                    .compareTo(BigDecimal.ZERO) > 0;

            if (colunaDataCustoAtual && possuiCustoAtual) {
                setText("ESTIMADO");
            } else {
                setText("S./REG.");
            }

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