package com.jermam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 30) String codigo,
        @Size(max = 4000) String especificacoes,
        @NotNull @Min(1) Integer tempoCicloIdealSegundos
) {}
