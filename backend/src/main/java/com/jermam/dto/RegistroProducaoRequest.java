package com.jermam.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegistroProducaoRequest(
        @NotNull @Min(1) Integer quantidadeProduzida,
        @NotNull @Min(0) Integer quantidadeRefugada,
        @NotNull @DecimalMin("0.0") BigDecimal consumoEnergeticoKwh
) {}
