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
import web.controle_estoque.service.FornecedorService;

@Controller
@RequestMapping("/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService service;
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
        Fornecedor fornecedor = service.buscarPorId(id);
        model.addAttribute("fornecedor", fornecedor);
        return "fornecedores/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid Fornecedor fornecedor, BindingResult result, HttpServletResponse response, Model model) {
        
        if (result.hasErrors()) {
            return "fornecedores/form"; 
        }

        try {
            boolean isNovo = (fornecedor.getId() == null);
            service.salvar(fornecedor);
            String mensagem = isNovo ? "Fornecedor cadastrado!" : "Fornecedor atualizado!";
            dispararToast(response, "sucesso", mensagem);
            response.addHeader("HX-Push-Url", "/fornecedores");
            return buscaPagina(0, "", model, false);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().toLowerCase().contains("cnpj")) {
                result.rejectValue("cnpj", "error.fornecedor", e.getMessage());
            } else {
                result.reject("global", e.getMessage());
            }
            return "fornecedores/form";
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
    
    private void dispararToast(HttpServletResponse response, String tipo, String mensagem) {
        String json = String.format("{\"mostrarMensagem\": {\"tipo\": \"%s\", \"mensagem\": \"%s\"}}", tipo, mensagem);
        response.addHeader("HX-Trigger", json);
    }
}