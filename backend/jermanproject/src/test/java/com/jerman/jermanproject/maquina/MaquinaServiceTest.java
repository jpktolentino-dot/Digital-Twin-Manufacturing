
package com.jerman.jermanproject.maquina;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.setor.SetorEntity;
import com.jerman.jermanproject.setor.SetorRepository;

@ExtendWith(MockitoExtension.class)
class MaquinaServiceTest {

    @Mock
    private MaquinaRepository maquinaRepository;

    @Mock
    private SetorRepository setorRepository;

    @InjectMocks
    private MaquinaService maquinaService;

    private SetorEntity setor;
    private MaquinaEntity maquina;

    @BeforeEach
    void setUp() {
        setor = new SetorEntity();
        setor.setId(1L);
        setor.setNome("Usinagem");
        setor.setCodigo("USI");
        setor.setAtivo(true);

        maquina = new MaquinaEntity();
        maquina.setId(10L);
        maquina.setNome("Torno CNC");
        maquina.setCodigo("MAQ-01");
        maquina.setEspecificacoes("Torno para peças metálicas");
        maquina.setEstadoAtual(EstadoMaquina.PARADA);
        maquina.setSetor(setor);
        maquina.setOperadorId(2L);
        maquina.setAtivo(true);
    }

    private MaquinaRequest criarRequest(
            String nome,
            String codigo,
            Long setorId,
            Long operadorId
    ) {
        return new MaquinaRequest(
                nome,
                codigo,
                "Especificações de teste",
                EstadoMaquina.PARADA,
                setorId,
                operadorId
        );
    }

    @Test
    void deveListarTodasAsMaquinas() {
        when(maquinaRepository.findAll())
                .thenReturn(List.of(maquina));

        List<MaquinaEntity> resultado =
                maquinaService.listarTodas();

        assertEquals(1, resultado.size());
        assertEquals("Torno CNC", resultado.get(0).getNome());

        verify(maquinaRepository).findAll();
    }

    @Test
    void deveListarMaquinasPorSetor() {
        MaquinaEntity maquinaInativa = new MaquinaEntity();
        maquinaInativa.setId(11L);
        maquinaInativa.setNome("Fresa CNC");
        maquinaInativa.setCodigo("MAQ-02");
        maquinaInativa.setSetor(setor);
        maquinaInativa.setAtivo(false);

        when(setorRepository.findById(1L))
                .thenReturn(Optional.of(setor));

        when(maquinaRepository.findAllBySetor_Id(1L))
                .thenReturn(List.of(maquina, maquinaInativa));

        List<MaquinaEntity> resultado =
                maquinaService.listarPorSetor(1L);

        assertEquals(2, resultado.size());
        assertEquals(10L, resultado.get(0).getId());
        assertEquals(11L, resultado.get(1).getId());

        assertTrue(resultado.get(0).isAtivo());
        assertFalse(resultado.get(1).isAtivo());

        verify(setorRepository).findById(1L);
        verify(maquinaRepository).findAllBySetor_Id(1L);
    }

    @Test
    void deveRetornarListaVaziaQuandoSetorNaoPossuiMaquinas() {
        when(setorRepository.findById(1L))
                .thenReturn(Optional.of(setor));

        when(maquinaRepository.findAllBySetor_Id(1L))
                .thenReturn(List.of());

        List<MaquinaEntity> resultado =
                maquinaService.listarPorSetor(1L);

        assertTrue(resultado.isEmpty());

        verify(maquinaRepository).findAllBySetor_Id(1L);
    }

