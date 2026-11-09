package br.com.unifecaf.estoque.controller;

import br.com.unifecaf.estoque.dto.ModeloDTO;
import br.com.unifecaf.estoque.service.ModeloService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modelos")
public class ModeloController {
    private final ModeloService service;

    public ModeloController(ModeloService service) {
        this.service = service;
    }

    @GetMapping
    public List<ModeloDTO.Response> listar(@RequestParam(required = false) Long marcaId) {
        return service.listar(marcaId);
    }

    @GetMapping("/{id}")
    public ModeloDTO.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModeloDTO.Response criar(@RequestBody @Valid ModeloDTO.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public ModeloDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid ModeloDTO.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
