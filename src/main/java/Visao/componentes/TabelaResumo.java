package Visao.componentes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class TabelaResumo extends GridPane {

    private static final double ALTURA_LINHA = 20;
    private static final double LARGURA_COLUNA_TITULO = 155;
    private static final double LARGURA_COLUNA_VALOR = 120;
    private static final double LARGURA_COLUNA_IPI = 150;

    public TabelaResumo(
            Label totalPropostaLabel,
            Label totalComIpiLabel,
            Label resultadoAtualLabel,
            Label resultadoAtualComIpiLabel,
            Label resultadoAnteriorLabel,
            Label resultadoAnteriorComIpiLabel
    ) {
        setHgap(0);
        setVgap(0);
        setStyle("-fx-padding: 0;");

        add(criarCelulaTexto("RESULTADOS", 0, 0, true, Pos.CENTER), 0, 0);
        add(criarCelulaTexto("VALOR TOTAL", 1, 0, true, Pos.CENTER), 1, 0);
        add(criarCelulaTexto("VALOR TOTAL + IPI", 2, 0, true, Pos.CENTER), 2, 0);

        add(criarCelulaTexto("RESULTADO PROPOSTA", 0, 1, false, Pos.CENTER), 0, 1);
        add(criarCelulaValor(totalPropostaLabel, 1, 1), 1, 1);
        add(criarCelulaValor(totalComIpiLabel, 2, 1), 2, 1);

        add(criarCelulaTexto("RESULTADO ATUAL", 0, 2, false, Pos.CENTER), 0, 2);
        add(criarCelulaValor(resultadoAtualLabel, 1, 2), 1, 2);
        add(criarCelulaValor(resultadoAtualComIpiLabel, 2, 2), 2, 2);

        add(criarCelulaTexto("RESULTADO ANTERIOR", 0, 3, false, Pos.CENTER), 0, 3);
        add(criarCelulaValor(resultadoAnteriorLabel, 1, 3), 1, 3);
        add(criarCelulaValor(resultadoAnteriorComIpiLabel, 2, 3), 2, 3);
    }

    private StackPane criarCelulaTexto(String texto, int col, int row, boolean cabecalho, Pos alinhamento) {
        Label label = new Label(texto);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setAlignment(alinhamento);
        label.setPadding(Insets.EMPTY);

        label.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #333333;"
        );

        return criarCelulaBase(label, col, row, cabecalho);
    }

    private StackPane criarCelulaValor(Label label, int col, int row) {
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_RIGHT);
        label.setPadding(new Insets(0, 6, 0, 0));

        label.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #000000;"
        );

        return criarCelulaBase(label, col, row, false);
    }

    private StackPane criarCelulaBase(Label label, int col, int row, boolean cabecalho) {
        StackPane celula = new StackPane(label);
        celula.setAlignment(Pos.CENTER);

        double largura = switch (col) {
            case 0 -> LARGURA_COLUNA_TITULO;
            case 1 -> LARGURA_COLUNA_VALOR;
            default -> LARGURA_COLUNA_IPI;
        };

        celula.setMinSize(largura, ALTURA_LINHA);
        celula.setPrefSize(largura, ALTURA_LINHA);
        celula.setMaxSize(largura, ALTURA_LINHA);

        String borda = montarBorda(col, row);

        celula.setStyle(
                "-fx-background-color: " + (cabecalho ? "#D9D9D9" : "#E9E9E9") + ";" +
                "-fx-border-color: #505050;" +
                "-fx-border-width: " + borda + ";" +
                "-fx-padding: 0;"
        );

        return celula;
    }

    private String montarBorda(int col, int row) {
        int topo = 2;
        int direita = col == 2 ? 2 : 0;
        int baixo = row == 3 ? 2 : 0;
        int esquerda = 2;

        return topo + " " + direita + " " + baixo + " " + esquerda;
    }
}