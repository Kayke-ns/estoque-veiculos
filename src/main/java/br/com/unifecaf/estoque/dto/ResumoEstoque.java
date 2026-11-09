package br.com.unifecaf.estoque.dto;

import java.math.BigDecimal;

/**
 * Indicadores rapidos do estoque, usados no painel do gerente.
 */
public record ResumoEstoque(
        long total,
        long disponiveis,
        long reservados,
        long vendidos,
        long descontinuados,
        BigDecimal valorEmEstoque) {
}
