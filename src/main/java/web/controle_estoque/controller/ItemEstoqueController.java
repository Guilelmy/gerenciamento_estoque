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
import web.controle_estoque.service.ItemEstoqueService;
import web.controle_estoque.service.ProdutoService;

@Controller
@RequestMapping("/estoque")
@RequiredArgsConstructor
public class ItemEstoqueController {

    private final ItemEstoqueService service;
    private final ProdutoService produtoService;
    private final int ITENS_POR_PAGINA = 10;
    
    // REMOVIDO: private final Long EMPRESA_ID = 1L; (Causa do erro)

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
        Pageable pageable = PageRequest.of(page, ITENS_POR_PAGINA, Sort.by("produto.nome"));
        
        // CORREÇÃO: Removemos o ID da empresa, o service resolve sozinho
        Page<ItemEstoque> pageResultado = service.listar(pageable, termo);

        model.addAttribute("itensEstoque", pageResultado);
        model.addAttribute("termoAtual", termo);
        
        if (apenasFragmento) {
            return "estoque/index :: tabela-estoque";
        } else {
            model.addAttribute("currentPage", "estoque");
            return "estoque/index";
        }
    }
    
    // ... Mantenha o método novo() ...
    @GetMapping("/novo")
    public String novo(Model model) {
        // CORREÇÃO: Usar o service para listar
        model.addAttribute("produtos", produtoService.listar(Pageable.unpaged(), "").getContent());
        return "estoque/modais :: modal-novo";
    }

    @PostMapping("/salvar-novo")
    public String salvarNovo(@RequestParam Long produtoId,
                             @RequestParam Integer quantidadeInicial,
                             @RequestParam Integer quantidadeMinima,
                             HttpServletResponse response,
                             Model model) {
        try {
            // CORREÇÃO: Passamos apenas 3 argumentos (o service pega a empresa)
            service.criarNovoItem(produtoId, quantidadeInicial, quantidadeMinima);
            
            dispararToast(response, "sucesso", "Produto adicionado!");
            response.addHeader("HX-Push-Url", "/estoque"); 
            return buscaPagina(0, "", model, false);
        } catch (IllegalArgumentException e) {
            dispararToast(response, "erro", e.getMessage());
            return buscaPagina(0, "", model, true);
        }
    }
    
    // ... Resto do controller (movimentar, config) permanece igual ao último exemplo correto
    // ... Apenas certifique-se de que não está passando EMPRESA_ID em lugar nenhum
    
    private void dispararToast(HttpServletResponse response, String tipo, String mensagem) {
        String json = String.format("{\"mostrarMensagem\": {\"tipo\": \"%s\", \"mensagem\": \"%s\"}}", tipo, mensagem);
        response.addHeader("HX-Trigger", json);
    }
    
    // Métodos auxiliares para completar o código
    @GetMapping("/movimentacao/{id}")
    public String modalMovimentacao(@PathVariable Long id, @RequestParam String tipo, Model model) {
        model.addAttribute("item", service.buscarPorId(id));
        model.addAttribute("tipo", tipo);
        return "estoque/modais :: modal-movimentacao";
    }
    
    @PostMapping("/movimentar")
    public String realizarMovimentacao(@RequestParam Long itemId, @RequestParam String tipo, @RequestParam Integer quantidade, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "") String termo, HttpServletResponse response, Model model) {
        try {
            if ("entrada".equals(tipo)) service.realizarEntrada(itemId, quantidade);
            else service.realizarSaida(itemId, quantidade);
            dispararToast(response, "sucesso", "Movimentação realizada!");
        } catch (Exception e) {
            dispararToast(response, "erro", e.getMessage());
        }
        return buscaPagina(page, termo, model, true);
    }
    
    @GetMapping("/config/{id}")
    public String modalConfig(@PathVariable Long id, Model model) {
        model.addAttribute("item", service.buscarPorId(id));
        return "estoque/modais :: modal-config"; // Assumindo que o fragmento existe
    }

    @PostMapping("/salvar-config")
    public String salvarConfig(@RequestParam Long itemId, @RequestParam Integer quantidadeMinima, HttpServletResponse response, Model model) {
        service.salvarConfiguracao(itemId, quantidadeMinima);
        dispararToast(response, "sucesso", "Configuração salva!");
        return buscaPagina(0, "", model, true);
    }
}