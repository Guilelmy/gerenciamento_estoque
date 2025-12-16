package web.controle_estoque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.controle_estoque.model.Fornecedor;
import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    Page<Fornecedor> findByEmpresaIdAndAtivoTrue(Long empresaId, Pageable pageable);

    List<Fornecedor> findByEmpresaIdAndAtivoTrue(Long empresaId);

    // --- MUDANÇA AQUI: Nova query que busca por Nome OU CNPJ ---
    @Query("SELECT f FROM Fornecedor f WHERE f.empresa.id = :empresaId AND f.ativo = true AND (LOWER(f.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR f.cnpj LIKE CONCAT('%', :termo, '%'))")
    Page<Fornecedor> buscarPorNomeOuCnpj(@Param("empresaId") Long empresaId, @Param("termo") String termo, Pageable pageable);

    long countByEmpresaIdAndAtivoTrue(Long empresaId);

    boolean existsByCnpjAndEmpresaId(String cnpj, Long empresaId);

    Optional<Fornecedor> findByIdAndEmpresaId(Long id, Long empresaId);
}