package br.com.unifecaf.estoque.dto;

import java.math.BigDecimal;

public record ResumoEstoque(
        long total,
        long disponiveis,
        long reservados,
        long vendidos,
        long descontinuados,
        BigDecimal valorEmEstoque) {
}
