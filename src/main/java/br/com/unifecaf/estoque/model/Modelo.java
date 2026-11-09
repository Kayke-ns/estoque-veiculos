package br.com.unifecaf.estoque.model;

import jakarta.persistence.*;

@Entity
@Table(name = "modelo",
       uniqueConstraints = @UniqueConstraint(name = "uk_modelo_marca", columnNames = {"nome", "marca_id"}))
public class Modelo extends EntidadeBase {
    @Column(nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVeiculo tipo;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca;

    protected Modelo() {
    }

    public Modelo(String nome, TipoVeiculo tipo, Marca marca) {
        setNome(nome);
        this.tipo = tipo;
        this.marca = marca;
    }

    @Override
    public String getDescricao() {
        return marca.getNome() + " " + nome + " - " + tipo.getRotulo();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do modelo e obrigatorio");
        }
        this.nome = nome.trim();
    }

    public TipoVeiculo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVeiculo tipo) {
        this.tipo = tipo;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }
}
