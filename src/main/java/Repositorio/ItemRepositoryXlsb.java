package Repositorio;

import Infraestrutura.ConexaoPlanilhaXlsb;
import Modelo.ItemCadastro;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ItemRepositoryXlsb implements ItemRepository {

    private static final String NOME_ABA = "Banco_Itens";

    private final ConexaoPlanilhaXlsb conexaoPlanilha;

    private Map<String, ItemCadastro> cacheItens;

    public ItemRepositoryXlsb() {
        this.conexaoPlanilha = new ConexaoPlanilhaXlsb();
    }

    @Override
    public Optional<ItemCadastro> buscarPorCodigo(String codigoItem) {
        if (codigoItem == null || codigoItem.isBlank()) {
            return Optional.empty();
        }

        carregarCacheSeNecessario();

        String chave = normalizarCodigo(codigoItem);
        return Optional.ofNullable(cacheItens.get(chave));
    }

    private void carregarCacheSeNecessario() {
        if (cacheItens != null) {
            return;
        }

        cacheItens = new HashMap<>();

        try {
            final Map<String, Integer>[] indicesColunas = new Map[]{null};
            final int[] numeroLinha = {0};

            conexaoPlanilha.percorrerLinhas(NOME_ABA, linha -> {
                numeroLinha[0]++;

                if (numeroLinha[0] == 1) {
                    indicesColunas[0] = mapearCabecalho(linha);
                    return;
                }

                if (linha == null || linha.isEmpty()) {
                    return;
                }

                ItemCadastro item = converterLinhaParaItem(linha, indicesColunas[0]);

                if (item == null || item.getCodigoItem() == null || item.getCodigoItem().isBlank()) {
                    return;
                }

                cacheItens.put(normalizarCodigo(item.getCodigoItem()), item);
            });

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar itens da planilha " + NOME_ABA, e);
        }
    }

    private Map<String, Integer> mapearCabecalho(List<String> cabecalho) {
        Map<String, Integer> mapa = new HashMap<>();

        for (int i = 0; i < cabecalho.size(); i++) {
            String nomeNormalizado = normalizarTexto(cabecalho.get(i));
            mapa.put(nomeNormalizado, i);
        }

        validarColunaObrigatoria(mapa, "ID_ITEM");
        validarColunaObrigatoria(mapa, "DESCRICAO");
        validarColunaObrigatoria(mapa, "ID_ITEM");
        validarColunaObrigatoria(mapa, "DESCRICAO");
        validarColunaObrigatoria(mapa, "IPI");
        validarColunaObrigatoria(mapa, "NCM");
        validarColunaObrigatoria(mapa, "CUSTO UNIT. ATUAL (BRL)");
        validarColunaObrigatoria(mapa, "REG. CUSTO ATUAL");
        validarColunaObrigatoria(mapa, "PRECO UNIT. LIQUIDO ATUAL (BRL)");

        return mapa;
    }

    private void validarColunaObrigatoria(Map<String, Integer> mapa, String nomeColunaNormalizado) {
        if (!mapa.containsKey(nomeColunaNormalizado)) {
            throw new IllegalStateException("Coluna obrigatória não encontrada na planilha: " + nomeColunaNormalizado);
        }
    }

    private ItemCadastro converterLinhaParaItem(List<String> linha, Map<String, Integer> colunas) {
        String codigoItem = valor(linha, colunas, "ID_ITEM");

        if (codigoItem == null || codigoItem.isBlank()) {
            return null;
        }

        return new ItemCadastro(
                codigoItem,
                valor(linha, colunas, "DESCRICAO"),
                converterBigDecimal(valor(linha, colunas, "IPI")),
                valor(linha, colunas, "NCM"),
                valor(linha, colunas, "UN.V"),
                converterBigDecimal(valor(linha, colunas, "PESO BRUTO (KG)")),
                converterBigDecimal(valor(linha, colunas, "PESO LIQUIDO (KG)")),
                converterBigDecimal(valor(linha, colunas, "CUSTO UNIT. ATUAL (BRL)")),
                valor(linha, colunas, "REG. CUSTO ATUAL"),
                converterBigDecimal(valor(linha, colunas, "PRECO UNIT. LIQUIDO ATUAL (BRL)"))
        );
    }

    private String valor(List<String> linha, Map<String, Integer> colunas, String nomeColuna) {
        String nomeNormalizado = normalizarTexto(nomeColuna);
        Integer indice = colunas.get(nomeNormalizado);

        if (indice == null || indice < 0 || indice >= linha.size()) {
            return "";
        }

        String valor = linha.get(indice);
        return valor == null ? "" : valor.trim();
    }

    private BigDecimal converterBigDecimal(String texto) {
        if (texto == null || texto.isBlank() || texto.equals("-")) {
            return BigDecimal.ZERO;
        }

        String normalizado = texto.trim();

        if (normalizado.contains(",")) {
            normalizado = normalizado.replace(".", "");
            normalizado = normalizado.replace(",", ".");
        }

        normalizado = normalizado.replace("R$", "").trim();

        try {
            return new BigDecimal(normalizado);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDateTime converterDataHora(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formatos = List.of(
                DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );

        for (DateTimeFormatter formato : formatos) {
            try {
                if (formato.toString().contains("HourOfDay")) {
                    return LocalDateTime.parse(texto, formato);
                }

                return LocalDate.parse(texto, formato).atStartOfDay();

            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }

        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return semAcento
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", " ");
    }
}