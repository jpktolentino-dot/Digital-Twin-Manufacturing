package com.jermam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrdemProducaoRequest(
        @NotBlank @Size(max = 40) String numero,
        @NotNull @Min(1) Integer quantidadePlanejada,
        @NotNull Long produtoId,
        @NotNull Long maquinaId
) {}
