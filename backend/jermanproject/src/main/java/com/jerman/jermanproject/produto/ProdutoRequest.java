
package com.jerman.jermanproject.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProdutoRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter até 100 caracteres.")
        String nome,

        @NotBlank(message = "O código é obrigatório.")
        @Size(max = 30, message = "O código deve ter até 30 caracteres.")
        String codigo,

        String especificacoes

) {
}
