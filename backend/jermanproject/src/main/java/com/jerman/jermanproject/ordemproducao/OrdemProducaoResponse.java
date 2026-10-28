
package com.jerman.jermanproject.ordemproducao;

import java.util.List;

public record OrdemProducaoResponse(
        Long id,
        String numero,
        Integer quantidadePlanejada,
        EstagioOrdemProducao estagioAtual,
        Long produtoId,
        String produtoNome,
        Long operadorId,
        String operadorNome,
        List<Long> maquinasIds
) {
}
