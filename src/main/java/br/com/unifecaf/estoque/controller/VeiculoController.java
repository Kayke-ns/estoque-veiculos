package br.com.unifecaf.estoque.controller;

import br.com.unifecaf.estoque.dto.FiltroVeiculo;
import br.com.unifecaf.estoque.dto.ResumoEstoque;
import br.com.unifecaf.estoque.dto.VeiculoDTO;
import br.com.unifecaf.estoque.model.StatusVeiculo;
import br.com.unifecaf.estoque.model.TipoVeiculo;
import br.com.unifecaf.estoque.service.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {
    private final VeiculoService service;

    public VeiculoController(VeiculoService service) {
        this.service = service;
    }

    @GetMapping
    public List<VeiculoDTO.Response> consultar(
            @RequestParam(required = false) Long marcaId,
            @RequestParam(required = false) Long modeloId,
            @RequestParam(required = false) TipoVeiculo tipo,
            @RequestParam(required = false) StatusVeiculo status,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) Integer anoMin,
            @RequestParam(required = false) Integer anoMax,
            @RequestParam(required = false) String cor,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "atualizadoEm") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direcao) {
        FiltroVeiculo filtro = new FiltroVeiculo(marcaId, modeloId, tipo, status,
                precoMin, precoMax, anoMin, anoMax, cor, busca);
        return service.consultar(filtro, ordenarPor, direcao);
    }

    @GetMapping("/resumo")
    public ResumoEstoque resumo() {
        return service.resumo();
    }

    @GetMapping("/{id}")
    public VeiculoDTO.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoDTO.Response cadastrar(@RequestBody @Valid VeiculoDTO.Request req) {
        return service.cadastrar(req);
    }

    @PutMapping("/{id}")
    public VeiculoDTO.Response substituir(@PathVariable Long id, @RequestBody @Valid VeiculoDTO.Request req) {
        return service.substituir(id, req);
    }

    @PatchMapping("/{id}")
    public VeiculoDTO.Response atualizar(@PathVariable Long id, @RequestBody @Valid VeiculoDTO.Atualizacao req) {
        return service.atualizarParcial(id, req);
    }

    @PostMapping("/{id}/vender")
    public VeiculoDTO.Response vender(@PathVariable Long id) {
        return service.vender(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
