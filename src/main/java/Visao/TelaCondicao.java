package Visao;

import Modelo.CondicaoVenda;
import Modelo.ItemAnalise;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class TelaCondicao {

    public boolean exibir(Window janelaDona, ItemAnalise item) {
        if (item == null) {
            return false;
        }

        final boolean[] confirmou = {false};

        Stage stage = new Stage();
        stage.setTitle("Condição do Item");
        stage.initModality(Modality.WINDOW_MODAL);

        if (janelaDona != null) {
            stage.initOwner(janelaDona);
        }

        VBox root = new VBox(18);
        root.setPadding(new Insets(26, 28, 18, 28));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #C7C4BA;");

        ToggleGroup grupo = new ToggleGroup();

        RadioButton normalRadio = criarRadio("NORMAL", grupo, CondicaoVenda.NORMAL);
        RadioButton aumentarPrecoRadio = criarRadio("AUMENTAR PREÇO", grupo, CondicaoVenda.AUMENTAR_PRECO);
        RadioButton somentePacoteRadio = criarRadio("SOMENTE NO PACOTE", grupo, CondicaoVenda.SOMENTE_NO_PACOTE);
        RadioButton especialRadio = criarRadio("ESPECIAL", grupo, CondicaoVenda.ESPECIAL);

        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(carregarCorEspecial(item));
        colorPicker.setPrefWidth(70);
        colorPicker.setCursor(Cursor.HAND);

        HBox linhaNormal = criarLinhaOpcao(normalRadio);
        HBox linhaAumentarPreco = criarLinhaOpcao(aumentarPrecoRadio);
        HBox linhaSomentePacote = criarLinhaOpcao(somentePacoteRadio);
        HBox linhaEspecial = criarLinhaOpcao(especialRadio, colorPicker);

        selecionarCondicaoAtual(
                item,
                normalRadio,
                aumentarPrecoRadio,
                somentePacoteRadio,
                especialRadio
        );

        atualizarEstadoVisualDasOpcoes(
                grupo,
                colorPicker,
                linhaNormal,
                linhaAumentarPreco,
                linhaSomentePacote,
                linhaEspecial
        );

        grupo.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            atualizarEstadoVisualDasOpcoes(
                    grupo,
                    colorPicker,
                    linhaNormal,
                    linhaAumentarPreco,
                    linhaSomentePacote,
                    linhaEspecial
            );
        });

        HBox botoes = new HBox(78);
        botoes.setAlignment(Pos.CENTER);
        botoes.setPadding(new Insets(18, 0, 0, 0));

        Button cancelarButton = new Button("CANCELAR");
        cancelarButton.setPrefWidth(120);
        cancelarButton.setOnAction(event -> stage.close());

        Button okButton = new Button("OK");
        okButton.setPrefWidth(70);
        
        cancelarButton.setCursor(Cursor.HAND);
        okButton.setCursor(Cursor.HAND);
        okButton.setOnAction(event -> {
            if (grupo.getSelectedToggle() == null) {
                return;
            }

            CondicaoVenda condicaoSelecionada =
                    (CondicaoVenda) grupo.getSelectedToggle().getUserData();

            item.setCondicaoVenda(condicaoSelecionada);

            if (condicaoSelecionada == CondicaoVenda.ESPECIAL) {
                Color corSelecionada = colorPicker.getValue();

                item.setCorEspecialFundo(converterCorParaCss(corSelecionada));
                item.setCorEspecialTexto(calcularCorTexto(corSelecionada));
            }

            confirmou[0] = true;
            stage.close();
        });

        botoes.getChildren().addAll(cancelarButton, okButton);

        root.getChildren().addAll(
                linhaNormal,
                linhaAumentarPreco,
                linhaSomentePacote,
                linhaEspecial,
                botoes
        );

        Scene scene = new Scene(root, 340, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        return confirmou[0];
    }

    private RadioButton criarRadio(String texto, ToggleGroup grupo, CondicaoVenda condicaoVenda) {
        RadioButton radio = new RadioButton(texto);
        radio.setToggleGroup(grupo);
        radio.setUserData(condicaoVenda);

        radio.setStyle(
                "-fx-font-size: 18px;" +
                "-fx-text-fill: #555555;"
        );

        return radio;
    }

    private HBox criarLinhaOpcao(RadioButton radioButton) {
        HBox linha = new HBox(radioButton);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setMinHeight(34);
        return linha;
    }

    private HBox criarLinhaOpcao(RadioButton radioButton, ColorPicker colorPicker) {
        HBox linha = new HBox(12, radioButton, colorPicker);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setMinHeight(34);
        return linha;
    }

    private void selecionarCondicaoAtual(
            ItemAnalise item,
            RadioButton normalRadio,
            RadioButton aumentarPrecoRadio,
            RadioButton somentePacoteRadio,
            RadioButton especialRadio
    ) {
        CondicaoVenda condicaoAtual = item.getCondicaoVenda();

        if (condicaoAtual == CondicaoVenda.AUMENTAR_PRECO) {
            aumentarPrecoRadio.setSelected(true);
            return;
        }

        if (condicaoAtual == CondicaoVenda.SOMENTE_NO_PACOTE) {
            somentePacoteRadio.setSelected(true);
            return;
        }

        if (condicaoAtual == CondicaoVenda.ESPECIAL) {
            especialRadio.setSelected(true);
            return;
        }

        normalRadio.setSelected(true);
    }

    private void atualizarEstadoVisualDasOpcoes(
            ToggleGroup grupo,
            ColorPicker colorPicker,
            HBox linhaNormal,
            HBox linhaAumentarPreco,
            HBox linhaSomentePacote,
            HBox linhaEspecial
    ) {
        aplicarOpacidadeLinha(linhaNormal);
        aplicarOpacidadeLinha(linhaAumentarPreco);
        aplicarOpacidadeLinha(linhaSomentePacote);
        aplicarOpacidadeLinha(linhaEspecial);

        CondicaoVenda condicaoSelecionada = null;

        if (grupo.getSelectedToggle() != null) {
            condicaoSelecionada = (CondicaoVenda) grupo.getSelectedToggle().getUserData();
        }

        colorPicker.setDisable(condicaoSelecionada != CondicaoVenda.ESPECIAL);
        colorPicker.setOpacity(condicaoSelecionada == CondicaoVenda.ESPECIAL ? 1.0 : 0.45);
    }

    private void aplicarOpacidadeLinha(HBox linha) {
        if (linha.getChildren().isEmpty()) {
            linha.setOpacity(0.45);
            return;
        }

        if (linha.getChildren().get(0) instanceof RadioButton radioButton && radioButton.isSelected()) {
            linha.setOpacity(1.0);
        } else {
            linha.setOpacity(0.45);
        }
    }

    private Color carregarCorEspecial(ItemAnalise item) {
        try {
            return Color.web(item.getCorEspecialFundo());
        } catch (Exception e) {
            return Color.web("#92D050");
        }
    }

    private String converterCorParaCss(Color cor) {
        int vermelho = (int) Math.round(cor.getRed() * 255);
        int verde = (int) Math.round(cor.getGreen() * 255);
        int azul = (int) Math.round(cor.getBlue() * 255);

        return String.format("#%02X%02X%02X", vermelho, verde, azul);
    }

    private String calcularCorTexto(Color cor) {
        double luminancia =
                0.299 * cor.getRed() +
                0.587 * cor.getGreen() +
                0.114 * cor.getBlue();

        return luminancia < 0.55 ? "#FFFFFF" : "#000000";
    }
}