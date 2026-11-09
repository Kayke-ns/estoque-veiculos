package br.com.unifecaf.estoque.dto;

import br.com.unifecaf.estoque.model.Modelo;
import br.com.unifecaf.estoque.model.TipoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ModeloDTO {
    private ModeloDTO() {
    }

    public record Request(
            @NotBlank(message = "nome e obrigatorio") @Size(max = 80) String nome,
            @NotNull(message = "tipo e obrigatorio") TipoVeiculo tipo,
            @NotNull(message = "marcaId e obrigatorio") Long marcaId) {
    }

    public record Response(Long id, String nome, TipoVeiculo tipo, Long marcaId, String marcaNome) {
        public static Response de(Modelo m) {
            return new Response(m.getId(), m.getNome(), m.getTipo(), m.getMarca().getId(), m.getMarca().getNome());
        }
    }
}
