package web.controle_estoque.model;

import java.util.List;

public class Fornecedor {
    Long id;
    String nome;
    String cnpj;
    String telefone;
    String email;

    List<Produto> produtos;
}
