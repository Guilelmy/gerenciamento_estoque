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
import web.controle_estoque.model.Fornecedor;
import web.controle_estoque.service.FornecedorService;

import java.util.List;

@Controller
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService service; // Injeta o Service, não o Repository
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
        
        // O Service já pega a empresa logada internamente
        Page<Fornecedor> pageResultado = service.listar(pageable, termo);

        model.addAttribute("fornecedores", pageResultado);
        model.addAttribute("termoAtual", termo);

        if (apenasFragmento) {
            return "fornecedores/index :: tabela-fornecedores";
        } else {
            model.addAttribute("currentPage", "fornecedores");
            return "fornecedores/index";
        }
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "fornecedores/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        // Service valida se o ID pertence à empresa
        Fornecedor fornecedor = service.buscarPorId(id);
        model.addAttribute("fornecedor", fornecedor);
        return "fornecedores/form";
    }

    @PostMapping("/salvar")
    public String salvar(Fornecedor fornecedor, HttpServletResponse response, Model model) {
        boolean isNovo = (fornecedor.getId() == null);
        
        try {
            service.salvar(fornecedor); // Service cuida do vínculo com a empresa
            
            String mensagem = isNovo ? "Fornecedor cadastrado com sucesso!" : "Fornecedor atualizado com sucesso!";
            dispararToast(response, "sucesso", mensagem);
            response.addHeader("HX-Push-Url", "/fornecedores");
            
            return buscaPagina(0, "", model, false);
            
        } catch (IllegalArgumentException e) {
            // Captura erro de CNPJ duplicado
            dispararToast(response, "erro", e.getMessage());
            // Retorna o form com os dados preenchidos e o erro (aqui simplificado retorna a lista)
            // O ideal seria retornar o fragmento do form com erro, mas para manter o fluxo atual:
            return buscaPagina(0, "", model, false);
        }
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "") String termo,
                          Model model,
                          HttpServletResponse response) {

        try {
            service.excluir(id);
            dispararToast(response, "sucesso", "Fornecedor removido!");
        } catch (Exception e) {
            dispararToast(response, "erro", "Erro ao excluir: " + e.getMessage());
        }

        return buscaPagina(page, termo, model, true);
    }

    @GetMapping("/busca-modal")
    public String buscaModal(@RequestParam("termo") String termo, Model model) {
        Pageable limit = PageRequest.of(0, 10);
        // Usamos o service para garantir filtro por empresa
        List<Fornecedor> resultados = service.listar(limit, termo).getContent();
        
        model.addAttribute("resultadosFornecedor", resultados);
        return "produtos/fragments :: lista-fornecedores";
    }

    private void dispararToast(HttpServletResponse response, String tipo, String mensagem) {
        String json = String.format("{\"mostrarMensagem\": {\"tipo\": \"%s\", \"mensagem\": \"%s\"}}", tipo, mensagem);
        response.addHeader("HX-Trigger", json);
    }
}