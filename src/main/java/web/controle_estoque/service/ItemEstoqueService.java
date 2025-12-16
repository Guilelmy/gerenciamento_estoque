package web.controle_estoque.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.controle_estoque.model.Empresa;
import web.controle_estoque.model.ItemEstoque;
import web.controle_estoque.model.Produto;
import web.controle_estoque.repository.EmpresaRepository;
import web.controle_estoque.repository.ItemEstoqueRepository;
import web.controle_estoque.repository.ProdutoRepository;

@Service
@RequiredArgsConstructor 
public class ItemEstoqueService {

    private final ItemEstoqueRepository itemEstoqueRepository;
    private final EmpresaRepository empresaRepository;
    private final ProdutoRepository produtoRepository;

    private final SecurityService securityService;

    public Page<ItemEstoque> listar(Pageable pageable, String termo, boolean apenasCriticos) {
    Empresa empresa = securityService.getEmpresaLogada();
    
    if (apenasCriticos) {
        return itemEstoqueRepository.findItensCriticos(empresa.getId(), pageable);
    }

    if (termo == null || termo.isEmpty()) {
        return itemEstoqueRepository.findByEmpresaId(empresa.getId(), pageable);
    } else {
        return itemEstoqueRepository.buscarPorNomeProduto(empresa.getId(), termo, pageable);
    }
}

    public ItemEstoque buscarPorId(Long id) {
        Empresa empresa = securityService.getEmpresaLogada();
        return itemEstoqueRepository.findByIdAndEmpresaId(id, empresa.getId())
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado"));
    }

    public long contarCriticos() {
        Empresa empresa = securityService.getEmpresaLogada();
        return itemEstoqueRepository.countItensCriticos(empresa.getId());
    }

    public Double valorTotalEstoque() {
        Empresa empresa = securityService.getEmpresaLogada();
        return itemEstoqueRepository.valorTotalEstoque(empresa.getId());
    }

    @Transactional
    public void criarNovoItem(Long produtoId, Integer qtdInicial, Integer qtdMinima) {
        Empresa empresa = securityService.getEmpresaLogada();
        if (itemEstoqueRepository.findByEmpresaIdAndProdutoId(empresa.getId(), produtoId).isPresent()) {
            throw new IllegalArgumentException("Já existe.");
        }
        Produto produto = produtoRepository.findById(produtoId).orElseThrow();
        ItemEstoque novo = new ItemEstoque();
        novo.setEmpresa(empresa);
        novo.setProduto(produto);
        novo.setQuantidadeAtual(qtdInicial);
        novo.setQuantidadeMinima(qtdMinima);
        itemEstoqueRepository.save(novo);
    }

    @Transactional
    public void realizarEntrada(Long id, Integer qtd) {
        ItemEstoque item = buscarPorId(id);
        item.setQuantidadeAtual(item.getQuantidadeAtual() + qtd);
        itemEstoqueRepository.save(item);
    }

    @Transactional
    public void realizarSaida(Long id, Integer qtd) {
        ItemEstoque item = buscarPorId(id);
        if (item.getQuantidadeAtual() < qtd)
            throw new IllegalArgumentException("Saldo insuficiente");
        item.setQuantidadeAtual(item.getQuantidadeAtual() - qtd);
        itemEstoqueRepository.save(item);
    }

    @Transactional
    public void salvarConfiguracao(Long id, Integer min) {
        ItemEstoque item = buscarPorId(id);
        item.setQuantidadeMinima(min);
        itemEstoqueRepository.save(item);
    }
}