package com.jerman.jermanproject.usuario;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um usuário cadastrado com esse e-mail."
            );
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(
                passwordEncoder.encode(request.senha())
        );
        usuario.setTipo(TipoUsuario.OPERADOR);
        usuario.setAtivo(true);

        UsuarioEntity salvo = usuarioRepository.save(usuario);

        return toResponse(salvo);
    }

    @Transactional
    public UsuarioResponse atualizar(
            Long id,
            UsuarioUpdateRequest request
    ) {
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        String email = normalizarEmail(request.email());

        if (usuarioRepository
                .existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O e-mail informado já pertence a outro usuário."
            );
        }

        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setAtivo(request.ativo());

        if (request.senha() != null
                && !request.senha().isBlank()) {
            usuario.setSenhaHash(
                    passwordEncoder.encode(request.senha())
            );
        }

        UsuarioEntity atualizado = usuarioRepository.save(usuario);

        return toResponse(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        usuarioRepository.delete(usuario);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UsuarioResponse toResponse(UsuarioEntity usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTipo(),
                usuario.isAtivo()
        );
    }
}