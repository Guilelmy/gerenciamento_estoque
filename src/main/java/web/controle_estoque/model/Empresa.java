package web.controle_estoque.model;

import java.util.List;
import jakarta.persistence.Entity;

@Entity
public class Empresa {
    Long id;
    String nome;
    String cnpj;
    String email;
    String senha;

    List<ItemEstoque> itensEstoque;
}
