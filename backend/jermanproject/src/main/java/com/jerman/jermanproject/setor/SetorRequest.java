package com.jerman.jermanproject.setor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetorRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter até 100 caracteres.")
        String nome,

        @NotBlank(message = "O código é obrigatório.")
        @Size(max = 30, message = "O código deve ter até 30 caracteres.")
        String codigo

) {
}