    @Test
    void naoDeveListarMaquinasQuandoSetorNaoExiste() {
        when(setorRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> maquinaService.listarPorSetor(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(maquinaRepository, never())
                .findAllBySetor_Id(anyLong());
    }

    @Test
    void deveBuscarMaquinaPorId() {
        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        MaquinaEntity resultado =
                maquinaService.buscarPorId(10L);

        assertEquals(10L, resultado.getId());
        assertEquals("MAQ-01", resultado.getCodigo());
    }

    @Test
    void deveLancarErroQuandoMaquinaNaoExiste() {
        when(maquinaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> maquinaService.buscarPorId(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void deveCriarMaquina() {
        MaquinaRequest request =
                criarRequest("Fresa CNC", "FRE-01", 1L, 2L);

        when(maquinaRepository.existsByCodigoIgnoreCase("FRE-01"))
                .thenReturn(false);

        when(setorRepository.findById(1L))
                .thenReturn(Optional.of(setor));

        when(maquinaRepository.save(any(MaquinaEntity.class)))
                .thenAnswer(invocation -> {
                    MaquinaEntity salva = invocation.getArgument(0);
                    salva.setId(11L);
                    return salva;
                });

        MaquinaEntity resultado = maquinaService.criar(request);

        assertEquals(11L, resultado.getId());
        assertEquals("Fresa CNC", resultado.getNome());
        assertEquals("FRE-01", resultado.getCodigo());
        assertEquals(1L, resultado.getSetor().getId());
        assertEquals(2L, resultado.getOperadorId());
        assertTrue(resultado.isAtivo());

        verify(maquinaRepository).save(any(MaquinaEntity.class));
    }

    @Test
    void naoDeveCriarMaquinaComCodigoDuplicado() {
        MaquinaRequest request =
                criarRequest("Outra máquina", "MAQ-01", 1L, 2L);

        when(maquinaRepository.existsByCodigoIgnoreCase("MAQ-01"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> maquinaService.criar(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(maquinaRepository, never())
                .save(any(MaquinaEntity.class));
    }

    @Test
    void naoDeveCriarMaquinaComSetorInexistente() {
        MaquinaRequest request =
                criarRequest("Fresa CNC", "FRE-01", 99L, 2L);

        when(maquinaRepository.existsByCodigoIgnoreCase("FRE-01"))
                .thenReturn(false);

        when(setorRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> maquinaService.criar(request)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(maquinaRepository, never())
                .save(any(MaquinaEntity.class));
    }

    @Test
    void deveAtualizarMaquina() {
        MaquinaRequest request =
                criarRequest(
                        "Torno CNC atualizado",
                        "MAQ-01",
                        1L,
                        2L
                );

        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(maquinaRepository.existsByCodigoIgnoreCaseAndIdNot(
                "MAQ-01", 10L))
                .thenReturn(false);

        when(setorRepository.findById(1L))
                .thenReturn(Optional.of(setor));

        when(maquinaRepository.save(any(MaquinaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MaquinaEntity resultado =
                maquinaService.atualizar(10L, request);

        assertEquals("Torno CNC atualizado", resultado.getNome());
        assertEquals("MAQ-01", resultado.getCodigo());

        verify(maquinaRepository).save(maquina);
    }

    @Test
    void naoDeveAtualizarComCodigoDeOutraMaquina() {
        MaquinaRequest request =
                criarRequest("Fresa CNC", "MAQ-02", 1L, 2L);

        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(maquinaRepository.existsByCodigoIgnoreCaseAndIdNot(
                "MAQ-02", 10L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> maquinaService.atualizar(10L, request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(maquinaRepository, never())
                .save(any(MaquinaEntity.class));
    }

    @Test
    void deveDesativarMaquina() {
        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(maquinaRepository.save(any(MaquinaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        maquinaService.desativar(10L);

        assertFalse(maquina.isAtivo());

        verify(maquinaRepository).save(maquina);
        verify(maquinaRepository, never()).delete(maquina);
    }

    @Test
    void deveAtivarMaquinaQuandoSetorEstaAtivo() {
        maquina.setAtivo(false);

        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(maquinaRepository.save(any(MaquinaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        maquinaService.ativar(10L);

        assertTrue(maquina.isAtivo());

        verify(maquinaRepository).save(maquina);
    }

    @Test
    void naoDeveDesativarMaquinaInexistente() {
        when(maquinaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> maquinaService.desativar(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(maquinaRepository, never())
                .save(any(MaquinaEntity.class));
    }
}
