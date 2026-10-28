
package com.jerman.jermanproject.kpi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jerman.jermanproject.historicomaquina.HistoricoMaquinaEntity;
import com.jerman.jermanproject.historicomaquina.HistoricoMaquinaRepository;
import com.jerman.jermanproject.maquina.MaquinaEntity;
import com.jerman.jermanproject.maquina.MaquinaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KpiMaquinaService {

    private final MaquinaRepository maquinaRepository;
    private final HistoricoMaquinaRepository historicoMaquinaRepository;

    @Transactional(readOnly = true)
    public KpiMaquinaResponse calcularPorMaquina(
            Long maquinaId,
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        validarPeriodo(inicio, fim);

        MaquinaEntity maquina = maquinaRepository.findById(maquinaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Máquina não encontrada."
                ));

        List<HistoricoMaquinaEntity> historicos =
                historicoMaquinaRepository
                        .findAllByMaquina_IdOrderByDataHoraInicioDesc(
                                maquinaId
                        )
                        .stream()
                        .sorted(Comparator.comparing(
                                HistoricoMaquinaEntity::getDataHoraInicio
                        ))
                        .toList();

        long tempoOperacao = 0;
        long tempoParada = 0;
        long tempoFalha = 0;
        long tempoManutencao = 0;

        LocalDateTime cursor = inicio;

        for (HistoricoMaquinaEntity historico : historicos) {
            LocalDateTime inicioIntervalo =
                    historico.getDataHoraInicio().isAfter(inicio)
                            ? historico.getDataHoraInicio()
                            : inicio;

            // Impede que intervalos sobrepostos sejam contados duas vezes.
            if (inicioIntervalo.isBefore(cursor)) {
                inicioIntervalo = cursor;
            }

            LocalDateTime fimHistorico =
                    historico.getDataHoraFim() != null
                            ? historico.getDataHoraFim()
                            : fim;

            LocalDateTime fimIntervalo =
                    fimHistorico.isBefore(fim)
                            ? fimHistorico
                            : fim;

            if (!fimIntervalo.isAfter(inicioIntervalo)) {
                continue;
            }

            long segundos = Duration.between(
                    inicioIntervalo,
                    fimIntervalo
            ).getSeconds();

            switch (historico.getEstadoNovo()) {
                case OPERANDO -> tempoOperacao += segundos;
                case PARADA -> tempoParada += segundos;
                case FALHA -> tempoFalha += segundos;
                case MANUTENCAO -> tempoManutencao += segundos;
            }

            cursor = fimIntervalo;
        }

        long tempoIndisponivel =
                tempoParada + tempoFalha + tempoManutencao;

        long tempoMonitorado =
                tempoOperacao + tempoIndisponivel;

        long tempoPeriodoSolicitado =
                Duration.between(inicio, fim).getSeconds();

        long tempoSemHistorico = Math.max(
                0,
                tempoPeriodoSolicitado - tempoMonitorado
        );

        BigDecimal disponibilidade = null;

        if (tempoMonitorado > 0) {
            disponibilidade = BigDecimal
                    .valueOf(tempoOperacao)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                            BigDecimal.valueOf(tempoMonitorado),
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return new KpiMaquinaResponse(
                maquina.getId(),
                maquina.getNome(),
                inicio,
                fim,
                tempoOperacao,
                tempoParada,
                tempoFalha,
                tempoManutencao,
                tempoIndisponivel,
                tempoMonitorado,
                tempoSemHistorico,
                disponibilidade
        );
    }

    private void validarPeriodo(
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
        if (inicio == null || fim == null || !inicio.isBefore(fim)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O início deve ser anterior ao fim do período."
            );
        }

        if (fim.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O fim do período não pode estar no futuro."
            );
        }
    }
}
