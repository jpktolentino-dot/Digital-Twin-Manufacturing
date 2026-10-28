
package com.jerman.jermanproject.historicomaquina;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jerman.jermanproject.maquina.EstadoMaquina;
import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.maquina.MaquinaRepository;
import com.jerman.jermanproject.setor.SetorEntity;

@ExtendWith(MockitoExtension.class)
class HistoricoMaquinaServiceTest {

    @Mock
    private HistoricoMaquinaRepository historicoMaquinaRepository;

    @Mock
    private MaquinaRepository maquinaRepository;

    @InjectMocks
    private HistoricoMaquinaService historicoMaquinaService;

    private MaquinaEntity maquina;

    @BeforeEach
    void setUp() {
        SetorEntity setor = new SetorEntity();
        setor.setId(1L);
        setor.setNome("Usinagem");
        setor.setCodigo("USI");
        setor.setAtivo(true);

        maquina = new MaquinaEntity();
        maquina.setId(10L);
        maquina.setNome("Torno CNC");
        maquina.setCodigo("MAQ-01");
        maquina.setEstadoAtual(EstadoMaquina.PARADA);
        maquina.setSetor(setor);
        maquina.setAtivo(true);
    }

    @Test
    void deveRegistrarNovaTransicao() {
        when(historicoMaquinaRepository
                .findFirstByMaquina_IdAndDataHoraFimIsNullOrderByDataHoraInicioDesc(
                        10L
                ))
                .thenReturn(Optional.empty());

        when(historicoMaquinaRepository.save(
                any(HistoricoMaquinaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime inicio = LocalDateTime.now();

        historicoMaquinaService.registrarTransicao(
                maquina,
                EstadoMaquina.PARADA,
                EstadoMaquina.OPERANDO
        );

        LocalDateTime fim = LocalDateTime.now();

        ArgumentCaptor<HistoricoMaquinaEntity> captor =
                ArgumentCaptor.forClass(HistoricoMaquinaEntity.class);

        verify(historicoMaquinaRepository).save(captor.capture());

        HistoricoMaquinaEntity resultado = captor.getValue();

        assertEquals(10L, resultado.getMaquina().getId());
        assertEquals(
                EstadoMaquina.PARADA,
                resultado.getEstadoAnterior()
        );
        assertEquals(
                EstadoMaquina.OPERANDO,
                resultado.getEstadoNovo()
        );
        assertNotNull(resultado.getDataHoraInicio());
        assertFalse(resultado.getDataHoraInicio().isBefore(inicio));
        assertFalse(resultado.getDataHoraInicio().isAfter(fim));
        assertNull(resultado.getDataHoraFim());
    }

    @Test
    void deveFecharHistoricoAnteriorEAbrirNovo() {
        HistoricoMaquinaEntity anterior =
                new HistoricoMaquinaEntity();

        anterior.setId(1L);
        anterior.setMaquina(maquina);
        anterior.setEstadoAnterior(EstadoMaquina.PARADA);
        anterior.setEstadoNovo(EstadoMaquina.OPERANDO);
        anterior.setDataHoraInicio(
                LocalDateTime.now().minusHours(1)
        );

        when(historicoMaquinaRepository
                .findFirstByMaquina_IdAndDataHoraFimIsNullOrderByDataHoraInicioDesc(
                        10L
                ))
                .thenReturn(Optional.of(anterior));

        when(historicoMaquinaRepository.save(
                any(HistoricoMaquinaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        historicoMaquinaService.registrarTransicao(
                maquina,
                EstadoMaquina.OPERANDO,
                EstadoMaquina.FALHA
        );

        ArgumentCaptor<HistoricoMaquinaEntity> captor =
                ArgumentCaptor.forClass(HistoricoMaquinaEntity.class);

        verify(historicoMaquinaRepository, times(2))
                .save(captor.capture());

        assertNotNull(anterior.getDataHoraFim());

        HistoricoMaquinaEntity novo = captor.getAllValues().get(1);

        assertEquals(
                EstadoMaquina.OPERANDO,
                novo.getEstadoAnterior()
        );
        assertEquals(
                EstadoMaquina.FALHA,
                novo.getEstadoNovo()
        );
        assertNotNull(novo.getDataHoraInicio());
        assertNull(novo.getDataHoraFim());
    }

    @Test
    void naoDeveRegistrarHistoricoQuandoEstadoNaoMuda() {
        historicoMaquinaService.registrarTransicao(
                maquina,
                EstadoMaquina.PARADA,
                EstadoMaquina.PARADA
        );

        verify(historicoMaquinaRepository, never())
                .save(any(HistoricoMaquinaEntity.class));
    }
}
