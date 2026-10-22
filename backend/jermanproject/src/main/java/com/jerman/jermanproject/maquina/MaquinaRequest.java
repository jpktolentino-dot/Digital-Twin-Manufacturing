package com.jerman.jermanproject.maquina;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MaquinaRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter até 100 caracteres.")
        String nome,

        @NotBlank(message = "O código é obrigatório.")
        @Size(max = 30, message = "O código deve ter até 30 caracteres.")
        String codigo,

        String especificacoes,

        @NotNull(message = "O estado atual é obrigatório.")
        EstadoMaquina estadoAtual,

        @NotNull(message = "O setor é obrigatório.")
        @Positive(message = "O ID do setor deve ser positivo.")
        Long setorId,

        @NotNull(message = "O operador é obrigatório.")
        @Positive(message = "O ID do operador deve ser positivo.")
        Long operadorId

) {
}