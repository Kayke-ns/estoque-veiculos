package br.com.unifecaf.estoque.service;

import br.com.unifecaf.estoque.dto.FiltroVeiculo;
import br.com.unifecaf.estoque.dto.ResumoEstoque;
import br.com.unifecaf.estoque.dto.VeiculoDTO;
import br.com.unifecaf.estoque.exception.RecursoNaoEncontradoException;
import br.com.unifecaf.estoque.exception.RegraNegocioException;
import br.com.unifecaf.estoque.model.Modelo;
import br.com.unifecaf.estoque.model.StatusVeiculo;
import br.com.unifecaf.estoque.model.Veiculo;
import br.com.unifecaf.estoque.repository.VeiculoRepository;
import br.com.unifecaf.estoque.repository.VeiculoSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class VeiculoService {
    private static final Set<String> CAMPOS_ORDENACAO = Set.of("preco", "anoFabricacao", "quilometragem", "atualizadoEm");

    private final VeiculoRepository veiculos;
    private final ModeloService modeloService;

    public VeiculoService(VeiculoRepository veiculos, ModeloService modeloService) {
        this.veiculos = veiculos;
        this.modeloService = modeloService;
    }

    @Transactional(readOnly = true)
    public List<VeiculoDTO.Response> consultar(FiltroVeiculo filtro, String ordenarPor, String direcao) {
        if (filtro.precoMin() != null && filtro.precoMax() != null && filtro.precoMin().compareTo(filtro.precoMax()) > 0) {
            throw new IllegalArgumentException("precoMin nao pode ser maior que precoMax");
        }
        if (filtro.anoMin() != null && filtro.anoMax() != null && filtro.anoMin() > filtro.anoMax()) {
            throw new IllegalArgumentException("anoMin nao pode ser maior que anoMax");
        }
        String campo = CAMPOS_ORDENACAO.contains(ordenarPor) ? ordenarPor : "atualizadoEm";
        Sort sort = "asc".equalsIgnoreCase(direcao) ? Sort.by(campo).ascending() : Sort.by(campo).descending();
        return veiculos.findAll(VeiculoSpecifications.comFiltro(filtro), sort)
                .stream().map(VeiculoDTO.Response::de).toList();
    }

    @Transactional(readOnly = true)
    public VeiculoDTO.Response buscar(Long id) {
        return VeiculoDTO.Response.de(obter(id));
    }

    @Transactional
    public VeiculoDTO.Response cadastrar(VeiculoDTO.Request req) {
        Modelo modelo = modeloService.obter(req.modeloId());
        String placa = normalizarPlaca(req.placa());
        if (placa != null && veiculos.existsByPlacaIgnoreCase(placa)) {
            throw new RegraNegocioException("Ja existe um veiculo com a placa " + placa);
        }
        Veiculo v = new Veiculo(modelo, req.anoFabricacao(), req.cor(), req.preco(), req.quilometragem(), placa);
        if (req.status() != null) {
            v.setStatus(req.status());
        }
        return VeiculoDTO.Response.de(veiculos.save(v));
    }

    @Transactional
    public VeiculoDTO.Response substituir(Long id, VeiculoDTO.Request req) {
        Veiculo v = obter(id);
        String placa = normalizarPlaca(req.placa());
        if (placa != null && !placa.equalsIgnoreCase(v.getPlaca()) && veiculos.existsByPlacaIgnoreCase(placa)) {
            throw new RegraNegocioException("Ja existe um veiculo com a placa " + placa);
        }
        v.setModelo(modeloService.obter(req.modeloId()));
        v.setAnoFabricacao(req.anoFabricacao());
        v.setCor(req.cor());
        v.setPreco(req.preco());
        v.setQuilometragem(req.quilometragem());
        v.setPlaca(placa);
        if (req.status() != null) {
            v.setStatus(req.status());
        }
        return VeiculoDTO.Response.de(v);
    }

    @Transactional
    public VeiculoDTO.Response atualizarParcial(Long id, VeiculoDTO.Atualizacao req) {
        Veiculo v = obter(id);
        if (req.preco() == null && req.quilometragem() == null && req.status() == null) {
            throw new IllegalArgumentException("Informe ao menos um campo: preco, quilometragem ou status");
        }
        if (req.preco() != null) v.setPreco(req.preco());
        if (req.quilometragem() != null) v.setQuilometragem(req.quilometragem());
        if (req.status() != null) v.setStatus(req.status());
        return VeiculoDTO.Response.de(v);
    }

    @Transactional
    public VeiculoDTO.Response vender(Long id) {
        Veiculo v = obter(id);
        v.vender();
        return VeiculoDTO.Response.de(v);
    }

    @Transactional
    public void remover(Long id) {
        veiculos.delete(obter(id));
    }

    @Transactional(readOnly = true)
    public ResumoEstoque resumo() {
        BigDecimal valor = veiculos.findAll().stream()
                .filter(Veiculo::isDisponivel)
                .map(Veiculo::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumoEstoque(
                veiculos.count(),
                veiculos.countByStatus(StatusVeiculo.DISPONIVEL),
                veiculos.countByStatus(StatusVeiculo.RESERVADO),
                veiculos.countByStatus(StatusVeiculo.VENDIDO),
                veiculos.countByStatus(StatusVeiculo.DESCONTINUADO),
                valor);
    }

    private Veiculo obter(Long id) {
        return veiculos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo", id));
    }

    private static String normalizarPlaca(String placa) {
        if (placa == null || placa.isBlank()) return null;
        return placa.replace("-", "").toUpperCase().trim();
    }
}
