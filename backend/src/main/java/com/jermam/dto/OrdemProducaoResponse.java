package com.jermam.dto;

import com.jermam.enums.EstagioOrdemProducao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemProducaoResponse(
        Long id, String numero, Integer quantidadePlanejada, Integer quantidadeProduzida,
        Integer quantidadeBoa, Integer quantidadeRefugada, BigDecimal consumoEnergeticoKwh,
        double percentualConcluido, EstagioOrdemProducao estagioAtual,
        Long produtoId, String produtoNome, Long maquinaId, String maquinaNome, String setorNome,
        LocalDateTime dataInicio, LocalDateTime dataFim
) {}
