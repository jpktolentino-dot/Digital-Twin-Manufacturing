
package com.jerman.jermanproject.historicomaquina;

import java.time.LocalDateTime;

import com.jerman.jermanproject.maquina.EstadoMaquina;

public record HistoricoMaquinaResponse(
        Long id,
        EstadoMaquina estadoAnterior,
        EstadoMaquina estadoNovo,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        Long maquinaId,
        String maquinaNome
) {
}
