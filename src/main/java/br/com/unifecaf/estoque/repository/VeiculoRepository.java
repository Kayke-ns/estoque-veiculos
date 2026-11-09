package br.com.unifecaf.estoque.repository;

import br.com.unifecaf.estoque.model.StatusVeiculo;
import br.com.unifecaf.estoque.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long>, JpaSpecificationExecutor<Veiculo> {

    boolean existsByModeloId(Long modeloId);

    boolean existsByPlacaIgnoreCase(String placa);

    long countByStatus(StatusVeiculo status);
}
