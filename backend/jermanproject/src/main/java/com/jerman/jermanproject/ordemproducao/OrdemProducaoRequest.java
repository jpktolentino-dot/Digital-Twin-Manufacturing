
package com.jerman.jermanproject.ordemproducao;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrdemProducaoRequest(

        @NotBlank(message = "O número da ordem é obrigatório.")
        @Size(max = 30, message = "O número deve ter até 30 caracteres.")
        String numero,

        @NotNull(message = "A quantidade planejada é obrigatória.")
        @Positive(message = "A quantidade planejada deve ser maior que zero.")
        Integer quantidadePlanejada,

        @NotNull(message = "O produto é obrigatório.")
        @Positive(message = "O ID do produto deve ser positivo.")
        Long produtoId,

        @NotNull(message = "O operador responsável é obrigatório.")
        @Positive(message = "O ID do operador deve ser positivo.")
        Long operadorId,

        @NotEmpty(message = "Informe pelo menos uma máquina.")
        List<
                @NotNull(message = "O ID da máquina não pode ser nulo.")
                @Positive(message = "O ID da máquina deve ser positivo.")
                Long
        > maquinasIds

) {
}
