
package com.jerman.jermanproject.usuario;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveListarUsuariosAtivosEInativos() {
        UsuarioEntity ativo = novoUsuario(
                1L,
                "ativo@exemplo.com"
        );

        UsuarioEntity inativo = novoUsuario(
                2L,
                "inativo@exemplo.com"
        );
        inativo.setAtivo(false);

        when(usuarioRepository.findAll())
                .thenReturn(List.of(ativo, inativo));

        List<UsuarioResponse> resposta = usuarioService.listar();

        assertEquals(2, resposta.size());
        assertTrue(resposta.get(0).ativo());
        assertFalse(resposta.get(1).ativo());
    }

    @Test
    void deveCadastrarUsuarioComSenhaCodificada() {
        UsuarioRequest request = new UsuarioRequest(
                "Operador Teste",
                "OPERADOR@EXEMPLO.COM",
                "Senha123!"
        );

        when(usuarioRepository
                .existsByEmailIgnoreCase("operador@exemplo.com"))
                .thenReturn(false);

        // Já existe um usuário, então este será OPERADOR.
        when(usuarioRepository.count()).thenReturn(1L);

        when(passwordEncoder.encode("Senha123!"))
                .thenReturn("hash-da-senha");

        when(usuarioRepository.save(any(UsuarioEntity.class)))
                .thenAnswer(invocation -> {
                    UsuarioEntity usuario = invocation.getArgument(0);
                    usuario.setId(2L);
                    return usuario;
                });

        UsuarioResponse resposta = usuarioService.criar(request);

        assertEquals(2L, resposta.id());
        assertEquals("Operador Teste", resposta.nome());
        assertEquals("operador@exemplo.com", resposta.email());
        assertEquals(TipoUsuario.OPERADOR, resposta.tipo());
        assertTrue(resposta.ativo());

        verify(passwordEncoder).encode("Senha123!");
        verify(usuarioRepository).save(any(UsuarioEntity.class));
    }

    @Test
    void deveCadastrarPrimeiroUsuarioComoAdministrador() {
        UsuarioRequest request = new UsuarioRequest(
                "Administrador",
                "admin@exemplo.com",
                "Senha123!"
        );

        when(usuarioRepository
                .existsByEmailIgnoreCase("admin@exemplo.com"))
                .thenReturn(false);

        when(usuarioRepository.count()).thenReturn(0L);

        when(passwordEncoder.encode("Senha123!"))
                .thenReturn("hash-da-senha");

        when(usuarioRepository.save(any(UsuarioEntity.class)))
                .thenAnswer(invocation -> {
                    UsuarioEntity usuario = invocation.getArgument(0);
                    usuario.setId(1L);
                    return usuario;
                });

        UsuarioResponse resposta = usuarioService.criar(request);

        assertEquals(TipoUsuario.ADMINISTRADOR, resposta.tipo());
        assertTrue(resposta.ativo());

        verify(usuarioRepository).count();
        verify(passwordEncoder).encode("Senha123!");
    }

    @Test
    void naoDeveCadastrarEmailDuplicado() {
        UsuarioRequest request = new UsuarioRequest(
                "Outro Operador",
                "operador@exemplo.com",
                "Senha123!"
        );

        when(usuarioRepository
                .existsByEmailIgnoreCase("operador@exemplo.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.criar(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(usuarioRepository, never())
                .save(any(UsuarioEntity.class));
    }

    @Test
    void deveBuscarUsuarioPorId() {
        UsuarioEntity existente = novoUsuario(
                1L,
                "operador@exemplo.com"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        UsuarioResponse resposta = usuarioService.buscarPorId(1L);

        assertEquals(1L, resposta.id());
        assertEquals("operador@exemplo.com", resposta.email());
        assertEquals(TipoUsuario.OPERADOR, resposta.tipo());
        assertTrue(resposta.ativo());
    }

    @Test
    void deveRetornarNotFoundQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.buscarPorId(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void deveAtualizarUsuarioSemAlterarSenhaQuandoNaoInformada() {
        UsuarioEntity existente = novoUsuario(
                1L,
                "operador@exemplo.com"
        );

        UsuarioUpdateRequest request = new UsuarioUpdateRequest(
                "Operador Atualizado",
                "novo@exemplo.com",
                "",
                TipoUsuario.OPERADOR,
                false
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        when(usuarioRepository
                .existsByEmailIgnoreCaseAndIdNot(
                        "novo@exemplo.com",
                        1L
                ))
                .thenReturn(false);

        when(usuarioRepository.save(any(UsuarioEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse resposta =
                usuarioService.atualizar(1L, request);

        assertEquals("Operador Atualizado", resposta.nome());
        assertEquals("novo@exemplo.com", resposta.email());
        assertFalse(resposta.ativo());

        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository).save(existente);
    }

    @Test
    void naoDeveAtualizarParaEmailDeOutroUsuario() {
        UsuarioEntity existente = novoUsuario(
                1L,
                "atual@exemplo.com"
        );

        UsuarioUpdateRequest request = new UsuarioUpdateRequest(
                "Operador",
                "outro@exemplo.com",
                "",
                TipoUsuario.OPERADOR,
                true
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        when(usuarioRepository
                .existsByEmailIgnoreCaseAndIdNot(
                        "outro@exemplo.com",
                        1L
                ))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.atualizar(1L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(usuarioRepository, never())
                .save(any(UsuarioEntity.class));
    }

    @Test
    void deveDesativarUsuarioSemExcluirFisicamente() {
        UsuarioEntity existente = novoUsuario(
                1L,
                "operador@exemplo.com"
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        when(usuarioRepository.save(any(UsuarioEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.desativar(1L);

        assertFalse(existente.isAtivo());

        verify(usuarioRepository).save(existente);
        verify(usuarioRepository, never()).delete(existente);
    }

    @Test
    void deveReativarUsuario() {
        UsuarioEntity existente = novoUsuario(
                1L,
                "operador@exemplo.com"
        );
        existente.setAtivo(false);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        when(usuarioRepository.save(any(UsuarioEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.ativar(1L);

        assertTrue(existente.isAtivo());

        verify(usuarioRepository).save(existente);
    }

    @Test
    void naoDeveDesativarUsuarioInexistente() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.desativar(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(usuarioRepository, never())
                .save(any(UsuarioEntity.class));
    }

    @Test
    void naoDeveAtivarUsuarioInexistente() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.ativar(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(usuarioRepository, never())
                .save(any(UsuarioEntity.class));
    }

    private UsuarioEntity novoUsuario(Long id, String email) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(id);
        usuario.setNome("Operador");
        usuario.setEmail(email);
        usuario.setSenhaHash("hash-existente");
        usuario.setTipo(TipoUsuario.OPERADOR);
        usuario.setAtivo(true);

        return usuario;
    }
}
