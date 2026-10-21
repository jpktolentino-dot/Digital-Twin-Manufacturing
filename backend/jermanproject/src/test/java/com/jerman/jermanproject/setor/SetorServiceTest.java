package com.jerman.jermanproject.setor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SetorServiceTest {

    @Mock
    private SetorRepository setorRepository;

    @InjectMocks
    private SetorService setorService;

    private SetorEntity setor;

    @BeforeEach
    void setUp() {
        setor = new SetorEntity();
        setor.setId(1L);
        setor.setNome("Usinagem");
        setor.setCodigo("USI");
    }

    @Test
    void deveListarTodosOsSetores() {
        when(setorRepository.findAll()).thenReturn(List.of(setor));

        List<SetorEntity> resultado = setorService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Usinagem", resultado.get(0).getNome());
        verify(setorRepository).findAll();
    }

    @Test
    void deveBuscarSetorPorId() {
        when(setorRepository.findById(1L))
                .thenReturn(Optional.of(setor));

        SetorEntity resultado = setorService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("USI", resultado.getCodigo());
    }

    @Test
    void deveLancarErroQuandoSetorNaoExiste() {
        when(setorRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> setorService.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deveCriarSetor() {
        SetorRequest request = new SetorRequest("Montagem", "MON");

        when(setorRepository.existsByCodigoIgnoreCase("MON"))
                .thenReturn(false);

        when(setorRepository.save(any(SetorEntity.class)))
                .thenAnswer(invocation -> {
                    SetorEntity salvo = invocation.getArgument(0);
                    salvo.setId(2L);
                    return salvo;
                });

        SetorEntity resultado = setorService.criar(request);

        assertEquals(2L, resultado.getId());
        assertEquals("Montagem", resultado.getNome());
        assertEquals("MON", resultado.getCodigo());

        verify(setorRepository).save(any(SetorEntity.class));
    }

    @Test
    void naoDeveCriarSetorComCodigoDuplicado() {
        SetorRequest request = new SetorRequest("Montagem", "USI");

        when(setorRepository.existsByCodigoIgnoreCase("USI"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> setorService.criar(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(setorRepository, never()).save(any());
    }

    @Test
    void deveAtualizarSetor() {
        SetorRequest request = new SetorRequest("Usinagem CNC", "USI");

        when(setorRepository.findById(1L))
                .thenReturn(Optional.of(setor));

        when(setorRepository.existsByCodigoIgnoreCaseAndIdNot("USI", 1L))
                .thenReturn(false);

        when(setorRepository.save(any(SetorEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SetorEntity resultado = setorService.atualizar(1L, request);

        assertEquals("Usinagem CNC", resultado.getNome());
        assertEquals("USI", resultado.getCodigo());
        verify(setorRepository).save(setor);
    }

    @Test
    void naoDeveAtualizarComCodigoDeOutroSetor() {
        SetorRequest request = new SetorRequest("Usinagem CNC", "MON");

        when(setorRepository.findById(1L))
                .thenReturn(Optional.of(setor));

        when(setorRepository.existsByCodigoIgnoreCaseAndIdNot("MON", 1L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> setorService.atualizar(1L, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(setorRepository, never()).save(any());
    }

    @Test
    void deveExcluirSetor() {
        when(setorRepository.findById(1L))
                .thenReturn(Optional.of(setor));

        setorService.excluir(1L);

        verify(setorRepository).delete(setor);
    }
}