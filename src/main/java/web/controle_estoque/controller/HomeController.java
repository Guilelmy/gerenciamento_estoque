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
        model.addAttribute("totalProdutos", produtoService.contarAtivos());
        model.addAttribute("totalFornecedores", fornecedorService.contarAtivos());
        model.addAttribute("totalItensCriticos", itemEstoqueService.contarCriticos());
        Double valorTotal = itemEstoqueService.valorTotalEstoque();
        model.addAttribute("valorTotalEstoque", valorTotal != null ? valorTotal : 0.0);
        return "index";
    }
}