package Infraestrutura.diagnostico;

public record DiagnosticoErro(
        String contexto,
        String origemProvavel,
        String mensagemUsuario,
        String orientacao,
        String localTecnico
) {
}