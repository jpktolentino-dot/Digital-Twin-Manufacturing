package com.jerman.jermanproject.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // Documentação disponível durante o desenvolvimento
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // Permite criar a primeira conta de operador
                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/usuarios"
                ).permitAll()

                // Gerenciamento de usuários só para administradores
                .requestMatchers(
                    "/api/usuarios",
                    "/api/usuarios/**"
                ).hasRole("ADMINISTRADOR")

                // Os demais endpoints exigem autenticação
                .anyRequest().authenticated()
            )

            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}