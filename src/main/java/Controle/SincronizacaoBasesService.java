package Controle;

import Configuracao.BaseSincronizavel;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SincronizacaoBasesService {

    private static final int MAXIMO_TENTATIVAS_COPIA = 3;

    private static final long INTERVALO_ENTRE_TENTATIVAS_MS =
            250L;

    /*
     * Executa a sincronização sem receber atualizações
     * de progresso.
     */
    public ResultadoSincronizacao sincronizarTodas() {
        return sincronizarTodas(null);
    }

    /*
     * Executa a sincronização das sete bases.
     *
     * O ouvinte será utilizado posteriormente pela interface
     * para mostrar o nome da base e o progresso da operação.
     */
    public ResultadoSincronizacao sincronizarTodas(
            OuvinteProgresso ouvinte
    ) {
        BaseSincronizavel[] bases =
                BaseSincronizavel.values();

        List<ResultadoBase> resultados =
                new ArrayList<>();

        int total = bases.length;

        for (int indice = 0; indice < total; indice++) {
            BaseSincronizavel base = bases[indice];

            notificarProgresso(
                    ouvinte,
                    indice + 1,
                    total,
                    base,
                    "Verificando base"
            );

            ResultadoBase resultado =
                    sincronizarBase(base);

            resultados.add(resultado);

            notificarProgresso(
                    ouvinte,
                    indice + 1,
                    total,
                    base,
                    resultado.status().getDescricao()
            );
        }

        return new ResultadoSincronizacao(resultados);
    }

    /*
     * Sincroniza uma única base.
     *
     * Este método também pode ser utilizado em testes
     * individuais ou em futuras atualizações seletivas.
     */
    public ResultadoBase sincronizarBase(
            BaseSincronizavel base
    ) {
        Objects.requireNonNull(
                base,
                "A base sincronizável não pode ser nula."
        );

        long inicio = System.currentTimeMillis();

        Path caminhoServidor =
                base.getCaminhoServidor();

        Path caminhoLocal =
                base.getCaminhoLocal();

        try {
            criarPastaLocal(caminhoLocal);

            /*
             * Se a base do servidor estiver indisponível,
             * tentamos continuar usando a cópia local.
             */
            if (!arquivoDisponivel(caminhoServidor)) {
                return tratarServidorIndisponivel(
                        base,
                        "O arquivo do servidor não está disponível.",
                        inicio
                );
            }

            /*
             * Quando tamanho e data são iguais, não copiamos
             * novamente o arquivo.
             */
            if (copiaLocalEstaAtualizada(
                    caminhoServidor,
                    caminhoLocal
            )) {
                return criarResultado(
                        base,
                        StatusSincronizacao.JA_ATUALIZADA,
                        "A cópia local já estava atualizada.",
                        caminhoLocal,
                        inicio,
                        null
                );
            }

            copiarBaseComSeguranca(
                    caminhoServidor,
                    caminhoLocal
            );

            return criarResultado(
                    base,
                    StatusSincronizacao.ATUALIZADA,
                    "A cópia local foi atualizada a partir do servidor.",
                    caminhoLocal,
                    inicio,
                    null
            );

        } catch (Exception erro) {
            return tratarFalhaSincronizacao(
                    base,
                    erro,
                    inicio
            );
        }
    }

    private void criarPastaLocal(
            Path caminhoLocal
    ) throws IOException {
        Path pasta = caminhoLocal.getParent();

        if (pasta != null) {
            Files.createDirectories(pasta);
        }
    }

    private boolean arquivoDisponivel(
            Path caminho
    ) {
        if (caminho == null) {
            return false;
        }

        try {
            return Files.isRegularFile(caminho)
                    && Files.isReadable(caminho)
                    && Files.size(caminho) > 0L;

        } catch (Exception erro) {
            return false;
        }
    }

    private boolean copiaLocalEstaAtualizada(
            Path caminhoServidor,
            Path caminhoLocal
    ) throws IOException {
        if (!arquivoDisponivel(caminhoLocal)) {
            return false;
        }

        long tamanhoServidor =
                Files.size(caminhoServidor);

        long tamanhoLocal =
                Files.size(caminhoLocal);

        if (tamanhoServidor != tamanhoLocal) {
            return false;
        }

        FileTime modificacaoServidor =
                Files.getLastModifiedTime(caminhoServidor);

        FileTime modificacaoLocal =
                Files.getLastModifiedTime(caminhoLocal);

        return modificacaoServidor.toMillis()
                == modificacaoLocal.toMillis();
    }

    /*
     * A cópia nunca substitui diretamente o banco local.
     *
     * Primeiro copiamos para um arquivo temporário,
     * validamos esse arquivo e só então substituímos
     * o banco local anterior.
     */
    private void copiarBaseComSeguranca(
            Path caminhoServidor,
            Path caminhoLocal
    ) throws IOException {
        Path caminhoTemporario =
                criarCaminhoTemporario(caminhoLocal);

        IOException ultimoErro = null;

        for (
                int tentativa = 1;
                tentativa <= MAXIMO_TENTATIVAS_COPIA;
                tentativa++
        ) {
            try {
                Files.deleteIfExists(caminhoTemporario);

                InformacaoArquivo antesDaCopia =
                        lerInformacaoArquivo(
                                caminhoServidor
                        );

                Files.copy(
                        caminhoServidor,
                        caminhoTemporario,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                );

                InformacaoArquivo depoisDaCopia =
                        lerInformacaoArquivo(
                                caminhoServidor
                        );

                /*
                 * Se tamanho ou data mudaram durante a cópia,
                 * o servidor estava atualizando a base.
                 *
                 * Nesse caso, descartamos o temporário
                 * e tentamos novamente.
                 */
                if (!antesDaCopia.equals(depoisDaCopia)) {
                    throw new IOException(
                            "A base do servidor foi alterada "
                                    + "durante a cópia."
                    );
                }

                long tamanhoTemporario =
                        Files.size(caminhoTemporario);

                if (tamanhoTemporario
                        != antesDaCopia.tamanhoBytes()) {

                    throw new IOException(
                            "O tamanho do arquivo copiado não "
                                    + "corresponde ao tamanho da origem."
                    );
                }

                validarBancoSqlite(caminhoTemporario);

                substituirArquivoLocal(
                        caminhoTemporario,
                        caminhoLocal
                );

                /*
                 * Mantemos a mesma data do servidor.
                 *
                 * Isso permite detectar rapidamente na próxima
                 * inicialização se a base mudou.
                 */
                Files.setLastModifiedTime(
                        caminhoLocal,
                        antesDaCopia.ultimaModificacao()
                );

                return;

            } catch (Exception erro) {
                Files.deleteIfExists(caminhoTemporario);

                ultimoErro = new IOException(
                        "Falha na tentativa "
                                + tentativa
                                + " de "
                                + MAXIMO_TENTATIVAS_COPIA
                                + " ao copiar "
                                + caminhoServidor.getFileName()
                                + ": "
                                + obterMensagemErro(erro),
                        erro
                );

                if (tentativa
                        < MAXIMO_TENTATIVAS_COPIA) {

                    aguardarNovaTentativa();
                }
            }
        }

        throw ultimoErro != null
                ? ultimoErro
                : new IOException(
                        "Não foi possível copiar a base."
                );
    }

    private Path criarCaminhoTemporario(
            Path caminhoLocal
    ) {
        String nomeTemporario =
                caminhoLocal.getFileName()
                        .toString()
                        + ".sincronizando.tmp";

        return caminhoLocal.resolveSibling(
                nomeTemporario
        );
    }

    private InformacaoArquivo lerInformacaoArquivo(
            Path caminho
    ) throws IOException {
        if (!Files.isRegularFile(caminho)) {
            throw new IOException(
                    "Arquivo não encontrado: " + caminho
            );
        }

        return new InformacaoArquivo(
                Files.size(caminho),
                Files.getLastModifiedTime(caminho)
        );
    }

    /*
     * PRAGMA quick_check verifica se o arquivo copiado
     * continua sendo um banco SQLite íntegro.
     */
    private void validarBancoSqlite(
            Path caminhoBanco
    ) throws IOException {
        if (!Files.isRegularFile(caminhoBanco)) {
            throw new IOException(
                    "Banco temporário não encontrado: "
                            + caminhoBanco
            );
        }

        if (Files.size(caminhoBanco) <= 0L) {
            throw new IOException(
                    "O banco temporário está vazio: "
                            + caminhoBanco
            );
        }

        String caminhoFormatado =
                caminhoBanco.toAbsolutePath()
                        .toString()
                        .replace("\\", "/");

        String url =
                "jdbc:sqlite:" + caminhoFormatado;

        try (
                Connection conexao =
                        DriverManager.getConnection(url);

                Statement statement =
                        conexao.createStatement()
        ) {
            statement.execute(
                    "PRAGMA query_only = ON"
            );

            statement.execute(
                    "PRAGMA busy_timeout = 5000"
            );

            try (
                    ResultSet resultado =
                            statement.executeQuery(
                                    "PRAGMA quick_check"
                            )
            ) {
                boolean encontrouResultado = false;

                while (resultado.next()) {
                    encontrouResultado = true;

                    String resposta =
                            resultado.getString(1);

                    if (resposta == null
                            || !"ok".equalsIgnoreCase(
                                    resposta.trim()
                            )) {

                        throw new IOException(
                                "Falha na validação SQLite: "
                                        + resposta
                        );
                    }
                }

                if (!encontrouResultado) {
                    throw new IOException(
                            "O SQLite não retornou resultado "
                                    + "para PRAGMA quick_check."
                    );
                }
            }

        } catch (SQLException erro) {
            throw new IOException(
                    "Não foi possível validar o banco SQLite "
                            + caminhoBanco.getFileName()
                            + ": "
                            + obterMensagemErro(erro),
                    erro
            );
        }
    }

    private void substituirArquivoLocal(
            Path caminhoTemporario,
            Path caminhoLocal
    ) throws IOException {
        try {
            Files.move(
                    caminhoTemporario,
                    caminhoLocal,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (
                AtomicMoveNotSupportedException erro
        ) {
            /*
             * Alguns sistemas de arquivos não suportam
             * substituição atômica.
             *
             * Nesse caso usamos a substituição convencional.
             */
            Files.move(
                    caminhoTemporario,
                    caminhoLocal,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private void aguardarNovaTentativa()
            throws IOException {
        try {
            Thread.sleep(
                    INTERVALO_ENTRE_TENTATIVAS_MS
            );

        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();

            throw new IOException(
                    "A sincronização foi interrompida.",
                    erro
            );
        }
    }

    private ResultadoBase tratarServidorIndisponivel(
            BaseSincronizavel base,
            String motivo,
            long inicio
    ) {
        Path caminhoLocal =
                base.getCaminhoLocal();

        if (copiaLocalValida(caminhoLocal)) {
            return criarResultado(
                    base,
                    StatusSincronizacao.COPIA_LOCAL_UTILIZADA,
                    motivo
                            + " O programa utilizará a última "
                            + "cópia local válida.",
                    caminhoLocal,
                    inicio,
                    null
            );
        }

        if (!base.isObrigatoria()) {
            return criarResultado(
                    base,
                    StatusSincronizacao.OPCIONAL_INDISPONIVEL,
                    motivo
                            + " A base é opcional e ainda não "
                            + "possui cópia local válida.",
                    caminhoLocal,
                    inicio,
                    null
            );
        }

        return criarResultado(
                base,
                StatusSincronizacao.FALHA,
                motivo
                        + " Também não existe uma cópia local "
                        + "válida para esta base obrigatória.",
                caminhoLocal,
                inicio,
                null
        );
    }

    private ResultadoBase tratarFalhaSincronizacao(
            BaseSincronizavel base,
            Exception erro,
            long inicio
    ) {
        Path caminhoLocal =
                base.getCaminhoLocal();

        String detalhe =
                obterMensagemErro(erro);

        /*
         * A cópia anterior nunca é apagada antes de a nova
         * ter sido validada.
         *
         * Portanto, mesmo que a atualização falhe, ainda
         * podemos continuar com a versão local anterior.
         */
        if (copiaLocalValida(caminhoLocal)) {
            return criarResultado(
                    base,
                    StatusSincronizacao.COPIA_LOCAL_UTILIZADA,
                    "Não foi possível atualizar a base. "
                            + "A última cópia local válida será "
                            + "utilizada. Detalhe: "
                            + detalhe,
                    caminhoLocal,
                    inicio,
                    erro
            );
        }

        if (!base.isObrigatoria()) {
            return criarResultado(
                    base,
                    StatusSincronizacao.OPCIONAL_INDISPONIVEL,
                    "Não foi possível sincronizar a base "
                            + "opcional e não há cópia local "
                            + "válida. Detalhe: "
                            + detalhe,
                    caminhoLocal,
                    inicio,
                    erro
            );
        }

        return criarResultado(
                base,
                StatusSincronizacao.FALHA,
                "Não foi possível sincronizar a base "
                        + "obrigatória e não há cópia local "
                        + "válida. Detalhe: "
                        + detalhe,
                caminhoLocal,
                inicio,
                erro
        );
    }

    private boolean copiaLocalValida(
            Path caminhoLocal
    ) {
        if (!arquivoDisponivel(caminhoLocal)) {
            return false;
        }

        try {
            validarBancoSqlite(caminhoLocal);
            return true;

        } catch (Exception erro) {
            return false;
        }
    }

    private ResultadoBase criarResultado(
            BaseSincronizavel base,
            StatusSincronizacao status,
            String mensagem,
            Path caminhoLocal,
            long inicio,
            Throwable erro
    ) {
        return new ResultadoBase(
                base,
                status,
                mensagem,
                obterTamanhoSeguro(caminhoLocal),
                System.currentTimeMillis() - inicio,
                erro
        );
    }

    private long obterTamanhoSeguro(
            Path caminho
    ) {
        try {
            if (Files.isRegularFile(caminho)) {
                return Files.size(caminho);
            }

        } catch (Exception ignorado) {
            /*
             * O resultado utilizará zero quando não for
             * possível obter o tamanho do arquivo.
             */
        }

        return 0L;
    }

    private String obterMensagemErro(
            Throwable erro
    ) {
        if (erro == null) {
            return "Erro desconhecido.";
        }

        String mensagem = erro.getMessage();

        if (mensagem == null
                || mensagem.isBlank()) {

            return erro.getClass().getSimpleName();
        }

        return mensagem;
    }

    private void notificarProgresso(
            OuvinteProgresso ouvinte,
            int atual,
            int total,
            BaseSincronizavel base,
            String etapa
    ) {
        if (ouvinte == null) {
            return;
        }

        try {
            ouvinte.atualizar(
                    atual,
                    total,
                    base,
                    etapa
            );

        } catch (RuntimeException ignorado) {
            /*
             * Uma falha visual no ouvinte de progresso
             * não deve cancelar a sincronização dos bancos.
             */
        }
    }

    @FunctionalInterface
    public interface OuvinteProgresso {

        void atualizar(
                int atual,
                int total,
                BaseSincronizavel base,
                String etapa
        );
    }

    public enum StatusSincronizacao {

        ATUALIZADA(
                "Atualizada"
        ),

        JA_ATUALIZADA(
                "Já atualizada"
        ),

        COPIA_LOCAL_UTILIZADA(
                "Usando cópia local"
        ),

        OPCIONAL_INDISPONIVEL(
                "Base opcional indisponível"
        ),

        FALHA(
                "Falha"
        );

        private final String descricao;

        StatusSincronizacao(
                String descricao
        ) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    public record ResultadoBase(
            BaseSincronizavel base,
            StatusSincronizacao status,
            String mensagem,
            long tamanhoBytes,
            long duracaoMillis,
            Throwable erro
    ) {

        public boolean falhouObrigatoria() {
            return base.isObrigatoria()
                    && status == StatusSincronizacao.FALHA;
        }

        public boolean atualizouArquivo() {
            return status
                    == StatusSincronizacao.ATUALIZADA;
        }

        public boolean utilizouFallbackLocal() {
            return status
                    == StatusSincronizacao.COPIA_LOCAL_UTILIZADA;
        }
    }

    public record ResultadoSincronizacao(
            List<ResultadoBase> resultados
    ) {

        public ResultadoSincronizacao {
            resultados = resultados == null
                    ? List.of()
                    : List.copyOf(resultados);
        }

        public boolean possuiFalhaObrigatoria() {
            return resultados.stream()
                    .anyMatch(
                            ResultadoBase::falhouObrigatoria
                    );
        }

        public boolean podeIniciarPrograma() {
            return !possuiFalhaObrigatoria();
        }

        public long quantidadeAtualizada() {
            return resultados.stream()
                    .filter(
                            ResultadoBase::atualizouArquivo
                    )
                    .count();
        }

        public long quantidadeFallbackLocal() {
            return resultados.stream()
                    .filter(
                            ResultadoBase::utilizouFallbackLocal
                    )
                    .count();
        }
    }

    private record InformacaoArquivo(
            long tamanhoBytes,
            FileTime ultimaModificacao
    ) {
    }
}