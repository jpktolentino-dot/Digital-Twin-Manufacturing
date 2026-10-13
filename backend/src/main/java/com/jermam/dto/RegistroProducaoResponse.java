package com.jermam.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RegistroProducaoResponse(Long id, Long ordemProducaoId, String numeroOrdem,
                                       LocalDateTime dataHora, Integer quantidadeProduzida,
                                       Integer quantidadeRefugada, BigDecimal consumoEnergeticoKwh) {}
