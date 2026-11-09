package br.com.unifecaf.estoque.dto;

import br.com.unifecaf.estoque.model.StatusVeiculo;
import br.com.unifecaf.estoque.model.TipoVeiculo;
import br.com.unifecaf.estoque.model.Veiculo;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class VeiculoDTO {

    private VeiculoDTO() {
    }

    /** Cadastro completo de um veiculo. */
    public record Request(
            @NotNull(message = "modeloId e obrigatorio") Long modeloId,
            @NotNull(message = "anoFabricacao e obrigatorio") @Min(1950) Integer anoFabricacao,
            @NotBlank(message = "cor e obrigatoria") @Size(max = 30) String cor,
            @NotNull(message = "preco e obrigatorio") @Positive(message = "preco deve ser positivo") BigDecimal preco,
            @NotNull(message = "quilometragem e obrigatoria") @PositiveOrZero Integer quilometragem,
            @Pattern(regexp = "^$|^[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}$", message = "placa invalida (ex.: ABC1D23)")
            String placa,
            StatusVeiculo status) {
    }

    /** Atualizacao parcial (PATCH): so preco, quilometragem e status. */
    public record Atualizacao(
            @Positive(message = "preco deve ser positivo") BigDecimal preco,
            @PositiveOrZero Integer quilometragem,
            StatusVeiculo status) {
    }

    public record Response(
            Long id,
            Long marcaId,
            String marca,
            Long modeloId,
            String modelo,
            TipoVeiculo tipo,
            Integer anoFabricacao,
            String cor,
            BigDecimal preco,
            Integer quilometragem,
            StatusVeiculo status,
            String placa,
            String descricao,
            LocalDateTime atualizadoEm) {

        public static Response de(Veiculo v) {
            return new Response(
                    v.getId(),
                    v.getMarca().getId(),
                    v.getMarca().getNome(),
                    v.getModelo().getId(),
                    v.getModelo().getNome(),
                    v.getModelo().getTipo(),
                    v.getAnoFabricacao(),
                    v.getCor(),
                    v.getPreco(),
                    v.getQuilometragem(),
                    v.getStatus(),
                    v.getPlaca(),
                    v.getDescricao(),
                    v.getAtualizadoEm());
        }
    }
}
