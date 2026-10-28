
package com.jerman.jermanproject.kpi;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record KpiMaquinaResponse(
        Long maquinaId,
        String maquinaNome,
        LocalDateTime inicio,
        LocalDateTime fim,
        long tempoOperacaoSegundos,
        long tempoParadaSegundos,
        long tempoFalhaSegundos,
        long tempoManutencaoSegundos,
        long tempoIndisponivelSegundos,
        long tempoMonitoradoSegundos,
        long tempoSemHistoricoSegundos,
        BigDecimal disponibilidadePercentual
) {
}
