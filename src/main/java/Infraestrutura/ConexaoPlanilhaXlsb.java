package Infraestrutura;

import Configuracao.CaminhosBase;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.binary.XSSFBCommentsTable;
import org.apache.poi.xssf.binary.XSSFBSharedStringsTable;
import org.apache.poi.xssf.binary.XSSFBSheetHandler;
import org.apache.poi.xssf.binary.XSSFBStylesTable;
import org.apache.poi.xssf.eventusermodel.XSSFBReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import java.util.function.Consumer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConexaoPlanilhaXlsb {

    public List<String> listarAbas() throws Exception {
        Path caminhoPlanilha = CaminhosBase.CAMINHO_BD_METAL_ITEM;

        if (!Files.isRegularFile(caminhoPlanilha)) {
            throw new FileNotFoundException("Planilha de itens não encontrada em: " + caminhoPlanilha);
        }

        List<String> abas = new ArrayList<>();

        try (OPCPackage pacote = OPCPackage.open(caminhoPlanilha.toFile(), PackageAccess.READ)) {
            XSSFBReader leitor = new XSSFBReader(pacote);

            XSSFBReader.SheetIterator iterator =
                    (XSSFBReader.SheetIterator) leitor.getSheetsData();

            while (iterator.hasNext()) {
                try (InputStream ignored = iterator.next()) {
                    abas.add(iterator.getSheetName());
                }
            }
        }

        return abas;
    }

    public List<List<String>> listarPrimeirasLinhas(String nomeAba, int limiteLinhas) throws Exception {
        Path caminhoPlanilha = CaminhosBase.CAMINHO_BD_METAL_ITEM;

        if (!Files.isRegularFile(caminhoPlanilha)) {
            throw new FileNotFoundException("Planilha de itens não encontrada em: " + caminhoPlanilha);
        }

        List<List<String>> linhas = new ArrayList<>();

        try (OPCPackage pacote = OPCPackage.open(caminhoPlanilha.toFile(), PackageAccess.READ)) {
            XSSFBReader leitor = new XSSFBReader(pacote);

            XSSFBSharedStringsTable sharedStrings = new XSSFBSharedStringsTable(pacote);
            XSSFBStylesTable styles = leitor.getXSSFBStylesTable();
            DataFormatter dataFormatter = new DataFormatter();

            XSSFBReader.SheetIterator iterator =
                    (XSSFBReader.SheetIterator) leitor.getSheetsData();

            while (iterator.hasNext()) {
                try (InputStream inputStream = iterator.next()) {
                    String nomeAbaAtual = iterator.getSheetName();

                    if (!nomeAbaAtual.equalsIgnoreCase(nomeAba)) {
                        continue;
                    }

                    XSSFBCommentsTable comments = iterator.getXSSFBSheetComments();

                    XSSFSheetXMLHandler.SheetContentsHandler handler =
                            new XSSFSheetXMLHandler.SheetContentsHandler() {

                                private List<String> linhaAtual;
                                private int ultimaColuna;

                                @Override
                                public void startRow(int rowNum) {
                                    linhaAtual = new ArrayList<>();
                                    ultimaColuna = -1;
                                }

                                @Override
                                public void endRow(int rowNum) {
                                    linhas.add(new ArrayList<>(linhaAtual));

                                    if (linhas.size() >= limiteLinhas) {
                                        throw new LeituraConcluidaException();
                                    }
                                }

                                @Override
                                public void cell(String cellReference, String formattedValue, XSSFComment comment) {
                                    int indiceColuna;

                                    if (cellReference == null) {
                                        indiceColuna = ultimaColuna + 1;
                                    } else {
                                        indiceColuna = new CellReference(cellReference).getCol();
                                    }

                                    while (linhaAtual.size() <= indiceColuna) {
                                        linhaAtual.add("");
                                    }

                                    linhaAtual.set(indiceColuna, formattedValue == null ? "" : formattedValue.trim());
                                    ultimaColuna = indiceColuna;
                                }

                                @Override
                                public void headerFooter(String text, boolean isHeader, String tagName) {
                                    // Não precisamos tratar cabeçalho/rodapé da planilha.
                                }
                            };

                    XSSFBSheetHandler sheetHandler = new XSSFBSheetHandler(
                            inputStream,
                            styles,
                            comments,
                            sharedStrings,
                            handler,
                            dataFormatter,
                            false
                    );

                    try {
                        sheetHandler.parse();
                    } catch (LeituraConcluidaException ignored) {
                        // Paramos a leitura de propósito após atingir o limite de linhas.
                    }

                    return linhas;
                }
            }
        }

        throw new IllegalArgumentException("Aba não encontrada na planilha: " + nomeAba);
    }

    public void percorrerLinhas(String nomeAba, Consumer<List<String>> consumidorLinha) throws Exception {
        Path caminhoPlanilha = CaminhosBase.CAMINHO_BD_METAL_ITEM;

        if (!Files.isRegularFile(caminhoPlanilha)) {
            throw new FileNotFoundException("Planilha de itens não encontrada em: " + caminhoPlanilha);
        }

        try (OPCPackage pacote = OPCPackage.open(caminhoPlanilha.toFile(), PackageAccess.READ)) {
            XSSFBReader leitor = new XSSFBReader(pacote);

            XSSFBSharedStringsTable sharedStrings = new XSSFBSharedStringsTable(pacote);
            XSSFBStylesTable styles = leitor.getXSSFBStylesTable();
            DataFormatter dataFormatter = new DataFormatter();

            XSSFBReader.SheetIterator iterator =
                    (XSSFBReader.SheetIterator) leitor.getSheetsData();

            while (iterator.hasNext()) {
                try (InputStream inputStream = iterator.next()) {
                    String nomeAbaAtual = iterator.getSheetName();

                    if (!nomeAbaAtual.equalsIgnoreCase(nomeAba)) {
                        continue;
                    }

                    XSSFBCommentsTable comments = iterator.getXSSFBSheetComments();

                    XSSFSheetXMLHandler.SheetContentsHandler handler =
                            new XSSFSheetXMLHandler.SheetContentsHandler() {

                                private List<String> linhaAtual;
                                private int ultimaColuna;

                                @Override
                                public void startRow(int rowNum) {
                                    linhaAtual = new ArrayList<>();
                                    ultimaColuna = -1;
                                }

                                @Override
                                public void endRow(int rowNum) {
                                    consumidorLinha.accept(new ArrayList<>(linhaAtual));
                                }

                                @Override
                                public void cell(String cellReference, String formattedValue, XSSFComment comment) {
                                    int indiceColuna;

                                    if (cellReference == null) {
                                        indiceColuna = ultimaColuna + 1;
                                    } else {
                                        indiceColuna = new CellReference(cellReference).getCol();
                                    }

                                    while (linhaAtual.size() <= indiceColuna) {
                                        linhaAtual.add("");
                                    }

                                    linhaAtual.set(indiceColuna, formattedValue == null ? "" : formattedValue.trim());
                                    ultimaColuna = indiceColuna;
                                }

                                @Override
                                public void headerFooter(String text, boolean isHeader, String tagName) {
                                    // Não precisamos tratar cabeçalho/rodapé.
                                }
                            };

                    XSSFBSheetHandler sheetHandler = new XSSFBSheetHandler(
                            inputStream,
                            styles,
                            comments,
                            sharedStrings,
                            handler,
                            dataFormatter,
                            false
                    );

                    sheetHandler.parse();
                    return;
                }
            }
        }

        throw new IllegalArgumentException("Aba não encontrada na planilha: " + nomeAba);
    }
    
    @SuppressWarnings("serial")
	private static class LeituraConcluidaException extends RuntimeException {
    }
}