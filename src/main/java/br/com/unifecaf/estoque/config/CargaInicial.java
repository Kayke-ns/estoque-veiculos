package br.com.unifecaf.estoque.config;

import br.com.unifecaf.estoque.model.*;
import br.com.unifecaf.estoque.repository.MarcaRepository;
import br.com.unifecaf.estoque.repository.ModeloRepository;
import br.com.unifecaf.estoque.repository.VeiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Popula o banco com dados de exemplo na primeira execucao (somente se estiver vazio).
 * Desligue com app.carga-inicial=false.
 */
@Component
@ConditionalOnProperty(name = "app.carga-inicial", havingValue = "true", matchIfMissing = true)
public class CargaInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);

    private final MarcaRepository marcas;
    private final ModeloRepository modelos;
    private final VeiculoRepository veiculos;

    public CargaInicial(MarcaRepository marcas, ModeloRepository modelos, VeiculoRepository veiculos) {
        this.marcas = marcas;
        this.modelos = modelos;
        this.veiculos = veiculos;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (marcas.count() > 0) {
            return;
        }
        Marca toyota = marcas.save(new Marca("Toyota", "Japao"));
        Marca vw = marcas.save(new Marca("Volkswagen", "Alemanha"));
        Marca honda = marcas.save(new Marca("Honda", "Japao"));
        Marca chevrolet = marcas.save(new Marca("Chevrolet", "Estados Unidos"));
        Marca fiat = marcas.save(new Marca("Fiat", "Italia"));

        Modelo corolla = modelos.save(new Modelo("Corolla", TipoVeiculo.SEDAN, toyota));
        Modelo hilux = modelos.save(new Modelo("Hilux", TipoVeiculo.PICAPE, toyota));
        Modelo polo = modelos.save(new Modelo("Polo", TipoVeiculo.HATCH, vw));
        Modelo tcross = modelos.save(new Modelo("T-Cross", TipoVeiculo.SUV, vw));
        Modelo civic = modelos.save(new Modelo("Civic", TipoVeiculo.SEDAN, honda));
        Modelo cg = modelos.save(new Modelo("CG 160", TipoVeiculo.MOTO, honda));
        Modelo onix = modelos.save(new Modelo("Onix", TipoVeiculo.HATCH, chevrolet));
        Modelo tracker = modelos.save(new Modelo("Tracker", TipoVeiculo.SUV, chevrolet));
        Modelo strada = modelos.save(new Modelo("Strada", TipoVeiculo.PICAPE, fiat));

        salvar(corolla, 2023, "Prata", "149900.00", 18500, "RTA1B23", StatusVeiculo.DISPONIVEL);
        salvar(corolla, 2020, "Preto", "112000.00", 61200, "QWE4F56", StatusVeiculo.DISPONIVEL);
        salvar(hilux, 2022, "Branco", "259900.00", 40100, "HIL2C22", StatusVeiculo.RESERVADO);
        salvar(polo, 2024, "Vermelho", "92900.00", 3200, "POL3D44", StatusVeiculo.DISPONIVEL);
        salvar(tcross, 2021, "Cinza", "118500.00", 52000, "TCR5E67", StatusVeiculo.VENDIDO);
        salvar(civic, 2019, "Azul", "104900.00", 78300, "CIV6G89", StatusVeiculo.DISPONIVEL);
        salvar(cg, 2023, "Vermelho", "16990.00", 9800, "MOT7H01", StatusVeiculo.DISPONIVEL);
        salvar(onix, 2022, "Branco", "74900.00", 35400, "ONX8J23", StatusVeiculo.DISPONIVEL);
        salvar(tracker, 2024, "Preto", "139900.00", 1200, "TRK9K45", StatusVeiculo.DISPONIVEL);
        salvar(strada, 2018, "Prata", "68900.00", 98100, "STR1L67", StatusVeiculo.DESCONTINUADO);

        log.info("Carga inicial concluida: {} marcas, {} modelos, {} veiculos",
                marcas.count(), modelos.count(), veiculos.count());
    }

    private void salvar(Modelo modelo, int ano, String cor, String preco, int km, String placa, StatusVeiculo status) {
        Veiculo v = new Veiculo(modelo, ano, cor, new BigDecimal(preco), km, placa);
        v.setStatus(status);
        veiculos.save(v);
    }
}
