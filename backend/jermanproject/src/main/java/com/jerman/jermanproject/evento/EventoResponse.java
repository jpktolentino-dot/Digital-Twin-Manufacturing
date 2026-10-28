
package com.jerman.jermanproject.evento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventoResponse(
        Long id,
        String categoria,
        String descricao,
        BigDecimal impactoEstimado,
        LocalDateTime dataHora,
        Long maquinaId,
        String maquinaNome,
        Long ordemProducaoId,
        String numeroOrdem,
        Long usuarioId,
        String usuarioNome
) {
}
