package br.com.unifecaf.estoque.repository;

import br.com.unifecaf.estoque.model.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModeloRepository extends JpaRepository<Modelo, Long> {

    List<Modelo> findByMarcaIdOrderByNomeAsc(Long marcaId);

    List<Modelo> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCaseAndMarcaId(String nome, Long marcaId);

    boolean existsByMarcaId(Long marcaId);
}
