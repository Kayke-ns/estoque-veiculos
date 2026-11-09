package br.com.unifecaf.estoque.controller;

import br.com.unifecaf.estoque.dto.MarcaDTO;
import br.com.unifecaf.estoque.service.MarcaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marcas")
public class MarcaController {

    private final MarcaService service;

    public MarcaController(MarcaService service) {
        this.service = service;
    }

    @GetMapping
    public List<MarcaDTO.Response> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public MarcaDTO.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MarcaDTO.Response criar(@RequestBody @Valid MarcaDTO.Request req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public MarcaDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid MarcaDTO.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
