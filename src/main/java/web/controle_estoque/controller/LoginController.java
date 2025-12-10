package web.controle_estoque.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import web.controle_estoque.model.Empresa;
import web.controle_estoque.repository.EmpresaRepository;

@Controller
public class LoginController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Tela de Login
    @GetMapping("/login")
    public String login() {
        return "login"; // Vai procurar login.html
    }

    // Tela de Cadastro
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "cadastro"; // Vai procurar cadastro.html
    }

    // Processar o Cadastro
    @PostMapping("/cadastro")
    public String processarCadastro(Empresa empresa, Model model) {
        // Verifica se já existe
        if (empresaRepository.existsByEmail(empresa.getEmail())) {
            model.addAttribute("erro", "Este e-mail já está cadastrado.");
            return "cadastro";
        }
        if (empresaRepository.existsByCnpj(empresa.getCnpj())) {
            model.addAttribute("erro", "Este CNPJ já está cadastrado.");
            return "cadastro";
        }

        // Criptografa a senha antes de salvar
        empresa.setSenha(passwordEncoder.encode(empresa.getSenha()));

        empresaRepository.save(empresa);

        return "redirect:/login?cadastrado=true";
    }

    
}
