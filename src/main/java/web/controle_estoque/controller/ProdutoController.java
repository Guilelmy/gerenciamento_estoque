package web.controle_estoque.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import web.controle_estoque.model.Produto;
import web.controle_estoque.service.FornecedorService;
import web.controle_estoque.service.ProdutoService;

@Controller
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;
    private final FornecedorService fornecedorService;

    private final int ITENS_POR_PAGINA = 10;

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String termo,
            Model model) {
        return buscaPagina(page, termo, model, false);
    }

    @GetMapping("/busca")
    public String busca(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String termo,
            Model model) {
        return buscaPagina(page, termo, model, true);
    }

    private String buscaPagina(int page, String termo, Model model, boolean apenasFragmento) {
        Pageable pageable = PageRequest.of(page, ITENS_POR_PAGINA, Sort.by("nome"));
        Page<Produto> pageResultado = service.listar(pageable, termo);

        model.addAttribute("produtos", pageResultado);
        model.addAttribute("termoAtual", termo);

        if (apenasFragmento) {
            return "produtos/index :: tabela-produtos";
        } else {
            model.addAttribute("currentPage", "produtos");
            return "produtos/index";
        }
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", new Produto());
        model.addAttribute("listaFornecedores", fornecedorService.listarTodos());
        return "produtos/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Produto produto = service.buscarPorId(id);
        model.addAttribute("produto", produto);
        model.addAttribute("listaFornecedores", fornecedorService.listarTodos());
        return "produtos/form";
    }

    @PostMapping("/salvar")
    public String salvar(Produto produto, HttpServletResponse response, Model model) {
        boolean isNovo = (produto.getId() == null);
        try {
            service.salvar(produto); 
            String mensagem = isNovo ? "Produto cadastrado com sucesso!" : "Produto atualizado com sucesso!";
            dispararToast(response, "sucesso", mensagem);
            response.addHeader("HX-Push-Url", "/produtos");

            return buscaPagina(0, "", model, false);
        } catch (Exception e) {
            dispararToast(response, "erro", "Erro ao salvar: " + e.getMessage());
            return buscaPagina(0, "", model, false);
        }
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String termo,
            Model model,
            HttpServletResponse response) {

        boolean sucesso = service.desativar(id);

        if (sucesso) {
            dispararToast(response, "sucesso", "Produto desativado com sucesso!");
        } else {
            dispararToast(response, "erro", "Erro ao desativar ou produto não encontrado.");
        }

        return buscaPagina(page, termo, model, true);
    }

    private void dispararToast(HttpServletResponse response, String tipo, String mensagem) {
        String json = String.format("{\"mostrarMensagem\": {\"tipo\": \"%s\", \"mensagem\": \"%s\"}}", tipo, mensagem);
        response.addHeader("HX-Trigger", json);
    }
}