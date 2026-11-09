package br.com.unifecaf.estoque.dto;

import br.com.unifecaf.estoque.model.StatusVeiculo;
import br.com.unifecaf.estoque.model.TipoVeiculo;

import java.math.BigDecimal;

public record FiltroVeiculo(
        Long marcaId,
        Long modeloId,
        TipoVeiculo tipo,
        StatusVeiculo status,
        BigDecimal precoMin,
        BigDecimal precoMax,
        Integer anoMin,
        Integer anoMax,
        String cor,
        String busca
) {
}
