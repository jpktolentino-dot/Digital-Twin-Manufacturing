package com.jermam.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SimulacaoConfigRequest(
        @NotNull @Min(3) Integer intervaloSegundos,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double probabilidadeFalha
) {}
