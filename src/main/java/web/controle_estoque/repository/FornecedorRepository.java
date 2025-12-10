package web.controle_estoque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.controle_estoque.model.Fornecedor;
import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {

    // Versão Paginada (Para a Tabela)
    Page<Fornecedor> findByEmpresaIdAndAtivoTrue(Long empresaId, Pageable pageable);
    
    // CORREÇÃO DO ERRO: Versão Lista (Para o Select de Produtos)
    List<Fornecedor> findByEmpresaIdAndAtivoTrue(Long empresaId);

    Page<Fornecedor> findByEmpresaIdAndNomeContainingIgnoreCaseAndAtivoTrue(Long empresaId, String nome, Pageable pageable);
    
    long countByEmpresaIdAndAtivoTrue(Long empresaId);
    
    boolean existsByCnpjAndEmpresaId(String cnpj, Long empresaId);
    
    // CORREÇÃO DO ERRO: Este método estava faltando
    Optional<Fornecedor> findByIdAndEmpresaId(Long id, Long empresaId);
}