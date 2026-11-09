package br.com.unifecaf.estoque.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Year;

/**
 * Veiculo do estoque. Concentra as regras de negocio que protegem seus proprios dados
 * (ENCAPSULAMENTO): preco nunca negativo, quilometragem nunca diminui, ano valido.
 */
@Entity
@Table(name = "veiculo", indexes = {
        @Index(name = "idx_veiculo_status", columnList = "status"),
        @Index(name = "idx_veiculo_preco", columnList = "preco"),
        @Index(name = "idx_veiculo_ano", columnList = "ano_fabricacao")
})
public class Veiculo extends EntidadeBase {

    public static final int ANO_MINIMO = 1950;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "modelo_id", nullable = false)
    private Modelo modelo;

    @Column(name = "ano_fabricacao", nullable = false)
    private Integer anoFabricacao;

    @Column(nullable = false, length = 30)
    private String cor;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer quilometragem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVeiculo status;

    @Column(unique = true, length = 10)
    private String placa;

    protected Veiculo() {
    }

    public Veiculo(Modelo modelo, Integer anoFabricacao, String cor, BigDecimal preco,
                   Integer quilometragem, String placa) {
        this.modelo = modelo;
        setAnoFabricacao(anoFabricacao);
        setCor(cor);
        setPreco(preco);
        this.quilometragem = validarKm(quilometragem);
        this.placa = placa;
        this.status = StatusVeiculo.DISPONIVEL;
    }

    @Override
    public String getDescricao() {
        return modelo.getMarca().getNome() + " " + modelo.getNome() + " " + anoFabricacao + " " + cor;
    }

    /** Regra de negocio: marca o veiculo como vendido. */
    public void vender() {
        if (status == StatusVeiculo.VENDIDO) {
            throw new IllegalStateException("Veiculo ja foi vendido");
        }
        this.status = StatusVeiculo.VENDIDO;
    }

    public boolean isDisponivel() {
        return status == StatusVeiculo.DISPONIVEL;
    }

    // ---------- getters e setters com validacao ----------

    public Modelo getModelo() {
        return modelo;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = modelo;
    }

    public Marca getMarca() {
        return modelo.getMarca();
    }

    public Integer getAnoFabricacao() {
        return anoFabricacao;
    }

    public void setAnoFabricacao(Integer ano) {
        int anoMaximo = Year.now().getValue() + 1;
        if (ano == null || ano < ANO_MINIMO || ano > anoMaximo) {
            throw new IllegalArgumentException("Ano de fabricacao deve estar entre " + ANO_MINIMO + " e " + anoMaximo);
        }
        this.anoFabricacao = ano;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        if (cor == null || cor.isBlank()) {
            throw new IllegalArgumentException("A cor e obrigatoria");
        }
        this.cor = cor.trim();
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        if (preco == null || preco.signum() <= 0) {
            throw new IllegalArgumentException("O preco deve ser maior que zero");
        }
        this.preco = preco;
    }

    public Integer getQuilometragem() {
        return quilometragem;
    }

    /** A quilometragem de um veiculo nunca pode voltar para tras. */
    public void setQuilometragem(Integer novaKm) {
        validarKm(novaKm);
        if (this.quilometragem != null && novaKm < this.quilometragem) {
            throw new IllegalArgumentException("A quilometragem nao pode ser menor que a atual (" + this.quilometragem + " km)");
        }
        this.quilometragem = novaKm;
    }

    private static Integer validarKm(Integer km) {
        if (km == null || km < 0) {
            throw new IllegalArgumentException("A quilometragem nao pode ser negativa");
        }
        return km;
    }

    public StatusVeiculo getStatus() {
        return status;
    }

    public void setStatus(StatusVeiculo status) {
        if (status == null) {
            throw new IllegalArgumentException("O status e obrigatorio");
        }
        this.status = status;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
}
