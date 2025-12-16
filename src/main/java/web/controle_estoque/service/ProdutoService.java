package web.controle_estoque.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.controle_estoque.model.Empresa;
import web.controle_estoque.model.Produto;
import web.controle_estoque.repository.ProdutoRepository;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;
    private final SecurityService securityService;

    // ... imports

    // Atualize o método listar
    public Page<Produto> listar(Pageable pageable, String nome, String fornecedor) {
        Empresa empresa = securityService.getEmpresaLogada();

        // Garante que não passamos null para a query (transforma null em "")
        String termoNome = (nome == null) ? "" : nome;
        String termoFornecedor = (fornecedor == null) ? "" : fornecedor;

        return repository.buscarPorFiltros(empresa.getId(), termoNome, termoFornecedor, pageable);
    }
    
    // ... restante dos métodos (salvar, excluir...)

    public Produto buscarPorId(Long id) {
        Empresa empresa = securityService.getEmpresaLogada();
        return repository.findByIdAndEmpresaId(id, empresa.getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado ou acesso negado ID: " + id));
    }

    @Transactional
    public void salvar(Produto produto) {
        Empresa empresa = securityService.getEmpresaLogada();

        produto.setEmpresa(empresa); // Vínculo Automático
        produto.setAtivo(true);

        repository.save(produto);
    }

    @Transactional
    public boolean desativar(Long id) {
        Empresa empresa = securityService.getEmpresaLogada();

        return repository.findByIdAndEmpresaId(id, empresa.getId()).map(produto -> {
            produto.setAtivo(false); // Soft Delete
            repository.save(produto);
            return true;
        }).orElse(false);
    }

    public long contarAtivos() {
        Empresa empresa = securityService.getEmpresaLogada();
        return repository.countByEmpresaIdAndAtivoTrue(empresa.getId());
    }
}