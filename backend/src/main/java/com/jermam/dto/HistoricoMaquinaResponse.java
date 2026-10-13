package com.jermam.dto;

import com.jermam.enums.EstadoMaquina;
import java.time.LocalDateTime;

public record HistoricoMaquinaResponse(Long id, EstadoMaquina estadoAnterior, EstadoMaquina estadoNovo,
                                       LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim,
                                       long duracaoMinutos, Long maquinaId, String maquinaCodigo) {}
