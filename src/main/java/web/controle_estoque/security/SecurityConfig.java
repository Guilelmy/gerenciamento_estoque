package web.controle_estoque.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests((requests) -> requests
            // ESTAS ROTAS PRECISAM SER PÚBLICAS
            .requestMatchers("/login", "/cadastro", "/css/**", "/js/**", "/images/**").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin((form) -> form
            .loginPage("/login") // URL da sua controller
            .defaultSuccessUrl("/", true) // Redireciona para home após sucesso
            .permitAll() // Importante!
        )
        .logout((logout) -> logout
    .logoutUrl("/logout")
    .logoutSuccessUrl("/login")
    .permitAll()
);

    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}