package web.controle_estoque.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import web.controle_estoque.model.ItemEstoque;

@Repository
public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {

    Page<ItemEstoque> findByEmpresaId(Long empresaId, Pageable pageable);

    @Query("SELECT i FROM ItemEstoque i WHERE i.empresa.id = :empresaId AND lower(i.produto.nome) LIKE lower(concat('%', :termo, '%'))")
    Page<ItemEstoque> buscarPorNomeProduto(@Param("empresaId") Long empresaId, @Param("termo") String termo,
            Pageable pageable);

    Optional<ItemEstoque> findByEmpresaIdAndProdutoId(Long empresaId, Long produtoId);

    Optional<ItemEstoque> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT COUNT(i) FROM ItemEstoque i WHERE i.empresa.id = :empresaId AND i.quantidadeAtual <= i.quantidadeMinima")
    long countItensCriticos(@Param("empresaId") Long empresaId);

    @Query("SELECT SUM(i.quantidadeAtual * i.produto.preco) FROM ItemEstoque i WHERE i.empresa.id = :empresaId")
    Double valorTotalEstoque(@Param("empresaId") Long empresaId);

    @Query("SELECT i FROM ItemEstoque i WHERE i.empresa.id = :empresaId AND i.quantidadeAtual <= i.quantidadeMinima")
    Page<ItemEstoque> findItensCriticos(@Param("empresaId") Long empresaId, Pageable pageable);

    List<ItemEstoque> findByEmpresaId(Long empresaId);
}