
package com.jerman.jermanproject.ordemproducao;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        enumAsRef = true,
        description = """
                Estágios possíveis de uma ordem:
                PLANEJADA: ordem criada, aguardando início.
                EM_PRODUCAO: produção em andamento.
                PAUSADA: produção temporariamente interrompida.
                CONCLUIDA: ordem finalizada.
                CANCELADA: ordem cancelada.
                """
)
public enum EstagioOrdemProducao {
    PLANEJADA,
    EM_PRODUCAO,
    PAUSADA,
    CONCLUIDA,
    CANCELADA
}
