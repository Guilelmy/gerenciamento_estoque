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
import web.controle_estoque.model.ItemEstoque;
import web.controle_estoque.model.Produto;
import web.controle_estoque.service.ItemEstoqueService;
import web.controle_estoque.service.ProdutoService;

import java.util.List;

@Controller
@RequestMapping("/estoque")
@RequiredArgsConstructor
public class ItemEstoqueController {

    private final ItemEstoqueService service;
    private final ProdutoService produtoService;
    private final int ITENS_POR_PAGINA = 10;

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "") String termo,
                        @RequestParam(defaultValue = "false") boolean criticos,
                        Model model) {
        return buscaPagina(page, termo, criticos, model, false);
    }

    @GetMapping("/busca")
    public String busca(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "") String termo,
                        @RequestParam(defaultValue = "false") boolean criticos,
                        Model model) {
        return buscaPagina(page, termo, criticos, model, true);
    }

    private String buscaPagina(int page, String termo, boolean criticos, Model model, boolean apenasFragmento) {
        Pageable pageable = PageRequest.of(page, ITENS_POR_PAGINA, Sort.by("produto.nome"));
        
        Page<ItemEstoque> pageResultado = service.listar(pageable, termo, criticos);

        model.addAttribute("itensEstoque", pageResultado);
        model.addAttribute("termoAtual", termo);
        model.addAttribute("filtroCriticos", criticos);

        if (apenasFragmento) {
            return "estoque/index :: tabela-estoque";
        } else {
            model.addAttribute("currentPage", "estoque");
            return "estoque/index";
        }
    }

    // --- MODAL NOVO ---
    @GetMapping("/novo")
    public String novo(Model model) {
        return "estoque/modais :: modal-novo";
    }

    @PostMapping("/salvar-novo")
    public String salvarNovo(@RequestParam Long produtoId,
                             @RequestParam Integer quantidadeInicial,
                             @RequestParam Integer quantidadeMinima,
                             HttpServletResponse response,
                             Model model) {
        try {
            service.criarNovoItem(produtoId, quantidadeInicial, quantidadeMinima);
            dispararToast(response, "sucesso", "Produto adicionado!");
            response.addHeader("HX-Push-Url", "/estoque");
            
            // Retorna para a lista completa ao criar novo item
            return buscaPagina(0, "", false, model, true);
            
        } catch (IllegalArgumentException e) {
            dispararToast(response, "erro", e.getMessage());
            return buscaPagina(0, "", false, model, true);
        }
    }

    // --- BUSCA DINÂMICA NO MODAL (HTML PURO) ---
    @GetMapping("/busca-produtos")
    @ResponseBody
    public String buscaProdutosModal(@RequestParam(defaultValue = "") String termo) {
        // Busca produtos pelo nome (termo), ignorando filtro de fornecedor ("")
        var page = produtoService.listar(PageRequest.of(0, 5), termo, "");
        List<Produto> lista = page.getContent();

        StringBuilder html = new StringBuilder();
        html.append("<div class='bg-white border border-gray-300 rounded-md shadow-2xl max-h-60 overflow-y-auto mt-1'>");

        if (lista.isEmpty()) {
            html.append("<div class='p-3 text-gray-500 text-sm text-center'>Nenhum produto encontrado.</div>");
        } else {
            for (Produto p : lista) {
                // Escapar aspas simples para segurança do JS
                String nomeSeguro = p.getNome().replace("'", "\\'");
                
                html.append("<div class='px-4 py-2 hover:bg-blue-100 cursor-pointer border-b border-gray-100 transition-colors' ")
                    .append("data-id='").append(p.getId()).append("' ")
                    .append("data-nome='").append(nomeSeguro).append("' ")
                    .append("onclick='window.selecionarProduto(this)'>")
                    .append("<span class='font-bold text-gray-800 block pointer-events-none'>").append(p.getNome()).append("</span>")
                    .append("<span class='text-xs text-gray-500 block pointer-events-none'>").append(p.getFornecedor().getNome()).append("</span>")
                    .append("</div>");
            }
        }
        html.append("</div>");

        return html.toString();
    }

    // --- MOVIMENTAÇÃO ---
    @GetMapping("/movimentacao/{id}")
    public String modalMovimentacao(@PathVariable Long id, 
                                    @RequestParam String tipo,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "") String termo,
                                    @RequestParam(defaultValue = "false") boolean criticos,
                                    Model model) {
        model.addAttribute("item", service.buscarPorId(id));
        model.addAttribute("tipo", tipo);
        
        model.addAttribute("page", page);
        model.addAttribute("termo", termo);
        model.addAttribute("criticos", criticos);
        
        return "estoque/modais :: modal-movimentacao";
    }

    @PostMapping("/movimentar")
    public String realizarMovimentacao(@RequestParam Long itemId, 
                                       @RequestParam String tipo,
                                       @RequestParam Integer quantidade, 
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "") String termo,
                                       @RequestParam(defaultValue = "false") boolean criticos,
                                       HttpServletResponse response, Model model) {
        try {
            if ("entrada".equals(tipo))
                service.realizarEntrada(itemId, quantidade);
            else
                service.realizarSaida(itemId, quantidade);
            dispararToast(response, "sucesso", "Movimentação realizada!");
        } catch (Exception e) {
            dispararToast(response, "erro", e.getMessage());
        }
        return buscaPagina(page, termo, criticos, model, true);
    }

    // --- CONFIGURAÇÃO ---
    @GetMapping("/config/{id}")
    public String modalConfig(@PathVariable Long id,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "") String termo,
                              @RequestParam(defaultValue = "false") boolean criticos,
                              Model model) {
        model.addAttribute("item", service.buscarPorId(id));
        model.addAttribute("page", page);
        model.addAttribute("termo", termo);
        model.addAttribute("criticos", criticos);
        return "estoque/modais :: modal-config";
    }

    @PostMapping("/salvar-config")
    public String salvarConfig(@RequestParam Long itemId, 
                               @RequestParam Integer quantidadeMinima,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "") String termo,
                               @RequestParam(defaultValue = "false") boolean criticos,
                               HttpServletResponse response, Model model) {
        service.salvarConfiguracao(itemId, quantidadeMinima);
        dispararToast(response, "sucesso", "Configuração salva!");
        return buscaPagina(page, termo, criticos, model, true);
    }

    private void dispararToast(HttpServletResponse response, String tipo, String mensagem) {
        String json = String.format("{\"mostrarMensagem\": {\"tipo\": \"%s\", \"mensagem\": \"%s\"}}", tipo, mensagem);
        response.addHeader("HX-Trigger", json);
    }
}