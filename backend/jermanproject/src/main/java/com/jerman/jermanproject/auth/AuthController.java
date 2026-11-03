package com.jerman.jermanproject.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.security.JwtService;
import com.jerman.jermanproject.usuario.CadastroInicialRequest;
import com.jerman.jermanproject.usuario.UsuarioEntity;
import com.jerman.jermanproject.usuario.UsuarioRepository;
import com.jerman.jermanproject.usuario.UsuarioResponse;
import com.jerman.jermanproject.usuario.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    /** Cria o primeiro administrador somente enquanto a tabela de usuários estiver vazia. */
    @PostMapping("/setup")
    public ResponseEntity<UsuarioResponse> setup(
            @Valid @RequestBody CadastroInicialRequest request
    ) {
        UsuarioResponse administrador =
                usuarioService.criarAdministradorInicial(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(administrador);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().trim(), request.senha()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "E-mail ou senha inválidos."
            );
        }

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        UsuarioEntity usuario = usuarioRepository
                .findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow();

        String token = jwtService.gerarToken(usuario.getEmail());

        return ResponseEntity.ok(new LoginResponse(
                token,
                "Bearer",
                expirationMs / 1000,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipo()
        ));
    }
}
