package web.controle_estoque.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "item_estoque", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"empresa_id", "produto_id"})
})
public class ItemEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Min(0)
    @Column(name = "quantidade_atual", nullable = false)
    private Integer quantidadeAtual;

    @Column(name = "data_atualizacao")
    private LocalDate dataAtualizacao;
    
    @PrePersist
    @PreUpdate
    public void preAtualizacao() {
        this.dataAtualizacao = LocalDate.now();
    }
}