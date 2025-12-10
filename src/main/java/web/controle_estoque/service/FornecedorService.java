package web.controle_estoque.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.controle_estoque.model.Empresa;
import web.controle_estoque.model.Fornecedor;
import web.controle_estoque.repository.FornecedorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository repository;
    private final SecurityService securityService;

    public List<Fornecedor> listarTodos() {
        Empresa empresa = securityService.getEmpresaLogada();
        return repository.findByEmpresaIdAndAtivoTrue(empresa.getId());
    }

    public Page<Fornecedor> listar(Pageable pageable, String termo) {
        Empresa empresa = securityService.getEmpresaLogada();

        if (termo == null || termo.isEmpty()) {
            return repository.findByEmpresaIdAndAtivoTrue(empresa.getId(), pageable);
        } else {
            return repository.findByEmpresaIdAndNomeContainingIgnoreCaseAndAtivoTrue(empresa.getId(), termo, pageable);
        }
    }

    public Fornecedor buscarPorId(Long id) {
        Empresa empresa = securityService.getEmpresaLogada();
        return repository.findByIdAndEmpresaId(id, empresa.getId())
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado ou acesso negado."));
    }

    @Transactional
    public Fornecedor salvar(Fornecedor fornecedor) {
        Empresa empresa = securityService.getEmpresaLogada();

        boolean cnpjJaExiste = repository.existsByCnpjAndEmpresaId(fornecedor.getCnpj(), empresa.getId());

        if (fornecedor.getId() == null && cnpjJaExiste) {
            throw new IllegalArgumentException("Já existe um fornecedor cadastrado com este CNPJ na sua empresa.");
        }

        fornecedor.setEmpresa(empresa);
        return repository.save(fornecedor);
    }

    @Transactional
    public void excluir(Long id) {
        Fornecedor fornecedor = buscarPorId(id);
        fornecedor.setAtivo(false);
        repository.save(fornecedor);
    }

    public Object contarAtivos() {
        Empresa empresa = securityService.getEmpresaLogada();
        return repository.countByEmpresaIdAndAtivoTrue(empresa.getId());
    }
}