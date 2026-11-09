package br.com.unifecaf.estoque.service;

import br.com.unifecaf.estoque.dto.MarcaDTO;
import br.com.unifecaf.estoque.exception.RecursoNaoEncontradoException;
import br.com.unifecaf.estoque.exception.RegraNegocioException;
import br.com.unifecaf.estoque.model.Marca;
import br.com.unifecaf.estoque.repository.MarcaRepository;
import br.com.unifecaf.estoque.repository.ModeloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MarcaService {
    private final MarcaRepository marcas;
    private final ModeloRepository modelos;

    public MarcaService(MarcaRepository marcas, ModeloRepository modelos) {
        this.marcas = marcas;
        this.modelos = modelos;
    }

    @Transactional(readOnly = true)
    public List<MarcaDTO.Response> listar() {
        return marcas.findAllByOrderByNomeAsc().stream().map(MarcaDTO.Response::de).toList();
    }

    @Transactional(readOnly = true)
    public MarcaDTO.Response buscar(Long id) {
        return MarcaDTO.Response.de(obter(id));
    }

    @Transactional
    public MarcaDTO.Response criar(MarcaDTO.Request req) {
        if (marcas.existsByNomeIgnoreCase(req.nome().trim())) {
            throw new RegraNegocioException("Ja existe uma marca chamada " + req.nome());
        }
        return MarcaDTO.Response.de(marcas.save(new Marca(req.nome(), req.paisOrigem())));
    }

    @Transactional
    public MarcaDTO.Response atualizar(Long id, MarcaDTO.Request req) {
        Marca marca = obter(id);
        if (!marca.getNome().equalsIgnoreCase(req.nome().trim()) && marcas.existsByNomeIgnoreCase(req.nome().trim())) {
            throw new RegraNegocioException("Ja existe uma marca chamada " + req.nome());
        }
        marca.setNome(req.nome());
        marca.setPaisOrigem(req.paisOrigem());
        return MarcaDTO.Response.de(marca);
    }

    @Transactional
    public void remover(Long id) {
        Marca marca = obter(id);
        if (modelos.existsByMarcaId(id)) {
            throw new RegraNegocioException("A marca " + marca.getNome() + " possui modelos cadastrados e nao pode ser removida");
        }
        marcas.delete(marca);
    }

    Marca obter(Long id) {
        return marcas.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Marca", id));
    }
}
