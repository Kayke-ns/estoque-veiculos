package br.com.unifecaf.estoque.repository;

import br.com.unifecaf.estoque.dto.FiltroVeiculo;
import br.com.unifecaf.estoque.model.Veiculo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class VeiculoSpecifications {
    private VeiculoSpecifications() {
    }

    public static Specification<Veiculo> comFiltro(FiltroVeiculo f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            var modelo = root.join("modelo");
            var marca = modelo.join("marca");

            if (f.marcaId() != null) p.add(cb.equal(marca.get("id"), f.marcaId()));
            if (f.modeloId() != null) p.add(cb.equal(modelo.get("id"), f.modeloId()));
            if (f.tipo() != null) p.add(cb.equal(modelo.get("tipo"), f.tipo()));
            if (f.status() != null) p.add(cb.equal(root.get("status"), f.status()));
            if (f.precoMin() != null) p.add(cb.greaterThanOrEqualTo(root.get("preco"), f.precoMin()));
            if (f.precoMax() != null) p.add(cb.lessThanOrEqualTo(root.get("preco"), f.precoMax()));
            if (f.anoMin() != null) p.add(cb.greaterThanOrEqualTo(root.get("anoFabricacao"), f.anoMin()));
            if (f.anoMax() != null) p.add(cb.lessThanOrEqualTo(root.get("anoFabricacao"), f.anoMax()));
            if (f.cor() != null && !f.cor().isBlank()) {
                p.add(cb.like(cb.lower(root.get("cor")), "%" + f.cor().toLowerCase().trim() + "%"));
            }
            if (f.busca() != null && !f.busca().isBlank()) {
                String termo = "%" + f.busca().toLowerCase().trim() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(modelo.get("nome")), termo),
                        cb.like(cb.lower(marca.get("nome")), termo),
                        cb.like(cb.lower(root.get("placa")), termo)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
