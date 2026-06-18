package Visao.componentes;

import Modelo.ItemAnalise;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

public class CodigoComBotaoCell extends TableCell<ItemAnalise, String> {

    private final TextField campoCodigo = new TextField();
    private final Button botaoOpcoes = new Button();
    private final HBox container = new HBox(4);

    private final Consumer<ItemAnalise> aoConfirmarCodigo;
    public CodigoComBotaoCell(
            Consumer<ItemAnalise> aoConfirmarCodigo,
            Consumer<ItemAnalise> aoClicarBotao
    ) {
        this.aoConfirmarCodigo = aoConfirmarCodigo;
        campoCodigo.getStyleClass().add("campo-codigo-tabela");
        campoCodigo.setMaxWidth(Double.MAX_VALUE);
        campoCodigo.setAlignment(Pos.CENTER_LEFT);

        botaoOpcoes.getStyleClass().add("botao-opcoes-item");
        botaoOpcoes.setGraphic(carregarIcone("item-opcoes.png", 18, 22));
        botaoOpcoes.setFocusTraversable(false);

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

            if (item != null && aoClicarBotao != null) {
                aoClicarBotao.accept(item);
            }
        });

        setAlignment(Pos.CENTER);
    }

    @Override
    protected void updateItem(String codigo, boolean empty) {
        super.updateItem(codigo, empty);

        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        campoCodigo.setText(codigo == null ? "" : codigo);

        setText(null);
        setGraphic(container);
        setAlignment(Pos.CENTER);
    }

    private void confirmarCodigo() {
        ItemAnalise item = getTableRow() == null ? null : getTableRow().getItem();

        if (item == null) {
            return;
        }

        String novoCodigo = campoCodigo.getText() == null ? "" : campoCodigo.getText().trim();

        if (!novoCodigo.equals(item.getCodigo())) {
            item.setCodigo(novoCodigo);

            if (aoConfirmarCodigo != null) {
                aoConfirmarCodigo.accept(item);
            }
        }
    }

    private ImageView carregarIcone(String nomeArquivo, double largura, double altura) {
        ImageView icone = new ImageView(
                new Image(getClass().getResourceAsStream("/icons/" + nomeArquivo))
        );

        icone.setFitWidth(largura);
        icone.setFitHeight(altura);
        icone.setPreserveRatio(true);
        icone.setSmooth(true);

        return icone;
    }
}