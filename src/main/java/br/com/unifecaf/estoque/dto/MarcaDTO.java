package br.com.unifecaf.estoque.dto;

import br.com.unifecaf.estoque.model.Marca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class MarcaDTO {

    private MarcaDTO() {
    }

    /** Dados recebidos para criar/editar uma marca. */
    public record Request(
            @NotBlank(message = "nome e obrigatorio") @Size(max = 60) String nome,
            @Size(max = 60) String paisOrigem) {
    }

    /** Dados devolvidos pela API. */
    public record Response(Long id, String nome, String paisOrigem) {
        public static Response de(Marca m) {
            return new Response(m.getId(), m.getNome(), m.getPaisOrigem());
        }
    }
}
