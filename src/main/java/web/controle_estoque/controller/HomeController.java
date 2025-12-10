package web.controle_estoque.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import web.controle_estoque.service.FornecedorService;
import web.controle_estoque.service.ItemEstoqueService;
import web.controle_estoque.service.ProdutoService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProdutoService produtoService;
    private final FornecedorService fornecedorService;
    private final ItemEstoqueService itemEstoqueService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("currentPage", "dashboard");

        // CORREÇÃO: Usando Service. O Service pega a empresa logada.
        model.addAttribute("totalProdutos", produtoService.contarAtivos());
        
        // CORREÇÃO: Certifique-se de ter criado o método contarAtivos() no FornecedorService
        // Se não tiver, crie lá retornando repository.countByEmpresaIdAndAtivoTrue(empresa.getId())
        model.addAttribute("totalFornecedores", fornecedorService.contarAtivos());        
        
        // CORREÇÃO: Usando métodos do Service criados acima
        model.addAttribute("totalItensCriticos", itemEstoqueService.contarCriticos());
        
        Double valorTotal = itemEstoqueService.valorTotalEstoque();
        model.addAttribute("valorTotalEstoque", valorTotal != null ? valorTotal : 0.0);

        return "index";
    }
}