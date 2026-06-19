package Visao.componentes;

import javafx.scene.Cursor;
import javafx.scene.control.Button;

public final class BotaoPadrao {

    private static final String COR_NORMAL = "#F2F2F2";
    private static final String COR_HOVER = "#D8D8D8";
    private static final String COR_PRESSIONADO = "#C8C8C8";
    private static final String COR_DESABILITADO = "#E6E6E6";
    private static final String COR_BORDA = "#333333";

    private BotaoPadrao() {
    }

    public static Button criar(String texto, double largura) {
        Button botao = new Button(texto);
        aplicar(botao, largura);
        return botao;
    }

    public static void aplicar(Button botao, double largura) {
        if (botao == null) {
            return;
        }

        botao.setPrefWidth(largura);
        botao.setMinHeight(28);
        botao.setCursor(Cursor.HAND);
        botao.setFocusTraversable(false);

        aplicarEstilo(botao, COR_NORMAL);

        botao.setOnMouseEntered(event -> {
            if (!botao.isDisabled()) {
                aplicarEstilo(botao, COR_HOVER);
            }
        });

        botao.setOnMouseExited(event -> {
            if (!botao.isDisabled()) {
                aplicarEstilo(botao, COR_NORMAL);
            }
        });

        botao.setOnMousePressed(event -> {
            if (!botao.isDisabled()) {
                aplicarEstilo(botao, COR_PRESSIONADO);
            }
        });

        botao.setOnMouseReleased(event -> {
            if (!botao.isDisabled()) {
                if (botao.isHover()) {
                    aplicarEstilo(botao, COR_HOVER);
                } else {
                    aplicarEstilo(botao, COR_NORMAL);
                }
            }
        });

        botao.disabledProperty().addListener((obs, estavaDesabilitado, estaDesabilitado) -> {
            if (estaDesabilitado) {
                botao.setCursor(Cursor.DEFAULT);
                aplicarEstiloDesabilitado(botao);
            } else {
                botao.setCursor(Cursor.HAND);
                aplicarEstilo(botao, COR_NORMAL);
            }
        });
    }

    private static void aplicarEstilo(Button botao, String corFundo) {
        botao.setStyle(
                "-fx-background-color: " + corFundo + ";" +
                "-fx-border-color: " + COR_BORDA + ";" +
                "-fx-border-width: 1.5;" +
                "-fx-background-radius: 3;" +
                "-fx-border-radius: 3;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #000000;" +
                "-fx-padding: 4 12 4 12;"
        );
    }

    private static void aplicarEstiloDesabilitado(Button botao) {
        botao.setStyle(
                "-fx-background-color: " + COR_DESABILITADO + ";" +
                "-fx-border-color: #999999;" +
                "-fx-border-width: 1.5;" +
                "-fx-background-radius: 3;" +
                "-fx-border-radius: 3;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #888888;" +
                "-fx-padding: 4 12 4 12;"
        );
    }
}