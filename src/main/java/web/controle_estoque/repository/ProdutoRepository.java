package web.controle_estoque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.controle_estoque.model.Produto;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Método para buscar quando não há filtros (ou filtros vazios)
    Page<Produto> findByEmpresaIdAndAtivoTrue(Long empresaId, Pageable pageable);

    long countByEmpresaIdAndAtivoTrue(Long empresaId);

    Optional<Produto> findByIdAndEmpresaId(Long id, Long empresaId);

    // --- NOVA QUERY COM DOIS PARÂMETROS ---
    // Filtra por Nome do Produto E Nome do Fornecedor
    // O % no LIKE garante que se o campo vier vazio, ele busca tudo ("%%")
    @Query("SELECT p FROM Produto p WHERE p.empresa.id = :empresaId AND p.ativo = true " +
           "AND LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')) " +
           "AND LOWER(p.fornecedor.nome) LIKE LOWER(CONCAT('%', :fornecedor, '%'))")
    Page<Produto> buscarPorFiltros(@Param("empresaId") Long empresaId, 
                                   @Param("nome") String nome, 
                                   @Param("fornecedor") String fornecedor, 
                                   Pageable pageable);
}