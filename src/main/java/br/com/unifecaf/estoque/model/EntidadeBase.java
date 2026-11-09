package br.com.unifecaf.estoque.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Superclasse abstrata de todas as entidades do sistema.
 *
 * HERANCA: Marca, Modelo e Veiculo herdam o identificador e as datas de auditoria.
 * ABSTRACAO: a classe e abstrata, ou seja, nao pode ser instanciada diretamente.
 * ENCAPSULAMENTO: atributos privados, acessados apenas por getters.
 */
@MappedSuperclass
public abstract class EntidadeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void aoCriar() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
    }

    @PreUpdate
    protected void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Metodo abstrato: cada subclasse descreve a si mesma de forma diferente (POLIMORFISMO).
     */
    public abstract String getDescricao();

    public Long getId() {
        return id;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
