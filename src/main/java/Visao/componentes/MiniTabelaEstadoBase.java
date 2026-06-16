package Visao.componentes;

import Modelo.EstadoInfo;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MiniTabelaEstadoBase extends VBox {

    public MiniTabelaEstadoBase(ComboBox<EstadoInfo> estadoCombo, Label baseEstadoLabel) {
        final double larguraEstado = 82;
        final double larguraBase = 230;
        final double alturaCabecalho = 24;
        final double alturaValor = 25;

        Label tituloEstado = criarCabecalho("ESTADO", larguraEstado, alturaCabecalho, "3 3 3 3");
        Label tituloBase = criarCabecalho("BASE PARA O ESTADO", larguraBase, alturaCabecalho, "3 3 3 0");

        configurarComboEstado(estadoCombo);

        baseEstadoLabel.setAlignment(Pos.CENTER);
        baseEstadoLabel.setMinSize(larguraBase, alturaValor);
        baseEstadoLabel.setPrefSize(larguraBase, alturaValor);
        baseEstadoLabel.setMaxSize(larguraBase, alturaValor);
        baseEstadoLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #555555;" +
                "-fx-padding: 0;"
        );

        StackPane celulaEstado = new StackPane(estadoCombo);
        celulaEstado.setAlignment(Pos.CENTER);
        celulaEstado.setMinSize(larguraEstado, alturaValor);
        celulaEstado.setPrefSize(larguraEstado, alturaValor);
        celulaEstado.setMaxSize(larguraEstado, alturaValor);
        celulaEstado.setStyle(
                "-fx-background-color: #eeeeee;" +
                "-fx-border-color: #555555;" +
                "-fx-border-width: 0 3 3 3;" +
                "-fx-padding: 0;"
        );

        StackPane celulaBase = new StackPane(baseEstadoLabel);
        celulaBase.setAlignment(Pos.CENTER);
        celulaBase.setMinSize(larguraBase, alturaValor);
        celulaBase.setPrefSize(larguraBase, alturaValor);
        celulaBase.setMaxSize(larguraBase, alturaValor);
        celulaBase.setStyle(
                "-fx-background-color: #eeeeee;" +
                "-fx-border-color: #555555;" +
                "-fx-border-width: 0 3 3 0;" +
                "-fx-padding: 0;"
        );

        HBox linhaCabecalho = new HBox(tituloEstado, tituloBase);
        linhaCabecalho.setSpacing(0);
        linhaCabecalho.setMinHeight(alturaCabecalho);
        linhaCabecalho.setPrefHeight(alturaCabecalho);
        linhaCabecalho.setMaxHeight(alturaCabecalho);

        HBox linhaValores = new HBox(celulaEstado, celulaBase);
        linhaValores.setSpacing(0);
        linhaValores.setMinHeight(alturaValor);
        linhaValores.setPrefHeight(alturaValor);
        linhaValores.setMaxHeight(alturaValor);

        getChildren().addAll(linhaCabecalho, linhaValores);
        setSpacing(0);
        setMinSize(larguraEstado + larguraBase, alturaCabecalho + alturaValor);
        setPrefSize(larguraEstado + larguraBase, alturaCabecalho + alturaValor);
        setMaxSize(larguraEstado + larguraBase, alturaCabecalho + alturaValor);
        setStyle(
        		"-fx-background-color: #E9E9E9;" +
        		"-fx-border-color: #505050;" +
                "-fx-padding: 0;"
        );
    }

    private Label criarCabecalho(String texto, double largura, double altura, String borda) {
        Label label = new Label(texto);
        label.setAlignment(Pos.CENTER);
        label.setMinSize(largura, altura);
        label.setPrefSize(largura, altura);
        label.setMaxSize(largura, altura);
        label.setStyle(
        		"-fx-background-color: #D9D9D9;" +
        		"-fx-border-color: #505050;" +
                "-fx-border-width: " + borda + ";" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #555555;" +
                "-fx-padding: 0;"
        );
        return label;
    }

    private void configurarComboEstado(ComboBox<EstadoInfo> estadoCombo) {
        estadoCombo.setMinSize(68, 23);
        estadoCombo.setPrefSize(68, 23);
        estadoCombo.setMaxSize(68, 23);
        estadoCombo.getStyleClass().add("combo-estado-mini");

        estadoCombo.setButtonCell(new ListCell<EstadoInfo>() {
            @Override
            protected void updateItem(EstadoInfo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
                setAlignment(Pos.CENTER);
                setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-text-fill: #555555;" +
                        "-fx-padding: 0;"
                );
            }
        });

        estadoCombo.setCellFactory(lista -> new ListCell<EstadoInfo>() {
            @Override
            protected void updateItem(EstadoInfo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
                setAlignment(Pos.CENTER);
            }
        });
    }
}