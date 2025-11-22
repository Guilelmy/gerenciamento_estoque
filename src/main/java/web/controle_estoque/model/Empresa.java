package web.controle_estoque.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    String nome;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20, unique = true)
    String cnpj;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100, unique = true)
    String email;

    @NotBlank
    @Column(nullable = false)
    String senha;

    @OneToMany(mappedBy = "empresa", cascade = jakarta.persistence.CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<ItemEstoque> itensEstoque;
}
