package com.jerman.jermanproject.maquina;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
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
    @SuppressWarnings("unused")
    void setUp() {
        setor = new SetorEntity();
        setor.setId(1L);
        setor.setNome("Usinagem");
        setor.setCodigo("USI");

        maquina = new MaquinaEntity();
        maquina.setId(10L);
        maquina.setNome("Torno CNC");
        maquina.setCodigo("MAQ-01");
        maquina.setEspecificacoes("Torno para peças metálicas");
        maquina.setEstadoAtual(EstadoMaquina.PARADA);
        maquina.setSetor(setor);
        maquina.setOperadorId(2L);
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

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
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

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(maquinaRepository, never()).save(any(MaquinaEntity.class));
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

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(maquinaRepository, never()).save(any(MaquinaEntity.class));
    }

    @Test
    void deveAtualizarMaquina() {
        MaquinaRequest request =
                criarRequest("Torno CNC atualizado", "MAQ-01", 1L, 2L);

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

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(maquinaRepository, never()).save(any(MaquinaEntity.class));
    }

    @Test
    void deveExcluirMaquina() {
        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        maquinaService.excluir(10L);

        verify(maquinaRepository).delete(maquina);
    }
}