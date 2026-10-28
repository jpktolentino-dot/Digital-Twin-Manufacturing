
package com.jerman.jermanproject.ordemproducao;

import jakarta.validation.constraints.NotNull;

public record AlterarEstagioOrdemRequest(

        @NotNull(message = "O novo estágio é obrigatório.")
        EstagioOrdemProducao estagioAtual

) {
}
