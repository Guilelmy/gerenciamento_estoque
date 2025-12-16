package web.controle_estoque.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import web.controle_estoque.model.Fornecedor;
import web.controle_estoque.model.Produto;
import web.controle_estoque.service.FornecedorService;
import web.controle_estoque.service.ProdutoService;

import java.util.List;

@Controller
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService service;
    private final FornecedorService fornecedorService;
    private final int ITENS_POR_PAGINA = 10;

    // --- LISTAGEM E BUSCA PRINCIPAL ---

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "") String nome,
                        @RequestParam(defaultValue = "") String fornecedor,
                        Model model) {
        return buscaPagina(page, nome, fornecedor, model, false);
    }

    @GetMapping("/busca")
    public String busca(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "") String nome,
                        @RequestParam(defaultValue = "") String fornecedor,
                        Model model) {
        return buscaPagina(page, nome, fornecedor, model, true);
    }

    private String buscaPagina(int page, String nome, String fornecedor, Model model, boolean apenasFragmento) {
        // Ordenação: Preço Decrescente (Maior -> Menor)
        Pageable pageable = PageRequest.of(page, ITENS_POR_PAGINA, Sort.by(Sort.Direction.DESC, "preco"));
        
        // Chama o serviço com os dois filtros (Nome Produto e Nome Fornecedor)
        Page<Produto> pageResultado = service.listar(pageable, nome, fornecedor);

        model.addAttribute("produtos", pageResultado);
        
        // Devolve os termos para a View manter os inputs preenchidos
        model.addAttribute("nomeAtual", nome);
        model.addAttribute("fornecedorAtual", fornecedor);

        if (apenasFragmento) {
            return "produtos/index :: tabela-produtos";
        } else {
            model.addAttribute("currentPage", "produtos");
            return "produtos/index";
        }
    }

    // --- FORMULÁRIOS (NOVO / EDITAR) ---

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produto", new Produto());
        // Não precisamos mais carregar a lista completa de fornecedores aqui
        // pois usamos o autocomplete dinâmico.
        return "produtos/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Produto produto = service.buscarPorId(id);
        model.addAttribute("produto", produto);
        return "produtos/form";
    }

    // --- AUTOCOMPLETE DE FORNECEDORES (NOVO MÉTODO) ---
    
    @GetMapping("/busca-fornecedores")
    @ResponseBody // Retorna HTML puro, sem layout
    public String buscaFornecedoresModal(@RequestParam(defaultValue = "") String termo) {
        // Busca os 5 primeiros fornecedores pelo nome ou CNPJ
        var page = fornecedorService.listar(PageRequest.of(0, 5), termo);
        List<Fornecedor> lista = page.getContent();

        StringBuilder html = new StringBuilder();
        html.append("<div class='bg-white border border-gray-300 rounded-md shadow-2xl max-h-60 overflow-y-auto mt-1 w-full'>");

        if (lista.isEmpty()) {
            html.append("<div class='p-3 text-gray-500 text-sm text-center'>Nenhum fornecedor encontrado.</div>");
        } else {
            for (Fornecedor f : lista) {
                // Escapar aspas para segurança do JavaScript inline
                String nomeSeguro = f.getNome().replace("'", "\\'");
                
                html.append("<div class='px-4 py-2 hover:bg-blue-100 cursor-pointer border-b border-gray-100 transition-colors' ")
                    .append("data-id='").append(f.getId()).append("' ")
                    .append("data-nome='").append(nomeSeguro).append("' ")
                    .append("onclick='window.selecionarFornecedor(this)'>")
                    .append("<span class='font-bold text-gray-800 block pointer-events-none'>").append(f.getNome()).append("</span>")
                    .append("<span class='text-xs text-gray-500 block pointer-events-none'>CNPJ: ").append(f.getCnpj()).append("</span>")
                    .append("</div>");
            }
        }
        html.append("</div>");

        return html.toString();
    }

    // --- AÇÕES (SALVAR / EXCLUIR) ---

    @PostMapping("/salvar")
    public String salvar(@Valid Produto produto, BindingResult result, HttpServletResponse response, Model model) {
        // 1. Se houver erro de validação
        if (result.hasErrors()) {
            return "produtos/form";
        }

        try {
            boolean isNovo = (produto.getId() == null);
            service.salvar(produto); 
            
            String mensagem = isNovo ? "Produto cadastrado!" : "Produto atualizado!";
            dispararToast(response, "sucesso", mensagem);
            response.addHeader("HX-Push-Url", "/produtos");

            // 2. Sucesso: Recarrega a lista limpa (sem filtros)
            return buscaPagina(0, "", "", model, false);
            
        } catch (Exception e) {
            // 3. Erro de negócio
            result.reject("global", "Erro ao salvar: " + e.getMessage());
            return "produtos/form";
        }
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id, 
                          @RequestParam(defaultValue = "0") int page, 
                          @RequestParam(defaultValue = "") String nome,
                          @RequestParam(defaultValue = "") String fornecedor,
                          Model model, 
                          HttpServletResponse response) {
        
        boolean sucesso = service.desativar(id);
        
        if (sucesso) dispararToast(response, "sucesso", "Produto desativado!");
        else dispararToast(response, "erro", "Erro ao desativar.");
        
        // Retorna a página atual mantendo os filtros
        return buscaPagina(page, nome, fornecedor, model, true);
    }

    private void dispararToast(HttpServletResponse response, String tipo, String mensagem) {
        String json = String.format("{\"mostrarMensagem\": {\"tipo\": \"%s\", \"mensagem\": \"%s\"}}", tipo, mensagem);
        response.addHeader("HX-Trigger", json);
    }
}