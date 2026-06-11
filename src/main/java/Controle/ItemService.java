package Controle;

import Modelo.DadosItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemService {
    private final Map<String, DadosItem> baseSimulada = new HashMap<>();

    public ItemService() {
        carregarBaseSimulada();
    }

    private void carregarBaseSimulada() {
        adicionar(new DadosItem(
                "MSC2404.1207",
                "TUBO QUADRADO A554 PE 304 040X040X1,5MM",
                new BigDecimal("36.50"),
                new BigDecimal("35.80"),
                new BigDecimal("37.20"),
                "CCA - 150526",
                "PROMOB - 150526",
                "ANT - 031224",
                LocalDate.of(2026, 5, 15)
        ));

        adicionar(new DadosItem(
                "MRT2401.1206",
                "TUBO REDONDO A270 C/C PIPE 304 OD025,40 (1P) X 1,5MM",
                new BigDecimal("28.70"),
                new BigDecimal("29.10"),
                new BigDecimal("30.00"),
                "CCA - 120526",
                "PROMOB - 120526",
                "ANT - 280425",
                LocalDate.of(2026, 5, 12)
        ));

        adicionar(new DadosItem(
                "MMR0000.0000",
                "ITEM EXEMPLO PARA TESTE DA TELA",
                new BigDecimal("10.00"),
                new BigDecimal("10.50"),
                new BigDecimal("11.00"),
                "CCA - TESTE",
                "PROMOB - TESTE",
                "ANT - TESTE",
                LocalDate.now()
        ));
    }

    private void adicionar(DadosItem dadosItem) {
        baseSimulada.put(dadosItem.getCodigo().toUpperCase(), dadosItem);
    }

    public Optional<DadosItem> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(baseSimulada.get(codigo.trim().toUpperCase()));
    }
}
