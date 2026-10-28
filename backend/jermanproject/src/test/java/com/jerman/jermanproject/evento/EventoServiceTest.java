
package com.jerman.jermanproject.evento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.maquina.MaquinaRepository;
import com.jerman.jermanproject.ordemproducao.OrdemProducaoEntity;
import com.jerman.jermanproject.ordemproducao.OrdemProducaoRepository;
import com.jerman.jermanproject.usuario.TipoUsuario;
import com.jerman.jermanproject.usuario.UsuarioEntity;
import com.jerman.jermanproject.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private MaquinaRepository maquinaRepository;

    @Mock
    private OrdemProducaoRepository ordemProducaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EventoService eventoService;

    private MaquinaEntity maquina;
    private OrdemProducaoEntity ordem;
    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        maquina = new MaquinaEntity();
        maquina.setId(10L);
        maquina.setNome("Torno CNC");
        maquina.setCodigo("MAQ-01");

        ordem = new OrdemProducaoEntity();
        ordem.setId(20L);
        ordem.setNumero("OP-2026-001");
        ordem.setMaquinas(
                new LinkedHashSet<>(Set.of(maquina))
        );

        usuario = new UsuarioEntity();
        usuario.setId(30L);
        usuario.setNome("Operador de teste");
        usuario.setEmail("operador@exemplo.com");
        usuario.setTipo(TipoUsuario.OPERADOR);
        usuario.setAtivo(true);
    }

    private EventoRequest criarRequest(
            Long maquinaId,
            Long ordemProducaoId,
            Long usuarioId
    ) {
        return new EventoRequest(
                "FALHA",
                "Falha no motor da máquina",
                new BigDecimal("15.50"),
                null,
                maquinaId,
                ordemProducaoId,
                usuarioId
        );
    }

    private EventoEntity criarEventoExistente() {
        EventoEntity evento = new EventoEntity();
        evento.setId(1L);
        evento.setCategoria("FALHA");
        evento.setDescricao("Falha no motor da máquina");
        evento.setImpactoEstimado(new BigDecimal("15.50"));
        evento.setDataHora(
                LocalDateTime.of(2026, 10, 28, 10, 30)
        );
        evento.setMaquina(maquina);
        evento.setOrdemProducao(ordem);
        evento.setUsuario(usuario);

        return evento;
    }

    @Test
    void deveCriarEventoAssociadoAMaquinaOrdemEUsuario() {
        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(ordemProducaoRepository.findById(20L))
                .thenReturn(Optional.of(ordem));

        when(usuarioRepository.findById(30L))
                .thenReturn(Optional.of(usuario));

        when(eventoRepository.save(any(EventoEntity.class)))
                .thenAnswer(invocation -> {
                    EventoEntity evento = invocation.getArgument(0);
                    evento.setId(1L);
                    return evento;
                });

        EventoResponse resposta = eventoService.criar(
                criarRequest(10L, 20L, 30L)
        );

        assertEquals(1L, resposta.id());
        assertEquals("FALHA", resposta.categoria());
        assertEquals("Torno CNC", resposta.maquinaNome());
        assertEquals(10L, resposta.maquinaId());
        assertEquals(20L, resposta.ordemProducaoId());
        assertEquals("OP-2026-001", resposta.numeroOrdem());
        assertEquals(30L, resposta.usuarioId());
        assertEquals("Operador de teste", resposta.usuarioNome());
        assertEquals(
                new BigDecimal("15.50"),
                resposta.impactoEstimado()
        );
        assertNotNull(resposta.dataHora());

        verify(eventoRepository).save(any(EventoEntity.class));
    }

    @Test
    void deveRegistrarDataHoraAutomaticamenteQuandoNaoInformada() {
        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(eventoRepository.save(any(EventoEntity.class)))
                .thenAnswer(invocation -> {
                    EventoEntity evento = invocation.getArgument(0);
                    evento.setId(2L);
                    return evento;
                });

        LocalDateTime inicio = LocalDateTime.now();

        EventoResponse resposta = eventoService.criar(
                criarRequest(10L, null, null)
        );

        LocalDateTime fim = LocalDateTime.now();

        assertNotNull(resposta.dataHora());
        assertFalse(resposta.dataHora().isBefore(inicio));
        assertFalse(resposta.dataHora().isAfter(fim));

        assertNull(resposta.ordemProducaoId());
        assertNull(resposta.numeroOrdem());
        assertNull(resposta.usuarioId());
        assertNull(resposta.usuarioNome());
    }

    @Test
    void deveRegistrarEventoComDataHoraInformada() {
        LocalDateTime dataHora = LocalDateTime.of(
                2026, 10, 20, 14, 30
        );

        EventoRequest request = new EventoRequest(
                "MANUTENCAO",
                "Manutenção registrada",
                null,
                dataHora,
                10L,
                null,
                null
        );

        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(eventoRepository.save(any(EventoEntity.class)))
                .thenAnswer(invocation -> {
                    EventoEntity evento = invocation.getArgument(0);
                    evento.setId(3L);
                    return evento;
                });

        EventoResponse resposta = eventoService.criar(request);

        assertEquals(dataHora, resposta.dataHora());
        assertNull(resposta.impactoEstimado());
    }

    @Test
    void naoDeveCriarEventoParaMaquinaInexistente() {
        when(maquinaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.criar(
                        criarRequest(99L, null, null)
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(eventoRepository, never())
                .save(any(EventoEntity.class));

        verifyNoInteractions(
                ordemProducaoRepository,
                usuarioRepository
        );
    }

    @Test
    void naoDeveCriarEventoParaOrdemInexistente() {
        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(ordemProducaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.criar(
                        criarRequest(10L, 99L, null)
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(eventoRepository, never())
                .save(any(EventoEntity.class));

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void naoDeveAssociarEventoAMaquinaForaDaOrdem() {
        ordem.setMaquinas(new LinkedHashSet<>());

        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(ordemProducaoRepository.findById(20L))
                .thenReturn(Optional.of(ordem));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.criar(
                        criarRequest(10L, 20L, null)
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(eventoRepository, never())
                .save(any(EventoEntity.class));

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void naoDeveAssociarEventoAUsuarioInexistente() {
        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.criar(
                        criarRequest(10L, null, 99L)
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(eventoRepository, never())
                .save(any(EventoEntity.class));
    }

    @Test
    void deveBuscarEventoPorId() {
        when(eventoRepository.findById(1L))
                .thenReturn(Optional.of(criarEventoExistente()));

        EventoResponse resposta = eventoService.buscarPorId(1L);

        assertEquals(1L, resposta.id());
        assertEquals("FALHA", resposta.categoria());
        assertEquals(10L, resposta.maquinaId());
        assertEquals(20L, resposta.ordemProducaoId());
    }

    @Test
    void naoDeveBuscarEventoInexistente() {
        when(eventoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.buscarPorId(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void deveListarEventosPorMaquina() {
        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(eventoRepository
                .findAllByMaquina_IdOrderByDataHoraDesc(10L))
                .thenReturn(List.of(criarEventoExistente()));

        List<EventoResponse> resultado =
                eventoService.listarPorMaquina(10L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).maquinaId());

        verify(eventoRepository)
                .findAllByMaquina_IdOrderByDataHoraDesc(10L);
    }

    @Test
    void deveListarEventosPorOrdemProducao() {
        when(ordemProducaoRepository.findById(20L))
                .thenReturn(Optional.of(ordem));

        when(eventoRepository
                .findAllByOrdemProducao_IdOrderByDataHoraDesc(20L))
                .thenReturn(List.of(criarEventoExistente()));

        List<EventoResponse> resultado =
                eventoService.listarPorOrdemProducao(20L);

        assertEquals(1, resultado.size());
        assertEquals(20L, resultado.get(0).ordemProducaoId());

        verify(eventoRepository)
                .findAllByOrdemProducao_IdOrderByDataHoraDesc(20L);
    }

    @Test
    void naoDeveListarEventosDeMaquinaInexistente() {
        when(maquinaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.listarPorMaquina(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(eventoRepository, never())
                .findAllByMaquina_IdOrderByDataHoraDesc(99L);
    }

    @Test
    void naoDeveListarEventosDeOrdemInexistente() {
        when(ordemProducaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> eventoService.listarPorOrdemProducao(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(eventoRepository, never())
                .findAllByOrdemProducao_IdOrderByDataHoraDesc(99L);
    }
}
