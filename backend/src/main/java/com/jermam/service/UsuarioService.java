package com.jermam.service;

import com.jermam.dto.UsuarioRequest;
import com.jermam.dto.UsuarioResponse;
import com.jermam.entity.Usuario;
import com.jermam.enums.TipoUsuario;
import com.jermam.exception.RecursoNaoEncontradoException;
import com.jermam.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(TipoUsuario tipo) {
        List<Usuario> usuarios = tipo == null ? repository.findAll() : repository.findByTipo(tipo);
        return usuarios.stream().map(this::mapear).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) { return mapear(obter(id)); }

    public UsuarioResponse criar(UsuarioRequest request) {
        String email = request.loginEmail().trim().toLowerCase();
        if (repository.existsByLoginEmailIgnoreCase(email)) throw new IllegalArgumentException("Já existe um usuário com esse e-mail.");
        if (request.senha() == null || request.senha().length() < 6) throw new IllegalArgumentException("A senha deve possuir pelo menos 6 caracteres.");
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome().trim());
        usuario.setLoginEmail(email);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setTipo(request.tipo());
        usuario.setAtivo(request.ativo() == null || request.ativo());
        return mapear(repository.save(usuario));
    }

    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = obter(id);
        String email = request.loginEmail().trim().toLowerCase();
        if (repository.existsByLoginEmailIgnoreCaseAndIdNot(email, id)) throw new IllegalArgumentException("Já existe um usuário com esse e-mail.");
        usuario.setNome(request.nome().trim());
        usuario.setLoginEmail(email);
        usuario.setTipo(request.tipo());
        if (request.ativo() != null) usuario.setAtivo(request.ativo());
        if (request.senha() != null && !request.senha().isBlank()) {
            if (request.senha().length() < 6) throw new IllegalArgumentException("A senha deve possuir pelo menos 6 caracteres.");
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }
        return mapear(repository.save(usuario));
    }

    public void excluir(Long id) { repository.delete(obter(id)); }

    public Usuario obter(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    public Usuario obterPorEmail(String email) {
        return repository.findByLoginEmailIgnoreCase(email).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private UsuarioResponse mapear(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getLoginEmail(), u.getDataCadastro(), u.isAtivo(), u.getTipo());
    }
}
