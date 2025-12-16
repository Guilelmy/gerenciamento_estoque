package web.controle_estoque.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import web.controle_estoque.model.Empresa;
import web.controle_estoque.repository.EmpresaRepository;

@Controller
public class LoginController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String processarCadastro(Empresa empresa, Model model) {
        if (empresaRepository.existsByEmail(empresa.getEmail())) {
            model.addAttribute("erro", "Este e-mail já está cadastrado.");
            return "cadastro";
        }
        if (empresaRepository.existsByCnpj(empresa.getCnpj())) {
            model.addAttribute("erro", "Este CNPJ já está cadastrado.");
            return "cadastro";
        }
        empresa.setSenha(passwordEncoder.encode(empresa.getSenha()));
        empresaRepository.save(empresa);
        return "redirect:/login?cadastrado=true";
    }

}
