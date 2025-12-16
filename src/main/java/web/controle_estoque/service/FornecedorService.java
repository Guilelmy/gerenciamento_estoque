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
            // --- MUDANÇA AQUI: Chamando o novo método do repositório ---
            return repository.buscarPorNomeOuCnpj(empresa.getId(), termo, pageable);
        }
    }

    // ... (o resto do arquivo continua igual: buscarPorId, salvar, excluir, contarAtivos) ...
    public Fornecedor buscarPorId(Long id) {
        Empresa empresa = securityService.getEmpresaLogada();
        return repository.findByIdAndEmpresaId(id, empresa.getId())
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado ou acesso negado."));
    }

    @Transactional
    public Fornecedor salvar(Fornecedor fornecedor) {
        Empresa empresa = securityService.getEmpresaLogada();
        boolean cnpjJaExiste = repository.existsByCnpjAndEmpresaId(fornecedor.getCnpj(), empresa.getId());

        // Validação simples: se for novo e já existe CNPJ, erro.
        // (Para editar, idealmente checamos se o ID do banco é diferente do atual, mas para simplificar manteremos assim ou aprimoramos depois)
        if (fornecedor.getId() == null && cnpjJaExiste) {
            throw new IllegalArgumentException("Já existe um fornecedor cadastrado com este CNPJ.");
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