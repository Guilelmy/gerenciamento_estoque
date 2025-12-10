package web.controle_estoque.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "fornecedor")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String cnpj;

    @Size(max = 20)
    private String telefone;
    @Column(nullable = false)
    private boolean ativo = true;
    @Email
    @Size(max = 100)
    private String email;

    @OneToMany(mappedBy = "fornecedor")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Produto> produtos;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa; // <--- ADICIONE ISSO
}