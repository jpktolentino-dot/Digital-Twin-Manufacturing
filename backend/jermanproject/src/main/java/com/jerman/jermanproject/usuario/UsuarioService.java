package com.jerman.jermanproject.usuario;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String LOCK_USUARIO_TABLE_SQL =
            "LOCK TABLE usuario IN EXCLUSIVE MODE";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        UsuarioEntity usuario = buscarUsuarioEntityPorId(id);
        return toResponse(usuario);
    }

    /**
     * Cria o único administrador pelo fluxo público de inicialização.
     * O bloqueio da tabela impede que duas requisições concorrentes
     * enxerguem simultaneamente o banco vazio e criem dois primeiros usuários.
     */
    @Transactional
    public UsuarioResponse criarAdministradorInicial(
            CadastroInicialRequest request
    ) {
        // PostgreSQL: o bloqueio permanece até o commit/rollback da transação.
        jdbcTemplate.execute(LOCK_USUARIO_TABLE_SQL);

        if (usuarioRepository.count() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O cadastro inicial já foi concluído. Solicite a criação de usuário a um administrador."
            );
        }

        return criarUsuario(
                request.nome(),
                request.email(),
                request.senha(),
                TipoUsuario.ADMINISTRADOR
        );
    }

    /** Cadastro normal: chamado apenas por administrador via SecurityConfig. */
    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        return criarUsuario(
                request.nome(),
                request.email(),
                request.senha(),
                request.tipo()
        );
    }

    private UsuarioResponse criarUsuario(
            String nome,
            String emailInformado,
            String senha,
            TipoUsuario tipo
    ) {
        String email = normalizarEmail(emailInformado);

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um usuário cadastrado com esse e-mail."
            );
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNome(nome.trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setTipo(tipo);
        usuario.setAtivo(true);

        UsuarioEntity salvo = usuarioRepository.save(usuario);
        return toResponse(salvo);
    }

    @Transactional
    public UsuarioResponse atualizar(
            Long id,
            UsuarioUpdateRequest request
    ) {
        UsuarioEntity usuario = buscarUsuarioEntityPorId(id);
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
        usuario.setTipo(request.tipoUsuario());

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
    public void desativar(Long id) {
        UsuarioEntity usuario = buscarUsuarioEntityPorId(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void ativar(Long id) {
        UsuarioEntity usuario = buscarUsuarioEntityPorId(id);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UsuarioEntity buscarUsuarioEntityPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));
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
