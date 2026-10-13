package com.jermam.controller;

import com.jermam.dto.AuthRequest;
import com.jermam.dto.AuthResponse;
import com.jermam.entity.Usuario;
import com.jermam.repository.UsuarioRepository;
import com.jermam.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.loginEmail().trim().toLowerCase(), request.senha()));
            Usuario usuario = usuarioRepository.findByLoginEmailIgnoreCase(auth.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas."));
            return new AuthResponse(jwtService.gerarToken(usuario.getLoginEmail()), "Bearer", usuario.getId(), usuario.getNome(), usuario.getLoginEmail(), usuario.getTipo());
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByLoginEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));
        return new AuthResponse(null, "Bearer", usuario.getId(), usuario.getNome(), usuario.getLoginEmail(), usuario.getTipo());
    }
}
