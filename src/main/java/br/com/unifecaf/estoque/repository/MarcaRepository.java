package br.com.unifecaf.estoque.repository;

import br.com.unifecaf.estoque.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarcaRepository extends JpaRepository<Marca, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    List<Marca> findAllByOrderByNomeAsc();
}
