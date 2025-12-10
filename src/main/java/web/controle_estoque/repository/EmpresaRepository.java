package web.controle_estoque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import web.controle_estoque.model.Empresa;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    
    // Necessário para o Spring Security fazer o login
    Optional<Empresa> findByEmail(String email);
    
    // Para validar cadastro duplicado
    boolean existsByCnpj(String cnpj);
    boolean existsByEmail(String email);
}