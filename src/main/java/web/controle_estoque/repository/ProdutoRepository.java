package web.controle_estoque.repository;

import web.controle_estoque.model.Produto;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Page<Produto> findByEmpresaIdAndAtivoTrue(Long empresaId, Pageable pageable);

    Page<Produto> findByEmpresaIdAndNomeContainingIgnoreCaseAndAtivoTrue(Long empresaId, String nome,
            Pageable pageable);

    long countByEmpresaIdAndAtivoTrue(Long empresaId);

    Optional<Produto> findByIdAndEmpresaId(Long id, Long empresaId);
}