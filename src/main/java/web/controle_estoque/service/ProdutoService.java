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

    public Page<Produto> listar(Pageable pageable, String termo) {
        Empresa empresa = securityService.getEmpresaLogada();

        if (termo == null || termo.isEmpty()) {
            return repository.findByEmpresaIdAndAtivoTrue(empresa.getId(), pageable);
        } else {
            return repository.findByEmpresaIdAndNomeContainingIgnoreCaseAndAtivoTrue(empresa.getId(), termo, pageable);
        }
    }

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