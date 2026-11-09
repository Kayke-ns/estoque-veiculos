package br.com.unifecaf.estoque.service;

import br.com.unifecaf.estoque.dto.ModeloDTO;
import br.com.unifecaf.estoque.exception.RecursoNaoEncontradoException;
import br.com.unifecaf.estoque.exception.RegraNegocioException;
import br.com.unifecaf.estoque.model.Marca;
import br.com.unifecaf.estoque.model.Modelo;
import br.com.unifecaf.estoque.repository.ModeloRepository;
import br.com.unifecaf.estoque.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModeloService {
    private final ModeloRepository modelos;
    private final VeiculoRepository veiculos;
    private final MarcaService marcaService;

    public ModeloService(ModeloRepository modelos, VeiculoRepository veiculos, MarcaService marcaService) {
        this.modelos = modelos;
        this.veiculos = veiculos;
        this.marcaService = marcaService;
    }

    @Transactional(readOnly = true)
    public List<ModeloDTO.Response> listar(Long marcaId) {
        List<Modelo> lista = marcaId == null
                ? modelos.findAllByOrderByNomeAsc()
                : modelos.findByMarcaIdOrderByNomeAsc(marcaId);
        return lista.stream().map(ModeloDTO.Response::de).toList();
    }

    @Transactional(readOnly = true)
    public ModeloDTO.Response buscar(Long id) {
        return ModeloDTO.Response.de(obter(id));
    }

    @Transactional
    public ModeloDTO.Response criar(ModeloDTO.Request req) {
        Marca marca = marcaService.obter(req.marcaId());
        if (modelos.existsByNomeIgnoreCaseAndMarcaId(req.nome().trim(), marca.getId())) {
            throw new RegraNegocioException("O modelo " + req.nome() + " ja existe para a marca " + marca.getNome());
        }
        return ModeloDTO.Response.de(modelos.save(new Modelo(req.nome(), req.tipo(), marca)));
    }

    @Transactional
    public ModeloDTO.Response atualizar(Long id, ModeloDTO.Request req) {
        Modelo modelo = obter(id);
        Marca marca = marcaService.obter(req.marcaId());
        boolean mudouChave = !modelo.getNome().equalsIgnoreCase(req.nome().trim())
                || !modelo.getMarca().getId().equals(marca.getId());
        if (mudouChave && modelos.existsByNomeIgnoreCaseAndMarcaId(req.nome().trim(), marca.getId())) {
            throw new RegraNegocioException("O modelo " + req.nome() + " ja existe para a marca " + marca.getNome());
        }
        modelo.setNome(req.nome());
        modelo.setTipo(req.tipo());
        modelo.setMarca(marca);
        return ModeloDTO.Response.de(modelo);
    }

    @Transactional
    public void remover(Long id) {
        Modelo modelo = obter(id);
        if (veiculos.existsByModeloId(id)) {
            throw new RegraNegocioException("O modelo " + modelo.getNome() + " possui veiculos no estoque e nao pode ser removido");
        }
        modelos.delete(modelo);
    }

    Modelo obter(Long id) {
        return modelos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Modelo", id));
    }
}
