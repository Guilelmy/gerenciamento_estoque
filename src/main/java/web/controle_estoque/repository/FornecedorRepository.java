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

    Page<Fornecedor> findByEmpresaIdAndAtivoTrue(Long empresaId, Pageable pageable);

    List<Fornecedor> findByEmpresaIdAndAtivoTrue(Long empresaId);

    Page<Fornecedor> findByEmpresaIdAndNomeContainingIgnoreCaseAndAtivoTrue(Long empresaId, String nome,
            Pageable pageable);

    long countByEmpresaIdAndAtivoTrue(Long empresaId);

    boolean existsByCnpjAndEmpresaId(String cnpj, Long empresaId);

    Optional<Fornecedor> findByIdAndEmpresaId(Long id, Long empresaId);
}