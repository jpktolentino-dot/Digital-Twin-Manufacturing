
package com.jerman.jermanproject.kpi;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.historicomaquina.HistoricoMaquinaEntity;
import com.jerman.jermanproject.historicomaquina.HistoricoMaquinaRepository;
import com.jerman.jermanproject.maquina.EstadoMaquina;
import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.maquina.MaquinaRepository;
import com.jerman.jermanproject.setor.SetorEntity;

@ExtendWith(MockitoExtension.class)
class KpiMaquinaServiceTest {

    @Mock
    private MaquinaRepository maquinaRepository;

    @Mock
    private HistoricoMaquinaRepository historicoMaquinaRepository;

    @InjectMocks
    private KpiMaquinaService kpiMaquinaService;

    private MaquinaEntity maquina;

    private LocalDateTime inicio;
    private LocalDateTime fim;

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

        inicio = LocalDateTime.of(2026, 10, 27, 10, 0);
        fim = LocalDateTime.of(2026, 10, 27, 12, 0);
    }

    private HistoricoMaquinaEntity historico(
            EstadoMaquina estadoAnterior,
            EstadoMaquina estadoNovo,
            LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim
    ) {
        HistoricoMaquinaEntity historico =
                new HistoricoMaquinaEntity();

        historico.setMaquina(maquina);
        historico.setEstadoAnterior(estadoAnterior);
        historico.setEstadoNovo(estadoNovo);
        historico.setDataHoraInicio(dataHoraInicio);
        historico.setDataHoraFim(dataHoraFim);

        return historico;
    }

    @Test
    void deveCalcularOperacaoParadaFalhaEDisponibilidade() {
        HistoricoMaquinaEntity operando = historico(
                EstadoMaquina.PARADA,
                EstadoMaquina.OPERANDO,
                LocalDateTime.of(2026, 10, 27, 9, 0),
                LocalDateTime.of(2026, 10, 27, 10, 30)
        );

        HistoricoMaquinaEntity falha = historico(
                EstadoMaquina.OPERANDO,
                EstadoMaquina.FALHA,
                LocalDateTime.of(2026, 10, 27, 10, 30),
                LocalDateTime.of(2026, 10, 27, 11, 0)
        );

        HistoricoMaquinaEntity parada = historico(
                EstadoMaquina.FALHA,
                EstadoMaquina.PARADA,
                LocalDateTime.of(2026, 10, 27, 11, 0),
                null
        );

        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(historicoMaquinaRepository
                .findAllByMaquina_IdOrderByDataHoraInicioDesc(10L))
                .thenReturn(List.of(parada, falha, operando));

        KpiMaquinaResponse resultado =
                kpiMaquinaService.calcularPorMaquina(
                        10L,
                        inicio,
                        fim
                );

        assertEquals(1800L, resultado.tempoOperacaoSegundos());
        assertEquals(3600L, resultado.tempoParadaSegundos());
        assertEquals(1800L, resultado.tempoFalhaSegundos());
        assertEquals(0L, resultado.tempoManutencaoSegundos());
        assertEquals(5400L, resultado.tempoIndisponivelSegundos());
        assertEquals(7200L, resultado.tempoMonitoradoSegundos());
        assertEquals(0L, resultado.tempoSemHistoricoSegundos());

        assertEquals(
                new BigDecimal("25.00"),
                resultado.disponibilidadePercentual()
        );
    }

    @Test
    void deveRespeitarPeriodoSolicitadoEInformarTempoSemHistorico() {
        HistoricoMaquinaEntity operando = historico(
                EstadoMaquina.PARADA,
                EstadoMaquina.OPERANDO,
                LocalDateTime.of(2026, 10, 27, 11, 0),
                null
        );

        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(historicoMaquinaRepository
                .findAllByMaquina_IdOrderByDataHoraInicioDesc(10L))
                .thenReturn(List.of(operando));

        KpiMaquinaResponse resultado =
                kpiMaquinaService.calcularPorMaquina(
                        10L,
                        inicio,
                        fim
                );

        assertEquals(3600L, resultado.tempoOperacaoSegundos());
        assertEquals(3600L, resultado.tempoMonitoradoSegundos());
        assertEquals(3600L, resultado.tempoSemHistoricoSegundos());

        assertEquals(
                new BigDecimal("100.00"),
                resultado.disponibilidadePercentual()
        );
    }

    @Test
    void deveRetornarDisponibilidadeNulaSemHistorico() {
        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(historicoMaquinaRepository
                .findAllByMaquina_IdOrderByDataHoraInicioDesc(10L))
                .thenReturn(List.of());

        KpiMaquinaResponse resultado =
                kpiMaquinaService.calcularPorMaquina(
                        10L,
                        inicio,
                        fim
                );

        assertEquals(0L, resultado.tempoMonitoradoSegundos());
        assertEquals(7200L, resultado.tempoSemHistoricoSegundos());
        assertNull(resultado.disponibilidadePercentual());
    }

    @Test
    void deveEvitarContarIntervalosSobrepostosDuasVezes() {
        HistoricoMaquinaEntity primeiro = historico(
                EstadoMaquina.PARADA,
                EstadoMaquina.OPERANDO,
                LocalDateTime.of(2026, 10, 27, 10, 0),
                LocalDateTime.of(2026, 10, 27, 11, 0)
        );

        HistoricoMaquinaEntity sobreposto = historico(
                EstadoMaquina.OPERANDO,
                EstadoMaquina.FALHA,
                LocalDateTime.of(2026, 10, 27, 10, 30),
                LocalDateTime.of(2026, 10, 27, 11, 30)
        );

        when(maquinaRepository.findById(10L))
                .thenReturn(Optional.of(maquina));

        when(historicoMaquinaRepository
                .findAllByMaquina_IdOrderByDataHoraInicioDesc(10L))
                .thenReturn(List.of(sobreposto, primeiro));

        KpiMaquinaResponse resultado =
                kpiMaquinaService.calcularPorMaquina(
                        10L,
                        inicio,
                        fim
                );

        assertEquals(3600L, resultado.tempoOperacaoSegundos());
        assertEquals(1800L, resultado.tempoFalhaSegundos());
        assertEquals(5400L, resultado.tempoMonitoradoSegundos());
        assertEquals(1800L, resultado.tempoSemHistoricoSegundos());
    }

    @Test
    void naoDeveCalcularComPeriodoInvalido() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> kpiMaquinaService.calcularPorMaquina(
                        10L,
                        fim,
                        inicio
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(
                maquinaRepository,
                historicoMaquinaRepository
        );
    }

    @Test
    void naoDeveCalcularParaMaquinaInexistente() {
        when(maquinaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> kpiMaquinaService.calcularPorMaquina(
                        99L,
                        inicio,
                        fim
                )
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(maquinaRepository).findById(99L);
        verifyNoInteractions(historicoMaquinaRepository);
    }
}
