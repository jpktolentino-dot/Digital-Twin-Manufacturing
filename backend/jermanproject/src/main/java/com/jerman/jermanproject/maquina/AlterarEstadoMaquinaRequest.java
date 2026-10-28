
package com.jerman.jermanproject.maquina;

import jakarta.validation.constraints.NotNull;

public record AlterarEstadoMaquinaRequest(

        @NotNull(message = "O novo estado da máquina é obrigatório.")
        EstadoMaquina estadoAtual

) {
}
