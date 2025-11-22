package web.controle_estoque.model;

import java.time.LocalDate;

public class ItemEstoque {
    Long id;
    Empresa empresa;
    Produto produto;
    Integer quantidadeAtual;
    LocalDate dataAtualizacao;
}
