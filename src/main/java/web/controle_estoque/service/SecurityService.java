package web.controle_estoque.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import web.controle_estoque.model.Empresa;
import web.controle_estoque.security.EmpresaUserDetails;

@Service
public class SecurityService {

    public Empresa getEmpresaLogada() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof EmpresaUserDetails) {
            return ((EmpresaUserDetails) principal).getEmpresa();
        }
        throw new RuntimeException("Nenhuma empresa logada.");
    }
}