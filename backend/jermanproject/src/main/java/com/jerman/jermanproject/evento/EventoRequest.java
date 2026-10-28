
package com.jerman.jermanproject.evento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EventoRequest(

        @NotBlank(message = "A categoria é obrigatória.")
        @Size(max = 60, message = "A categoria deve ter até 60 caracteres.")
        String categoria,

        @NotBlank(message = "A descrição é obrigatória.")
        String descricao,

        @DecimalMin(
                value = "0.0",
                message = "O impacto estimado não pode ser negativo."
        )
        BigDecimal impactoEstimado,

        @PastOrPresent(
                message = "A data e hora do evento não podem estar no futuro."
        )
        LocalDateTime dataHora,

        @NotNull(message = "A máquina é obrigatória.")
        @Positive(message = "O ID da máquina deve ser positivo.")
        Long maquinaId,

        @Positive(message = "O ID da ordem deve ser positivo.")
        Long ordemProducaoId,

        @Positive(message = "O ID do usuário deve ser positivo.")
        Long usuarioId

) {
}
