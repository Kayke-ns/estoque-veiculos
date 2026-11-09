package br.com.unifecaf.estoque.model;

import jakarta.persistence.*;

@Entity
@Table(name = "marca")
public class Marca extends EntidadeBase {
    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(name = "pais_origem", length = 60)
    private String paisOrigem;

    protected Marca() {
    }

    public Marca(String nome, String paisOrigem) {
        setNome(nome);
        this.paisOrigem = paisOrigem;
    }

    @Override
    public String getDescricao() {
        return paisOrigem == null ? nome : nome + " (" + paisOrigem + ")";
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da marca e obrigatorio");
        }
        this.nome = nome.trim();
    }

    public String getPaisOrigem() {
        return paisOrigem;
    }

    public void setPaisOrigem(String paisOrigem) {
        this.paisOrigem = paisOrigem;
    }
}
