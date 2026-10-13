package com.jermam.dto;

import com.jermam.enums.CategoriaEvento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EventoRequest(
        @NotNull CategoriaEvento categoria,
        @NotBlank @Size(max = 4000) String descricao,
        @NotNull @DecimalMin("0.0") BigDecimal impactoEstimadoMinutos,
        @NotNull Long maquinaId,
        Long ordemProducaoId
) {}
